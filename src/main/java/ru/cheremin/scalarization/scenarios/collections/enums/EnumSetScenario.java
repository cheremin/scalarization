package ru.cheremin.scalarization.scenarios.collections.enums;


import java.util.*;

import ru.cheremin.scalarization.ScenarioRun;
import ru.cheremin.scalarization.infra.ScenarioRunArgs;
import ru.cheremin.scalarization.scenarios.AllocationScenario;
import ru.cheremin.scalarization.scenarios.collections.enums.SampleEnums.*;

import static ru.cheremin.scalarization.ScenarioRun.allOf;
import static ru.cheremin.scalarization.ScenarioRun.crossJoin;

/**
 * EnumSet.allOf() is not scalarized. My first hypothesis was it is because of branch
 * between RegularEnumSet/JumboEnumSet in EnumSet.noneOf(...) factory method, which
 * leads to "merge point" issue is in play (see {@linkplain ru.cheremin.scalarization.scenarios.plain.ControlFlowScenario}.
 * <p/>
 * But I was incorrect: I've made my own EnumSetEx/RegularEnumSet/JumboEnumSet implementations
 * by simply copying originals, and, after a series of experiments, find out it is
 * EnumSetIterator which confuses scalarization. The story is the same as in {@linkplain ru.cheremin.scalarization.scenarios.collections.ArrayAsListScenario}:
 * EnumSetIterator is an inner class, and hold reference to parent EnumSet, and such a
 * reference cause a bug in EA, making it believe there are "merge points"
 * See <a href="https://bugs.openjdk.java.net/browse/JDK-8155769">JDK-8155769</a>
 * <p/>
 * The crucial difference with Arrays.asList() scenario is that here we can't just
 * re-implement iterator as static class, because of EnumSetIterator.remove() methods,
 * which needs access to parent class .elements field (I was forced to not implement
 * .remove() in my version)
 *
 * @author ruslan
 *         created 13.11.12 at 23:11
 */
public class EnumSetScenario extends AllocationScenario {

	private static final String USE_ENHANCED_ENUM_SET_KEY = "scenario.use-enhanced-enum-set";
	private static final boolean USE_ENHANCED_ENUM_SET = Boolean.getBoolean( USE_ENHANCED_ENUM_SET_KEY );


	@Override
	public long run() {
		if( USE_ENHANCED_ENUM_SET ) {
			switch( SIZE ) {
				case 1: {
					final EnumSetEx<Enum1> set = EnumSetEx.allOf( Enum1.class );
					return accessContent( set );
				}
				case 3: {
					final EnumSetEx<Enum3> set = EnumSetEx.allOf( Enum3.class );
					return accessContent( set );
				}
				case 16: {
					final EnumSetEx<Enum16> set = EnumSetEx.allOf( Enum16.class );
					return accessContent( set );
				}
				case 70: {
					final EnumSetEx<Enum70> set = EnumSetEx.allOf( Enum70.class );
					return accessContent( set );
				}
			}
			throw new IllegalStateException( "Unsupported size=" + SIZE );
		} else {
			switch( SIZE ) {
				case 1: {
					final EnumSet<Enum1> set = EnumSet.allOf( Enum1.class );
					return accessContent( set );
				}
				case 3: {
					final EnumSet<Enum3> set = EnumSet.allOf( Enum3.class );
					return accessContent( set );
				}
				case 16: {
					final EnumSet<Enum16> set = EnumSet.allOf( Enum16.class );
					return accessContent( set );
				}
				case 70: {
					final EnumSet<Enum70> set = EnumSet.allOf( Enum70.class );
					return accessContent( set );
				}
			}
			throw new IllegalStateException( "Unsupported size=" + SIZE );
		}
	}

	private static long accessContent( final EnumSetEx<?> set ) {
		return set.hashCode();
	}

	private static long accessContent( final EnumSet<?> set ) {
		return set.hashCode();
	}

	@Override
	public String additionalInfo() {
		return USE_ENHANCED_ENUM_SET ? "r.c.s.c.e.EnumSetEx" : "j.u.EnumSet";
	}

	@ScenarioRunArgs
	public static List<ScenarioRun> parametersToRunWith() {
		return crossJoin(
				allOf( SIZE_KEY, 1, 3, 16, 70 ),
				allOf( USE_ENHANCED_ENUM_SET_KEY, false, true )
		);
	}

}
