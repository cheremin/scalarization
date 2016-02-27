package ru.cheremin.scalarization.infra;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author ruslan
 *         created 27/02/16 at 12:58
 */
@Immutable
public abstract class JvmArg {

	protected final String name;
	protected final Kind kind;

	protected JvmArg( final Kind kind,
	                  final String name ) {
		checkArgument( isNotEmpty( name ), "name can't be null nor empty" );
		checkArgument( kind != null, "kind can't be null" );
		this.kind = kind;
		this.name = name;
	}

	public String name() {
		return name;
	}

	public Kind kind() {
		return kind;
	}

	public abstract boolean hasValue();

	public abstract String asCommandLineString();

	@Override
	public String toString() {
		return asCommandLineString();
	}

	private static final Pattern XX_OPTIONS_PATTERN = Pattern.compile( "-XX:([+-])?([\\w\\d]+)(?:=(.+))?" );

	/** There are more, but I do not care of them right now */
	private static final String[] JVM_PROPERTIES_NAMES = new String[] {
			"-Xmx", "-Xms", "-Xss", "-Xbatch",

			"-server", "-client", "-d64",
			"-ea", "-da",

			"-agentlib:", "-agentpath:", "-javaagent:"
	};


	public static JvmArg parse( final String jvmArg ) {
		if( jvmArg.startsWith( "-D" ) ) {
			final int equalsIndex = jvmArg.indexOf( '=' );
			final String name = jvmArg.substring( "-D".length(), equalsIndex );
			final String value = jvmArg.substring( equalsIndex + 1 );
			return new SystemProperty( name, value );
		} else if( jvmArg.startsWith( "-XX:" ) ) {
			final Matcher m = XX_OPTIONS_PATTERN.matcher( jvmArg );

			if( !m.matches() ) {
				throw new IllegalArgumentException( "Can't parse [" + jvmArg + "] as -XX:... jvm option" );
			}
			final String flagSign = m.group( 1 );
			final String name = m.group( 2 );
			final String value = m.group( 3 );

			if( flagSign != null ) {
				return new JvmExtendedFlag( name, flagSign.equals( "+" ) );
			} else {
				return new JvmExtendedProperty( name, value );
			}

		}

		for( final String name : JVM_PROPERTIES_NAMES ) {
			if( jvmArg.startsWith( name ) ) {
				final String value = jvmArg.substring( name.length() );
				return new JvmProperty( name, value );
			}
		}
		return null;
	}

	@Immutable
	public static class SystemProperty extends JvmArg {
		private final String value;

		public SystemProperty( final String name,
		                       final String value ) {
			super( Kind.SYSTEM_PROPERTIES, name );

			checkArgument( value != null, "value can't be null" );
			this.value = value;
		}

		@Override
		public boolean hasValue() {
			return true;
		}


		@Override
		public String asCommandLineString() {
			if( isEmpty( value ) ) {
				return "-D" + name() + "=\"\"";
			}
			return "-D" + name() + "=" + value;
		}
	}

	@Immutable
	public static class JvmProperty extends JvmArg {
		private final String value;

		public JvmProperty( final String name,
		                    final String value ) {
			super( Kind.JVM_PROPERTY, name );

			checkArgument( value != null, "value can't be null" );
			this.value = value;
		}

		@Override
		public boolean hasValue() {
			return isNotEmpty( value );
		}

		@Override
		public String asCommandLineString() {
			return name() + value;
		}
	}

	@Immutable
	public static class JvmExtendedFlag extends JvmArg {
		private final boolean value;

		public JvmExtendedFlag( final String name,
		                        final boolean value ) {
			super( Kind.JVM_EXTENDED_FLAG, name );

			this.value = value;
		}

		@Override
		public boolean hasValue() {
			return false;
		}

		@Override
		public String asCommandLineString() {
			if( value ) {
				return "-XX:+" + name();
			} else {
				return "-XX:-" + name();
			}
		}
	}

	@Immutable
	public static class JvmExtendedProperty extends JvmArg {
		private final String value;

		public JvmExtendedProperty( final String name,
		                            final String value ) {
			super( Kind.JVM_EXTENDED_PROPERTY, name );

			checkArgument( isNotEmpty( value ), "value can't be null nor empty" );
			this.value = value;
		}

		@Override
		public boolean hasValue() {
			return true;
		}

		@Override
		public String asCommandLineString() {
			return "-XX:" + name() + "=" + value;
		}
	}


	public enum Kind {
		/** -Dproperty=value */
		SYSTEM_PROPERTIES,
		/** -Xproperty[value] */
		JVM_PROPERTY,
		/** -XX:[+/-]property */
		JVM_EXTENDED_FLAG,
		/** -XX:property[=value] */
		JVM_EXTENDED_PROPERTY
	}
}
