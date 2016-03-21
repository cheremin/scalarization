package ru.cheremin.scalarization.scenarios;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static ru.cheremin.scalarization.scenarios.ScenarioRunsUtils.runForAll;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class IteratorAllocator extends AllocationScenario {
	public List<Integer> arrayList = new ArrayList<Integer>( SIZE );
	public List<Integer> linkedList = new LinkedList<Integer>();

	{
		for( int i = 0; i < SIZE; i++ ) {
			arrayList.add( new Integer( i ) );
			linkedList.add( new Integer( i ) );
		}
	}

	@Override
	public long allocate() {
		return iterate( arrayList );
	}

	private long iterate( final Collection<Integer> list ) {
		long sum = 0;
		for( final Integer i : list ) {
			sum += i;
		}
		return sum;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		//TODO RC: array vs linked list
		return runForAll( SCENARIO_SIZE_KEY, 0, 1, 2, 4, 64, 65 );
	}
}
