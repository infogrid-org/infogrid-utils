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

package org.infogrid.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A degenerate implementation of CachingMap that uses a memory-only HashMap.
 */
public class MCachingHashMap<K,V>
        extends
            HashMap<K,V>
        implements
            CachingMap<K,V>
{
    /**
     * Factory method.
     * 
     * @return the created MCachingHashMap
     */
    public static <K,V> MCachingHashMap<K, V> create()
    {
        return new MCachingHashMap<K,V>();
    }

    /**
     * Constructor.
     * 
     * @param delegate the Map whose mappings are to be placed in this map.
     * @return the created MCachingHashMap
     */
    public static <K,V> MCachingHashMap<K, V> create(
            HashMap<? extends K, ? extends V> delegate )
    {
        return new MCachingHashMap<K,V>( delegate );
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @return the created MCachingHashMap
     */
    public static <K,V> MCachingHashMap<K, V> create(
            int initialCapacity )
    {
        return new MCachingHashMap<K,V>( initialCapacity );
    }

    /**
     * Constructor.
     * 
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @param loadFactor the load factor
     * @return the created MCachingHashMap
     */
    public static <K,V> MCachingHashMap<K, V> create(
            int   initialCapacity,
            float loadFactor )
    {
        return new MCachingHashMap<K,V>( initialCapacity, loadFactor );
    }

    /**
     * Constructor.
     */
    protected MCachingHashMap()
    {
        super();
    }

    /**
     * Constructor.
     *
     * @param delegate the Map whose mappings are to be placed in this map.
     */
    protected MCachingHashMap(
            Map<? extends K, ?extends V> delegate )
    {
        super( delegate );
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity of the CachingHashMap
     */
    protected MCachingHashMap(
            int initialCapacity )
    {
        super( initialCapacity );
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial capacity of the CachingHashMap
     * @param loadFactor the load factor
     */
    protected MCachingHashMap(
            int   initialCapacity,
            float loadFactor )
    {
        super( initialCapacity, loadFactor );
    }

    /**
     * Add a value.
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the HashMap previously associated
     *	       <tt>null</tt> with the specified key.
     */
    @SuppressWarnings(value={"unchecked"})
    @Override
    public V put(
            K key,
            V value )
    {
        V ret = super.put( key, value );

        if( ret != null ) {
            theListeners.fireEvent( new CachingMapEvent.Removed( this, key ), 1 );
        }
        theListeners.fireEvent( new CachingMapEvent.Added( this, key, value ), 0 );
        
        return ret;
    }
    
    /**
     * Remove a value.
     *
     * @param  key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or <tt>null</tt>
     *	       if there was no mapping for key.  A <tt>null</tt> return can
     *	       also indicate that the map previously associated <tt>null</tt>
     *	       with the specified key.
     */
    @SuppressWarnings(value={"unchecked"})
    @Override
    public V remove(
            Object key )
    {
        V ret = super.remove( key );
        
        if( ret != null ) {
            theListeners.fireEvent( new CachingMapEvent.Removed( this, key ), 1 );
        }
        return ret;
    }


    /**
     * Clear the local cache.
     */
    public void clearLocalCache()
    {
        // do nothing, we have memory only
    }

    /**
     * Determine whether this CachingMap is persistent.
     *
     * @return true if it is persistent
     */
    public boolean isPersistent()
    {
        return false;
    }

    /**
     * Obtain a CursorIterator on the keys of this Map.
     *
     * @return the CursorIterator
     */
    public CursorIterator<K> keysIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        CursorIterator<K> ret = MapCursorIterator.<K,V>createForKeys( this, keyArrayComponentType, valueArrayComponentType );
        return ret;
    }

    /**
     * Obtain a CursorIterator on the values of this Map.
     *
     * @return the CursorIterator
     */
    public CursorIterator<V> valuesIterator(
            Class<K> keyArrayComponentType,
            Class<V> valueArrayComponentType )
    {
        CursorIterator<V> ret = MapCursorIterator.<K,V>createForValues( this, keyArrayComponentType, valueArrayComponentType );
        return ret;
    }

    /**
     * Invoked only by objects held in this CachingMap, this enables
     * the held objects to indicate to the CachingMap that they have been updated.
     * Depending on the implementation of the CachingMap, that may cause the
     * CachingMap to write changes to disk, for example.
     *
     * @param key the key
     * @param value the value
     */
    public void valueUpdated(
            K key,
            V value )
    {
        // no op
    }

    /**
      * Add a listener.
      *
      * @param newListener the to-be-added listener
      * @see #removeCachingMapListener
      */
    public void addDirectCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addDirect( newListener );
    }

    /**
     * Remove a listener.
     * 
     * @param oldListener the to-be-removed listener
     * @see #addCachingMapListener
     */
    public void removeCachingMapListener(
            CachingMapListener oldListener )
    {
        theListeners.remove( oldListener );
    }

    /**
      * The listeners (if any).
      */
    private FlexibleListenerSet<CachingMapListener, CachingMapEvent, Integer> theListeners
            = new FlexibleListenerSet<CachingMapListener,CachingMapEvent,Integer>() {
                    protected void fireEventToListener(
                            CachingMapListener l,
                            CachingMapEvent    e,
                            Integer              p )
                    {
                        switch( p.intValue() ) {
                            case 0:
                                l.swappingHashMapElementAdded( (CachingMapEvent.Added) e );
                                break;

                            case 1:
                                l.swappingHashMapElementRemoved( (CachingMapEvent.Removed) e );
                                break;
                                
                            // there is no Expired case, this map never expires anything

                        }
                    }
    };
}

