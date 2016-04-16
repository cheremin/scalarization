package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import gnu.trove.set.hash.THashSet;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check iterating with {@linkplain Iterator} over various {@linkplain Collection}
 * implementations.
 * <p/>
 * Results are surprisingly complex. While most of scenarios are scalarized in both
 * 1.7 and 1.8 JVMs, but:
 * <p/>
 * 1. [size=0] case is not scalarized with 1.7. This is because of Iterator.next()
 * method: it is never called, but still present in bytecode. Such method is big enough
 * (65bc) to be skipped by default inlining policy for small methods, and is not called
 * frequently enough to be inlined as hot method (because not called at all). So it
 * is not inlined, and EA stops on it. This applied to ArrayList, LinkedList,
 * Arrays.asList, THashSet...
 * ....but surprisingly, NOT HashSet -- HashSet with size=0 is scalarized like a charm.
 * Most probably it is because HashSet.keySet() has it's own iterator, not inherited
 * from AbstractList, but such an iterator is not simplier than AbstractList's one...
 * (TODO investigate why).
 * <p/>
 * With 1.8 some JIT magic removes .next() from method profile entirely for [size=0]
 * -- I suspect something like DCE or advanced frequent/unfrequent path extraction
 * is applied before EA. Without this offender method EA/SA is succeeded with grace
 * <p/>
 * <p/>
 * 2. Scalarization is unstable for almost all scenarios. Most of the time it works,
 * but not always: repeated runs give different results. With 1.7 "not works" means
 * no scalarization at all, but with 1.8 it means "part of iterations are allocated
 * something, while most of them are not". This looks like compile-decompile-recompile
 * cycles, so may be it is effect of advanced tiered compilation which adopts to
 * changing profile. This hypothesis is supported by the fact that with longer runs
 * 1.8 reach steady state with allocations finally eliminated.
 * ... but 1.7 does not.
 * <p/>
 * TODO Both phenomena are still to be cleared
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class IterateCollectionsScenario extends AllocationScenario {
	public static final String COLLECTION_TYPE_KEY = "scenario.collection-type";

	private static final CollectionType COLLECTION_TYPE = CollectionType.valueOf(
			System.getProperty( COLLECTION_TYPE_KEY, CollectionType.ARRAY_LIST.name() )
	);

	public Collection<Integer> collection = COLLECTION_TYPE.create();

	@Override
	public long run() {
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
//				allOf( SIZE_KEY,
//				       0, 0, 0,
//				       1, 1, 1,
//				       2, 2, 2,
//				       4, 4, 4/*, 64, 65*/ ),

				allOf( COLLECTION_TYPE_KEY, CollectionType.values() )
		);
	}
}
