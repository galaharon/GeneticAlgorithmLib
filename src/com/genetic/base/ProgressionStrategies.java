package com.genetic.base;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

public class ProgressionStrategies
{
	/**
	 * Returns a progression strategy that progresses by randomly choosing 3
	 * creatures, killing off the one with the lowest fitness, and breeding the
	 * remaining two.
	 * 
	 * @param breedingStrategy
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Creature> Function<Map<T, Double>, Collection<T>> stochasticWorstOfThree(
			BinaryOperator<T> breedingStrategy)
	{
		return population -> {
			List<Entry<T, Double>> list = new ArrayList<>();
			list.addAll(population.entrySet());

			Object selection[] = ThreadLocalRandom.current().ints(0, population.size()).distinct()
					.limit(3).mapToObj(list::get)
					.sorted((e1, e2) -> Double.compare(e1.getValue(), e2.getValue())).toArray();

			population.remove(((Entry<T, Double>) selection[0]).getKey());

			T child = breedingStrategy.apply(((Entry<T, Double>) selection[1]).getKey(),
					((Entry<T, Double>) selection[2]).getKey());
			return Lists.newArrayList(child);
		};
	}

	/**
	 * Similar to {@link #stochasticWorstOfThree(BinaryOperator)}, but occurs n
	 * times. Does not include children in random selection.
	 * 
	 * @param breedingStrategy
	 * @param n
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Creature> Function<Map<T, Double>, Collection<T>> stochasticWorstOfThreeNTimes(
			BinaryOperator<T> breedingStrategy, int n)
	{
		return population -> {
			List<T> children = new ArrayList<>();
			List<Entry<T, Double>> list = new ArrayList<>();
			list.addAll(population.entrySet());

			for(int i = 0; i < n && population.size() >= 3; i++)
			{
				Object selection[] = ThreadLocalRandom.current().ints(0, population.size())
						.distinct().limit(3).mapToObj(list::get)
						.sorted((e1, e2) -> Double.compare(e1.getValue(), e2.getValue())).toArray();

				population.remove(((Entry<T, Double>) selection[0]).getKey());
				list.remove(selection[0]);

				T child = breedingStrategy.apply(((Entry<T, Double>) selection[1]).getKey(),
						((Entry<T, Double>) selection[2]).getKey());
				children.add(child);
			}
			return children;
		};
	}

	/**
	 * Kills the bottom half of the population (lowest fitness), then randomly
	 * breeds the remaining half in pairs.
	 * 
	 * @param breedingStrategy
	 * @param n
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Creature> Function<Map<T, Double>, Collection<T>> killWorstHalf(
			BinaryOperator<T> breedingStrategy)
	{
		return population -> {
			List<T> children = new ArrayList<>();
			List<Entry<T, Double>> list = population.entrySet().stream()
					.sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
					.collect(Collectors.toList());

			int size = list.size();
			List<Entry<T, Double>> bottomHalf = list.subList(size / 2, size);
			bottomHalf.forEach(e -> population.remove(e.getKey()));
			bottomHalf.clear();

			for(int i = 0; i < size - list.size(); i++)
			{
				Object selection[] = ThreadLocalRandom.current().ints(0, list.size()).distinct()
						.limit(2).mapToObj(list::get).toArray();

				T child = breedingStrategy.apply(((Entry<T, Double>) selection[0]).getKey(),
						((Entry<T, Double>) selection[1]).getKey());
				children.add(child);
			}
			return children;
		};
	}
}
