package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.withoutSpecificParameters;

/**
 * EnumSet.iterator() is scalarized successfully, at least for small enums
 *
 * TODO check enums > 64 elements
 *
 * EnumSet.allOf() is not scalarized. I'm still not sure why, but most probable
 * it is because of branch between RegularEnumSet/JumboEnumSet in EnumSet.noneOf(...)
 * factory method. It looks like universe.length value can't be statically evaluated
 * by JIT, and so branch can't be statically resolved, so "merge point" issue is in
 * play (see {@linkplain ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario}
 * for details)
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class EnumSetIterateScenario extends AllocationScenario {

	@Override
	public long run() {
		final EnumSet<Enum3> set = EnumSet.allOf( Enum3.class );
		long result = 0;
		for( final Enum3 e : set ) {
			result += e.ordinal();
		}
		return result;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
//		return runWithAll(
//				SIZE_KEY, 1, 3, 70
//		);
		return withoutSpecificParameters();
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
