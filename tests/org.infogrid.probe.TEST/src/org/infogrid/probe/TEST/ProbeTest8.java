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

package org.infogrid.probe.TEST;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.instrument.Breakpoint;
import org.infogrid.util.instrument.InstrumentedThread;
import org.infogrid.util.logging.Log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.probe.m.MProbeDirectory;

/**
  * Tests multi-threaded behavior of Probes / Shadows.
  */
public class ProbeTest8
    extends
        AbstractProbeTest
{
    /**
     * run the test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Creating threads" );

        Runnable [] runnables = new Runnable[] {
                new MyRunnable( TEST1_URL ) {
                    protected void assignResult(
                            MeshObject result )
                    {
                        foundObject1 = result;
                    }
                },
                new MyRunnable( TEST2_URL ) {
                    protected void assignResult(
                            MeshObject result )
                    {
                        foundObject2 = result;
                    }
                },
                new MyRunnable( TEST3_URL ) {
                    protected void assignResult(
                            MeshObject result )
                    {
                        foundObject3 = result;
                    }
                },
                new MyRunnable( TEST2_URL ) { // this is the same as TEST2
                    protected void assignResult(
                            MeshObject result )
                    {
                        foundObject4 = result;
                    }
                }
        };

        InstrumentedThread [] threads = createThreads( runnables, new String[] {
                    "one",
                    "two",
                    "three",
                    "four"
                } );

        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkCondition( foundObject2 == null, "found object 2 too early" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkCondition( foundObject4 == null, "found object 4 too early" );

        //

        log.info( "Advancing threads" );

        threads[0].advanceTo( location1 );
        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkCondition( foundObject2 == null, "found object 2 too early" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkCondition( foundObject4 == null, "found object 4 too early" );

        threads[1].advanceTo( location2 );
        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkCondition( foundObject2 == null, "found object 2 too early" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkCondition( foundObject4 == null, "found object 4 too early" );

        threads[2].advanceTo( location3 );
        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkCondition( foundObject2 == null, "found object 2 too early" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkCondition( foundObject4 == null, "found object 4 too early" );

        threads[3].advanceNormally();
        Thread.sleep( 1000L ); // give it a little to be blocked by threads[1]'s BreakPoint at location2
        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkCondition( foundObject2 == null, "found object 2 too early" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkCondition( foundObject4 == null, "found object 4 too early" );

        //

        log.info( "Completing threads" );

        threads[1].completeThreadAndWait();
        Thread.sleep( 1000L ); // give it a little so threads[3] can finish

        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkObject( foundObject2, "did not find object 2" );
        checkCondition( foundObject3 == null, "found object 3 too early" );
        checkObject( foundObject4, "did not find object 4" );

        threads[2].completeThreadAndWait();
        checkCondition( foundObject1 == null, "found object 1 too early" );
        checkObject( foundObject2, "did not find object 2" );
        checkObject( foundObject3, "did not find object 3" );
        checkObject( foundObject4, "did not find object 4" );

        threads[0].completeThreadAndWait();
        checkObject( foundObject1, "did not find object 1" );
        checkObject( foundObject2, "did not find object 2" );
        checkObject( foundObject3, "did not find object 3" );
        checkObject( foundObject4, "did not find object 4" );

        //

        log.info( "Make sure we only have 3 Shadows" );

        checkEquals( theMeshBase.getAllShadowMeshBases().size(), 3, "wrong number of shadows" );
    }

    /**
     * This method creates a Thread for each of the passed Runnables, and returns the Threads it created.
     * It does not start the Threads.
     *
     * @param runs the Runnables for which we want to create Threads
     * @param names the names for the different Threads in the same sequence as runs
     * @return the created Threads
     */
    protected InstrumentedThread [] createThreads(
            Runnable [] runs,
            String []   names )
    {
        if( getLog().isDebugEnabled() )
            getLog().debug( "createThreads( " + ArrayHelper.arrayToString( runs ) + " )" );

        InstrumentedThread [] ret = new InstrumentedThread[ runs.length ];
        for( int i=0 ; i<runs.length ; ++i )
        {
            if( runs[i] != null )
                ret[i] = new InstrumentedThread( runs[i], names != null ? names[i] : null );
            else
                log.info( "Skipping start of Thread with index " + i );
        }
        return ret;
    }

    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ProbeTest8 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no args>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest8( args );
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
     */
    public ProbeTest8(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest7.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST1_URL.toExternalForm(),
                TestApiProbe1.class ));
        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST2_URL.toExternalForm(),
                TestApiProbe2.class ));
        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST3_URL.toExternalForm(),
                TestApiProbe3.class ));

        NetMeshBaseIdentifier here = NetMeshBaseIdentifier.create( "http://here.local/" ); // this is not going to work for communications

        theMeshBase = LocalNetMMeshBase.create( here, theModelBase, null, exec, theProbeDirectory, rootContext );
    }

    /**
     * Clean up.
     */
    @Override
    public void cleanup()
    {
        theMeshBase.die();
    }

    /**
     * our naming scheme
     */
    protected String scheme;

    /**
     * The ModelObjectRepository that we use to test.
     */
    protected LocalNetMMeshBase theMeshBase;

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    // Our Logger
    private static Log log = Log.getLogInstance(ProbeTest8.class);

    /**
     * the test protocol, in the real world this would be something like "jdbc"
     */
    private static final String PROTOCOL_NAME = "ProbeTest8Protocol";

    /**
     * The first URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST1_URL;

    /**
     * The second URL that we are accessing
     */
    private static NetMeshBaseIdentifier TEST2_URL;

    /**
     * The third URL that we are accessing.
     */
    private static NetMeshBaseIdentifier TEST3_URL;

    static {
        try {
            TEST1_URL = NetMeshBaseIdentifier.createUnresolvable( PROTOCOL_NAME + "://myhost.local/remainder" );
            TEST2_URL = NetMeshBaseIdentifier.createUnresolvable( PROTOCOL_NAME + "://otherhost.local/remainder" );
            TEST3_URL = NetMeshBaseIdentifier.createUnresolvable( PROTOCOL_NAME + "://otherhost.local/empty" );

        } catch( Exception ex ) {
            log.error( ex );
            
            TEST1_URL = null; // make compiler happy
            TEST2_URL = null; // make compiler happy
            TEST3_URL = null; // make compiler happy
        }
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * A "breakpoint" location. This has to be static as we can't provide non-static variables to our Probes.
     */
    private static Breakpoint location1 = new Breakpoint( "location1", 60000L );

    /**
     * A "breakpoint" location.
     */
    private static Breakpoint location2 = new Breakpoint( "location2", 60000L );

    /**
     * A "breakpoint" location.
     */
    private static Breakpoint location3 = new Breakpoint( "location3", 60000L );

    /**
     * the result delivered by Probe1
     */
    MeshObject foundObject1 = null;

    /**
     * the result delivered by Probe2
     */
    MeshObject foundObject2 = null;

    /**
     * the result delivered by Probe3
     */
    MeshObject foundObject3 = null;

    /**
     * the result delivered by Probe4
     */
    MeshObject foundObject4 = null;

    /**
     * The test Probe superclass.
     */
    public static abstract class TestApiProbe
        implements
            ApiProbe
    {
        /**
         * Read from the API and instantiate corresponding MeshObjects.
         * 
         * @param networkId the NetMeshBaseIdentifier that is being accessed
         * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
         *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
         *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
         *         Probe must bless the Probe's HomeObject with a subtype of ProbeUpdateSpecification (defined
         *         in the <code>org.infogrid.model.Probe</code>) that reflects the policy.
         * @param mb the StagingMeshBase in which the corresponding MeshObjects are instantiated by the Probe
         * @throws IdeMeshObjectIdentifierNotUniqueExceptionrown if the Probe developer incorrectly
         *         assigned duplicate Identifiers to created MeshObjects
         * @throws RelatedAlreadyException thrown if the Probe developer incorrectly attempted to
         *         relate two already-related MeshObjects
         * @throws TransactionException this Exception is declared to make programming easier,
         *         although actually throwing it would be a programming error
         * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
         * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
         * @throws IOException an input/output error occurred during execution of the Probe
         * @throws ModuleException thrown if a Module required by the Probe could not be loaded
         * @throws URISyntaxException thrown if a URI was constructed in an invalid way
         */
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                MeshObjectIdentifierNotUniqueException,
                RelatedAlreadyException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException
        {
            MeshObject                      home   = mb.getHomeObject();
            StagingMeshBaseLifecycleManager life   = mb.getMeshBaseLifecycleManager();
            NetMeshObjectIdentifierFactory  idFact = mb.getMeshObjectIdentifierFactory();

            MeshObject one = life.createMeshObject( idFact.fromExternalForm( "one" ));
            MeshObject two = life.createMeshObject( idFact.fromExternalForm( "two" ));
            
            home.relate( one );

            try {
                breakpoint();

            } catch( InterruptedException ex ) {
                throw new ProbeException.Other( networkId, ex );
            }

            home.relate( two );
        }

        protected abstract void breakpoint()
            throws
                InterruptedException;
    }

    /**
     * The first test Probe.
     */
    public static class TestApiProbe1
        extends
            TestApiProbe
    {
        protected void breakpoint()
            throws
                InterruptedException
        {
            location1.reached();
        }
    }

    /**
     * The second test Probe.
     */
    public static class TestApiProbe2
        extends
            TestApiProbe
    {
        protected void breakpoint()
            throws
                InterruptedException
        {
            location2.reached();
        }
    }

    /**
     * The third test Probe.
     */
    public static class TestApiProbe3
        extends
            TestApiProbe
    {
        protected void breakpoint()
            throws
                InterruptedException
        {
            location3.reached();
        }
    }

    /**
     * Code to be invoked by the threads.
     */
    public abstract class MyRunnable
        implements
            Runnable
    {
        public MyRunnable(
                NetMeshBaseIdentifier n )
        {
            thePath = n;
        }

        public void run()
        {
            try {
                MeshObject obj = theMeshBase.accessLocally(
                        thePath,
                        CoherenceSpecification.ONE_TIME_ONLY );
                assignResult( obj );

            } catch( Exception ex ) {
                reportError( "Unexpected exception", ex );
            }
        }

        protected abstract void assignResult(
                MeshObject result );

        private NetMeshBaseIdentifier thePath;
    }

}
