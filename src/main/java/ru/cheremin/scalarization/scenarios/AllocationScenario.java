package ru.cheremin.scalarization.scenarios;

import ru.cheremin.scalarization.ForkingMain;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;

/**
 * @author ruslan
 *         created 09/02/16 at 12:36
 */
public abstract class AllocationScenario {
	public static final String SCENARIO_SIZE_KEY = "scenario.size";

	public static final int SIZE = Integer.getInteger( SCENARIO_SIZE_KEY, 16 );

	public abstract long allocate();

	public String additionalInfo() {
		return "";
	}


	@Override
	public String toString() {
		final String result = String.format( "%s[size:%d]", getClass().getSimpleName(), SIZE );
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
		return new ForkingMain.ScenarioRun(
				new SystemProperty( propertyName, propertyValue.toString() )
		);
	}

	protected static ForkingMain.ScenarioRun runWith( final String propertyName1,
	                                                  final Object propertyValue1,
	                                                  final String propertyName2,
	                                                  final Object propertyValue2 ) {
		return new ForkingMain.ScenarioRun(
				new SystemProperty( propertyName1, propertyValue1.toString() ),
				new SystemProperty( propertyName2, propertyValue2.toString() )
		);
	}

	protected static ForkingMain.ScenarioRun runWith( final String propertyName1,
	                                                  final Object propertyValue1,
	                                                  final String propertyName2,
	                                                  final Object propertyValue2,
	                                                  final String propertyName3,
	                                                  final Object propertyValue3 ) {
		return new ForkingMain.ScenarioRun(
				new SystemProperty( propertyName1, propertyValue1.toString() ),
				new SystemProperty( propertyName2, propertyValue2.toString() ),
				new SystemProperty( propertyName3, propertyValue3.toString() )
		);
	}
}
