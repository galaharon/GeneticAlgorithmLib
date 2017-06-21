package com.genetic.test;

import java.util.*;

import com.genetic.base.*;

public class Main
{

	public static void main(String[] args)
	{
		Population<LinearAlgorithm> population = new Population.Builder<LinearAlgorithm>()
				.fitnessFunction(LinearAlgorithm::fitness).withInitialSize(10000)
				.generator(LinearAlgorithm::new).progressionStrategy(ProgressionStrategies
						.killWorstHalf(LinearAlgorithm::breed, new Random(0)).reverse())
				.build();

		for(int i = 0; i < 1000; i++)
		{
			System.out.println(String.format("Generation %d - Average %f, max %f",
					population.generation(), population.statistics().getAverage(),
					population.statistics().getMax()));
			if(population.statistics().getMax() <= 0.00001D)
			{
				for(LinearAlgorithm l : population.creatures())
				{
					if(population.fitnessOf(l) < 0.01)
						System.out.println(l + ", " + population.fitnessOf(l));
				}
				break;
			}
			population.progress();
		}
	}

	public static class LinearAlgorithm implements Creature
	{
		private double a;
		private double b;

		private static final Random r = new Random(0);

		public LinearAlgorithm()
		{
			this((2 * r.nextDouble() - 1) * 20, (2 * r.nextDouble() - 1) * 20);
		}

		public LinearAlgorithm(double a, double b)
		{
			this.a = a;
			this.b = b;
		}

		public double getResult(double x)
		{
			return a * x + b;
		}

		@Override
		public String toString()
		{
			return String.format("%.2fx + %.2f", a, b);
		}

		public static LinearAlgorithm breed(LinearAlgorithm parent1, LinearAlgorithm parent2)
		{
			return new LinearAlgorithm(average(parent1.a, parent2.a),
					average(parent1.b, parent2.b));
		}

		private static double average(double a, double b)
		{
			return (a + b) / 2;
		}

		@FitnessFunction
		public static double fitness(LinearAlgorithm a)
		{
			int aggregate = 0;
			for(int i = 0; i < 50; i++)
			{
				double d = a.getResult(i) - actual(i);
				aggregate += d * d;
			}
			return aggregate;
		}

		@ProgressionFunction
		public static void progression(List<LinearAlgorithm> population,
				Map<LinearAlgorithm, Double> map)
		{
			ProgressionStrategies.killWorstHalf(LinearAlgorithm::breed).reverse()
					.progress(population, map);
		}

	}

	public static double actual(double x)
	{
		return 19 * x - 19;
	}

}
