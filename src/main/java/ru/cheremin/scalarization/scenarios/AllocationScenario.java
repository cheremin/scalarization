package ru.cheremin.scalarization.scenarios;

import ru.cheremin.scalarization.ForkingMain;
import ru.cheremin.scalarization.infra.JvmArg;

/**
 * @author ruslan
 *         created 09/02/16 at 12:36
 */
public abstract class AllocationScenario {
	public static final int SIZE = Integer.getInteger( "scenario.size", 16 );

	public abstract long allocate();

	public String additionalInfo() {
		return "";
	}


	@Override
	public String toString() {
		final String result = String.format( "%s[%d]", getClass().getSimpleName(), SIZE );
		final String additionalInfo = additionalInfo();

		if( additionalInfo.isEmpty() ) {
			return result;
		} else {
			return result + '[' + additionalInfo + ']';
		}
	}

	/* ======  helpers for @ScenarioRunArgs methods ============================== */

	protected static ForkingMain.ScenarioRun runWith( final String propertyName,
	                                                  final Object propertyValue ) {
		return new ForkingMain.ScenarioRun( new JvmArg.SystemProperty( propertyName, propertyValue.toString() ) );
	}

	protected static ForkingMain.ScenarioRun runWith( final String propertyName1,
	                                                  final Object propertyValue1,
	                                                  final String propertyName2,
	                                                  final Object propertyValue2 ) {
		return new ForkingMain.ScenarioRun(
				new JvmArg.SystemProperty( propertyName1, propertyValue1.toString() ),
				new JvmArg.SystemProperty( propertyName2, propertyValue2.toString() )
		);
	}
}
