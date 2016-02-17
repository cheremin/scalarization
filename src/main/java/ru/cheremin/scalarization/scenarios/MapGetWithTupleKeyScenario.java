package ru.cheremin.scalarization.scenarios;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * With SimplestMap keys are successfully scalarized on 1.8.0_73, but not on 1.7. With
 * -XX:+PrintInlining 1.7 JVM state for SimplestMap.get() : "already compiled into a
 * big method", which <a href="https://groups.google.com/forum/#!topic/mechanical-sympathy/8ARGnMds7tU">means</a>
 * method .get() is already compiled in native code, but such code is too big for
 * inlining. Increasing -XX:InlineSmallCode 1000 -> 2000 solves the issue, and keys
 * are now scalarized even by 1.7 jvm. For 1.8 default value for InlineSmallCode=2000,
 * so it is clear why it scalarizes from the start. Setting this value back to 1000
 * prevents even 1.8 from scalarizing keys with same "already compiled into a big
 * method" diagnostics (but result is unstable, seems like 1.8 re-compiles method few
 * times with different results, and sometimes scalarization is resurrected).
 *
 * With THashMap.get() keys scalarized under both 1.7 and 1.8, even with default InlineSmallCode
 * size.
 *
 * With ImmutableMap.get() keys are scalarized under 1.7 only with InlineSmallCode=2000,
 * with 1000 (default) keys are not scalarized ("already compiled into a big method")
 * Under 1.8 it is scalarized with default settings (.get() method is listed as
 * inlined), even with InlineSmallCode=1000 it still inlines, and scanarization still
 * do happen.
 *
 * With HashMap.get() keys are scalarized under 1.8 with default settings, and are not
 * scalarized under 1.7 with default settings, but are scalarized with InlineSmallCode=2000
 *
 * Generally, both successful and unsuccessful lookups are scalarized/not scalarized.
 *
 * @author ruslan
 *         created 09/02/16 at 21:47
 */
public class MapGetWithTupleKeyScenario extends AllocationScenario {
	private static final double SUCCESSFUL_LOOKUPS_PROBABILITY = Double.valueOf(
			System.getProperty( "scenario.successful-lookups-probability", "0.5" )
	);

	private final String[] keys = Utils.generateStringArray( SIZE );

	private final HashMap<StringKey, String> map;

	{
		final HashMap<StringKey, String> map = new HashMap<>();
		for( int i = 0; i < SIZE; i++ ) {
			final String key1 = nextKey();
			final String key2 = nextKey();
			map.put( new StringKey( key1, key2 ), key1 + key2 );
		}
		this.map = new HashMap<>( map );
	}

	private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

	@Override
	public long allocate() {

		final boolean successful = ( rnd.nextDouble() <= SUCCESSFUL_LOOKUPS_PROBABILITY );
		final String key1;
		final String key2;
		if( successful ) {
			key1 = nextKey();
			key2 = nextKey();
		} else {
			key2 = nextKey();
			key1 = nextKey();
		}

		final StringKey combinedKey = new StringKey( key1, key2 );

		final String value = map.get( combinedKey );

		if( value == null ) {
			return key1.length();
		} else {
			return value.length();
		}
	}

	@Override
	public String additionalInfo() {
		return ( ( int ) ( SUCCESSFUL_LOOKUPS_PROBABILITY * 100 ) ) + "% lookups successful";
	}

	private int index = 0;

	public String nextKey() {
		index = ( index + 1 ) % keys.length;
		return keys[index];
	}

	public static class Key<T> {
		public T item1;
		public T item2;

		public Key( final T item1,
		            final T item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		@Override
		public boolean equals( final Object o ) {
			if( this == o ) {
				return true;
			}

			final Key<T> key = ( Key<T> ) o;

			return item1.equals( key.item1 )
					&& item2.equals( key.item2 );

		}

		@Override
		public int hashCode() {
			int result = item1 != null ? item1.hashCode() : 0;
			result = 31 * result + ( item2 != null ? item2.hashCode() : 0 );
			return result;
		}
	}

	public static class StringKey {
		public String item1;
		public String item2;

		public StringKey( final String item1,
		                  final String item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		@Override
		public boolean equals( final Object o ) {
			if( this == o ) {
				return true;
			}
			if( !( o instanceof StringKey ) ) {
				return false;
			}

			final StringKey key = ( StringKey ) o;

			return item1.equals( key.item1 )
					&& item2.equals( key.item2 );

		}

		@Override
		public int hashCode() {
			return item1.hashCode() * 31 + item2.hashCode();
		}
	}

	public static class SimplestMap<K, V> {
		private static final Object EMPTY = null;

		private Object[] keys;
		private Object[] values;

		public SimplestMap( final Map<K, V> origin ) {
			keys = new Object[origin.size() * 2 + 1];
			values = new Object[origin.size() * 2 + 1];

			for( final Map.Entry<K, V> entry : origin.entrySet() ) {
				put( entry.getKey(), entry.getValue() );
			}
		}


		public V get( final K key ) {
			final int hash = key.hashCode();
			final int length = keys.length;
			for( int i = 0; i < length; i++ ) {
				final int index = ( hash + i ) % length;
				final Object candidateKey = keys[index];
				if( candidateKey != EMPTY
						&& candidateKey.equals( key ) ) {
					return ( V ) values[index];
				}
			}
			return null;
		}

		public Object put( final K key,
		                   final V value ) {
			final int hash = key.hashCode();
			final int length = keys.length;
			for( int i = 0; i < length; i++ ) {
				final int index = ( hash + i ) % length;
				final Object candidateKey = keys[index];
				if( candidateKey == EMPTY ) {
					keys[index] = key;
					values[index] = value;
					return null;
				} else if( candidateKey.equals( key ) ) {
					final Object oldValue = values[index];
					values[index] = value;
					return ( V ) oldValue;
				}
			}

			throw new IllegalStateException( "Overloaded" );
//			final Object[] newValues = new Object[length*2+1];
//			final Object[] newKeys = new Object[length*2+1];
//
		}
	}
}
