package ru.cheremin.scalarization.scenarios;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class PrimitiveArrayAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final byte[] array = new byte[SIZE];
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
