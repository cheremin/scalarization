package ru.cheremin.scalarization.lab.collections;


import java.util.*;

import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

/**
 * Check is ArrayList scalarized, at least for small sizes?
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class SmallArrayListScenario extends AllocationScenario {

	@Override
	public long run() {
		final ArrayList<String> list = new ArrayList<>( SIZE );
		for( int i = 0; i < SIZE; i++ ) {
			list.add( "" );
		}
		list.clear();
		return list.size();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return ScenarioRun.runWithAll(
				SIZE_KEY, 0, 1, 2, 4
		);
	}
}
