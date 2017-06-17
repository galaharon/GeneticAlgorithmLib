package com.genetic.test;

import java.util.*;
import java.util.Map.Entry;

import com.genetic.base.*;

public class Main
{

	public static void main(String[] args)
	{
		Population<LinearAlgorithm> population = Population.<LinearAlgorithm>builder()
				.setFitnessFunction(LinearAlgorithm::fitness).setInitialPopulationSize(10000)
				.setGenerator(LinearAlgorithm::new)
				.setProgressionStrategy(ProgressionStrategies.killWorstHalf(LinearAlgorithm::breed))
				.build();

		for(int i = 0; i < 100; i++)
		{
			System.out.println(String.format("Generation %d - Average %f, max %f",
					population.generation(), population.getStatistics().getAverage(),
					population.getStatistics().getMax()));
			if(population.getStatistics().getMax() >= -0.0001D)
			{
				for(Entry<?, Double> e : population.getCreatures().entrySet())
				{
					if(e.getValue().doubleValue() > -0.01) System.out.println(e);
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
			return -aggregate;
		}

		@ProgressionFunction
		public static Collection<LinearAlgorithm> progression(
				Map<LinearAlgorithm, Double> population)
		{
			return ProgressionStrategies.killWorstHalf(LinearAlgorithm::breed).apply(population);
		}

	}

	public static double actual(double x)
	{
		return 19 * x - 19;
	}

}
