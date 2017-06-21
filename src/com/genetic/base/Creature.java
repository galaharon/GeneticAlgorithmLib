package com.genetic.base;

import java.lang.annotation.*;

public interface Creature
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public static @interface FitnessFunction
	{
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public static @interface ProgressionFunction
	{
	}
}
