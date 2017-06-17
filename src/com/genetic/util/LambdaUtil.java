package com.genetic.util;

import java.util.function.Function;
import java.util.function.Supplier;

public class LambdaUtil
{
	@FunctionalInterface
	public static interface FunctionWithException<A, B>
	{
		B apply(A a) throws Exception;
	}

	@FunctionalInterface
	public static interface SupplierWithException<T>
	{
		T get() throws Exception;
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
