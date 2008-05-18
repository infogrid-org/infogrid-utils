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

package org.infogrid.kernel.net.TEST.xpriso;

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObject;
import org.infogrid.mesh.net.externalized.ParserFriendlyExternalizedNetMeshObjectFactory;
import org.infogrid.mesh.net.externalized.SimpleExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.net.transaction.NetMeshObjectCreatedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.xpriso.ParserFriendlyXprisoMessage;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.net.xpriso.xml.XprisoMessageXmlEncoder;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.m.MMeshTypeIdentifierFactory;
import org.infogrid.util.logging.Log;

import java.io.ByteArrayInputStream;

/**
 * Tests XprisoMessage serialization.
 */
public class XprisoMessageSerializationTest1
        extends
            AbstractXprisoTest
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
        XprisoMessage [] testMessages = constructTestMessages();
        
        for( int i=0 ; i<testMessages.length ; ++i ) {
            runOne( i, testMessages[i] );
        }
    }

    /**
     * Run a single test.
     */
    protected void runOne(
            int           index,
            XprisoMessage message )
        throws
            Exception
    {
        log.info( "Now running test " + index );
        
        XprisoMessageXmlEncoder encoder = new XprisoMessageXmlEncoder();
        StringBuilder           buf     = new StringBuilder();
        
        encoder.appendXprisoMessage( message, buf );
        
        String encodedMessage = buf.toString();
        
        log.debug( "Serialized message: " + encodedMessage );
        
        ByteArrayInputStream inStream = new ByteArrayInputStream( encodedMessage.getBytes());
        
        XprisoMessage recovered = encoder.decodeXprisoMessage(
                inStream,
                theExternalizedMeshObjectFactory,
                theNetMeshObjectIdentifierFactory,
                theMeshTypeIdentifierFactory );

        checkEquals( message, recovered, "Recovered XprisoMessage not the same" );
    }

    /**
     * Construct the test messages.
     */
    protected XprisoMessage [] constructTestMessages()
        throws
            Exception
    {
        // some useful data setup
        
        NetMeshBaseIdentifier id1 = NetMeshBaseIdentifier.create( "http://some.where.example.com/" );
        NetMeshBaseIdentifier id2 = NetMeshBaseIdentifier.create( "http://foo.net/" ); // FIXME Make this work with plain =foo, too
        NetMeshBaseIdentifier id3 = NetMeshBaseIdentifier.create( "http://abc.example.net/?some=parameter&other=%20" );
        
        NetMeshObjectIdentifier nmo_ref1 = theNetMeshObjectIdentifierFactory.fromExternalForm( "" );
        NetMeshObjectIdentifier nmo_ref2 = theNetMeshObjectIdentifierFactory.fromExternalForm( "abc" );
        NetMeshObjectIdentifier nmo_ref3 = theNetMeshObjectIdentifierFactory.fromExternalForm( "example.com" );
        NetMeshObjectIdentifier nmo_ref4 = theNetMeshObjectIdentifierFactory.fromExternalForm( "http://example.net/foo/bar#abc" );
        NetMeshObjectIdentifier nmo_ref5 = theNetMeshObjectIdentifierFactory.fromExternalForm( "some.where#abc" );
        NetMeshObjectIdentifier nmo_ref6 = theNetMeshObjectIdentifierFactory.fromExternalForm( "some.other%20place#123456" );

        MeshTypeIdentifier mt_ref1 = theMeshTypeIdentifierFactory.fromExternalForm( "org.infogrid.model.Some.Model" );
        MeshTypeIdentifier mt_ref2 = theMeshTypeIdentifierFactory.fromExternalForm( "org.infogrid.model/Some/Model" );
        MeshTypeIdentifier mt_ref3 = theMeshTypeIdentifierFactory.fromExternalForm( "http://foo.bar.example/com#123" );
        MeshTypeIdentifier mt_ref4 = theMeshTypeIdentifierFactory.fromExternalForm( "=foobar" );
        MeshTypeIdentifier mt_ref5 = theMeshTypeIdentifierFactory.fromExternalForm( "mailto:dev@null.com" );
        
        // Message 0
        
        ParserFriendlyXprisoMessage zero = ParserFriendlyXprisoMessage.create( 0, 33, id1, id2 );
        zero.addRequestedFirstTimeObject( NetMeshObjectAccessSpecification.create( id2 ));
        zero.addRequestedFirstTimeObject( NetMeshObjectAccessSpecification.create(
                NetMeshBaseAccessSpecification.create( new NetMeshBaseIdentifier[] { id3, id1 } )));
        
        zero.addPushLockObject( nmo_ref3 );
        zero.addPushLockObject( nmo_ref4 );
        
        zero.addRequestedCanceledObject( nmo_ref1 );
        zero.addRequestedLockObjects( nmo_ref2 );
        
        // Message 1
        
        ParserFriendlyXprisoMessage one = ParserFriendlyXprisoMessage.create( 111, 123456, null, null );
        one.addReclaimedLockObject( nmo_ref2 );
        one.addReclaimedLockObject( nmo_ref3 );
        one.addRequestedResynchronizeDependentReplica( nmo_ref1 );
        one.setCeaseCommunications( true );
        
        // Message 2
        
        ParserFriendlyXprisoMessage two = ParserFriendlyXprisoMessage.create( 222, 0, id3, id1 );
        two.addConveyedMeshObject( SimpleExternalizedNetMeshObject.create(
                nmo_ref1, // identifier
                new MeshTypeIdentifier[] {
                        mt_ref1,
                        mt_ref2 
                }, // typeNames
                12L, // timeCreated
                34L, // timeUpdated
                56L, // timeRead
                78L, // timeExpires
                new MeshTypeIdentifier[] {
                        mt_ref3,
                        mt_ref4
                }, // propertyTypes
                new PropertyValue[] {
                        StringValue.create( "test stringy" ),
                        FloatValue.create( 999.987 )
                }, // propertyValues
                new NetMeshObjectIdentifier[] {
                        nmo_ref6,
                        nmo_ref4
                }, // neighbors
                new MeshTypeIdentifier [][] {
                        new MeshTypeIdentifier [] { mt_ref5, mt_ref2, mt_ref1 },
                        null
                }, // roleTypes
                new NetMeshObjectIdentifier[] {
                        nmo_ref3,
                        nmo_ref5
                }, // equivalents
                false, // giveUpHomeReplica
                true, // giveUpLock
                new NetMeshBaseIdentifier[] {
                        id3,
                        id1
                }, // proxyNames
                1, // proxyTowardsHomeIndex
                0 )); // proxyTowardsLockIndex
        
        // Message 3
        
        ParserFriendlyXprisoMessage three = ParserFriendlyXprisoMessage.create( 333, -27, id3, id1 );
        three.addCreation( new NetMeshObjectCreatedEvent(
                null,
                id2,
                SimpleExternalizedNetMeshObject.create(
                        nmo_ref2, // identifier
                        new MeshTypeIdentifier[] {
                                mt_ref4
                        }, // typeNames
                        112L, // timeCreated
                        134L, // timeUpdated
                        156L, // timeRead
                        178L, // timeExpires
                        new MeshTypeIdentifier[] {
                                mt_ref4,
                                mt_ref1
                        }, // propertyTypes
                        new PropertyValue[] {
                                IntegerValue.create( 77778 ),
                                TimeStampValue.create( (short) 1, (short) 2, (short) 3, (short) 4, (short) 5, 6.f )
                        }, // propertyValues
                        new NetMeshObjectIdentifier[] {
                                nmo_ref2
                        }, // neighbors
                        new MeshTypeIdentifier [][] {
                                new MeshTypeIdentifier [] { mt_ref3 },
                                new MeshTypeIdentifier [] { mt_ref5 }
                        }, // roleTypes
                        new NetMeshObjectIdentifier[] {
                                nmo_ref5
                        }, // equivalents
                        true, // giveUpHomeReplica
                        false, // giveUpLock
                        new NetMeshBaseIdentifier[] {
                                id1
                        }, // proxyNames
                        -1, // proxyTowardsHomeIndex
                        0 ), // proxyTowardsLockIndex
                id3 ));
        three.addDeleteChange( new NetMeshObjectDeletedEvent(
                null,
                id2,
                null,
                nmo_ref4,
                id3,
                null,
                5834L ));

        // Message 4
        
        ParserFriendlyXprisoMessage four = ParserFriendlyXprisoMessage.create( 444, -11111, id2, id3 );
        four.addResynchronizeDependentReplica( SimpleExternalizedNetMeshObject.create(
                        nmo_ref1, // identifier
                        new MeshTypeIdentifier[] {
                                mt_ref2
                        }, // typeNames
                        112L, // timeCreated
                        134L, // timeUpdated
                        156L, // timeRead
                        178L, // timeExpires
                        new MeshTypeIdentifier[0], // propertyTypes
                        new PropertyValue[0], // propertyValues
                        new NetMeshObjectIdentifier[0], // neighbors
                        new MeshTypeIdentifier [0][], // roleTypes
                        new NetMeshObjectIdentifier[0], // equivalents
                        true, // giveUpHomeReplica
                        true, // giveUpLock
                        new NetMeshBaseIdentifier[0], // proxyNames
                        -1, // proxyTowardsHomeIndex
                        -1 )); // proxyTowardsLockIndex

        // Message 5
        
        ParserFriendlyXprisoMessage five = ParserFriendlyXprisoMessage.create( 555, 0, null, null );
        five.addNeighborAddition( new NetMeshObjectNeighborAddedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref5, mt_ref1 },
                nmo_ref5,
                id3,
                17L,
                null ) );

        // Message 6

        ParserFriendlyXprisoMessage six = ParserFriendlyXprisoMessage.create( 666, 0, null, null );
        six.addNeighborRemoval( new NetMeshObjectNeighborRemovedEvent(
                nmo_ref2,
                nmo_ref3,
                id2,
                92L,
                null ) );
        
        // Message 7

        ParserFriendlyXprisoMessage seven = ParserFriendlyXprisoMessage.create( 777, 0, null, null );
        seven.addRoleAdditions( new NetMeshObjectRoleAddedEvent(
                nmo_ref4,
                new MeshTypeIdentifier[] { mt_ref4, mt_ref3 },
                nmo_ref1,
                id1,
                7777L,
                null ) );
        seven.addRoleRemoval( new NetMeshObjectRoleRemovedEvent(
                nmo_ref2,
                new MeshTypeIdentifier[] { mt_ref2, mt_ref5 },
                nmo_ref1,
                id2,
                8888L,
                null ) );
        
        // Message 8

        ParserFriendlyXprisoMessage eight = ParserFriendlyXprisoMessage.create( 888, 0, null, null );
        eight.addTypeAddition( new NetMeshObjectTypeAddedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref4, mt_ref1 },
                id2,
                1L,
                null ) );
        eight.addTypeRemoval( new NetMeshObjectTypeRemovedEvent(
                nmo_ref3,
                new MeshTypeIdentifier[] { mt_ref1, mt_ref2 },
                id3,
                17L,
                null ) );

        // Message 7
        
        ParserFriendlyXprisoMessage nine = ParserFriendlyXprisoMessage.create( 999, 0, null, null );
        nine.addPropertyChange( new NetMeshObjectPropertyChangeEvent(
                nmo_ref3,
                mt_ref4,
                TimePeriodValue.create( 12L ),
                TimePeriodValue.create( (short) 2008, (short) 1, (short) 2, (short) 3, (short) 4, 56.789f ),
                id1,
                17L,
                null ) );
        
        // Put response together
        return new XprisoMessage[] {
                zero,
                one,
                two,
                three,
                four,
                five,
                six,
                seven,
                eight,
                nine
        };
    }

    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        XprisoMessageSerializationTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new XprisoMessageSerializationTest1( args );
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
    public XprisoMessageSerializationTest1(
            String [] args )
        throws
            Exception
    {
        super( XprisoMessageSerializationTest1.class );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( XprisoMessageSerializationTest1.class );
    
    /**
     * The test NetMeshBaseIdentifier.
     */
    private static NetMeshBaseIdentifier theMeshBaseIdentifier;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = NetMeshBaseIdentifier.create( "http://example.com/" );
        } catch( Throwable t ) {
            log.error( t );
        }
        theMeshBaseIdentifier = temp;
    }

    protected NetMeshBaseIdentifier nmbid1 = NetMeshBaseIdentifier.fromExternalForm( "https://foo.exampe.com/%27" );

    protected ParserFriendlyExternalizedNetMeshObjectFactory theExternalizedMeshObjectFactory
            = new ParserFriendlyExternalizedNetMeshObjectFactory() {
                    public ParserFriendlyExternalizedNetMeshObject createParserFriendlyExternalizedMeshObject() {
                        return new ParserFriendlyExternalizedNetMeshObject();
                    }
            };
    
    protected NetMeshObjectIdentifierFactory theNetMeshObjectIdentifierFactory
            = DefaultAnetMeshObjectIdentifierFactory.create( nmbid1 );

    protected MeshTypeIdentifierFactory theMeshTypeIdentifierFactory
            = MMeshTypeIdentifierFactory.create();
}
