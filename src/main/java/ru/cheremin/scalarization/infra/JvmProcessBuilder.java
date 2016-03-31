package ru.cheremin.scalarization.infra;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.openjdk.jmh.util.Utils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Builder to set up JVM process. Allows incrementally add/overwrite all parameters
 * required. Although it is builder, it is also immutable, so each mutator returns
 * new instance.
 *
 * @author ruslan
 *         created 20/03/16 at 16:35
 */
@Immutable
public class JvmProcessBuilder {

	private final String classPath;
	private final String pathToJvmExecutable;
	private final Class<?> mainClass;
	private final ImmutableList<JvmArg> jvmArguments;

	private JvmProcessBuilder( final String classPath,
	                           final String pathToJvmExecutable,
	                           final List<JvmArg> jvmArguments,
	                           final Class<?> mainClass ) {
		this.classPath = classPath;
		this.pathToJvmExecutable = pathToJvmExecutable;
		this.mainClass = mainClass;
		checkArgument( jvmArguments != null, "jvmArguments can't be null" );
		this.jvmArguments = ImmutableList.copyOf( jvmArguments );
	}

	public JvmProcessBuilder withSystemProperties( final Properties systemProperties ) {
		return appendArgsOverriding( convertSystemProperties( systemProperties ) );
	}

	public JvmProcessBuilder appendArgOverriding( final JvmArg argToAppend ) {
		return appendArgsOverriding( ImmutableList.of( argToAppend ) );
	}

	public JvmProcessBuilder appendArgsOverriding( final Iterable<JvmArg> argsToAppend ) {
		final List<JvmArg> modifiedArgs = appendArgsOverriding( jvmArguments, argsToAppend );
		return new JvmProcessBuilder(
				classPath,
				pathToJvmExecutable,
				modifiedArgs,
				mainClass
		);
	}

	public JvmProcessBuilder removeArg( final Predicate<JvmArg> filter ) {
		final ArrayList<JvmArg> modifiedArgs = new ArrayList<>();
		for( final JvmArg jvmArg : jvmArguments ) {
			if( !filter.apply( jvmArg ) ) {
				modifiedArgs.add( jvmArg );
			}
		}
		return new JvmProcessBuilder(
				classPath,
				pathToJvmExecutable,
				modifiedArgs,
				mainClass
		);
	}

	public JvmProcessBuilder withClassPath( final String newClassPath ) {
		return new JvmProcessBuilder(
				newClassPath,
				pathToJvmExecutable,
				jvmArguments,
				mainClass
		);
	}

	public JvmProcessBuilder withPathToJvmExecutable( final String newPathToJvmExecutable ) {
		return new JvmProcessBuilder(
				classPath,
				newPathToJvmExecutable,
				jvmArguments,
				mainClass
		);
	}

	public JvmProcessBuilder withMainClass( final Class<?> newMainClass ) {
		return new JvmProcessBuilder(
				classPath,
				pathToJvmExecutable,
				jvmArguments,
				newMainClass
		);
	}

	public ImmutableList<String> buildJvmCommandLine() {
		final ImmutableList.Builder<String> jvmCmdLineBuilder = ImmutableList.<String>builder()
				.add( pathToJvmExecutable )
				.add( "-cp" )
				.add( classPath );

		for( final JvmArg jvmArg : jvmArguments ) {
			jvmCmdLineBuilder.add( jvmArg.asCommandLineString() );
		}

		final ImmutableList<String> jvmCommandLine = jvmCmdLineBuilder
				.add( mainClass.getName() )
				.build();
//		System.out.println( jvmCommandLine );
		return jvmCommandLine;
	}

	@Override
	public String toString() {
		return "JvmProcessBuilder[" + buildJvmCommandLine() + ']';
	}

	/* ============================== FACTORIES ================================== */
	public static JvmProcessBuilder copyCurrentJvm() {
		final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		final String classPath = runtimeMXBean.getClassPath();

		final List<JvmArg> currentJvmArgs = JvmProcessBuilder.parseJvmArgs( runtimeMXBean );

		final String jvmPath = Utils.getCurrentJvm();

		final JvmProcessBuilder jvmBuilder = new JvmProcessBuilder(
				classPath,
				jvmPath,
				currentJvmArgs,
				Void.class //RC: fake
		).withSystemProperties( System.getProperties() );
		return jvmBuilder;
	}

	/* ================================= INFRA =================================== */

	public static List<JvmArg> appendArgsOverriding(
			final List<JvmArg> sourceArgs,
			final Iterable<JvmArg> argsToAppend ) {
		final ArrayList<JvmArg> modifiableArgsList = Lists.newArrayList( sourceArgs );

		for( final JvmArg argToAppend : argsToAppend ) {
			final String argName = argToAppend.name();
			final JvmArg.Kind argKind = argToAppend.kind();

			for( final Iterator<JvmArg> i = modifiableArgsList.iterator(); i.hasNext(); ) {
				final JvmArg arg = i.next();

				if( argKind == arg.kind()
						&& argName.equals( arg.name() ) ) {
					i.remove();
					break;
				}
			}
			modifiableArgsList.add( argToAppend );
		}
		return modifiableArgsList;
	}


	public static List<JvmArg> parseJvmArgs( final RuntimeMXBean runtimeMXBean ) {
		final List<String> jvmArgsStrings = runtimeMXBean.getInputArguments();
		return Lists.newArrayList(
				Lists.transform(
						jvmArgsStrings,
						new Function<String, JvmArg>() {
							@Override
							public JvmArg apply( final String jvmArgString ) {
								final JvmArg jvmArg = JvmArg.parse( jvmArgString );
//								System.out.println( jvmArgString + " -> " + jvmArg );
								return jvmArg;
							}
						}
				)
		);
	}

	private static List<JvmArg> convertSystemProperties( final Properties systemProperties ) {
		final List<JvmArg> jvmArgs = Lists.newArrayList();
		for( final Map.Entry<Object, Object> entry : systemProperties.entrySet() ) {
			final String propertyName = ( String ) entry.getKey();
			final String propertyValue = ( String ) entry.getValue();
			jvmArgs.add( new JvmArg.SystemProperty( propertyName, propertyValue ) );
		}
		return jvmArgs;
	}


}
