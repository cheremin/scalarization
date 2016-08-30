package ru.cheremin.scalarization.lab.collections;


import java.util.*;

import gnu.trove.set.hash.THashSet;
import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check iterating with {@linkplain Iterator} over various {@linkplain Collection}
 * implementations.
 * <p/>
 * For 1.8.0_77 almost everything is scalarized successfully.
 * <p/>
 * Results for 1.7.0_80 are surprisingly complex:
 * 1. ArrayList and Arrays.asList() iterators are scalarized successfully.
 * 2. LinkedList iterator are unstable scalarized for size > 2-4. Sometimes yes, sometimes
 * not. -XX:+PrintInlining shows early in log:
 * "java.util.LinkedList::listIterator (15 bytes)   executed < MinInliningThreshold times"
 * ...and many lines after:
 * "java.util.LinkedList::listIterator (15 bytes)   already compiled into a medium method"
 * So, the scenario looks like: on early compilation turn LinkedList.listIterator()
 * is not frequent enough to be inlined right from the start, so it is compiled separately
 * from root iteration method. And somehow resulting native code become quite big ("medium
 * method" it is > InlineSmallCode/4 = 250bytes), so on following re-compilation turns
 * it is not inlined because of this.
 * TODO RC: This is quite interesting how do few lines of java code in LinkedList.listIterator()
 * could be compiled into >250 bytes of asm code, though...
 * <p/>
 * By the way, increasing InlineSmallCode, or decreasing MinInliningThreshold could "fix"
 * this.
 * Why 1.8.0_77 does not suffer from same shit? It is because of higher InlineSmallCode
 * defaults: 2000 instead of 1000 in 1.7. With InlineSmallCode=1000 1.8 jvm shows same
 * behavior as 1.7
 * <p/>
 * But for HashSet/THashSet lab are even more mysterious: even though PrintInlining
 * shows everything is inlined successfully, scalarization still not happens for some
 * sizes, like 4..32. Surprisingly, sizes 64-65 sometimes are scalarized. This is truly
 * mystery! TODO RC: to investigate
 * <p/>
 * Also, quite an interesting behavior for older JVM: 1.7.0_25. size=0 case is not
 * scalarized with 1.7.0_25. This is because of Iterator.next() method: it is never
 * called, but still present in bytecode. Such method is big enough (65bc) to be skipped
 * by default inlining policy for small methods, and is not called frequently enough
 * to be inlined as hot method (because not called at all). So it is not inlined, and
 * EA stops on it.
 * This applied to ArrayList, LinkedList, Arrays.asList, THashSet...but surprisingly,
 * NOT HashSet -- HashSet with size=0 is scalarized like a charm. Most probably it is
 * because HashSet.keySet() has it's own iterator, not inherited from AbstractList,
 * but such an iterator is not simpler than AbstractList's one...
 * <p/>
 * With 1.7.0_80/1.8.0_77 some JIT magic removes .next() from method profile entirely
 * for [size=0] -- I suspect something like DCE or advanced frequent/unfrequent path
 * extraction is applied before EA. Without this offender method EA/SA is succeeded
 * with grace
 * <p/>
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class IterateCollectionsScenario extends AllocationScenario {
	public static final String COLLECTION_TYPE_KEY = "scenario.collection-type";

	private static final CollectionType COLLECTION_TYPE = CollectionType.valueOf(
			System.getProperty( COLLECTION_TYPE_KEY, CollectionType.ARRAY_LIST.name() )
	);

	private Iterable<Integer> iterable = COLLECTION_TYPE.create();

	@Override
	public long run() {
		return iterate( iterable );
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
				allOf( COLLECTION_TYPE_KEY, CollectionType.values() ),

				allOf( SIZE_KEY, 0, 1, 2, 4, 16, 32, 64, 65 )
//				allOf( SIZE_KEY,
//				       0, 0, 0,
//				       1, 1, 1,
//				       2, 2, 2,
//				       4, 4, 4/*, 64, 65*/ ),


		);
	}
}
