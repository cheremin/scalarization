package ru.cheremin.scalarization.scenarios;


import java.util.*;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ArrayListAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final ArrayList list = new ArrayList( SIZE );
		for( int i = 0; i < SIZE; i++ ) {
			list.add( "" );
		}
		list.clear();
		return list.size();
	}
}
