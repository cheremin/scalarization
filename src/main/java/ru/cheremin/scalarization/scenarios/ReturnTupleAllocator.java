package ru.cheremin.scalarization.scenarios;

/**
 * @author ruslan
 *         created 09/02/16 at 21:41
 */
public class ReturnTupleAllocator extends AllocationScenario {
	@Override
	public long allocate() {
		final Tuple2<String> tuple = createTuple();

		return tuple.item1.length() + tuple.item2.length();
	}

	private Tuple2<String> createTuple() {
		return new Tuple2( "18", "678" );
	}

	public static class Tuple2<T> {
		public T item1;
		public T item2;

		public Tuple2( final T item1,
		               final T item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}
	}
}
