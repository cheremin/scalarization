package ru.cheremin.scalarization.scenarios.plain;

import java.util.*;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import ru.cheremin.scalarization.ForkingMain.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.SystemProperty;
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
				.toHashCode();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return Arrays.asList(
				new ScenarioRun( new SystemProperty( "scenario.size", "0" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "1" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "2" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "4" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "8" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "16" ) ),
				new ScenarioRun( new SystemProperty( "scenario.size", "32" ) )
		);
	}

}
