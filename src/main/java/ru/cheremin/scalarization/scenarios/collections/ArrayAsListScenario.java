package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check if Arrays.asList(array) is scalarized (only wrapper, not wrapped array itself).
 * My goal is to check pattern like for( item : Arrays.asList(array) ){...} -- will
 * converting arrays to Lists be "free"?
 * <p/>
 * With 1.7/1.8 iteration with index access is scalarized -- no Arrays.ArrayList
 * instances created. But iteration with iterator is not scalarized. Surprisingly,
 * iterator itself is scalarized, but given iterator created, Arrays.ArrayList instance
 * is not scalarized anymore! Reasons are still not clear: e.g. PrintCompilation/PrintInlining
 * shows everything is fine with inlining, all methods are inlined.
 *
 * But interesting observation: Arrays.ArrayList inherit iterator from it's superclass,
 * {@linkplain AbstractList}. AbstractList.Itr is non-static inner class, it captures
 * "this" of enclosing class (AbstractList). I have copied Arrays.ArrayList impl here,
 * and override .iterator() to return {@linkplain ArrayIterator} -- iterator over
 * array, implemented as static class keeping no reference to the ArrayList it was
 * created from. And with such "detached" iterator both iterator and ArrayList are
 * scalarized successfully. This leads me to conclusion it iterator-to-list reference
 * prevents list from scalarization.
 *
 * See <a href="https://bugs.openjdk.java.net/browse/JDK-8155769">JDK-8155769</a> for
 * details
 *
 * <p/>
 *
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ArrayAsListScenario extends AllocationScenario {
	public static final String ITERATION_TYPE_KEY = "scenario.iteration-type";

	private static final IterationType ITERATION_TYPE = IterationType.valueOf(
			System.getProperty( ITERATION_TYPE_KEY, IterationType.WITH_INDEX.name() )
	);

	private final Integer[] array = new Integer[SIZE];

	{
		for( int i = 0; i < array.length; i++ ) {
			array[i] = new Integer( i );
		}
	}

	@Override
	public long run() {

		switch( ITERATION_TYPE ) {
			case WITH_INDEX:
				return arrayWithIndex();
			case WITH_ITERATOR:
				return arrayWithIterator();
			case WITH_DETACHED_ITERATOR:
				return arrayWithDetachedIterator();
			default:
				throw new IllegalStateException( ITERATION_TYPE + " is unknown" );
		}
	}

	private long arrayWithIndex() {
		final List<Integer> list = Arrays.asList( array );
		long sum = 0;
		for( int i = 0; i < list.size(); i++ ) {
			final Integer integer = list.get( i );
			sum += integer.intValue();
		}
		return sum;
	}


	private long arrayWithIterator() {
		final List<Integer> list = Arrays.asList( array );
		long sum = 0;
		for( final Integer integer : list ) {
			sum += integer.intValue();
		}
		return sum;
	}

	private long arrayWithDetachedIterator() {
		final List<Integer> list = new ArrayList<>( array );
		long sum = 0;
		for( final Integer integer : list ) {
			sum += integer.intValue();
		}
		return sum;
	}

	public enum IterationType {
		WITH_ITERATOR,
		WITH_DETACHED_ITERATOR,
		WITH_INDEX
	}

	@Override
	public String additionalInfo() {
		return ITERATION_TYPE.name();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( ITERATION_TYPE_KEY, IterationType.values() ),
				allOf( SIZE_KEY, 0, 1, 2, 4, 65 )
		);
	}

	/**
	 * Copied from {@linkplain java.util.Arrays.ArrayList}, with only {@linkplain #iterator()}
	 * method changed: return "detached" iterator, i.e. static class, which captures
	 * array reference, not ArrayList instance reference.
	 */
	public static class ArrayList<E> extends AbstractList<E>
			implements RandomAccess, java.io.Serializable {
		private static final long serialVersionUID = -2764017481108945198L;

		private final E[] a;

		ArrayList( final E[] array ) {
			if( array == null ) {
				throw new NullPointerException();
			}
			a = array;
		}

		public int size() {
			return a.length;
		}

		public Object[] toArray() {
			return a.clone();
		}

		public <T> T[] toArray( T[] a ) {
			int size = size();
			if( a.length < size ) {
				return Arrays.copyOf( this.a, size,
				                      ( Class<? extends T[]> ) a.getClass() );
			}
			System.arraycopy( this.a, 0, a, 0, size );
			if( a.length > size ) {
				a[size] = null;
			}
			return a;
		}

		public E get( int index ) {
			return a[index];
		}

		public E set( int index, E element ) {
			E oldValue = a[index];
			a[index] = element;
			return oldValue;
		}

		public int indexOf( Object o ) {
			if( o == null ) {
				for( int i = 0; i < a.length; i++ ) {
					if( a[i] == null ) {
						return i;
					}
				}
			} else {
				for( int i = 0; i < a.length; i++ ) {
					if( o.equals( a[i] ) ) {
						return i;
					}
				}
			}
			return -1;
		}

		public boolean contains( Object o ) {
			return indexOf( o ) != -1;
		}

		@Override
		public Iterator<E> iterator() {
			return new ArrayIterator<>( a );
		}

	}

	private static class ArrayIterator<E> implements Iterator<E> {

		private final E[] array;

		public ArrayIterator( final E[] array ) {
			this.array = array;
		}

		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < array.length;
		}

		@Override
		public E next() {
			if( !hasNext() ) {
				throw new NoSuchElementException();
			}

			final E item = array[index];
			index++;
			return item;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException( "Method not implemented" );
		}
	}
}
