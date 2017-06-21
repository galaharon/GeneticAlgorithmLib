package com.genetic.base;

import java.util.*;
import java.util.function.BinaryOperator;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

public class ProgressionStrategies
{

	private static final Random DEFAULT = new Random();

	public static enum Goal
	{
		MINIMISE, MAXIMISE;

		public Goal reverse()
		{
			return this == MAXIMISE ? MINIMISE : MAXIMISE;
		}
	}

	public static abstract class ReversibleProgressionStrategy<T extends Creature>
			implements ProgressionStrategy<T>
	{
		protected Goal goal = Goal.MAXIMISE;
		protected final Random rand;
		protected final BinaryOperator<T> breedingStrategy;

		protected ReversibleProgressionStrategy(Random rand,
				@Nonnull BinaryOperator<T> breedingStrategy)
		{
			this.rand = rand == null ? DEFAULT : rand;
			this.breedingStrategy = breedingStrategy;
		}

		public final ReversibleProgressionStrategy<T> reverse()
		{
			goal = goal.reverse();
			return this;
		}

		// TODO superclass implementation that reverse creature list?

		private static class StochasticWorstOfThree<T extends Creature>
				extends ReversibleProgressionStrategy<T>
		{
			private final int iterations;

			private StochasticWorstOfThree(int iterations, Random rand,
					@Nonnull BinaryOperator<T> breedingStrategy)
			{
				super(rand, breedingStrategy);
				this.iterations = iterations;
			}

			@Override
			public void progress(List<T> creatures, Map<T, Double> fitnessMap)
			{
				List<T> children = new ArrayList<>();

				if(this.goal == Goal.MINIMISE) creatures = Lists.reverse(creatures);

				for(int i = 0; i < iterations && creatures.size() >= 3; i++)
				{
					int selection[] = this.rand.ints(0, creatures.size()).distinct().limit(3)
							.sorted().toArray();
					creatures.remove(selection[2]);
					children.add(breedingStrategy.apply(creatures.get(selection[0]),
							creatures.get(selection[1])));
				}

				creatures.addAll(children);
			}
		}

		private static class KillLowestHalf<T extends Creature>
				extends ReversibleProgressionStrategy<T>
		{
			private KillLowestHalf(Random rand, @Nonnull BinaryOperator<T> breedingStrategy)
			{
				super(rand, breedingStrategy);
			}

			@Override
			public void progress(List<T> creatures, Map<T, Double> fitnessMap)
			{
				if(this.goal == Goal.MINIMISE) creatures = Lists.reverse(creatures);

				List<T> children = new ArrayList<>();
				int size = creatures.size();
				creatures.subList(size / 2, size).clear();

				int newSize = creatures.size();

				for(int i = 0; i < size - newSize; i++)
				{
					int selection[] = this.rand.ints(0, newSize).distinct().limit(2).toArray();
					children.add(this.breedingStrategy.apply(creatures.get(selection[0]),
							creatures.get(selection[1])));
				}
				creatures.addAll(children);
			}
		}

	}

	/**
	 * Returns a progression strategy that progresses by randomly choosing 3
	 * creatures, killing off the one with the lowest fitness, and breeding the
	 * remaining two.
	 * 
	 * @param breedingStrategy
	 * @return
	 */
	public static <T extends Creature> ReversibleProgressionStrategy<T> stochasticWorstOfThree(
			BinaryOperator<T> breedingStrategy)
	{
		return stochasticWorstOfThree(breedingStrategy, null);
	}

	// TODO documentation

	public static <T extends Creature> ReversibleProgressionStrategy<T> stochasticWorstOfThree(
			BinaryOperator<T> breedingStrategy, Random rand)
	{
		return stochasticWorstOfThreeNTimes(breedingStrategy, rand, 1);
	}

	public static <T extends Creature> ReversibleProgressionStrategy<T> stochasticWorstOfThreeNTimes(
			BinaryOperator<T> breedingStrategy, int iterations)
	{
		return stochasticWorstOfThreeNTimes(breedingStrategy, null, iterations);
	}

	public static <T extends Creature> ReversibleProgressionStrategy<T> stochasticWorstOfThreeNTimes(
			BinaryOperator<T> breedingStrategy, Random rand, int iterations)
	{
		return new ReversibleProgressionStrategy.StochasticWorstOfThree<>(iterations, rand,
				breedingStrategy);
	}

	public static <T extends Creature> ReversibleProgressionStrategy<T> killWorstHalf(
			BinaryOperator<T> breedingStrategy)
	{
		return killWorstHalf(breedingStrategy, null);
	}

	public static <T extends Creature> ReversibleProgressionStrategy<T> killWorstHalf(
			BinaryOperator<T> breedingStrategy, Random rand)
	{
		return new ReversibleProgressionStrategy.KillLowestHalf<>(rand, breedingStrategy);
	}
}
