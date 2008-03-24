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

package org.infogrid.comm.pingpong.m;

import org.infogrid.comm.MessageEndpointIsDeadException;
import org.infogrid.comm.MessageSendException;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of PingPongMessageEndpoint.
 */
public class MPingPongMessageEndpoint<T>
        extends
            PingPongMessageEndpoint<T>
{
    private static final Log log = Log.getLogInstance( MPingPongMessageEndpoint.class ); // our own, private logger

    /**
     * Factory method.
     */
    public static <T> MPingPongMessageEndpoint<T> create(
            ScheduledExecutorService exec )
    {
        String name            = "MPingPongMessageEndpoint";
        long   deltaRespond    = theResourceHelper.getResourceLongOrDefault(   "DeltaRespond",   1000L );
        long   deltaResend     = theResourceHelper.getResourceLongOrDefault(   "DeltaResend",     500L );
        long   deltaRecover    = theResourceHelper.getResourceLongOrDefault(   "DeltaRecover",   5000L );
        double randomVariation = theResourceHelper.getResourceDoubleOrDefault( "RandomVariation", 0.02 ); // 2%
        
        // it is advantageous if the recover time is larger than 4 times the respond time: that way, a
        // second RPC call can be successfully completed before returning to the parent RPC caller.
        
        MPingPongMessageEndpoint<T> ret = new MPingPongMessageEndpoint<T>(
                name,
                deltaRespond,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec,
                -1,
                -1,
                null,
                null );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespond the number of milliseconds until this PingPongMessageEndpoint returns the token
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param exec the ScheduledExecutorService to use for threading
     * @return the created MPingPongMessageEndpoint
     */
    public static <T> MPingPongMessageEndpoint<T> create(
            String                   name,
            long                     deltaRespond,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec )
    {
        MPingPongMessageEndpoint<T> ret = new MPingPongMessageEndpoint<T>(
                name,
                deltaRespond,
                deltaResend,
                deltaRecover,
                randomVariation,
                exec,
                -1,
                -1,
                null,
                null );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param name the name of the PingPongMessageEndpoint (for debugging only)
     * @param deltaRespond the number of milliseconds until this PingPongMessageEndpoint returns the token
     * @param deltaResend  the number of milliseconds until this PingPongMessageEndpoint resends the token if sending the token failed
     * @param deltaRecover the number of milliseconds until this PingPongMessageEndpoint decides that the token
     *                     was not received by the partner PingPongMessageEndpoint, and resends
     * @param exec the ScheduledExecutorService to use for threading
     * @param lastSentToken the last token sent in a previous instantiation of this MessageEndpoint
     * @param lastReceivedToken the last token received in a previous instantiation of this MessageEndpoint
     * @param messagesSentLast the last set of Messages sent in a previous instantiation of this MessageEndpoint
     * @param messageToBeSent the Messages to be sent as soon as possible
     */
    protected MPingPongMessageEndpoint(
            String                   name,
            long                     deltaRespond,
            long                     deltaResend,
            long                     deltaRecover,
            double                   randomVariation,
            ScheduledExecutorService exec,
            long                     lastSentToken,
            long                     lastReceivedToken,
            List<T>                  messagesSentLast,
            List<T>                  messagesToBeSent )
    {
        super(  name,
                deltaRespond,
                deltaResend, 
                deltaRecover,
                randomVariation,
                exec,
                lastSentToken,
                lastReceivedToken,
                messagesSentLast,
                messagesToBeSent );
    }

    /**
     * Set the partner endpoint.
     *
     * @param partner the partner
     */
    public void setPartnerAndInitiateCommunications(
            MPingPongMessageEndpoint<T> partner )
    {
        if( thePartner != null ) {
            throw new IllegalStateException();
        }
        if( thePartner == this ) {
            throw new IllegalArgumentException( "Cannot communicate with myself" );
        }
        thePartner = partner;

        thePartner.thePartner = this; // point back to us
        
        startCommunicating();
    }
    
    /**
     * Send a message via the next ping or pong.
     *
     * @param msg the Message to send.
     */
    @Override
    public void enqueueMessageForSend(
            T msg )
    {
        if( isGracefullyDead ) {
            throw new IllegalStateException( this + " is dead" );
        }
        super.enqueueMessageForSend( msg );
    }

    /**
     * Do the message send.
     *
     * @param token the token of the message
     * @param content the content to send.
     */
    protected void sendMessage(
            long    token,
            List<T> content )
        throws
            MessageSendException
    {
        MPingPongMessageEndpoint<T> partner = thePartner;
        if( partner != null ) {
            partner.incomingMessage( token, content );
        } else {
            throw new MessageSendException( content, "No partner MPingPongMessageEndpoint has been set" );
        }
    }

    /**
     * Invoked by subclasses to provide the content of a received message.
     *
     * @param token the integer representing the token
     * @param content the content of a received message
     * @throws MessageEndpointIsDeadException thrown if the MessageEndpoint is dead
     */
    @Override
    protected void incomingMessage(
            long    token,
            List<T> content )
        throws
            MessageEndpointIsDeadException
    {
        if( isGracefullyDead ) {
            throw new MessageEndpointIsDeadException();
        } else {

            try {
                super.incomingMessage( token, content );

            } catch( RejectedExecutionException ex ) {
                throw new MessageEndpointIsDeadException( ex );
            }
        }
    }

    /**
     * Obtain the messages still to be sent.
     *
     * @return the List
     */    
    @SuppressWarnings(value={"unchecked"})
    public synchronized List<T> messagesToBeSent()
    {
        ArrayList<T> ret = new ArrayList<T>( theMessagesToBeSent != null ? theMessagesToBeSent.size() : 1 );

        if( theMessagesToBeSent != null && !theMessagesToBeSent.isEmpty() ) {
            ret.addAll( theMessagesToBeSent );
        }
        return ret;
    }
    
    /**
     * Obtain the messages that were sent most recently.
     *
     * @return the List
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized List<T> messagesLastSent()
    {
        ArrayList<T> ret = new ArrayList<T>( theMessagesSentLast != null ? theMessagesSentLast.size() : 1 );

        if( theMessagesSentLast != null && !theMessagesSentLast.isEmpty() ) {
            ret.addAll( theMessagesSentLast );
        }
        return ret;
    }

    /**
     * Attempt to send the outgoing messages, but stop receiving incoming messages.
     */
    public void gracefulDie()
    {
        isGracefullyDead = true;
        
        if( theFuture != null ) {
            if( log.isDebugEnabled() ) {
                log.debug( this + " canceling future" );
            }
            theFuture.cancel( false );
            theFuture = null;
        }
    }

//    /**
//     * Allow the setting of breakpoints.
//     */
//    public void finalize()
//    {
//        if( log.isDebugEnabled() ) {
//            log.debug( this + ".finalize()" );
//        }
//    }

    /**
     * The partner PingPongMessageEndpoint.
     */
    protected MPingPongMessageEndpoint<T> thePartner;
    
    /**
     * If this is true, the MessageEndpoint is dead, but still attempts to send queued
     * messages.
     */
    protected boolean isGracefullyDead = false;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MPingPongMessageEndpoint.class );
}
