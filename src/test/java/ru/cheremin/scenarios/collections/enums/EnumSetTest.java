package ru.cheremin.scenarios.collections.enums;

import java.util.*;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import ru.cheremin.scalarization.junit.AllocationMatcher.Scenario;
import ru.cheremin.scalarization.scenarios.collections.enums.SampleEnums;
import ru.cheremin.scalarization.scenarios.collections.enums.SampleEnums.*;

import static org.junit.Assert.assertThat;
import static ru.cheremin.scalarization.junit.AllocationMatcher.allocatesNothing;

/**
 * @author ruslan
 *         created 07/08/16 at 16:25
 */
@RunWith( Theories.class )
public class EnumSetTest {

	@DataPoints
	public static final Class<?>[] ENUM_CLASSES = {
			Enum1.class,
			Enum3.class,
			Enum16.class,
			Enum70.class
	};

	@Test
	public void enumSetAllOfIsScalarized() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final EnumSet<Enum3> set = EnumSet.allOf( Enum3.class );
						final boolean b = set.contains( SampleEnums.Enum3.SECOND );
						if( b ) {
							return 1;
						} else {
							return 4;
						}
					}
				},
				allocatesNothing()
		);
	}

	@Theory
	public <E extends Enum<E>> void enumSetAllOfIsScalarized( final Class<E> enumClass ) throws Exception {
		final E element = enumClass.getEnumConstants()[0];
		assertThat(
				"Enum: " + enumClass,
				new Scenario() {
					@Override
					public long run() {
						final EnumSet<E> set = EnumSet.allOf( enumClass );
						final boolean b = set.contains( element );
						if( b ) {
							return 1;
						} else {
							return 4;
						}
					}
				},
				allocatesNothing()
		);

	}

	@Theory
	public <E extends Enum<E>> void enumSetIteratorIsScalarized( final Class<E> enumClass ) throws Exception {
		final EnumSet<E> set = EnumSet.allOf( enumClass );
		assertThat(
				"Enum: " + enumClass,
				new Scenario() {
					@Override
					public long run() {
						long sum = 0;
						for( final E e : set ) {
							sum = e.ordinal();
						}
						return sum;
					}
				},
				allocatesNothing()
		);
	}
}
