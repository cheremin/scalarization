package ru.cheremin.scalarization.scenarios;

import static com.google.common.base.Preconditions.checkArgument;

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

	public static StringKeysGenerator randomKeysGenerator( final int universeSize ) {
		final String[] keys = generateStringArray( universeSize );
		return new StringKeysGenerator( keys );
	}

	public static class StringKeysGenerator {
		private final String[] keys;

		public StringKeysGenerator( final String[] keys ) {
			checkArgument( keys != null, "keys can't be null" );
			this.keys = keys;
		}

		private int index = 0;

		public String next() {
			index = ( index + 1 ) % keys.length;
			return keys[index];
		}
	}
}
