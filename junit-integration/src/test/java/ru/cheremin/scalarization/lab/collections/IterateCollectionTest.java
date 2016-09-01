package ru.cheremin.scalarization.lab.collections;

import java.util.*;

import gnu.trove.set.hash.THashSet;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;

/**
 * @author ruslan
 *         created 07/08/16 at 16:25
 */
public class IterateCollectionTest {
	final int SIZE = 16;

	@Test
	public void arrayListIteratorIsScalarized() throws Exception {
		final List<Integer> arrayList = new ArrayList<>();
		for( int i = 0; i < SIZE; i++ ) {
			arrayList.add( i );
		}
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return summarize( arrayList );
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void linkedListIteratorIsScalarized() throws Exception {
		final List<Integer> linkedList = new LinkedList<>();
		for( int i = 0; i < SIZE; i++ ) {
			linkedList.add( i );
		}
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return summarize( linkedList );
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void hashSetIteratorIsScalarized() throws Exception {
		final Collection<Integer> hashSet = new HashSet<>();
		for( int i = 0; i < SIZE; i++ ) {
			hashSet.add( i );
		}
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return summarize( hashSet );
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void troveThashSetIteratorIsScalarized() throws Exception {
		final Collection<Integer> thashSet = new THashSet<>();
		for( int i = 0; i < SIZE; i++ ) {
			thashSet.add( i );
		}
		assertThat(
				new Scenario() {

					@Override
					public long run() {
						return summarize( thashSet );
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void arraysAsListIteratorIsScalarized() throws Exception {
		final Integer[] array = new Integer[SIZE];
		for( int i = 0; i < SIZE; i++ ) {
			array[i] = i;
		}
		final Iterable<Integer> list = Arrays.asList( array );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						return summarize( list );
					}
				},
				finallyAllocatesNothing()
		);
	}

	private static long summarize( final Iterable<Integer> list ) {
		long sum = 0;
		for( final Integer i : list ) {
			sum += i;
		}
		return sum;
	}

}
