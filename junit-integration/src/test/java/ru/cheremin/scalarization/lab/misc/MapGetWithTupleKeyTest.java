package ru.cheremin.scalarization.lab.misc;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.THashMap;
import org.junit.Ignore;
import org.junit.Test;
import ru.cheremin.scalarization.Scenario;
import ru.cheremin.scalarization.lab.Utils;
import ru.cheremin.scalarization.lab.Utils.Pool;
import ru.cheremin.scalarization.lab.misc.MapGetWithTupleKeyScenario.SimplestMap;
import ru.cheremin.scalarization.lab.misc.MapGetWithTupleKeyScenario.StringKey;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.finallyAllocatesNothing;

/**
 * @author ruslan
 *         created 01/09/16 at 23:18
 */
public class MapGetWithTupleKeyTest {
	public static final int SIZE = 32;

	private static final Pool<String> KEYS = Utils.randomStringsPool( SIZE );

	//TODO RC: ensure keys are different from KEYS!!!
	private static final Pool<String> WRONG_KEYS = Utils.randomStringsPool( SIZE );

	private static final Map<StringKey, String> MAP = new HashMap<>( SIZE );

	static {
		for( int i = 0; i < SIZE; i++ ) {
			final String key = KEYS.next();
			final StringKey combinedKey = new StringKey( key, key );
//			System.out.println( combinedKey.hashCode() );
			MAP.put( combinedKey, key + key );
		}
	}

	private static final double SUCCESSFUL_LOOKUPS_PROBABILITY = 0.9;


	@Test
	@Ignore( "JEP-180 prevents HM.get() from inlining if TreeBin-s are used inside" )
	public void combinedKeyWithHashMapIsScalarized() throws Exception {
		//http://openjdk.java.net/jeps/180

		//RC: Why TreeBins are used? Because keys formed as (key,key) have bad hash
		//    distribution, and triggers treefying of bins...

		final HashMap<StringKey, String> hashMap = new HashMap<>( MAP );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final StringKey combinedKey = generateKey( SUCCESSFUL_LOOKUPS_PROBABILITY );
						final String value = hashMap.get( combinedKey );
						if( value == null ) {
							return combinedKey.item1.length();
						} else {
							return value.length();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void combinedKeyWithTHashMapIsScalarized() throws Exception {
		final THashMap<StringKey, String> troveMap = new THashMap<>( MAP );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final StringKey combinedKey = generateKey( SUCCESSFUL_LOOKUPS_PROBABILITY );
						final String value = troveMap.get( combinedKey );
						if( value == null ) {
							return combinedKey.item1.length();
						} else {
							return value.length();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void combinedKeyWithImmutableMapIsScalarized() throws Exception {
		final ImmutableMap<StringKey, String> guavaMap = ImmutableMap.copyOf( MAP );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final StringKey combinedKey = generateKey( SUCCESSFUL_LOOKUPS_PROBABILITY );
						final String value = guavaMap.get( combinedKey );
						if( value == null ) {
							return combinedKey.item1.length();
						} else {
							return value.length();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void combinedKeyWithConcurrentHashMapIsScalarized() throws Exception {
		final ConcurrentHashMap<StringKey, String> simplestMap = new ConcurrentHashMap<>( MAP );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final StringKey combinedKey = generateKey( SUCCESSFUL_LOOKUPS_PROBABILITY );
						final String value = simplestMap.get( combinedKey );
						if( value == null ) {
							return combinedKey.item1.length();
						} else {
							return value.length();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	@Test
	public void combinedKeyWithSimplestMapIsScalarized() throws Exception {
		final SimplestMap<StringKey, String> simplestMap = new SimplestMap<>( MAP );
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final StringKey combinedKey = generateKey( SUCCESSFUL_LOOKUPS_PROBABILITY );
						final String value = simplestMap.get( combinedKey );
						if( value == null ) {
							return combinedKey.item1.length();
						} else {
							return value.length();
						}
					}
				},
				finallyAllocatesNothing()
		);
	}

	private static StringKey generateKey( final double successfulLookupsProbability ) {
		final ThreadLocalRandom rnd = ThreadLocalRandom.current();
		final boolean successful = ( rnd.nextDouble() <= successfulLookupsProbability );
		final String key1;
		final String key2;
		if( successful ) {
			key2 = key1 = KEYS.next();
		} else {
			key1 = KEYS.next();
			key2 = WRONG_KEYS.next();
		}
		return new StringKey( key1, key2 );
	}
}