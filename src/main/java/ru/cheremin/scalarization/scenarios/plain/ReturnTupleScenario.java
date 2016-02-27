package ru.cheremin.scalarization.scenarios.plain;

import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils.StringKeysGenerator;

import static ru.cheremin.scalarization.scenarios.Utils.randomKeysGenerator;

/**
 * Check pattern: method with multiple return values wrapped in composite object.
 *
 *
 *
 * @author ruslan
 *         created 09/02/16 at 21:41
 */
public class ReturnTupleScenario extends AllocationScenario {
	private final StringKeysGenerator generator = randomKeysGenerator( SIZE );

	@Override
	public long allocate() {
		final Tuple2<String> tuple = createTuple2();

		return tuple.getItem1().length()
				+ tuple.getItem2().length();
	}

	private Tuple2<String> createTuple2() {
		final String key1 = generator.next();
		final String key2 = generator.next();
		return new Tuple2( key1, key2 );
	}

	public static class Tuple2<T> {
		public T item1;
		public T item2;

		public Tuple2( final T item1,
		               final T item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		public T getItem1() {
			return item1;
		}

		public T getItem2() {
			return item2;
		}
	}
}
