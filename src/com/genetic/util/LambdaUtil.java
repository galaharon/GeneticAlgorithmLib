package com.genetic.util;

import java.util.List;
import java.util.Map;
import java.util.function.*;

import com.genetic.base.Creature;
import com.genetic.base.ProgressionStrategy;

public class LambdaUtil
{
	@FunctionalInterface
	public static interface FunctionWithException<A, B>
	{
		B apply(A a) throws Exception;
	}

	@FunctionalInterface
	public static interface ToDoubleFunctionWithException<A>
	{
		double applyAsDouble(A a) throws Exception;
	}

	@FunctionalInterface
	public static interface SupplierWithException<T>
	{
		T get() throws Exception;
	}

	@FunctionalInterface
	public static interface ProgressionStrategyWithException<T>
	{
		void progess(List<T> l, Map<T, Double> m) throws Exception;
	}

	public static <T extends Creature> ProgressionStrategy<T> uncheckedPS(
			ProgressionStrategyWithException<T> p)
	{
		return (t, m) -> {
			try
			{
				p.progess(t, m);
			}
			catch(Exception e)
			{
				throw propagate(e);
			}
		};
	}

	public static <A, B> Function<A, B> uncheckedF(FunctionWithException<A, B> f)
	{
		return a -> {
			try
			{
				return f.apply(a);
			}
			catch(Exception e)
			{
				throw propagate(e);
			}
		};
	}

	public static <A> ToDoubleFunction<A> uncheckedFD(ToDoubleFunctionWithException<A> f)
	{
		return a -> {
			try
			{
				return f.applyAsDouble(a);
			}
			catch(Exception e)
			{
				throw propagate(e);
			}
		};
	}

	public static <T> Supplier<T> uncheckedS(SupplierWithException<T> s)
	{
		return () -> {
			try
			{
				return s.get();
			}
			catch(Exception e)
			{
				throw propagate(e);
			}
		};
	}

	// @me, Guava
	public static RuntimeException propagate(Throwable throwable)
	{
		if(throwable instanceof RuntimeException)
		{
			throw (RuntimeException) throwable;
		}
		if(throwable instanceof Error)
		{
			throw (Error) throwable;
		}
		throw new RuntimeException(throwable);
	}
}
