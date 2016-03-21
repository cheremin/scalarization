package ru.cheremin.scalarization;

import java.util.*;

import com.google.common.collect.Lists;
import ru.cheremin.scalarization.infra.JvmArg;

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
}
