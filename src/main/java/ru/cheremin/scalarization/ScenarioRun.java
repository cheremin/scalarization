package ru.cheremin.scalarization;

import java.util.*;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import ru.cheremin.scalarization.infra.JvmArg;
import ru.cheremin.scalarization.scenarios.AllocationScenario;

/**
 * List of additional jvm args to run specific scenario with.
 */
public class ScenarioRun {
	private final List<JvmArg> jvmArgs;

	public ScenarioRun( final List<JvmArg> jvmArgs ) {
		this.jvmArgs = Lists.newArrayList( jvmArgs );
	}

	public ScenarioRun( final JvmArg... jvmArgs ) {
		this.jvmArgs = Lists.newArrayList( jvmArgs );
	}

	public List<JvmArg> getJvmArgs() {
		return jvmArgs;
	}

	@Override
	public String toString() {
		return jvmArgs.toString();
	}

	/* ====================== factory methods ==================================== */

	//TODO RC: ScenarioRunBuilder?

	public static List<ScenarioRun> withoutSpecificParameters() {
		return Arrays.asList(
				runWith( AllocationScenario.SIZE_KEY, -1 )
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
			final List<? extends JvmArg> jvmArgsList1,
			final List<? extends JvmArg> jvmArgsList2 ) {
		final ArrayList<ScenarioRun> runs = new ArrayList<>();

		for( final JvmArg jvmArg1 : jvmArgsList1 ) {
			for( final JvmArg jvmArg2 : jvmArgsList2 ) {
				runs.add( new ScenarioRun( jvmArg1, jvmArg2 ) );
			}
		}

		return runs;
	}

	public static List<ScenarioRun> crossJoin(
			final List<? extends JvmArg> jvmArgsList1,
			final List<? extends JvmArg> jvmArgsList2,
			final List<? extends JvmArg> jvmArgsList3 ) {
		final ArrayList<ScenarioRun> runs = new ArrayList<>();

		for( final JvmArg jvmArg1 : jvmArgsList1 ) {
			for( final JvmArg jvmArg2 : jvmArgsList2 ) {
				for( final JvmArg jvmArg3 : jvmArgsList3 ) {
					runs.add( new ScenarioRun( jvmArg1, jvmArg2, jvmArg3 ) );
				}
			}
		}

		return runs;
	}

	public static List<ScenarioRun> crossJoin(
			final List<? extends JvmArg> jvmArgsList1,
			final List<? extends JvmArg> jvmArgsList2,
			final List<? extends JvmArg> jvmArgsList3,
			final List<? extends JvmArg> jvmArgsList4 ) {
		final ArrayList<ScenarioRun> runs = new ArrayList<>();

		for( final JvmArg jvmArg1 : jvmArgsList1 ) {
			for( final JvmArg jvmArg2 : jvmArgsList2 ) {
				for( final JvmArg jvmArg3 : jvmArgsList3 ) {
					for( final JvmArg jvmArg4 : jvmArgsList4 ) {
						runs.add( new ScenarioRun( jvmArg1, jvmArg2, jvmArg3, jvmArg4 ) );
					}
				}
			}
		}

		return runs;
	}


}
