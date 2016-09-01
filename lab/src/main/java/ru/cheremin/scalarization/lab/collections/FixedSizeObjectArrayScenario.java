package ru.cheremin.scalarization.lab.collections;

import java.util.*;

import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * Check if array of fixed size (size known to JIT) is scalarized with different
 * read/write operations. I've tried 2 kind of read/write access:
 * 1. "vectorized" -- loop-based, with variable indexes
 * 2. "unrolled" -- with constant indexes
 * <p/>
 * There is much difference between this 2 options. With vectorized access (variable
 * indexes) array instantiation is scalarized only for size=[0,1] (for both, 1.7.0_80
 * and 1.8.0_77). Arrays of size > 1 are never seen scalarized with vectorized access.
 * <p/>
 * In contrast, with constant-index access ("unrolled loop") array is scalarized up
 * to size=64 (inclusive).
 * <p/>
 * It is important to note: for large arrays it is hard to write constant-index access
 * code which will touch _all_ arrays cells, because such code will be big, and it will
 * breach inlining limits, but without inlining scalarization is dead. So I cheat: I
 * access all cells only for short arrays (<10), and for longer arrays I access only
 * 9 first cells + the last one, leaving cells in between untouched.
 * <p/>
 * <p/>
 * TODO RC: with full 64b pointers (i.e. without compressed oops) scalarization must
 * be up to TrackedInitializationLimit=50 only, but I see it up to 64...
 * <p/>
 * TODO RC: try fill with .arraycopy (seems like .arraycopy specifically annotated for
 * EA?)
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class FixedSizeObjectArrayScenario extends AllocationScenario {
	private static final String VECTORIZED_ACCESS_KEY = "scenario.vectorized-array-access";
	private static final boolean VECTORIZED = Boolean.getBoolean( VECTORIZED_ACCESS_KEY );

	private static final Integer ONE = 1;

	@Override
	public long run() {
		final Integer[] array = new Integer[SIZE];

		writeToArray( array, ONE );

		final long sum = readArray( array );

		return sum;
	}


	private static long readArray( final Integer[] array ) {
		long sum = 0;
		if( VECTORIZED ) {
			for( final Integer n : array ) {
				sum += n.intValue();
			}
		} else {
			//manual loop unrolling. Do not go too far, because inlining limits on
			// method size will catch you! So only limited number of cells are accessed
			switch( array.length ) {
				default:
					sum += array[array.length - 1];
				case 9:
					sum += array[8].intValue();
				case 8:
					sum += array[7].intValue();
				case 7:
					sum += array[6].intValue();
				case 6:
					sum += array[5].intValue();
				case 5:
					sum += array[4].intValue();
				case 4:
					sum += array[3].intValue();
				case 3:
					sum += array[2].intValue();
				case 2:
					sum += array[1].intValue();
				case 1:
					sum += array[0].intValue();
				case 0:
					break;
			}
		}
		return sum;
	}

	private static void writeToArray( final Integer[] array,
	                                  final Integer value ) {
		if( VECTORIZED ) {
			for( int i = 0; i < array.length; i++ ) {
				array[i] = value;
			}
		} else {
			//manual loop unrolling. Do not go too far, because inlining limits on
			// method size will catch you! So only limited number of cells are accessed
			switch( array.length ) {
				default:
					array[array.length - 1] = value;
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
				allOf( VECTORIZED_ACCESS_KEY, true, false ),
				allOf( SIZE_KEY, 0, 1, 2, 4, 8, /*50, 51, */64, 65 )

//				Arrays.asList( /* does scalarization depend on size of array slot? */
//						new JvmArg.JvmExtendedFlag( "UseCompressedOops", true ),
//						new JvmArg.JvmExtendedFlag( "UseCompressedOops", false )
//				)
		);
	}

}
