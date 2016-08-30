/*
 * Copyright (c) 2003, 2011, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package ru.cheremin.scalarization.lab.collections.enums;

import java.util.*;

import sun.misc.SharedSecrets;

/**
 */
abstract class EnumSetEx<E extends Enum<E>> extends AbstractSet<E>
		implements Cloneable {
	final Class<E> elementType;

	final Enum[] universe;

	EnumSetEx( Class<E> elementType, Enum[] universe ) {
		this.elementType = elementType;
		this.universe = universe;
	}

	public static <E extends Enum<E>> EnumSetEx<E> noneOf( Class<E> elementType ) {
		Enum[] universe = getUniverse( elementType );
		if( universe == null ) {
			throw new ClassCastException( elementType + " not an enum" );
		}

		if( universe.length <= 64 ) {
			return new RegularEnumSet<>( elementType, universe );
		} else {
			return new JumboEnumSet<>( elementType, universe );
		}
	}

	public static <E extends Enum<E>> EnumSetEx<E> allOf( Class<E> elementType ) {
		EnumSetEx<E> result = noneOf( elementType );
		result.addAll();
		return result;
	}

	abstract void addAll();

	public static <E extends Enum<E>> EnumSetEx<E> copyOf( EnumSetEx<E> s ) {
		return s.clone();
	}

	public static <E extends Enum<E>> EnumSetEx<E> copyOf( Collection<E> c ) {
		if( c instanceof EnumSetEx ) {
			return ( ( EnumSetEx<E> ) c ).clone();
		} else {
			if( c.isEmpty() ) {
				throw new IllegalArgumentException( "Collection is empty" );
			}
			Iterator<E> i = c.iterator();
			E first = i.next();
			EnumSetEx<E> result = EnumSetEx.of( first );
			while( i.hasNext() ) {
				result.add( i.next() );
			}
			return result;
		}
	}

	public static <E extends Enum<E>> EnumSetEx<E> complementOf( EnumSetEx<E> s ) {
		EnumSetEx<E> result = copyOf( s );
		result.complement();
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> of( E e ) {
		EnumSetEx<E> result = noneOf( e.getDeclaringClass() );
		result.add( e );
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> of( E e1, E e2 ) {
		EnumSetEx<E> result = noneOf( e1.getDeclaringClass() );
		result.add( e1 );
		result.add( e2 );
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> of( E e1, E e2, E e3 ) {
		EnumSetEx<E> result = noneOf( e1.getDeclaringClass() );
		result.add( e1 );
		result.add( e2 );
		result.add( e3 );
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> of( E e1, E e2, E e3, E e4 ) {
		EnumSetEx<E> result = noneOf( e1.getDeclaringClass() );
		result.add( e1 );
		result.add( e2 );
		result.add( e3 );
		result.add( e4 );
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> of( E e1, E e2, E e3, E e4,
	                                                   E e5 ) {
		EnumSetEx<E> result = noneOf( e1.getDeclaringClass() );
		result.add( e1 );
		result.add( e2 );
		result.add( e3 );
		result.add( e4 );
		result.add( e5 );
		return result;
	}

	@SafeVarargs
	public static <E extends Enum<E>> EnumSetEx<E> of( E first, E... rest ) {
		EnumSetEx<E> result = noneOf( first.getDeclaringClass() );
		result.add( first );
		for( E e : rest ) {
			result.add( e );
		}
		return result;
	}

	public static <E extends Enum<E>> EnumSetEx<E> range( E from, E to ) {
		if( from.compareTo( to ) > 0 ) {
			throw new IllegalArgumentException( from + " > " + to );
		}
		EnumSetEx<E> result = noneOf( from.getDeclaringClass() );
		result.addRange( from, to );
		return result;
	}

	abstract void addRange( E from, E to );

	public EnumSetEx<E> clone() {
		try {
			return ( EnumSetEx<E> ) super.clone();
		} catch( CloneNotSupportedException e ) {
			throw new AssertionError( e );
		}
	}

	abstract void complement();

	final void typeCheck( E e ) {
		Class eClass = e.getClass();
		if( eClass != elementType && eClass.getSuperclass() != elementType ) {
			throw new ClassCastException( eClass + " != " + elementType );
		}
	}

	private static <E extends Enum<E>> E[] getUniverse( Class<E> elementType ) {
		return SharedSecrets.getJavaLangAccess()
				.getEnumConstantsShared( elementType );
	}
}
