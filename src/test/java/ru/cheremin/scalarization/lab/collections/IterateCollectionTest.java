package ru.cheremin.scalarization.lab.collections;

import java.util.*;

import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.allocatesNothing;

/**
 * @author ruslan
 *         created 07/08/16 at 16:25
 */
public class IterateCollectionTest {

	@Test
	public void iterateArrayListAllocatesNothing() throws Exception {
		final int SIZE = 16;
		final List<Integer> collection = new ArrayList<Integer>( SIZE );
		for( int i = 0; i < SIZE; i++ ) {
			collection.add( i );
		}
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final Integer i : collection ) {
							sum += i;
						}
						return sum;
					}
				},
				allocatesNothing()
		);

	}
}
