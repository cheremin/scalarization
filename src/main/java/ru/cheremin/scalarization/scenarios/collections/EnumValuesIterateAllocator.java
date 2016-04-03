package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;

/**
 * Seems like no way .values() could be scalarized...
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class EnumValuesIterateAllocator extends AllocationScenario {
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
				SIZE_KEY, 1, 3, 70
		);
	}


	public static enum Enum1 {
		ONLY
	}

	public static enum Enum3 {
		FIRST,
		SECOND,
		THIRD
	}

	public static enum Enum70 {
		_1, _2, _3, _4, _5, _6, _7, _8, _9, _10,
		_11, _12, _13, _14, _15, _16, _17, _18, _19, _20,
		_21, _22, _23, _24, _25, _26, _27, _28, _29, _30,
		_31, _32, _33, _34, _35, _36, _37, _38, _39, _40,
		_41, _42, _43, _44, _45, _46, _47, _48, _49, _50,
		_51, _52, _53, _54, _55, _56, _57, _58, _59, _60,
		_61, _62, _63, _64, _65, _66, _67, _68, _69, _70

	}
}
