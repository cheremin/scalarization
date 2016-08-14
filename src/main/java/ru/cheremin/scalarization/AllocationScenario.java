package ru.cheremin.scalarization;

/**
 * Typical scenario extends this class
 *
 * @author ruslan
 *         created 09/02/16 at 12:36
 */
public abstract class AllocationScenario implements Scenario{
	public static final String SIZE_KEY = "scenario.size";

	public static final int SIZE = Integer.getInteger( SIZE_KEY, 16 );

	public abstract long run();

	protected String additionalInfo() {
		return "";
	}


	@Override
	public String toString() {
		final String result;
		if( SIZE < 0 ) {
			result = getClass().getSimpleName();
		} else {
			result = String.format( "%s[SIZE:%d]", getClass().getSimpleName(), SIZE );
		}
		final String additionalInfo = additionalInfo();

		if( additionalInfo.isEmpty() ) {
			return result;
		} else {
			return result + '[' + additionalInfo + ']';
		}
	}

}
