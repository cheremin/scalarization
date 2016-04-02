package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runForAll;

/**
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class ObjectArrayUnpredictableSizeAllocator extends AllocationScenario {

	@Override
	public long run() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		final String[] array = new String[rnd.nextInt( SIZE )];

		return array.length;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runForAll( SIZE_KEY, 0, 1, 2, 16 /*, 64, 65*/ );
	}
}
