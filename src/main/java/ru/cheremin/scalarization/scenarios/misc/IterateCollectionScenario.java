package ru.cheremin.scalarization.scenarios.misc;


import java.util.*;

import gnu.trove.set.hash.THashSet;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check iterating with {@linkplain Iterator} over various {@linkplain Collection}
 * implementations
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class IterateCollectionScenario extends AllocationScenario {
	public static final String COLLECTION_TYPE_KEY = "scenario.collection-type";

	private static final CollectionType COLLECTION_TYPE = CollectionType.valueOf(
			System.getProperty( COLLECTION_TYPE_KEY, CollectionType.ARRAY_LIST.name() )
	);

	public Collection<Integer> collection = COLLECTION_TYPE.create();

	@Override
	public long allocate() {
		return iterate( collection );
	}

	private long iterate( final Iterable<Integer> iterable ) {
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
		ARRAY_LIST {
			@Override
			public Collection<Integer> create() {
				final List<Integer> collection = new ArrayList<>( SIZE );
				for( int i = 0; i < SIZE; i++ ) {
					collection.add( new Integer( i ) );
				}
				return collection;
			}
		},
		LINKED_LIST {
			@Override
			public Collection<Integer> create() {
				final List<Integer> collection = new LinkedList<>();
				for( int i = 0; i < SIZE; i++ ) {
					collection.add( new Integer( i ) );
				}
				return collection;
			}
		},
		ARRAYS_AS_LIST {
			@Override
			public Collection<Integer> create() {
				final Integer[] array = new Integer[SIZE];
				for( int i = 0; i < SIZE; i++ ) {
					array[i] = i;
				}
				return Arrays.asList( array );
			}
		},
		HASH_SET {
			@Override
			public Collection<Integer> create() {
				final Set<Integer> collection = new HashSet<>( SIZE );
				for( int i = 0; i < SIZE; i++ ) {
					collection.add( new Integer( i ) );
				}
				return collection;
			}
		},
		THASH_SET {
			@Override
			public Collection<Integer> create() {
				final Set<Integer> collection = new THashSet<>( SIZE );
				for( int i = 0; i < SIZE; i++ ) {
					collection.add( new Integer( i ) );
				}
				return collection;
			}
		};

		public abstract Collection<Integer> create();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 0, 1, 2, 4, 64, 65 ),

				allOf( COLLECTION_TYPE_KEY, CollectionType.values() )
		);
	}
}
