package ru.cheremin.scalarization.scenarios.collections;

import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Both 1.8.0_73 and 1.7.0_25 JVMs:
 * <p/>
 * With explicit cell access, like array[4], array is scalarized if length <= 64
 * With vectorized (looping) access array is NOT scalarized with length >1 (and IS
 * scalarized for length = 1).
 * Manual loop unrolling with switch still allow to scalarize array
 * <p/>
 * <p/>
 * TODO RC: random index array access
 * TODO RC: random array length
 *
 * TODO long/double array (possible different upper limit)
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class FixedSizePrimitiveArrayAllocator extends AllocationScenario {
	private static final String VECTORIZED_ACCESS_KEY = "scenario.vectorized-array-access";
	private static final boolean VECTORIZED = Boolean.getBoolean( VECTORIZED_ACCESS_KEY );

	@Override
	public long allocate() {
		final int[] array = new int[SIZE];

		fill( array, 42 );

		return sum( array );
	}

	private static long sum( final int[] array ) {
		long sum = 0;
		if( VECTORIZED ) {
			for( final int b : array ) {
				sum += b;
			}
		} else {
			//manual loop unrolling
			switch( array.length ) {
				default:
				case 16:
					sum += array[15];
				case 15:
					sum += array[14];
				case 14:
					sum += array[13];
				case 13:
					sum += array[12];
				case 12:
					sum += array[11];
				case 11:
					sum += array[10];
				case 10:
					sum += array[9];
				case 9:
					sum += array[8];
				case 8:
					sum += array[7];
				case 7:
					sum += array[6];
				case 6:
					sum += array[5];
				case 5:
					sum += array[4];
				case 4:
					sum += array[3];
				case 3:
					sum += array[2];
				case 2:
					sum += array[1];
				case 1:
					sum += array[0];
				case 0:
					break;
			}
		}
		return sum;
	}

	private static void fill( final int[] array,
	                          final int value ) {
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
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 0, 1, 2, /*4, 8, 16, */ 64, 65 ),
				allOf( VECTORIZED_ACCESS_KEY, true, false )
		);
	}
}
