package ru.cheremin.scalarization.scenarios;

/**
 * Both 1.8.0_73 and 1.7.0_25 JVMs:
 *
 * With explicit cell access, like array[4], array is scalarized if length <= 64
 * With vectorized (looping) access array is not scalarized with length >1 (and is
 * scalarized for length = 1).
 * Manual loop unrolling with switch still allow to scalarize array
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class PrimitiveArrayAllocator extends AllocationScenario {
	private static final boolean VECTORIZED = Boolean.getBoolean( "scenario.vectorized-array-access" );

	@Override
	public long allocate() {
		final byte[] array = new byte[SIZE];
		fill( array, ( byte ) 42 );


		return sum( array );
	}

	private static long sum( final byte[] array ) {
		long sum = 0;
		if( VECTORIZED ) {
			for( final byte b : array ) {
				sum += b;
			}
		} else {
			//manual loop unrolling
			switch( array.length ) {
				default:
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

	private static void fill( final byte[] array,
	                          final byte value ) {
		if( VECTORIZED ) {
			for( int i = 0; i < array.length; i++ ) {
				array[i] = value;
			}
		} else {
			//manual loop unrolling

			switch( array.length ) {
				default:
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
}
