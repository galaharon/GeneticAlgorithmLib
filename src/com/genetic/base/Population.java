package com.genetic.base;

import static com.genetic.util.LambdaUtil.uncheckedF;
import static com.genetic.util.LambdaUtil.uncheckedS;
import static java.util.function.Function.identity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class Population<T extends Creature>
{
	private final Function<Map<T, Double>, Collection<T>> progressionStrategy;
	private final Function<T, Double> fitnessFunction;
	private DoubleSummaryStatistics statistics;
	private int generation = 1;

	private final Map<T, Double> population;

	private Population(List<T> initialPopulation,
			Function<Map<T, Double>, Collection<T>> progressionStrategy,
			Function<T, Double> fitnessFunction)
	{
		this.progressionStrategy = progressionStrategy;
		this.fitnessFunction = fitnessFunction;
		this.population = initialPopulation.stream()
				.collect(Collectors.toMap(identity(), fitnessFunction));
		updateStatistics();
	}

	/**
	 * Updates the statistics.
	 */
	private void updateStatistics()
	{
		statistics = population.values().stream()
				.collect(Collectors.summarizingDouble(Double::doubleValue));
	}

	/**
	 * Returns a summary of the current statistics.
	 * 
	 * @return
	 */
	public DoubleSummaryStatistics getStatistics()
	{
		return statistics;
	}

	/**
	 * Returns the current generation count
	 * 
	 * @return
	 */
	public int generation()
	{
		return generation;
	}

	/**
	 * Progresses a generation.
	 */
	public void progress()
	{
		progress(true);
	}

	private void progress(boolean updateStatistics)
	{
		generation++;
		Collection<T> children = progressionStrategy.apply(population);
		children.stream().forEach(child -> population.put(child, fitnessFunction.apply(child)));
		if(updateStatistics) updateStatistics();
	}

	/**
	 * Progresses until the given generation is reached.
	 * 
	 * @param maxGenerations
	 * @param updateStats
	 */
	public void progressUntilGeneration(int maxGenerations, boolean updateStats)
	{
		while(generation < maxGenerations)
		{
			progress(updateStats);
		}
		if(!updateStats) updateStatistics();
	}

	/**
	 * Returns an immutable view of the creature-fitness map.
	 * 
	 * @return
	 */
	public Map<T, Double> getCreatures()
	{
		return ImmutableMap.copyOf(population);
	}

	/**
	 * Same as {@code new Population.Builder<>()}.
	 * 
	 * @return a new builder.
	 */
	public static <T extends Creature> Builder<T> builder()
	{
		return new Builder<>();
	}

	@SuppressWarnings("unchecked")
	public static <T extends Creature> Population<T> fromClass(Class<T> clazz,
			int initialPopulation)
	{
		Builder<T> builder = builder();
		builder.setInitialPopulationSize(initialPopulation);

		try
		{
			Constructor<T> con = clazz.getConstructor();
			builder.setGenerator(uncheckedS(con::newInstance));
		}
		catch(Exception e)
		{
			throw new RuntimeException("Empty constructor must be present in class.", e);
		}

		
		for(Method method : clazz.getDeclaredMethods())
		{
			if(method.isAnnotationPresent(Creature.FitnessFunction.class))
			{
				builder.setFitnessFunction(uncheckedF(c -> (Double) method.invoke(null, c)));
			}
			else if(method.isAnnotationPresent(Creature.ProgressionFunction.class))
			{
				builder.setProgressionStrategy(uncheckedF(p -> (Collection<T>) method.invoke(null, p)));
			}
		}
		

		return builder.build();
	}

	public static class Builder<T extends Creature>
	{
		private Supplier<T> generator;
		private Function<Map<T, Double>, Collection<T>> progressionStrategy;
		private int populationSize = 1000;
		private Function<T, Double> fitnessFunction;

		public Builder()
		{
			// empty
		}

		public Builder<T> setGenerator(@Nonnull Supplier<T> creatureGenerator)
		{
			this.generator = creatureGenerator;
			return this;
		}

		/**
		 * Sets the progression strategy. This function recieves the current map
		 * of the population in the form creature -> fitness. It may remove as
		 * many entries as it wants, and must return a collection of new
		 * children.
		 * 
		 * @param progressionStrategy
		 * @return this
		 */
		public Builder<T> setProgressionStrategy(
				@Nonnull Function<Map<T, Double>, Collection<T>> progressionStrategy)
		{
			this.progressionStrategy = progressionStrategy;
			return this;
		}

		/**
		 * Sets the initial size of the population. By default this is equal to
		 * 1000.
		 * 
		 * @param size
		 * @return this
		 */
		public Builder<T> setInitialPopulationSize(int size)
		{
			this.populationSize = size;
			return this;
		}

		/**
		 * A function that grades the fitness of a creature. It is up to you in
		 * terms of how you implement fitness. Most methods in
		 * {@link ProgressionStrategies} relies on lower fitness meaning less
		 * fit.
		 * 
		 * @param fitnessFunction
		 * @return
		 */
		public Builder<T> setFitnessFunction(Function<T, Double> fitnessFunction)
		{
			this.fitnessFunction = fitnessFunction;
			return this;
		}

		/**
		 * Builds the initial population.
		 * 
		 * @return a list containing the initial population.
		 */
		private List<T> buildInitialPopulation()
		{
			return Stream.generate(generator).limit(populationSize).collect(Collectors.toList());
		}

		/**
		 * Builds the population object
		 * 
		 * @return the population representation
		 */
		public Population<T> build()
		{
			Preconditions.checkNotNull(generator, "Creature generator cannot be null.");
			Preconditions.checkNotNull(progressionStrategy, "Progression strategy cannot be null.");
			Preconditions.checkNotNull(fitnessFunction, "Fitness function cannot be null.");
			Preconditions.checkArgument(populationSize > 1, "Population size %d must be > 1",
					populationSize);

			Population<T> population = new Population<>(buildInitialPopulation(),
					progressionStrategy, fitnessFunction);
			return population;
		}
	}
}
