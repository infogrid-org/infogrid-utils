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

import org.infogrid.comm.AbstractMessageEndpointListener;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.DefaultProxyFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.Proxy;
import org.infogrid.meshbase.net.ProxyManager;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.net.m.MPingPongNetMessageEndpoint;
import org.infogrid.net.m.MPingPongNetMessageEndpointFactory;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.manager.ScheduledExecutorProbeManager;
import org.infogrid.probe.manager.m.MScheduledExecutorProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.probe.shadow.m.MShadowMeshBaseFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.FactoryException;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.mesh.set.m.ImmutableMMeshObjectSetFactory;
import org.infogrid.probe.m.MProbeDirectory;

/**
 * Make sure PingPong stops if a Probe fails.
 */
public class ProbeTest7
        extends
            AbstractProbeTest
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
        NetMeshBaseIdentifier here = NetMeshBaseIdentifier.create( "http://here.local/" ); // this is not going to work for communications

        final MyListener theListener = new MyListener();
        
        // need to instrument the NetMeshBase
        final MPingPongNetMessageEndpointFactory shadowEndpointFactory = MPingPongNetMessageEndpointFactory.create( exec );

        ShadowMeshBaseFactory delegate
                = MShadowMeshBaseFactory.create( theModelBase, shadowEndpointFactory, theProbeDirectory, 12000L, rootContext );

        ScheduledExecutorProbeManager probeManager = MScheduledExecutorProbeManager.create( delegate );

        shadowEndpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MPingPongNetMessageEndpointFactory endpointFactory = new MPingPongNetMessageEndpointFactory( exec ) {
                @Override
                public MPingPongNetMessageEndpoint obtainFor(
                        NetMeshBaseIdentifier partnerIdentifier,
                        NetMeshBaseIdentifier myIdentifier )
                    throws
                        FactoryException
                {
                    MPingPongNetMessageEndpoint ret = super.obtainFor( partnerIdentifier, myIdentifier );
                    ret.addWeakMessageEndpointListener( theListener );
                    return ret;
                }
        };

        endpointFactory.setNameServer( probeManager.getNetMeshBaseNameServer() );

        MCachingHashMap<NetMeshBaseIdentifier,Proxy> proxyStorage = MCachingHashMap.create();
        DefaultProxyFactory                          proxyFactory = DefaultProxyFactory.create( endpointFactory );        
        
        ProxyManager proxyManager = ProxyManager.create( proxyFactory, proxyStorage );
                
        CachingMap<MeshObjectIdentifier,MeshObject> theCache          = MCachingHashMap.create();
        DefaultAnetMeshObjectIdentifierFactory      identifierFactory = DefaultAnetMeshObjectIdentifierFactory.create( here );
        ImmutableMMeshObjectSetFactory              setFactory        = ImmutableMMeshObjectSetFactory.create();
        
        
        LocalNetMMeshBase base = new LocalNetMMeshBase(
                here,
                identifierFactory,
                setFactory,
                theModelBase,
                null,
                theCache,
                proxyManager,
                probeManager,
                rootContext )
        {
            {
                initializeHomeObject(); // gotta be somewhere
            }
        };

        setFactory.setMeshBase( base );
        proxyFactory.setNetMeshBase( base );
        probeManager.setMainNetMeshBase( base );
        probeManager.start( exec );
        
        //
        
        log.info( "Accessing buggy probe" );

        MeshObject obj;
        try {
            obj = base.accessLocally( TEST_NETWORK_IDENTIFIER );

        } catch( Throwable ex ) {
            if( log.isDebugEnabled() ) {
                log.debug( "Received exception as expected" ); // , ex );
            }
        }
        
        //
        
        log.debug( "Resetting counter and sleeping for a while" );
        
        theListener.reset();
        Thread.sleep( 10000L );

        //
        
        log.debug( "Checking that Ping-Pong does not continue" );
        
        checkEquals( theListener.getFailedCounter(),          0, "PingPong kept going on" );
        checkEquals( theListener.getDisablingErrors().size(), 0, "PingPong was disabled" );
    }
 
    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ProbeTest7 test = null;
        try {
            if( args.length > 0 ) {
                System.err.println( "Synopsis: <no args>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ProbeTest7( args );
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
    public ProbeTest7(
            String [] args )
        throws
            Exception
    {
        super( ProbeTest7.class );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                TEST_NETWORK_IDENTIFIER.toExternalForm(),
                TestApiProbe.class ));
    }

    /**
     * Cleanup.
     */
    @Override
    public void cleanup()
    {
        exec.shutdown();
    }

    /**
     * A counter that is incremented every time the Probe is run.
     */
    static int probeRunCounter = 0;

    // Our Logger
    private static Log log = Log.getLogInstance(ProbeTest7.class);

    /**
     * The ProbeDirectory to use.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * The NetMeshBaseIdentifier identifying this Probe.
     */
    protected static final NetMeshBaseIdentifier TEST_NETWORK_IDENTIFIER;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = NetMeshBaseIdentifier.createUnresolvable( "TEST_NETWORK_IDENTIFIER.local" );

        } catch( Throwable t ) {
            log.error( t );
        }
        TEST_NETWORK_IDENTIFIER = temp;
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = Executors.newScheduledThreadPool( 1 );

    /**
     * The test Probe.
     */
    public static class TestApiProbe
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
         */
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
        {
            throw new NullPointerException();
        }
    }
    
    /**
     * Listener.
     */
    public static class MyListener
            extends
                AbstractMessageEndpointListener<XprisoMessage>
    {
        /**
         * Called when an outoing message failed to be sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the outgoing messages, in order
         */
        @Override
        public void messageSendingFailed(
                MessageEndpoint<XprisoMessage> endpoint,
                List<XprisoMessage>            msg )
        {
            log.debug( "Outgoing message sending failed: " + endpoint );
            
            theFailedCounter++;
        }
        
        /**
         * Called when an error was severe enough that continuing as a MessageEndPoint makes
         * no sense.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the status of the outgoing queue
         * @param t the Throwable indicating the error. This may be null if not available
         */
        @Override
        public void disablingError(
                MessageEndpoint<XprisoMessage> endpoint,
                List<XprisoMessage>            msg,
                Throwable                      t )
        {
            log.debug( "Disabling error: " + endpoint, t );
            
            theDisablingErrors.add( t );
        }

        /**
         * Reset counter.
         */
        public void reset()
        {
            theFailedCounter = 0;
            theDisablingErrors.clear();
        }

        /**
         * Obtain the counter.
         * 
         * @return the counter
         */
        public int getFailedCounter()
        {
            return theFailedCounter;
        }
        
        /**
         * Obtain the found disabling errors.
         */
        public ArrayList<Throwable> getDisablingErrors()
        {
            return theDisablingErrors;
        }

        protected int                  theFailedCounter   = 0;
        protected ArrayList<Throwable> theDisablingErrors = new ArrayList<Throwable>();
    }
}
