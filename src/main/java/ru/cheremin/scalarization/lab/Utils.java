package ru.cheremin.scalarization.lab;

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

	public static Pool<String> randomStringsPool( final int universeSize ) {
		final String[] strings = generateStringArray( universeSize );
		return new Pool<>( strings );
	}

	public static class Pool<T> {
		private final T[] items;

		public Pool( final T[] items ) {
			checkArgument( items != null, "items can't be null" );
			checkArgument( items.length > 0, "items can't be empty" );
			this.items = items;
		}

		private int index = 0;

		public T next() {
			index = ( index + 1 ) % items.length;
			return items[index];
		}
	}

	public static class BytecodePadder {
		private long value;

		public void pad() {
			//code is taken from Blackhole with small mods. The idea is similar, but
			//  not the same: I also want to avoid DCE to eliminate my code, so I
			//  need "JIT-unpredictable" side-effect. But I do not need to spent CPU
			//  cycles, I just need to insert some amount of bytecodes, which can't
			//  be removed
			value = ( value * 0x5DEECE66DL + 0xBL ) & ( 0xFFFFFFFFFFFFL );
		}

		public long value() {
			return value;
		}
	}

}
