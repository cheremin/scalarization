package ru.cheremin.scalarization.scenarios;

/**
 * String2Key allocations is most likely eliminated successfully. {@linkplain #getHashCode(String2Key)}
 * may be public or private.
 * <p/>
 * TODO: How it depends on StringKey size? Make String64Key?
 * TODO: Why keys not scalarized in {@linkplain MapGetWithTupleKeyScenario}?
 *
 * @author ruslan
 *         created 09/02/16 at 23:51
 */
public class GetHashCodeScenario extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );


	@Override
	public long allocate() {
		final String key1 = nextKey();
		final String key2 = nextKey();

		final String2Key key = new String2Key( key1, key2 );

		return getHashCode( key );
	}

	private int index = 0;

	public String nextKey() {
		index = ( index + 1 ) % keys.length;
		return keys[index];
	}

	private int getHashCode( final String2Key key ) {
		return key.hashCode();
	}

	public static class String2Key {
		public String item1;
		public String item2;

		public String2Key( final String item1,
		                   final String item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		@Override
		public boolean equals( final Object o ) {
			if( this == o ) {
				return true;
			}
			if( o == null || o.getClass() != getClass() ) {
				return false;
			}

			final String2Key key = ( String2Key ) o;

			return item1.equals( key.item1 )
					&& item2.equals( key.item2 );

		}

		@Override
		public int hashCode() {
			return item1.hashCode() * 31 + item2.hashCode();
		}
	}
}
