package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils.StringKeysGenerator;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;
import static ru.cheremin.scalarization.scenarios.Utils.randomKeysGenerator;

/**
 * Check pattern: method with multiple return values wrapped in composite object.
 * <p/>
 * For both 1.7 and 1.8:
 * Simple one-type tuples are successfully scalarized with 1.7/1.8, even being casted
 * to generic interface.
 * <p/>
 * Mixed type tuples are not scalarized.
 * <p/>
 * Single or mixed types with nulls are not scalarized
 *
 * @author ruslan
 *         created 09/02/16 at 21:41
 */
public class ReturnTupleScenario extends AllocationScenario {

	public static final String TUPLE_TYPE_KEY = "scenario.tuple-type";

	private static final TupleType TUPLE_TYPE = TupleType.valueOf(
			System.getProperty( TUPLE_TYPE_KEY, TupleType.IMMUTABLE_PAIR.name() )
	);

	private final StringKeysGenerator generator = randomKeysGenerator( 1024 );

	@Override
	public long run() {
		final Pair<String, String> tuple = TUPLE_TYPE.create(
				generator.next(),
				generator.next()
		);
		if( tuple != null ) {
			return tuple.getLeft().length()
					+ tuple.getRight().length();
		} else {
			return 0;
		}

	}

	@Override
	public String additionalInfo() {
		return TUPLE_TYPE.name();
	}

	public static enum TupleType {
		IMMUTABLE_PAIR {
			@Override
			public Pair<String, String> create( final String key1,
			                                    final String key2 ) {
				return ImmutablePair.of( key1, key2 );
			}
		},
		MUTABLE_PAIR {
			@Override
			public Pair<String, String> create( final String key1,
			                                    final String key2 ) {
				return MutablePair.of( key1, key2 );
			}
		},
		PAIR_NULL {
			@Override
			public Pair<String, String> create( final String key1,
			                                    final String key2 ) {
				if( key1.length() < key2.length() ) {
					return ImmutablePair.of( key1, key2 );
				} else {
					return null;
				}
			}
		},
		MIXED_PAIR {
			@Override
			public Pair<String, String> create( final String key1,
			                                    final String key2 ) {
				if( key1.length() < key2.length() ) {
					return MutablePair.of( key1, key2 );
				} else {
					return ImmutablePair.of( key1, key2 );
				}
			}
		},
		MIXED_PAIR_NULL {
			@Override
			public Pair<String, String> create( final String key1,
			                                    final String key2 ) {
				final char c1 = key1.charAt( 0 );
				final char c2 = key2.charAt( 0 );
				if( c1 > c2 ) {
					return MutablePair.of( key1, key2 );
				} else if( c1 < c2 ) {
					return ImmutablePair.of( key1, key2 );
				} else {
					return null;
				}
			}
		};

		public abstract Pair<String, String> create( final String key1,
		                                             final String key2 );
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll( TUPLE_TYPE_KEY, TupleType.values() );
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
