package ru.cheremin.scenarios.collections.enums;

import java.util.*;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import ru.cheremin.scalarization.junit.AllocationMatcher.Scenario;
import ru.cheremin.scalarization.lab.collections.enums.SampleEnums.Enum1;
import ru.cheremin.scalarization.lab.collections.enums.SampleEnums.Enum16;
import ru.cheremin.scalarization.lab.collections.enums.SampleEnums.Enum3;

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
			Enum16.class
//			,Enum70.class
	};

	@Test
	public void enum1AllOfIsScalarizedInIsolation() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final EnumSet<Enum1> set = EnumSet.allOf( Enum1.class );
						final boolean b = set.contains( Enum1.ONLY );
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

	@Test
	public void enum3AllOfIsScalarizedInIsolation() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final EnumSet<Enum3> set = EnumSet.allOf( Enum3.class );
						final boolean b = set.contains( Enum3.SECOND );
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

	@Test
	public void enum16AllOfIsScalarizedInIsolation() throws Exception {
		assertThat(
				new Scenario() {
					@Override
					public long run() {
						final EnumSet<Enum16> set = EnumSet.allOf( Enum16.class );
						final boolean b = set.contains( Enum16._9 );
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
	public <E extends Enum<E>> void enumSetAllOfIsScalarizedInIsolation( final Class<E> enumClass ) throws Exception {
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
