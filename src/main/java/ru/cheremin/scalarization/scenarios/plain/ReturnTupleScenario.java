package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils.Pool;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;
import static ru.cheremin.scalarization.scenarios.Utils.randomStringsPool;

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

	private final Pool<String> pool = randomStringsPool( 1024 );

	@Override
	public long run() {
		final Pair<String, String> tuple = TUPLE_TYPE.tuple( pool );
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

	public enum TupleType {
		IMMUTABLE_PAIR {
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				return ImmutablePair.of( value1, value2 );
			}
		},
		MUTABLE_PAIR {
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				return MutablePair.of( value1, value2 );
			}
		},
		PAIR_OR_NULL {
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				if( value1.length() < value2.length() ) {
					return ImmutablePair.of( value1, value2 );
				} else {
					return null;
				}
			}
		},
		MIXED_PAIR {
			//RC: this is not scalarized, and my first guess was it is because of mixed
			//  type -- i.e. runtime will need exact type for de-optimization, but here
			//  it could be 2 types. While the hypothesis is reasonable, this specific
			//  case is not about it...
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				if( value1.length() < value2.length() ) {
					return MutablePair.of( value1, value2 );
				} else {
					return ImmutablePair.of( value1, value2 );
				}
			}
		},
		SAME_PAIR_IN_BRANCHES {
			//... because this is ALSO not scalarized, despite the fact here types are
			//  the same. So now it looks like same issue as in ControlFlowScenario:
			//  i.e. join in 1 reference values come from different branches.
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				if( value1.length() < value2.length() ) {
					return MutablePair.of( value1, value2 );
				} else {
					return MutablePair.of( value1, value2 );
				}
			}
		},
		MIXED_PAIR_OR_NULL {
			@Override
			public Pair<String, String> tuple( final Pool<String> pool ) {
				final String value1 = pool.next();
				final String value2 = pool.next();
				final char c1 = value1.charAt( 0 );
				final char c2 = value2.charAt( 0 );
				if( c1 > c2 ) {
					return MutablePair.of( value1, value2 );
				} else if( c1 < c2 ) {
					return ImmutablePair.of( value1, value2 );
				} else {
					return null;
				}
			}
		};

		public abstract Pair<String, String> tuple( final Pool<String> pool );
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll( TUPLE_TYPE_KEY, TupleType.values() );
	}
}
