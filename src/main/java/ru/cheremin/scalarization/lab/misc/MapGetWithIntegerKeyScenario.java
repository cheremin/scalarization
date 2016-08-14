package ru.cheremin.scalarization.lab.misc;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.ImmutableMap;
import gnu.trove.map.hash.THashMap;
import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * @author ruslan
 *         created 09/02/16 at 21:47
 */
public class MapGetWithIntegerKeyScenario extends AllocationScenario {
	public static final String SUCCESSFUL_LOOKUPS_PROBABILITY_KEY = "scenario.successful-lookups-probability";

	public static final String MAP_TYPE_KEY = "scenario.map-type";


	private static final double SUCCESSFUL_LOOKUPS_PROBABILITY = Double.valueOf(
			System.getProperty( SUCCESSFUL_LOOKUPS_PROBABILITY_KEY, "0.5" )
	);
	private static final MapType MAP_TYPE = MapType.valueOf(
			System.getProperty( MAP_TYPE_KEY, MapType.HASH_MAP.name() )
	);


	private final Map<Integer, Integer> map;

	{
		final HashMap<Integer, Integer> map = new HashMap<>();
		for( int i = 0; i < SIZE; i++ ) {
			map.put( keyOf( i ), i );
		}

		switch( MAP_TYPE ) {
			case HASH_MAP: {
				this.map = new HashMap<>( map );
				break;
			}
			case THASH_MAP: {
				this.map = new THashMap<>( map );
				break;
			}
			case GUAVA_IMMUTABLE_MAP: {
				this.map = ImmutableMap.copyOf( map );
				break;
			}
			default: {
				throw new AssertionError( "Code bug: " + MAP_TYPE + " is unknown" );
			}
		}
	}

	private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

	private int index = 0;


	@Override
	public long run() {
		final Integer key = generateKey();

		final Integer value = map.get( key );

		if( value == null ) {
			return key;
		} else {
			return value;
		}
	}

	private Integer generateKey() {
		index = ( index + 1 ) % SIZE;

		final boolean successful = ( rnd.nextDouble() <= SUCCESSFUL_LOOKUPS_PROBABILITY );
		final int keyNo = successful ? index : index + rnd.nextInt( 1024 );

		return keyOf( keyNo );
	}

	private Integer keyOf( final int keyNo ) {
		return Integer.valueOf( keyNo + 126 );
	}

	@Override
	public String additionalInfo() {
		return MAP_TYPE + ": " + ( ( int ) ( SUCCESSFUL_LOOKUPS_PROBABILITY * 100 ) ) + "% lookups successful";
	}

	public enum MapType {
		HASH_MAP,
		THASH_MAP,
		GUAVA_IMMUTABLE_MAP
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(

				allOf( MAP_TYPE_KEY, ( Object[] ) MapType.values() ),

				allOf( SIZE_KEY, /*  0, */ 1, 16, 65 ),

				allOf( SUCCESSFUL_LOOKUPS_PROBABILITY_KEY, 0.0, 0.5, 1.0 )
//
//				asList(
//						new JvmExtendedProperty( "InlineSmallCode", "1000" ),
//						new JvmExtendedProperty( "InlineSmallCode", "2000" )
//				)
		);
	}
}
