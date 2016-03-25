package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runForAll;
import static ru.cheremin.scalarization.scenarios.Utils.generateStringArray;

/**
 * Check is Arrays.asList(array) scalarized -- only wrapper, not wrapped array itself
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ArrayAsListScenario extends AllocationScenario {
	private final String[] array = generateStringArray( SIZE );

	@Override
	public long allocate() {
		int sum = 0;
		//TODO RC: somehow it is not scalarized! Why?
		//TODO but there are difference between +/- EA: 56 bytes/run vs 24 bytes/run
		for( final String s : Arrays.asList( array ) ) {
			sum += s.length();
		}
		return sum;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runForAll(
				SIZE_KEY, 0, 1, 2, 4, 65
		);
	}
}
