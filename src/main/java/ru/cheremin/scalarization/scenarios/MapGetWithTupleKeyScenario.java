package ru.cheremin.scalarization.scenarios;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@linkplain StringKey} allocation is scalarized on 1.8.0_73 for Hash/THash/ImmutableMaps,
 * but not scalarized in 1.7
 * <p/>
 * Under 1.8 both successful and unsuccessful lookups are scalarized  for ImmutableMap,
 * TODO but mixed successful/unsuccessful lookups are not, event with 0.1% probability!!!
 * <p/>
 * <p/>
 * TODO why 1.7 is not working?
 * TODO try simples linear-probing lookup map for 1.7?
 *
 * @author ruslan
 *         created 09/02/16 at 21:47
 */
public class MapGetWithTupleKeyScenario extends AllocationScenario {
	private static final double SUCCESSFUL_LOOKUPS_PROBABILITY = Double.valueOf(
			System.getProperty( "scenario.successful-lookups-probability", "0.001" )
	);

	private final String[] keys = Utils.generateStringArray( SIZE );

	private final Map<StringKey, String> map;

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
		final String key1 = nextKey();
		final String key2 = nextKey();

		final boolean successful = ( rnd.nextDouble() <= SUCCESSFUL_LOOKUPS_PROBABILITY );
		final StringKey combinedKey;
		if( successful ) {
			combinedKey = new StringKey( key1, key2 );
		} else {
			combinedKey = new StringKey( key2, key1 );
		}

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
}
