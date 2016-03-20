package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import ru.cheremin.scalarization.ForkingMain.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils;

/**
 * Looks like HashCodeBuilder stable scalarized by 1.7-1.8 with SIZE up to 128
 *
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class HashCodeBuilderScenario extends AllocationScenario {
	private final String[] keys = Utils.generateStringArray( SIZE );


	@Override
	public long allocate() {
		final HashCodeBuilder builder = new HashCodeBuilder();
		for( final String key : keys ) {
			builder.append( key );
		}
		return builder
				.append( true )
				.append( 'A' )
				.append( "Abc" )
				.append( 1.45f )
				.toHashCode();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return Arrays.asList(
				runWith( SCENARIO_SIZE_KEY, "0" ),
				runWith( SCENARIO_SIZE_KEY, "1" ),
				runWith( SCENARIO_SIZE_KEY, "2" ),
				runWith( SCENARIO_SIZE_KEY, "4" ),
				runWith( SCENARIO_SIZE_KEY, "16" ),
				runWith( SCENARIO_SIZE_KEY, "128" )
		);
	}

}
