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

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import org.hamcrest.MatcherAssert;
import org.infogrid.comm.MessageEndpoint;
import org.infogrid.comm.MessageEndpointListener;
import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.comm.SendingMessageEndpoint;
import org.infogrid.comm.pingpong.PingPongMessageEndpoint;
import org.infogrid.comm.pingpong.m.MPingPongMessageEndpoint;
import org.infogrid.util.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the "asap" passing of messages between the endpoints.
 *
 * FIXME: The first message still takes a second to be conveyed. Should that be different?
 */
public class PingPongTest6
    extends
        AbstractPingPongTest
{
    @Test
    public void run()
            throws
                Exception
    {
        MPingPongMessageEndpoint<String> ep1 = MPingPongMessageEndpoint.create( "ep1", 1000L, 1L, 500L, 10000L, 0.f, exec );
        MPingPongMessageEndpoint<String> ep2 = MPingPongMessageEndpoint.create( "ep2", 1000L, 1L, 500L, 10000L, 0.f, exec );

        MyListener l1 = new MyListener( ep1, "one" );
        MyListener l2 = new MyListener( ep2, "two" );
        ep1.addDirectMessageEndpointListener( l1 );
        ep2.addDirectMessageEndpointListener( l2 );

        log.info( "Starting to ping-pong" );
        log.debug( "Note that the events seem to be a bit out of order as we only print the event after it was successfully sent (and received)" );

        startClock();

        ep1.setPartnerAndInitiateCommunications( ep2 );

        ep2.sendMessageAsap( "seed" );

        Thread.sleep( 10000L ); // four ping and five pongs

        ep1.stopCommunicating();
        ep2.stopCommunicating();

        Assert.assertTrue( "Not enough sent messages for l1: " + l1.sent, l1.sent >= MAX_RECEIVED-1 );
        Assert.assertTrue( "Not enough sent messages for l2: " + l2.sent, l2.sent >= MAX_RECEIVED-1 );
        Assert.assertTrue( "Not enough received messages for l1: " + l1.received, l1.received >= MAX_RECEIVED-1 );
        Assert.assertTrue( "Not enough received messages for l2: " + l2.received, l2.received >= MAX_RECEIVED-1 );

        log.debug( "l1.timeLastMessageReceived: " + l1.timeLastMessageReceived );
        log.debug( "l2.timeLastMessageReceived: " + l2.timeLastMessageReceived );

        Assert.assertTrue( "listener 1 received last message too early: " + l1.timeLastMessageReceived, l1.timeLastMessageReceived > 0 );
        Assert.assertTrue( "listener 1 received last message too late: "  + l1.timeLastMessageReceived, l1.timeLastMessageReceived < 1500L );
        Assert.assertTrue( "listener 2 received last message too early: " + l2.timeLastMessageReceived, l2.timeLastMessageReceived > 0 );
        Assert.assertTrue( "listener 2 received last message too late: "  + l2.timeLastMessageReceived, l2.timeLastMessageReceived < 1500L );
    }

    @Before
    public void setup() 
    {
        exec = createThreadPool( 1 );
    }

    @After
    public void cleanup()
    {
        done = true;

        exec.shutdown();        
    }

    // Our Logger
    private static Log log = Log.getLogInstance( PingPongTest6.class );

    /**
     * Our ThreadPool
     */
    protected ScheduledExecutorService exec;

    /**
     * Set to true if the test is done, so listeners won't report an error.
     */
    protected boolean done = false;

    /**
     * The maximum number of messages to respond to.
     */
    protected static int MAX_RECEIVED = 10;

    /**
     * Listener.
     */
    class MyListener
            implements
                MessageEndpointListener<String>
    {
        public MyListener(
                PingPongMessageEndpoint<String> end,
                String                          prefix )
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

            lastMessageReceived     = msgs.get( 0 );
            timeLastMessageReceived = getRelativeTime();
            ++received;

            if( received < MAX_RECEIVED ) {
                theEndpoint.sendMessageAsap( thePrefix + " " + lastMessageReceived );
                ++sent;
            }
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
         * @param t the error
         */
        public void disablingError(
                MessageEndpoint<String> endpoint,
                List<String>            msg,
                Throwable               t )
        {
            if( !done ) {
                Assert.fail( "Receiving endpoint is dead: " + msg );
            }
        }

        /**
         * Clear state.
         */
        public void clear()
        {
            received = 0;
            sent     = 0;
            lastMessageReceived     = null;
            timeLastMessageReceived = 0L;
        }

        /**
         * The endpoint through which we communicate.
         */
        protected PingPongMessageEndpoint<String> theEndpoint;

        /**
         * String to prepend to message before responding.
         */
        protected String thePrefix;

        /**
         * Caches the last received message.
         */
        protected String lastMessageReceived;

        /**
         * The number of messages received so far.
         */
        protected int received;

        /**
         * The number of messages sent so far.
         */
        protected int sent;

        /**
         * The time at which the last message came in.
         */
        protected long timeLastMessageReceived;
    }
}
