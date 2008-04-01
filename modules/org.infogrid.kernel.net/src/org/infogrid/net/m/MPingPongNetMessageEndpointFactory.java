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

package org.infogrid.net.m;

import org.infogrid.net.NetMessageEndpoint;
import org.infogrid.net.NetMessageEndpointFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.NetMeshBase;

import org.infogrid.util.FactoryException;
import org.infogrid.util.NameServer;
import org.infogrid.util.ResourceHelper;

import java.util.concurrent.ScheduledExecutorService;
import java.util.List;

/**
 *
 */
public class MPingPongNetMessageEndpointFactory
        implements
            NetMessageEndpointFactory
{
    /**
     * Factory method.
     *
     * @param exec the ScheduledExecutorService to schedule communication-related events
     * @return the created MPingPongNetMessageEndpointFactory
     */
    public static MPingPongNetMessageEndpointFactory create(
            ScheduledExecutorService exec )
    {
        return new MPingPongNetMessageEndpointFactory( exec );
    }

    /**
     * Constructor.
     *
     * @param nameServer the NameServer
     * @param exec the ScheduledExecutorService to schedule communication-related events
     */
    protected MPingPongNetMessageEndpointFactory(
            ScheduledExecutorService exec )
    {
        theExecService = exec;
    }

    /**
     * Set the NameServer to use. This is a separate method and not in the constructor because
     * there are situations where the NameServer is not available at the time of construction
     * of an instance.
     *
     * @param nameServer the NameServer
     */
    public void setNameServer(
            NameServer<NetMeshBaseIdentifier, ? extends NetMeshBase> nameServer )
    {
        theNameServer = nameServer;
    }

    /**
     * Factory method.
     *
     * @param partnerIdentifier the key information required for object creation
     * @param myIdentifier any argument-style information required for object creation
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public MPingPongNetMessageEndpoint obtainFor(
            NetMeshBaseIdentifier partnerIdentifier,
            NetMeshBaseIdentifier myIdentifier )
        throws
            FactoryException
    {
        MPingPongNetMessageEndpoint ret = MPingPongNetMessageEndpoint.create(
                "Endpoint " + myIdentifier.toExternalForm() + " -> " + partnerIdentifier.toExternalForm(),
                partnerIdentifier,
                myIdentifier,
                deltaRespond,
                deltaResend,
                deltaRecover,
                randomVariation,
                theNameServer,
                theExecService );

        return ret;
    }
    
    /**
     * Restore a NetMessageEndpoint from storage.
     */
    public NetMessageEndpoint restoreNetMessageEndpoint(
            NetMeshBaseIdentifier   partnerIdentifier,
            NetMeshBaseIdentifier   myIdentifier,
            long                lastTokenSent,
            long                lastTokenReceived,
            List<XprisoMessage> lastMessagesSent,
            List<XprisoMessage> messagesToBeSent )
        throws
            FactoryException
    {
        MPingPongNetMessageEndpoint ret = MPingPongNetMessageEndpoint.restore(
                "Endpoint " + myIdentifier.toExternalForm() + " -> " + partnerIdentifier.toExternalForm(),
                partnerIdentifier,
                myIdentifier,
                deltaRespond,
                deltaResend,
                deltaRecover,
                randomVariation,
                theNameServer,
                theExecService,
                lastTokenSent,
                lastTokenReceived,
                lastMessagesSent,
                messagesToBeSent );

        return ret;
    }
    
    /**
     * The ScheduledExecutorService to use.
     */
    protected ScheduledExecutorService theExecService;

    /**
     * The NameServer to find Proxies to communicate with.
     */
    protected NameServer<NetMeshBaseIdentifier, ? extends NetMeshBase> theNameServer;

    /**
     * Our ResourceHelper.
     */
    protected static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MPingPongNetMessageEndpointFactory.class );
    
    /**
     * Milliseconds from ping to pong.
     */
    protected static long deltaRespond = theResourceHelper.getResourceLongOrDefault( "DeltaRespond", 1000L );
    
    /**
     * Millisecond until we attempt to resend failed messages.
     */
    protected static long deltaResend = theResourceHelper.getResourceLongOrDefault( "DeltaResend", 500L );
    
    /**
     * Millisecond until we attempt to recover.
     */
    protected static long deltaRecover = theResourceHelper.getResourceLongOrDefault( "DeltaRecover", 5000L );
    
    /**
     * Random variation, as percentage, of the respond and recover times.
     */
    protected static double randomVariation = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation", 0.02 ); // 2%
}
