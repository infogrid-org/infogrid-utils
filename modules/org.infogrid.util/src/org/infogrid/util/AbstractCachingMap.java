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

/**
 * Factors out common functionality for CachingMap implementations.
 */
public abstract class AbstractCachingMap<K,V>
        implements
            CachingMap<K,V>
{
    /**
      * Add a listener.
      *
      * @param newListener the to-be-added listener
      * @see #removeCachingMapListener
      */
    public final void addDirectCachingMapListener(
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
    public final void removeCachingMapListener(
            CachingMapListener oldListener )
    {
        theListeners.remove( oldListener );
    }

    /**
     * Fire a "an element has been added" event.
     *
     * @param key the key of the element that has been added
     * @param value the value that has been added
     */
    protected void fireValueAdded(
            K key,
            V value )
    {
        theListeners.fireEvent( new CachingMapEvent.Added<K,V>( this, key, value ), 0 );
    }

    /**
     * Fire a "an element has been removed" event.
     * This does not carry the removed value because it might be gone by this time.
     *
     * @param key the key of the element that has been removed
     */
    protected void fireValueRemoved(
            K key )
    {
        theListeners.fireEvent( new CachingMapEvent.Removed<K,V>( this, key ), 1 );
    }

    /**
     * Fire a "an element has been removed from the cache because it expired" event.
     * Compare: because it was removed by the client.
     *
     * @param key the key whose value has been removed
     */
    protected final void fireValueCleanedUp(
            K key )
    {
        theListeners.fireEvent( new CachingMapEvent.Expired<K,V>( this, key ), 2 );
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

                            case 2:
                                l.swappingHashMapElementExpired( (CachingMapEvent.Expired) e );
                                break;
                        }
                    }
    };    
}
