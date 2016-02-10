package ru.cheremin.scalarization.scenarios;

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
}
