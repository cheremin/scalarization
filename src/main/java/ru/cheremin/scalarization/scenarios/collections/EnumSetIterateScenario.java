package ru.cheremin.scalarization.scenarios.collections;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.runWithAll;

/**
 * EnumSet.iterator() is scalarized successfully, at least for small enums (size<=16)
 * <p/>
 * Enum70 is not scalarized (TODO RC: investigate why)
 *
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class EnumSetIterateScenario extends AllocationScenario {

	private final EnumSet<?> set;

	{
		switch( SIZE ) {
			case 1: {
				set = EnumSet.allOf( Enum1.class );
				break;
			}
			case 3: {
				set = EnumSet.allOf( Enum3.class );
				break;
			}
			case 16: {
				set = EnumSet.allOf( Enum16.class );
				break;
			}
			case 70: {
				set = EnumSet.allOf( Enum70.class );
				break;
			}
			default: {
				throw new IllegalStateException( "Unsupported size=" + SIZE );
			}
		}
	}

	@Override
	public long run() {
		return iterateContent( set );
	}

	private static long iterateContent( final EnumSet<?> set ) {
		long result = 0;
		for( final Enum e : set ) {
			result += e.ordinal();
		}
		return result;
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return runWithAll(
				SIZE_KEY, 1, 3, 16, 70
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

	public static enum Enum16 {
		_1, _2, _3, _4, _5, _6, _7, _8, _9, _10,
		_11, _12, _13, _14, _15, _16
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
