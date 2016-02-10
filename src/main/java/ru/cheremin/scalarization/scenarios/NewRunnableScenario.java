package ru.cheremin.scalarization.scenarios;

import java.util.concurrent.Callable;

/**
 * @author ruslan
 *         created 10/02/16 at 15:11
 */
public class NewRunnableScenario extends AllocationScenario {
	private final String[] names = Utils.generateStringArray( SIZE );

	@Override
	public long allocate() {
		final ImplementsCallable object = new ImplementsCallable( nextName() );
		object.call();
		return object.name.length();
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

	private int index = 0;

	public String nextName() {
		index = ( index + 1 ) % names.length;
		return names[index];
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

	public static class OverridesFinalize {
		public final String name;

		public OverridesFinalize( final String name ) {
			this.name = name;
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			//do nothing
		}
	}
}
