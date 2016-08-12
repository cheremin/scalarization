package ru.cheremin.scalarization.scenarios.collections.enums;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.collections.enums.SampleEnums.*;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;

/**
 * Seems like no way .values() could be scalarized...
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class EnumValuesIterateScenario extends AllocationScenario {
	//TODO RC: generate enum class of appropriate size on the fly, with byte-code-gen

	@Override
	public long run() {
		switch( SIZE ) {
			case 1: {
				final Enum1[] values = Enum1.values();
				return values[0].ordinal();
			}
			case 3: {
				final Enum3[] values = Enum3.values();
				return values[2].ordinal();
			}
			case 16: {
				final Enum16[] values = Enum16.values();
				return values[7].ordinal();
			}
			case 70: {
				final Enum70[] values = Enum70.values();
				return values[42].ordinal();
			}
			default:
				throw new IllegalStateException( SIZE + " is not supported" );
		}

	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll(
				SIZE_KEY, 1, 3, 16, 70
		);
	}
}
