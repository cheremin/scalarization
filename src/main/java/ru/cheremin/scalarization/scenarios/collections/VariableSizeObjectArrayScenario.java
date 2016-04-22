package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;

/**
 * Seems to scalarize with size 1. This is strange, because escape.cpp explicitly
 * states only statically-sized arrays have chance to be scalarized. I think, this
 * is because for small SIZE JIT is able to predict rnd.nextInt(SIZE) (it may be not
 * so hard because of [bits % 1] statement inside loop).
 *
 * TODO RC: I'm waiting for the way to generate 0-1 so JIT can't predict them
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class VariableSizeObjectArrayScenario extends AllocationScenario {

	@Override
	public long run() {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();

		final String[] array = new String[rnd.nextInt( SIZE )];

		return array.length;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		//RC: 0 is not acceptable for rnd.nextInt()
		return runWithAll( SIZE_KEY, 1, 2, 16 /*, 64, 65*/ );
	}
}
