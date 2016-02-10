package ru.cheremin.scalarization.scenarios;

/**

 * But for Object[]/int[] size <= 64! (see <a href="http://www.javaspecialists.eu/archive/Issue179.html">link</a>)
 * ...but only if all accesses are going to explicit cells, args[7].
 *
 * TODO : If access is going via looping -- array scalarized only for 1 element. Why?
 *
 * @author ruslan
 *         created 09/02/16 at 13:19
 */
public class VarargsAllocation extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );

	@Override
	public long allocate() {
//		final String[] args = new String[SIZE];
//		for( int i = 0; i < args.length; i++ ) {
//			args[i] = nextKey();
//		}
		final String[] args = new String[SIZE];
		args[0] = "1";
		args[5] = "1";
		final int result = varargMethod( "asdfasf", args );
//		final int result = varargMethod( "asdfasf", nextKey(), nextKey() );
//		final int result = varargMethod( new int[SIZE] );
		return result;
	}

	public static int varargMethod( final String format,
	                                final String... args ) {

		return args[0].length()
				+ args[5].length()
				- format.length();
	}

//	public static int varargMethod( final int... args ) {
//		int sum = 0;
//		for( final int arg : args ) {
//			sum += arg;
//		}
//		return args[0] + args[2] + + args[4] + args[16] + args.length;
//	}

	private int index = 0;

	public String nextKey() {
		index = ( index + 1 ) % keys.length;
		return keys[index];
	}
}
