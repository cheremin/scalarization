package ru.cheremin.scalarization.scenarios;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.JvmArg;

/**
 * helpers for {@linkplain ru.cheremin.scalarization.infra.ScenarioRunArgs} methods
 *
 * @author ruslan
 *         created 21/03/16 at 20:48
 */
public class ScenarioRunsUtils {

	public static List<ScenarioRun> withoutSpecificParameters() {
		return Arrays.asList(
				runWith( AllocationScenario.SCENARIO_SIZE_KEY, -1 )
		);
	}

	public static ScenarioRun runWith( final String propertyName,
	                                   final Object propertyValue ) {
		return new ScenarioRun(
				new JvmArg.SystemProperty( propertyName, propertyValue.toString() )
		);
	}

	public static ScenarioRun runWith( final String propertyName1,
	                                   final Object propertyValue1,
	                                   final String propertyName2,
	                                   final Object propertyValue2 ) {
		return new ScenarioRun(
				new JvmArg.SystemProperty( propertyName1, propertyValue1.toString() ),
				new JvmArg.SystemProperty( propertyName2, propertyValue2.toString() )
		);
	}

	protected static ScenarioRun runWith( final String propertyName1,
	                                      final Object propertyValue1,
	                                      final String propertyName2,
	                                      final Object propertyValue2,
	                                      final String propertyName3,
	                                      final Object propertyValue3 ) {
		return new ScenarioRun(
				new JvmArg.SystemProperty( propertyName1, propertyValue1.toString() ),
				new JvmArg.SystemProperty( propertyName2, propertyValue2.toString() ),
				new JvmArg.SystemProperty( propertyName3, propertyValue3.toString() )
		);
	}

	public static List<ScenarioRun> runForAll( final String propertyName,
	                                           final Object... propertyValues ) {
		return Lists.newArrayList(
				Lists.transform(
						allOf( propertyName, propertyValues ),
						new Function<JvmArg, ScenarioRun>() {
							@Override
							public ScenarioRun apply( final JvmArg jvmArg ) {
								return new ScenarioRun( jvmArg );
							}
						}
				)
		);
	}


	public static List<JvmArg> allOf(
			final String propertyName,
			final Object... values ) {
		final ArrayList<JvmArg> jvmArgs = new ArrayList<>();
		for( final Object value : values ) {
			jvmArgs.add( new JvmArg.SystemProperty( propertyName, String.valueOf( value ) ) );
		}
		return jvmArgs;
	}

	public static List<ScenarioRun> crossJoin(
			final List<JvmArg> jvmArgsList1,
			final List<JvmArg> jvmArgsList2 ) {
		final ArrayList<ScenarioRun> runs = new ArrayList<>();

		for( final JvmArg jvmArg1 : jvmArgsList1 ) {
			for( final JvmArg jvmArg2 : jvmArgsList2 ) {
				runs.add( new ScenarioRun( jvmArg1, jvmArg2 ) );
			}
		}

		return runs;
	}
}
