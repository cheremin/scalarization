package ru.cheremin.scalarization.scenarios;

/**
 * Only array of size 1 is scalarized with 1.8. Array of size >=2 is not scalarized
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ObjectArrayAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final Integer[] array = new Integer[SIZE];
		for( int i = 0; i < SIZE; i++ ) {
			array[i] = 1;
		}

		long sum = 0;
		for( int i = 0; i < SIZE; i++ ) {
			sum += array[i];
		}

		return sum;
	}
}
