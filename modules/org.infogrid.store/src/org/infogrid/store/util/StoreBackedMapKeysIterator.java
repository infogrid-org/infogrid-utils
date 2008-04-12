//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.store.util;

import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.util.AbstractCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CursorIterator;

import java.util.NoSuchElementException;

/**
 * Iterator over all keys in an InterableStoreBackedMap.
 * 
 * @param K the type of key
 * @param V the type of value
 */
public class StoreBackedMapKeysIterator<K,V>
        extends
            AbstractCursorIterator<K>
{
    /**
     * Constructor.
     *
     * @param delegate the underling Iterator over the StoreValues in the Store
     * @param cache the in-memory cache in the StoreMeshBase
     * @param mapper the mapper to use
     * @param arrayComponentClass the Class to use when returning arrays
     */
    public StoreBackedMapKeysIterator(
            IterableStoreCursor   delegate,
            StoreBackedMap<K,V>   cache,
            StoreEntryMapper<K,V> mapper,
            Class<K>              arrayComponentClass )
    {
        super( arrayComponentClass );

        theDelegate            = delegate;
        theCache               = cache;
        theMapper              = mapper;
    }

    /**
     * Obtain the Map that this Iterator iterates over.
     *
     * @return the StoreBackedMap
     */
    public StoreBackedMap<K,V> getMap()
    {
        return theCache;
    }

    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public K peekNext()
    {
        StoreValue value = theDelegate.peekNext();
        K          ret   = theMapper.stringToKey( value.getKey() );

        return ret;
    }
    
    /**
     * Obtain the previous element, without iterating backwards.
     *
     * @return the previous element
     * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public K peekPrevious()
    {
        StoreValue value = theDelegate.peekNext();
        K          ret   = theMapper.stringToKey( value.getKey() );

        return ret;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the forward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the forward direction.
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    @Override
    public boolean hasNext()
    {
        return theDelegate.hasNext();
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements in the backward direction.
     *
     * @return <tt>true</tt> if the iterator has more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious(int)
     * @see #hasNext(int)
     */
    @Override
    public boolean hasPrevious()
    {
        return theDelegate.hasPrevious();
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the forward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the forward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     */
    public boolean hasNext(
            int n )
    {
        return theDelegate.hasNext( n );
    }

    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the backward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasNext(int)
     */
    public boolean hasPrevious(
            int n )
    {
        return theDelegate.hasPrevious( n );
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public K next()
    {
        StoreValue value = theDelegate.next();
        K          ret   = theMapper.stringToKey( value.getKey() );

        return ret;
    }

    /**
     * <p>Obtain the next N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * @return the next no more than N elements
     * @see #previous(int)
     */
    @Override
    public K [] next(
            int n )
    {
        StoreValue [] values  = theDelegate.next( n );
        K          [] ret     = ArrayHelper.createArray( theArrayComponentType, n );
        
        for( int i=0 ; i<values.length ; ++i ) {
            ret[i] = theMapper.stringToKey( values[i].getKey() );
        }
        
        return ret;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @see #next()
     */
    public K previous()
    {
        StoreValue value = theDelegate.previous();
        K          ret   = theMapper.stringToKey( value.getKey() );

        return ret;
    }

    /**
     * <p>Obtain the previous N elements. If fewer than N elements are available, return
     * as many elements are available in a shorter array.</p>
     * 
     * <p>Note that the elements
     * will be ordered in the opposite direction as you might expect: they are
     * returned in the sequence in which the CursorIterator visits them, not in the
     * sequence in which the underlying Iterable stores them.</p>
     *
     * @return the previous no more than N elements
     * @see #next(int)
     */
    @Override
    public K [] previous(
            int  n )
    {
        StoreValue [] values  = theDelegate.previous( n );
        K          [] ret     = ArrayHelper.createArray( theArrayComponentType, n );
        
        for( int i=0 ; i<values.length ; ++i ) {
            ret[i] = theMapper.stringToKey( values[i].getKey() );
        }
        
        return ret;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backwards movement.
     * Throws NoSuchElementException if the position does not exist.
     *
     * @param n the number of positions to move
     * @exception NoSuchElementException
     */
    @Override
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        theDelegate.moveBy( n );
    }

    /**
     * Move the cursor to just before this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    @Override
    public int moveToBefore(
            K pos )
        throws
            NoSuchElementException
    {
        String delegateKey = theMapper.keyToString( pos );
        return theDelegate.moveToBefore( delegateKey );
    }

    /**
     * Move the cursor to just after this element, i.e. return this element when {@link #previous previous} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    @Override
    public int moveToAfter(
            K pos )
        throws
            NoSuchElementException
    {
        String delegateKey = theMapper.keyToString( pos );
        return theDelegate.moveToAfter( delegateKey );
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation). This is the same as the current element.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @throws IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    @Override
    public void remove()
    {
        theDelegate.remove();
    }
    
    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public StoreBackedMapKeysIterator<K,V> createCopy()
    {
        return new StoreBackedMapKeysIterator<K,V>( theDelegate.createCopy(), theCache, theMapper, theArrayComponentType );
    }
    
    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
     *         or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<K> position )
        throws
            IllegalArgumentException
    {
        if( !( position instanceof StoreBackedMapKeysIterator )) {
            throw new IllegalArgumentException( "Wrong type of CursorIterator: " + position );
        }
        StoreBackedMapKeysIterator realPosition = (StoreBackedMapKeysIterator) position;

        if( theCache != realPosition.theCache ) {
            throw new IllegalArgumentException( "Not the same instance of cache" );
        }
        if( theMapper != realPosition.theMapper ) {
            throw new IllegalArgumentException( "Not the same instance of mapper" );
        }
        
        theDelegate.setPositionTo( realPosition.theDelegate ); // this may throw
    }

    /**
     * The underlying Iterator over the Store content.
     */
    protected IterableStoreCursor theDelegate;
    
    /**
     * The cache to use.
     */
    protected StoreBackedMap<K,V> theCache;

    /**
     * The mapper to use.
     */
    protected StoreEntryMapper<K,V> theMapper;
}
