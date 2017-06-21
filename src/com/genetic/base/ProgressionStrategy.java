package com.genetic.base;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface ProgressionStrategy<T extends Creature>
{
	/**
	 * This function must remove all unfit creatures from the list, and add all
	 * new children. Creatures will be sorted from largest to smallest fitness.
	 * 
	 * @param creatures
	 * @param fitnessMap
	 */
	void progress(List<T> creatures, Map<T, Double> fitnessMap);

}
