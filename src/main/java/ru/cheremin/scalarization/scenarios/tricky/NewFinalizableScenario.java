package ru.cheremin.scalarization.scenarios.tricky;

import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils;

import static ru.cheremin.scalarization.scenarios.Utils.randomStringsPool;

/**
 * Objects with non-default .finalize()-ers are treated conservatively, and
 * never scalarized with current EA approach.
 *
 * @author ruslan
 *         created 10/02/16 at 15:11
 */
public class NewFinalizableScenario extends AllocationScenario {
	private final Utils.StringsPool generator = randomStringsPool( SIZE );

	@Override
	public long run() {
		final String name = generator.next();
		final OverridesFinalize object = new OverridesFinalize( name );
		return object.name.length();
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
