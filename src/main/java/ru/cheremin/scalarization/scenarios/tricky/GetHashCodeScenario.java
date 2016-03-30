package ru.cheremin.scalarization.scenarios.tricky;

import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.Utils;

import static ru.cheremin.scalarization.ScenarioRun.withoutSpecificParameters;

/**
 * .hashCode() is very like something "identity-like" (object-y), so I've tried to
 * check is calling .hashCode() prevents scalarization. Looks like it is not,
 * overwritten .hashCode() behaves as any other method in relate to scalarization:
 * String2Key allocations is most likely eliminated successfully.
 * (Even with {@linkplain #getHashCode(String2Key)} public or private).
 * <p/>
 * See {@linkplain ObjectyStaffScenario} for more objecty-tests like this
 * one. Maybe even remove this test, since it is duplicated there
 *
 *
 * TODO: How it depends on StringKey size? Make String64Key?
 *
 * @author ruslan
 *         created 09/02/16 at 23:51
 */
public class GetHashCodeScenario extends AllocationScenario {
	private final Utils.StringKeysGenerator keysGenerator = Utils.randomKeysGenerator( 1024 );


	@Override
	public long allocate() {
		final String key1 = keysGenerator.next();
		final String key2 = keysGenerator.next();

		final String2Key key = new String2Key( key1, key2 );

		return getHashCode( key );
	}

	private int getHashCode( final String2Key key ) {
		return key.hashCode();
	}


	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return withoutSpecificParameters();
	}

	public static class String2Key {
		public String item1;
		public String item2;

		public String2Key( final String item1,
		                   final String item2 ) {
			this.item1 = item1;
			this.item2 = item2;
		}

		@Override
		public boolean equals( final Object o ) {
			if( this == o ) {
				return true;
			}
			if( o == null || o.getClass() != getClass() ) {
				return false;
			}

			final String2Key key = ( String2Key ) o;

			return item1.equals( key.item1 )
					&& item2.equals( key.item2 );

		}

		@Override
		public int hashCode() {
			return item1.hashCode() * 31 + item2.hashCode();
		}
	}
}
