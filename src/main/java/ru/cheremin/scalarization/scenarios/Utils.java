package ru.cheremin.scalarization.scenarios;

/**
 * @author ruslan
 *         created 10/02/16 at 15:15
 */
public class Utils {
	public static String[] generateStringArray( final int size ) {
		final String[] keys = new String[size];
		for( int i = 0; i < size; i++ ) {
			keys[i] = String.valueOf( i );
		}
		return keys;
	}
}
