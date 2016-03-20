package ru.cheremin.scalarization.scenarios.collections;

import java.util.*;

import ru.cheremin.scalarization.ForkingMain;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * Only array of size 1 is scalarized with 1.8. Array of size >=2 is not scalarized
 * <p/>
 * TODO RC: random index array access
 * TODO RC: check array of size 0 (with .getClass())
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class FixedSizeObjectArrayAllocator extends AllocationScenario {
	private static final String VECTORIZED_ACCESS_KEY = "scenario.vectorized-array-access";
	private static final boolean VECTORIZED = Boolean.getBoolean( VECTORIZED_ACCESS_KEY );

	@Override
	public long allocate() {
		final Integer[] array = new Integer[SIZE];
		//TODO RC: fill with .arraycopy
		fill( array, 1 );

		final long sum = sum( array );

		return sum;
	}


	private static long sum( final Number[] array ) {
		long sum = 0;
		if( VECTORIZED ) {
			for( final Number b : array ) {
				sum += b.longValue();
			}
		} else {
			//manual loop unrolling
			switch( array.length ) {
				default:
				case 16:
					sum += array[15].longValue();
				case 15:
					sum += array[14].longValue();
				case 14:
					sum += array[13].longValue();
				case 13:
					sum += array[12].longValue();
				case 12:
					sum += array[11].longValue();
				case 11:
					sum += array[10].longValue();
				case 10:
					sum += array[9].longValue();
				case 9:
					sum += array[8].longValue();
				case 8:
					sum += array[7].longValue();
				case 7:
					sum += array[6].longValue();
				case 6:
					sum += array[5].longValue();
				case 5:
					sum += array[4].longValue();
				case 4:
					sum += array[3].longValue();
				case 3:
					sum += array[2].longValue();
				case 2:
					sum += array[1].longValue();
				case 1:
					sum += array[0].longValue();
				case 0:
					break;
			}
		}
		return sum;
	}

	private static <T> void fill( final T[] array,
	                              final T value ) {
		if( VECTORIZED ) {
			for( int i = 0; i < array.length; i++ ) {
				array[i] = value;
			}
		} else {
			//manual loop unrolling
			switch( array.length ) {
				default:
				case 16:
					array[15] = value;
				case 15:
					array[14] = value;
				case 14:
					array[13] = value;
				case 13:
					array[12] = value;
				case 12:
					array[11] = value;
				case 11:
					array[10] = value;
				case 10:
					array[9] = value;
				case 9:
					array[8] = value;
				case 8:
					array[7] = value;
				case 7:
					array[6] = value;
				case 6:
					array[5] = value;
				case 5:
					array[4] = value;
				case 4:
					array[3] = value;
				case 3:
					array[2] = value;
				case 2:
					array[1] = value;
				case 1:
					array[0] = value;
				case 0:
					break;
			}
		}
	}

	@Override
	public String additionalInfo() {
		return VECTORIZED ? "vectorized" : "manually unrolled";
	}

	@ScenarioRunArgs
	public static List<ForkingMain.ScenarioRun> parametersToRunWith() {
		return Arrays.asList(
				runWith( SCENARIO_SIZE_KEY, "0", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "0", VECTORIZED_ACCESS_KEY, false ),

				runWith( SCENARIO_SIZE_KEY, "1", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "1", VECTORIZED_ACCESS_KEY, false ),

				runWith( SCENARIO_SIZE_KEY, "2", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "2", VECTORIZED_ACCESS_KEY, false ),


				runWith( SCENARIO_SIZE_KEY, "16", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "16", VECTORIZED_ACCESS_KEY, false ),

				runWith( SCENARIO_SIZE_KEY, "64", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "64", VECTORIZED_ACCESS_KEY, false ),

				runWith( SCENARIO_SIZE_KEY, "65", VECTORIZED_ACCESS_KEY, true ),
				runWith( SCENARIO_SIZE_KEY, "65", VECTORIZED_ACCESS_KEY, false )
		);
	}

}
