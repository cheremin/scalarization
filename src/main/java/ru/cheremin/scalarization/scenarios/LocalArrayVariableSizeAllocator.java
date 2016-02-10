package ru.cheremin.scalarization.scenarios;


/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class LocalArrayVariableSizeAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final String [] array = new String[SIZE];

		for( int i = 0; i < array.length; i++ ) {
			array[i] = "1";//( byte ) ( i * i );
		}

		long sum = 0;
		for( final String item : array ) {
			sum += item.length();
		}

		return sum;
	}
}
