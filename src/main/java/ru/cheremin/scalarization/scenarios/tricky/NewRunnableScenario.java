package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;
import java.util.concurrent.Callable;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils.StringKeysGenerator;

import static ru.cheremin.scalarization.scenarios.Utils.randomKeysGenerator;

/**
 * In original paper (Choi99) there was a simplification which conservatively assumes
 * any Runnable instance as escaping. I've checked, and currently this is not true:
 * Runnable/Callable instances are scalarized as an ordinary objects.
 *
 * @author ruslan
 *         created 10/02/16 at 15:11
 */
public class NewRunnableScenario extends AllocationScenario {
	private final StringKeysGenerator generator = randomKeysGenerator( 1024 );

	@Override
	public long allocate() {
		final ImplementsRunnable object = new ImplementsRunnable(
				generator.next()
		);
		object.run();
		return object.name.length();
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return Arrays.asList(
				ScenarioRun.runWith( SIZE_KEY, -1 )
		);
	}


	public static class ImplementsRunnable implements Runnable {
		public final String name;

		public ImplementsRunnable( final String name ) {
			this.name = name;
		}

		@Override
		public void run() {
			//nothing
		}
	}

	public static class ImplementsCallable implements Callable<String> {
		public final String name;

		public ImplementsCallable( final String name ) {
			this.name = name;
		}

		@Override
		public String call() {
			return null;
		}
	}
}
