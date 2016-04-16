package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;
import static ru.cheremin.scalarization.scenarios.Utils.generateStringArray;

/**
 * Check is Arrays.asList(array) scalarized (only wrapper, not wrapped array itself).
 * <p/>
 * With 1.7/1.8 index access is scalarized, but iterator access is not. Reasons are
 * unknown.
 * <p/>
 * Moreover, there is a difference in memory allocations between +/- EA, so it looks
 * like _iterators_ are scalarized, but list itself is not...
 * <p/>
 * <p/>
 * TODO RC: Why iterator not scalarized?
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ArrayAsListScenario extends AllocationScenario {
	public static final String ITERATION_TYPE_KEY = "scenario.iteration-type";

	private static final IterationType ITERATION_TYPE = IterationType.valueOf(
			System.getProperty( ITERATION_TYPE_KEY, IterationType.WITH_INDEX.name() )
	);

	private final String[] array = generateStringArray( SIZE );

	@Override
	public long run() {

		switch( ITERATION_TYPE ) {
			case WITH_INDEX:
				return runWithIndex();
			case WITH_ITERATOR:
				return runWithIterator();
			default:
				throw new IllegalStateException( ITERATION_TYPE + " is unknown" );
		}
	}

	private long runWithIndex() {
		final List<String> list = Arrays.asList( array );
		return sumWithIndex( list );
	}

	private long sumWithIndex( final List<String> list ) {
		long sum = 0;
		for( int i = 0; i < list.size(); i++ ) {
			final String s = list.get( i );
			sum += s.length();
		}
		return sum;
	}

	private long runWithIterator() {
		final List<String> list = Arrays.asList( array );
		return sumWithIterator( list );
	}

	private long sumWithIterator( final List<String> list ) {
		long sum = 0;
		final Iterator<String> i = list.iterator();
		while( i.hasNext() ) {
			final String s = i.next();
			sum += s.length();
		}
		return sum;
	}

	public enum IterationType {
		WITH_INDEX,
		WITH_ITERATOR
	}

	@Override
	public String additionalInfo() {
		return ITERATION_TYPE.name();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 0, 1, 2, 4, 65 ),
				allOf( ITERATION_TYPE_KEY, IterationType.values() )
		);
	}
}
