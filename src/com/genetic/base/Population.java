package com.genetic.base;

import static com.genetic.util.LambdaUtil.*;
import static com.google.common.base.Predicates.not;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Population<T extends Creature>
{
	private long generation;

	private final List<T> creatures;
	private final Map<T, Double> fitnessMap;

	private final ProgressionStrategy<T> progressionStrategy;
	private final ToDoubleFunction<T> fitnessFunction;

	private boolean statsUpToDate = false;
	private DoubleSummaryStatistics stats;

	private Population(List<T> initialPopulation, ProgressionStrategy<T> progressionStrategy,
			ToDoubleFunction<T> fitnessFunction)
	{
		this.creatures = initialPopulation;
		this.progressionStrategy = progressionStrategy;
		this.fitnessMap = new WeakHashMap<>(creatures.size());
		this.fitnessFunction = fitnessFunction;
		refreshFitness();
		this.generation = 1;
	}

	private void refreshFitness()
	{
		creatures.stream().filter(not(fitnessMap::containsKey))
				.forEach(c -> fitnessMap.put(c, fitnessFunction.applyAsDouble(c)));
		sortCreatures();
	}

	public void progress()
	{
		generation++;
		statsUpToDate = false;
		progressionStrategy.progress(creatures, fitnessMap);
		refreshFitness();
	}

	public DoubleSummaryStatistics statistics()
	{
		if(!statsUpToDate) // lazy loading
		{
			stats = creatures.stream().mapToDouble(fitnessMap::get).summaryStatistics();
			statsUpToDate = true;
		}
		return stats;
	}

	private void sortCreatures()
	{
		Collections.sort(creatures, (a, b) -> fitnessMap.get(b).compareTo(fitnessMap.get(a)));
	}

	public ImmutableList<T> creatures()
	{
		return ImmutableList.copyOf(creatures);
	}

	public long generation()
	{
		return generation;
	}

	public double fitnessOf(T creature)
	{
		return this.fitnessMap.get(creature);
	}

	public static <T extends Creature> Population<T> fromClass(Class<T> clazz,
			long initialPopulation)
	{
		Builder<T> builder = new Builder<>();
		builder.withInitialSize(initialPopulation);

		try
		{
			Constructor<T> con = clazz.getConstructor();
			builder.generator(uncheckedS(con::newInstance));
		}
		catch(Exception e)
		{
			throw new RuntimeException("Empty constructor must be present in class.", e);
		}

		for(Method method : clazz.getDeclaredMethods())
		{
			if(method.isAnnotationPresent(Creature.FitnessFunction.class))
			{
				builder.fitnessFunction(uncheckedFD(c -> (Double) method.invoke(null, c)));
			}
			else if(method.isAnnotationPresent(Creature.ProgressionFunction.class))
			{
				builder.progressionStrategy(uncheckedPS((p, f) -> method.invoke(null, p, f)));
			}
		}
		return builder.build();
	}

	public static class Builder<T extends Creature>
	{
		private Supplier<T> generator;
		private long size = 1000;
		private ProgressionStrategy<T> strategy;
		private ToDoubleFunction<T> fitnessFunction;

		public Builder<T> generator(@Nonnull Supplier<T> generator)
		{
			this.generator = generator;
			return this;
		}

		public Builder<T> progressionStrategy(@Nonnull ProgressionStrategy<T> strategy)
		{
			this.strategy = strategy;
			return this;
		}

		public Builder<T> fitnessFunction(@Nonnull ToDoubleFunction<T> fitnessFunction)
		{
			this.fitnessFunction = fitnessFunction;
			return this;
		}

		/**
		 * By default, this is set to 1000.
		 * 
		 * @param size
		 * @return
		 */
		public Builder<T> withInitialSize(long size)
		{
			Preconditions.checkArgument(size > 0, "Initial population size must be positive.");
			this.size = size;
			return this;
		}

		public Population<T> build()
		{
			Preconditions.checkNotNull(strategy, "Progression strategy was not set.");
			Population<T> population = new Population<>(buildInitialPopulation(), strategy,
					fitnessFunction);
			return population;
		}

		private List<T> buildInitialPopulation()
		{
			Preconditions.checkNotNull(generator, "Generator was not set.");
			return Stream.generate(generator).limit(size).collect(Collectors.toList());
		}
	}
}
