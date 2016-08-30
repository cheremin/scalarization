package ru.cheremin.scalarization.lab.plain;

import java.util.*;

import org.apache.commons.lang3.builder.CompareToBuilder;
import ru.cheremin.scalarization.AllocationScenario;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg.JvmExtendedProperty;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;

import static java.util.Arrays.asList;
import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;
import static ru.cheremin.scalarization.lab.Utils.generateStringArray;

/**
 * With using at least one .append(Object,Object) method (even without looping) -> EA
 * fails. -XX:+PrintInlining produces "callee is too large" and "hot method too big"
 * for CompareToBuilder.append(), so it looks like inlining threshold is breached. Default
 * FreqInlineSize is 325, which is less than 327 bc/append. Increasing -XX:FreqInlineSize
 * 325->500 indeed removes all allocations with 1.8 and 1.7 JVMs.
 * <p/>
 * In other words, issue is the same as in {@linkplain EqualsBuilderScenario}
 *
 * @author ruslan
 *         created 16/02/16 at 23:45
 */
public class CompareToBuilderScenario extends AllocationScenario {

	private final String[] keys = generateStringArray( SIZE );


	@Override
	public long run() {
		final CompareToBuilder builder = new CompareToBuilder();
		for( final String key : keys ) {
			builder.append( key, key );
		}
		return builder
				.append( true, true )
				.append( 1.1, 1.2 )
				.append( 1, 1 )
				.toComparison();
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 0, 1, 4, 128 ),

				asList(
						new JvmExtendedProperty( "FreqInlineSize", "325" ),
						new JvmExtendedProperty( "FreqInlineSize", "500" )
				)
		);
	}
}
