package ru.cheremin.scalarization.lab.collections;

import java.util.*;

import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesSomething;

/**
 * @author ruslan
 *         created 31/08/16 at 23:06
 */
public class ArrayAsListTest {
	public static final int SIZE = 16;

	private static final Integer[] ARRAY = new Integer[SIZE];
	static{
		for( int i = 0; i < ARRAY.length; i++ ) {
			ARRAY[i] = i;
		}
	}

	@Test
	public void arraysAsListAccessedByIndexIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final List<Integer> list = Arrays.asList( ARRAY );
						long sum = 0;
						for( int i = 0; i < list.size(); i++ ) {
							final Integer n = list.get( i );
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void arraysAsListAccessedByIteratorIsNOTScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final List<Integer> list = Arrays.asList( ARRAY );
						long sum = 0;
						for( final Integer n : list ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesSomething()
		);
	}

	@Test
	public void arraysAsListAccessedByDetachedIteratorIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final List<Integer> list = new ArrayAsListScenario.ArrayList<Integer>( ARRAY );
						long sum = 0;
						for( final Integer n : list ) {
							sum += n;
						}
						return sum;
					}
				},
				finallyAllocatesNothing()
		);
	}
}