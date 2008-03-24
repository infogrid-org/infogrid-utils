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

package org.infogrid.probe.manager;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;

import org.infogrid.util.CachingMap;
import org.infogrid.util.IsDeadException;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

import java.lang.ref.WeakReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A ProbeManager implementation that uses a ScheduledExecutorService to schedule
 * updates.
 */
public abstract class ScheduledExecutorProbeManager
        extends
            AbstractProbeManager
{
    private static final Log log = Log.getLogInstance( ScheduledExecutorProbeManager.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param delegate the underlying Factory
     * @param executorService the ScheduledExecutorService that schedules our tasks
     */
    protected ScheduledExecutorProbeManager(
            ShadowMeshBaseFactory                            delegate,
            CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegate, storage );

        theExecutorService = null; // must invoke start() to start
    }
    
    /**
     * Obtain the ScheduledExecutorService that is used by this ProbeManager.
     * 
     * @return the ScheduledExecutorService
     */
    public ScheduledExecutorService getScheduledExecutorService()
    {
        return theExecutorService;
    }
    
    /**
     * Start this ScheduledExecutorProbeManager.
     * 
     * @param exec the ScheduledExecutorService to use
     */
    public synchronized void start(
            ScheduledExecutorService exec )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".start( " + exec + " )" );
        }
        if( theExecutorService != null ) {
            throw new IllegalStateException( "Already started" );
        }
        theExecutorService = exec;

        // re-initialize
        Iterator<NetMeshBaseIdentifier> keyIter = theKeyValueMap.keysIterator( NetMeshBaseIdentifier.class, ShadowMeshBase.class );
        while( keyIter.hasNext() ) {
            NetMeshBaseIdentifier key   = keyIter.next();
            ShadowMeshBase        value = theKeyValueMap.get( key );
            
            long nextTime = value.getDelayUntilNextUpdate();
            if( nextTime >= 0 ) {  // allow 0 for immediate execution
                ScheduledFuture<Long> newFuture = theExecutorService.schedule(
                        new ExecutorAdapter( this, key, nextTime ),
                        nextTime,
                        TimeUnit.MILLISECONDS );
                theFutures.add( newFuture );
            }
        }
    }

    /**
     * Stop this ScheduledExecutorProbeManager.
     */
    public synchronized void stop()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".stop()" );
        }
        if( theExecutorService == null ) {
            throw new IllegalStateException( "Already stopped" );
        }

        for( ScheduledFuture<Long> current : theFutures ) {
            current.cancel( false );
        }
        theFutures.clear();
        
        theExecutorService = null;
    }

    /**
     * This overridable method allows our subclasses to invoke particular functionality
     * every time this SmartFactory created a new value by invoking the delegate Factory.
     * It is not invoked for those returned values that are merely retrieved from
     * the storage in the smart factory.
     * 
     * @param key the key of the newly created value
     * @param value the newly created value
     * @param argument the argument into the creation of the newly created value
     */
    @Override
    protected void createdHook(
            NetMeshBaseIdentifier  key,
            ShadowMeshBase         value,
            CoherenceSpecification argument )
    {
        long nextTime = value.getDelayUntilNextUpdate();
        if( nextTime >= 0 ) { // allow 0 for immediate execution
            ScheduledFuture<Long> newFuture = theExecutorService.schedule(
                    new ExecutorAdapter( this, key, nextTime ),
                    nextTime,
                    TimeUnit.MILLISECONDS );
            theFutures.add( newFuture );
        }
    }

    /**
     * We are not needed any more.
     * 
     * @param isPermanent if true, this MeshBase will go away permanently; if false, it may come alive again some time later
     */
    public synchronized void die(
            boolean isPermanent )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".die()" );
        }
        if( theExecutorService != null ) {
            stop(); // cleans up nicely
        }
    }

    /**
     * The ScheduledExecutorService that executes our Probe runs.
     */
    protected ScheduledExecutorService theExecutorService;
    
    /**
     * The Futures currently waiting to be executed on behalf of this ScheduledExecutorProbeManager.
     */
    protected ArrayList<ScheduledFuture<Long>> theFutures = new ArrayList<ScheduledFuture<Long>>();

    /**
     * The default thread-pool size.
     */
    protected static int DEFAULT_THREAD_POOL_SIZE = 1;
    
    /**
     * Helper class to be able to reschedule the ShadowMeshBase. This is a static class, so the ProbeManager
     * can be garbage-collected, even while some ScheduledExecutorService still thinks it has a future call to make.
     */
    protected static class ExecutorAdapter
            implements
                Callable<Long>
    {
        /**
         * Constructor.
         *
         * @param shadow the ShadowMeshBase to call
         */
        public ExecutorAdapter(
                ScheduledExecutorProbeManager belongsTo,
                NetMeshBaseIdentifier         shadowIdentifier,
                long                          nextTime )
        {
            theBelongsTo        = new WeakReference<ScheduledExecutorProbeManager>( belongsTo );
            theShadowIdentifier = shadowIdentifier;
            theWillBeCalledAt   = new Date( System.currentTimeMillis() + nextTime );

            if( log.isDebugEnabled() ) {
                log.debug( "Created " + this );
            }
        }

        /**
         * The main call when invoked by a background Thread.
         */
        public Long call()
            throws
                Exception
        {
            return call( null );
        }

        /**
         * The main call when invoked on the thread of the application programmer.
         * 
         * @param coherence optional CoherenceSpecification
         */
        public Long call(
                CoherenceSpecification coherence )
            throws
                Exception
        {
            if( log.isDebugEnabled() ) {
                log.debug( this + ".call( " + coherence + " )" );
            }
            
            ScheduledExecutorProbeManager belongsTo = theBelongsTo.get();
            try {
                Long nextTime= -1L;

                if( belongsTo == null ) {
                    // we are done
                    return nextTime;
                }

                ShadowMeshBase shadow = belongsTo.get( theShadowIdentifier );
                if( shadow == null ) {
                    return nextTime;
                }

                try {
                    nextTime = shadow.doUpdateNow( coherence );

                    if( nextTime != null && nextTime.longValue() >= 0 ) { // allow 0 for immediate execution
                        if( log.isDebugEnabled() ) {
                            log.debug( this + ".call ... schedule in " + nextTime.longValue() );
                        }
                        belongsTo.theExecutorService.schedule( this, nextTime.longValue(), TimeUnit.MILLISECONDS );
                    }

                } catch( IsDeadException ex ) {
                    log.info( ex );
                    // simply don't reschedule
                }
                return nextTime;

            } finally {
                if( belongsTo != null ) {
                    belongsTo.theFutures.remove( this );
                }
            }
        }
        
        /**
         * Convert to String representation, for debugging.
         *
         * @return String representation
         */
        @Override
        public String toString()
        {
            return StringHelper.objectLogString(
                    this,
                    new String[] {
                        "shadowIdentifier",
                        "theWillBeCalledAt"
                    },
                    new Object[] {
                        theShadowIdentifier,
                        theWillBeCalledAt
                    });
        }

        /**
         * The ScheduledExecutorProbeManager that this instance belogs to. This is a static
         * class with a WeakReference in order to not get in the way of garbage collection.
         */
        protected WeakReference<ScheduledExecutorProbeManager> theBelongsTo;

        /**
         * The NetMeshBaseIdentifier of the ShadowMeshBase to call. This allows the garbage collection
         * for the ShadowMeshBase to proceed even if future runs are scheduled.
         */
        protected NetMeshBaseIdentifier theShadowIdentifier;
        
        /**
         * The time at which this Callable will be called. This is only here for debugging purposes.
         */
        protected Date theWillBeCalledAt;
    }
}
