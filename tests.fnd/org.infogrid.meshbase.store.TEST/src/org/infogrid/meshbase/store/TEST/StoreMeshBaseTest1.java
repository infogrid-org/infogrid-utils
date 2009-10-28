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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.store.TEST;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.store.StoreMeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.store.Store;
import org.infogrid.store.StoreListener;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Test that we do the right number of writes and reads.
 */
public class StoreMeshBaseTest1
        extends
            AbstractStoreMeshBaseTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        EntityType   typeAA = theModelBase.findEntityType(   "org.infogrid.model.Test", null, "AA" );
        PropertyType ptX    = theModelBase.findPropertyType( "org.infogrid.model.Test", null, "A",  "X" );

        //
        
        log.info( "Deleting old database and creating new database" );

        theSqlStore.initializeHard();
        
        MyListener listener = new MyListener();
        theSqlStore.addDirectStoreListener( listener );

        //

        log.info( "Creating MeshBase" );

        super.startClock();

        StoreMeshBase mb = StoreMeshBase.create(
                theMeshBaseIdentifierFactory.fromExternalForm( "MeshBase" ),
                theModelBase,
                null,
                theSqlStore,
                rootContext );

        MeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

        checkEquals( listener.thePuts.size(),       1, "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0, "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0, "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 2, "Wrong number of failedGets" ); // twice for a non-existing home object
        checkEquals( listener.theDeletes.size(),    0, "Wrong number of deletes" );
        listener.reset();

        //

        log.info( "Creating MeshObjects" );
        
        Transaction tx = mb.createTransactionNow();

        MeshObject []           mesh  = new MeshObject[ theTestSize ];
        MeshObjectIdentifier [] names = new MeshObjectIdentifier[ theTestSize ];
        
        WeakReference [] refs = new WeakReference[ theTestSize ];
        
        int updateCount = 0;
        for( int i=0 ; i<mesh.length ; ++i ) {
            mesh[i]  = life.createMeshObject();
            names[i] = mesh[i].getIdentifier();
            refs[i]  = new WeakReference<MeshObject>( mesh[i] );
            
            if( i % 3 == 1 ) {
                mesh[i].bless( typeAA );
                ++updateCount;
            } else if( i % 3 == 2 ) {
                mesh[i].bless( typeAA );
                mesh[i].setPropertyValue( ptX, StringValue.create( "Testing ... " + i ));
                ++updateCount;
            }
        }

        tx.commitTransaction();
        tx = null;

        checkEquals( listener.thePuts.size(),       theTestSize, "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    updateCount, "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0,           "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), theTestSize, "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,           "Wrong number of deletes" );
        listener.reset();

        //
        
        log.info( "Clearing cache, and loading MeshObjects again" );
        
        mesh = new MeshObject[ names.length ]; // forget old references
        mb.clearMemoryCache();
        collectGarbage();

        for( int i=0 ; i<refs.length ; ++i ) {
            checkCondition( refs[i].get() == null, "MeshObject " + i + " still found" );
        }
        
        for( int i=0 ; i<names.length ; ++i ) {
            mesh[i] = mb.findMeshObjectByIdentifier( names[i] );
            
            checkObject( mesh[i], "Could not retrieve MeshObject with Identifier " + names[i] );
        }

        checkEquals( listener.thePuts.size(),       0,            "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,            "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       names.length, "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,            "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,            "Wrong number of deletes" );
        listener.reset();
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreMeshBaseTest1 test = null;
        try {
            if( args.length < 1 ) {
                System.err.println( "Synopsis: <test size>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreMeshBaseTest1( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }
        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.info( "FAIL (" + errorCount + " errors)" );
        }
        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args command-line arguments
     * @throws Exception anything can go wrong in a test
     */
    public StoreMeshBaseTest1(
            String [] args )
        throws
            Exception
    {
        super( StoreMeshBaseTest1.class );

        theTestSize = Integer.parseInt( args[0] );
    }

    /**
     * The number of MeshObjects to create for the test.
     */
    protected int theTestSize;

    // Our Logger
    private static Log log = Log.getLogInstance( StoreMeshBaseTest1.class );
    
    /**
     * Test listener.
     */
    class MyListener
            implements
                StoreListener
    {
        /**
         * A put operation was performed. This indicates either a
         * <code>Store.put</code> or a <code>Store.putOrUpdate</code> operation
         * in which an actual <code>put</code> was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was put
         */
        public void putPerformed(
                Store      store,
                StoreValue value )
        {
            thePuts.add( value.getKey() );
        }

        /**
         * An update operation was performed. This indicates either a
         * <code>Store.update</code> or a <code>Store.putOrUpdate</code> operation
         * in which an actual <code>update</code> was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was updated
         */
        public void updatePerformed(
                Store      store,
                StoreValue value )
        {
            theUpdates.add( value.getKey() );
        }

        /**
         * A get operation was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was obtained
         */
        public void getPerformed(
                Store      store,
                StoreValue value )
        {            
            theGets.add( value.getKey() );
        }                

        /**
         * A get operation was attempted but not value could be found.
         *
         * @param store the Store that emitted this event
         * @param key the key that was attempted
         */
        public void getFailed(
                Store  store,
                String key )
        {
            theFailedGets.add( key );
        }

        /**
         * A delete operation was performed.
         *
         * @param store the Store that emitted this event
         * @param key the key with which the data element was stored
         */
        public void deletePerformed(
                Store  store,
                String key )
        {
            theDeletes.add( key );
        }

        /**
         * A delete-all operation was performed.
         *
         * @param store the Store that emitted this event
         * @param prefix if given, indicates the prefix of all keys that were deleted. If null, indicates &quot;all&quot;.
         */
        public void deleteAllPerformed(
                Store  store,
                String prefix )
        {
            theAllDeletes.add( prefix );
        }

        /**
         * Reset the listener.
         */
        public void reset()
        {
            thePuts.clear();
            theUpdates.clear();
            theGets.clear();
            theFailedGets.clear();
            theDeletes.clear();
            theAllDeletes.clear();
        }

        protected ArrayList<String> thePuts       = new ArrayList<String>();
        protected ArrayList<String> theUpdates    = new ArrayList<String>();
        protected ArrayList<String> theGets       = new ArrayList<String>();
        protected ArrayList<String> theFailedGets = new ArrayList<String>();
        protected ArrayList<String> theDeletes    = new ArrayList<String>();
        protected ArrayList<String> theAllDeletes = new ArrayList<String>();
    }
}
