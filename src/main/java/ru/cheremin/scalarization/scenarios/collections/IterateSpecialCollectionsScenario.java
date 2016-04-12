package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static java.util.Collections.singletonList;
import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check iterating with {@linkplain Iterator} over various {@linkplain Collections}.singletonXXX()
 * and .EMPTY_ implementations. In contrast with {@linkplain IterateCollectionsScenario}
 * we allocate new collection not once, in ctor, but on each run. So the test is not
 * only for iterator through singleton-like collections, but also about collections
 * itself. The idea was to check if I have method(Collection), and want to pass single
 * object, or no object at all, with using appropriate Collections factory method --
 * will it be scalarized?
 *
 *
 * Seems like yes, all such patterns are stable scalarized in both 1.7 and 1.8.
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class IterateSpecialCollectionsScenario extends AllocationScenario {
	public static final String COLLECTION_TYPE_KEY = "scenario.collection-type";

	private static final CollectionType COLLECTION_TYPE = CollectionType.valueOf(
			System.getProperty( COLLECTION_TYPE_KEY, CollectionType.SINGLETON_SET.name() )
	);

	public static final Integer VALUE = 1256547;


	@Override
	public long run() {

		final Collection<Integer> collection = COLLECTION_TYPE.create();
		return iterate( collection );
	}

	private static long iterate( final Iterable<Integer> iterable ) {
		long sum = 0;
		for( final Integer i : iterable ) {
			sum += i;
		}
		return sum;
	}

	@Override
	public String additionalInfo() {
		return COLLECTION_TYPE.name();
	}

	public enum CollectionType {
		SINGLETON_LIST {
			@Override
			public Collection<Integer> create() {
				return singletonList( VALUE );
			}
		},
		SINGLETON_SET {
			@Override
			public Collection<Integer> create() {
				return Collections.singleton( VALUE );
			}
		},

		SINGLETON_MAP {
			@Override
			public Collection<Integer> create() {
				return Collections.singletonMap( "KEY", VALUE ).values();
			}
		},

		//yes, there is no point in checking allocation for below, since they are
		//  singleton instances, and even return singleton iterators. But just for
		//  completeness...

		EMPTY_LIST {
			@Override
			public Collection<Integer> create() {
				return Collections.emptyList();
			}
		},

		EMPTY_SET {
			@Override
			public Collection<Integer> create() {
				return Collections.emptySet();
			}
		},
		EMPTY_MAP {
			@Override
			public Collection<Integer> create() {
				return Collections.<Integer, Integer>emptyMap().values();
			}
		};

		public abstract Collection<Integer> create();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				singletonList( new SystemProperty( SIZE_KEY, "-1" ) ),

				allOf( COLLECTION_TYPE_KEY, CollectionType.values() )
		);
	}
}
