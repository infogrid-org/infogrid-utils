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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.comm.pingpong.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.comm.BidirectionalMessageEndpoint;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.MessageEndpointListener;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.m.MPingPongMessageEndpoint;
import org.infogrid.util.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that ping-ponging endpoints gracefully die.
 */
public class PingPongTest4
        extends
            AbstractPingPongTest
{
    @Test
    public void run()
            throws
                Exception
    {
        MPingPongMessageEndpoint<String> ep1 = MPingPongMessageEndpoint.create( "ep1", 1000L, 1000L, 500L, 5000L, 0.f, exec1 );
        MPingPongMessageEndpoint<String> ep2 = MPingPongMessageEndpoint.create( "ep2", 1000L, 1000L, 500L, 5000L, 0.f, exec2 );
        
        MyListener l1 = new MyListener( ep1, "one " );
        MyListener l2 = new MyListener( ep2, "two " );
        ep1.addDirectMessageEndpointListener( l1 );
        ep2.addDirectMessageEndpointListener( l2 );
        
        log.info( "Starting to ping-pong" );
        log.debug( "Note that the events seem to be a bit out of order as we only print the event after it was successfully sent (and received)" );

        ep1.setPartnerAndInitiateCommunications( ep2 );
        
        // only after we initiate communications, so the first message send does not happen on the main thread
        
        ep1.enqueueMessageForSend( "seed" );
    
        Thread.sleep( 4000L );

        Assert.assertTrue( "L1 has no messages", l1.messages.size() > 0 );
        Assert.assertTrue( "L2 has no messages", l2.messages.size() > 0 );
    
        ep1.gracefulDie();
        ep2.gracefulDie();
        
        Thread.sleep( 100L ); // just give the Threads a little bit of time to run
        
        l1.clear();
        l2.clear();
        
        Thread.sleep( 2500L );
        
        Assert.assertEquals( "L1 still got messages", l1.messages.size(), 0 );
        Assert.assertEquals( "L2 still got messages", l2.messages.size(), 0 );
    }

    @Before
    public void setup() 
    {
        exec1 = createThreadPool( getClass().getName() + "-exec1", 2 );
        exec2 = createThreadPool( getClass().getName() + "-exec2", 2 );
    }

    @After
    public void cleanup()
    {
        done = true;

        exec1.shutdown();
        exec2.shutdown();
    }

    // Our Logger
    private static Log log = Log.getLogInstance( PingPongTest4.class );
    
    /**
     * ThreadPool.
     */
    protected ScheduledExecutorService exec1;

    /**
     * ThreadPool.
     */
    protected ScheduledExecutorService exec2;
        
    /**
     * Set to true if the test is done, so listeners won't report an error.
     */
    protected boolean done = false;
    
    /**
     * Listener.
     */
    class MyListener
            implements
                MessageEndpointListener<String>
    {
        public MyListener(
                BidirectionalMessageEndpoint<String> end,
                String                  prefix )
        {
            theEndpoint = end;
            thePrefix   = prefix;
        }

        /**
         * Called when one more more incoming messages have arrived.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msgs the received messages
         */
        public void messageReceived(
                ReceivingMessageEndpoint<String> endpoint,
                List<String>                     msgs )
        {
            log.traceMethodCallEntry( this, "messageReceived", msgs );
            Assert.assertEquals( msgs.size(), 1 );

            messages.add( msgs.get( 0 ));
            theEndpoint.enqueueMessageForSend( thePrefix + ": responding to messageReceived" );
        }

        /**
         * Called when an outgoing message has been sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the sent message
         */
        public void messageSent(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            log.traceMethodCallEntry( this, "messageSent", msg );
        }

        /**
         * Called when an outgoing message has enqueued for sending.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the enqueued message
         */
        public void messageEnqueued(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            log.traceMethodCallEntry( this, "messageEnqueued", msg );
        }
    
        /**
         * Called when an outoing message failed to be sent.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the outgoing message
         */
        public void messageSendingFailed(
                SendingMessageEndpoint<String> endpoint,
                String                         msg )
        {
            Assert.fail( "Message sending failed: " + msg );
        }

        /**
         * Called when the receiving endpoint threw the EndpointIsDeadException.
         *
         * @param endpoint the MessageEndpoint that sent this event
         * @param msg the status of the outgoing queue
         * @param t the disabling error
         */
        public void disablingError(
                MessageEndpoint<String> endpoint,
                List<String>            msg,
                Throwable               t )
        {
            if( log.isDebugEnabled() ) {
                log.debug( "This is not an error in this test:" + msg );
            }
        }

        /**
         * Clear the stored event long.
         */
        public void clear()
        {
            messages = new ArrayList<String>();
        }
        
        /**
         * Convert to String representation, for debugging only.
         * 
         * @return String representation
         */
        @Override
        public String toString()
        {
            return "Listener (prefix: " + thePrefix + "): ";
        }

        BidirectionalMessageEndpoint<String> theEndpoint;
        String                  thePrefix;
        ArrayList<String>       messages = new ArrayList<String>();
    }
}

