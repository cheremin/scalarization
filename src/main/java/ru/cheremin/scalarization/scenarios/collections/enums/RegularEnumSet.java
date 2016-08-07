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

package ru.cheremin.scalarization.scenarios.collections.enums;

import java.util.*;

/**
 * Private implementation class for EnumSet, for "regular sized" enum types
 * (i.e., those with 64 or fewer enum constants).
 *
 * @author Josh Bloch
 * @serial exclude
 * @since 1.5
 */
class RegularEnumSet<E extends Enum<E>> extends EnumSetEx<E> {
	private static final long serialVersionUID = 3411599620347842686L;
	private long elements = 0L;

	RegularEnumSet( Class<E> elementType, Enum[] universe ) {
		super( elementType, universe );
	}

	void addRange( E from, E to ) {
		elements = ( -1L >>> ( from.ordinal() - to.ordinal() - 1 ) ) << from.ordinal();
	}

	void addAll() {
		if( universe.length != 0 ) {
			elements = -1L >>> -universe.length;
		}
	}

	void complement() {
		if( universe.length != 0 ) {
			elements = ~elements;
			elements &= -1L >>> -universe.length;  // Mask unused bits
		}
	}

	public Iterator<E> iterator() {
		return new EnumSetIterator<>( elements, ( E[] ) universe );
	}

	private static class EnumSetIterator<E extends Enum<E>> implements Iterator<E> {
		/**
		 * A bit vector representing the elements in the set not yet
		 * returned by this iterator.
		 */
		long unseen;

		/**
		 * The bit representing the last element returned by this iterator
		 * but not removed, or zero if no such element exists.
		 */
		long lastReturned = 0;

		final E[] universe;

		EnumSetIterator( final long elements,
		                 final E[] universe ) {
			unseen = elements;
			this.universe = universe;
		}

		public boolean hasNext() {
			return unseen != 0;
		}

		public E next() {
			if( unseen == 0 ) {
				throw new NoSuchElementException();
			}
			lastReturned = unseen & -unseen;
			unseen -= lastReturned;
			return ( E ) universe[Long.numberOfTrailingZeros( lastReturned )];
		}

		public void remove() {
//            if (lastReturned == 0)
//                throw new IllegalStateException();
//            elements &= ~lastReturned;
//            lastReturned = 0;
		}
	}

	public int size() {
		return Long.bitCount( elements );
	}

	public boolean isEmpty() {
		return elements == 0;
	}

	public boolean contains( Object e ) {
		if( e == null ) {
			return false;
		}
		Class eClass = e.getClass();
		if( eClass != elementType && eClass.getSuperclass() != elementType ) {
			return false;
		}

		return ( elements & ( 1L << ( ( Enum ) e ).ordinal() ) ) != 0;
	}

	public boolean add( E e ) {
		typeCheck( e );

		long oldElements = elements;
		elements |= ( 1L << ( ( Enum ) e ).ordinal() );
		return elements != oldElements;
	}

	public boolean remove( Object e ) {
		if( e == null ) {
			return false;
		}
		Class eClass = e.getClass();
		if( eClass != elementType && eClass.getSuperclass() != elementType ) {
			return false;
		}

		long oldElements = elements;
		elements &= ~( 1L << ( ( Enum ) e ).ordinal() );
		return elements != oldElements;
	}

	public boolean containsAll( Collection<?> c ) {
		if( !( c instanceof RegularEnumSet ) ) {
			return super.containsAll( c );
		}

		RegularEnumSet es = ( RegularEnumSet ) c;
		if( es.elementType != elementType ) {
			return es.isEmpty();
		}

		return ( es.elements & ~elements ) == 0;
	}

	public boolean addAll( Collection<? extends E> c ) {
		if( !( c instanceof RegularEnumSet ) ) {
			return super.addAll( c );
		}

		RegularEnumSet es = ( RegularEnumSet ) c;
		if( es.elementType != elementType ) {
			if( es.isEmpty() ) {
				return false;
			} else {
				throw new ClassCastException(
						es.elementType + " != " + elementType );
			}
		}

		long oldElements = elements;
		elements |= es.elements;
		return elements != oldElements;
	}

	public boolean removeAll( Collection<?> c ) {
		if( !( c instanceof RegularEnumSet ) ) {
			return super.removeAll( c );
		}

		RegularEnumSet es = ( RegularEnumSet ) c;
		if( es.elementType != elementType ) {
			return false;
		}

		long oldElements = elements;
		elements &= ~es.elements;
		return elements != oldElements;
	}

	public boolean retainAll( Collection<?> c ) {
		if( !( c instanceof RegularEnumSet ) ) {
			return super.retainAll( c );
		}

		RegularEnumSet<?> es = ( RegularEnumSet<?> ) c;
		if( es.elementType != elementType ) {
			boolean changed = ( elements != 0 );
			elements = 0;
			return changed;
		}

		long oldElements = elements;
		elements &= es.elements;
		return elements != oldElements;
	}

	public void clear() {
		elements = 0;
	}

	public boolean equals( Object o ) {
		if( !( o instanceof RegularEnumSet ) ) {
			return super.equals( o );
		}

		RegularEnumSet es = ( RegularEnumSet ) o;
		if( es.elementType != elementType )
			return elements == 0 && es.elements == 0;
		return es.elements == elements;
	}
}
