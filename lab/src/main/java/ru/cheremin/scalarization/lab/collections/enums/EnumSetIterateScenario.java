package ru.cheremin.scalarization.lab.collections.enums;


import java.util.*;

import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.lab.collections.enums.SampleEnums.*;

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

}
