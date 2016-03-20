package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.ForkingMain;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ObjectArrayUnpredictableSizeAllocator extends AllocationScenario {

	@Override
	public long allocate() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		final String[] array = new String[rnd.nextInt( SIZE )];

		return array.length;
	}

	@ScenarioRunArgs
	public static List<ForkingMain.ScenarioRun> parametersToRunWith() {
		return Arrays.asList(
				runWith( SCENARIO_SIZE_KEY, "0" ),

				runWith( SCENARIO_SIZE_KEY, "1" ),

				runWith( SCENARIO_SIZE_KEY, "2" ),

				runWith( SCENARIO_SIZE_KEY, "16" )

//				runWith( SCENARIO_SIZE_KEY, "64" ),
//
//				runWith( SCENARIO_SIZE_KEY, "65" )
		);
	}
}
