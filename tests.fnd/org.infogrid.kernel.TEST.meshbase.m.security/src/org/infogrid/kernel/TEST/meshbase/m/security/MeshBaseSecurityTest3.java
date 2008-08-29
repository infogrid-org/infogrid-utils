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

package org.infogrid.kernel.TEST.meshbase.m.security;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.model.AclBasedSecurity.AclBasedSecuritySubjectArea;
import org.infogrid.util.logging.Log;

/**
 * Tests that MeshObjects can only be added/removed to/from ProtectionDomains by the owner
 * of both the ProtectionDomain and the MeshObject.
 */
public class MeshBaseSecurityTest3
        extends
            AbstractMeshBaseSecurityTest
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
        log.info( "Creating principals" );
        
        Transaction tx = theMeshBase.createTransactionNow();
        
        MeshObject owner    = life.createMeshObject();

        theAccessManager.setCaller( owner );

        MeshObject ownerProtectionDomain = life.createMeshObject( AclBasedSecuritySubjectArea.PROTECTIONDOMAIN );
        MeshObject ownerData             = life.createMeshObject();

        theAccessManager.unsetCaller();
        
        MeshObject attacker = life.createMeshObject();

        theAccessManager.setCaller( attacker );

        MeshObject attackerProtectionDomain = life.createMeshObject( AclBasedSecuritySubjectArea.PROTECTIONDOMAIN );
        MeshObject attackerData             = life.createMeshObject();

        theAccessManager.unsetCaller();

        tx.commitTransaction();
        
        checkEquals(    owner.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getDestination() ).size(), 2, "wrong number objects owned by owner" );
        checkEquals( attacker.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getDestination() ).size(), 2, "wrong number objects owned by attacker" );

        //
        
        log.info( "Attacker attempting to put owner's data into his own ProtectionDomain" );
        
        theAccessManager.setCaller( attacker );
        tx = theMeshBase.createTransactionNow();
        
        try {
            ownerData.relateAndBless( AclBasedSecuritySubjectArea.PROTECTIONDOMAIN_GOVERNS_MESHOBJECT.getDestination(), attackerProtectionDomain );
            reportError( "Attacker succeeded putting owner's data into attacker's ProtectionDomain" );

        } catch( NotPermittedException ex ) {
            // no op
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Owner putting owner's data into his own ProtectionDomain" );
        
        theAccessManager.setCaller( owner );
        tx = theMeshBase.createTransactionNow();
        
        try {
            ownerData.relateAndBless( AclBasedSecuritySubjectArea.PROTECTIONDOMAIN_GOVERNS_MESHOBJECT.getDestination(), ownerProtectionDomain );

        } catch( NotPermittedException ex ) {
            reportError( "Owner unable putting owner's data into owner's ProtectionDomain" );
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Attacker attempting to remove owner's ProtectionDomain from owner's data" );
        
        theAccessManager.setCaller( attacker );
        tx = theMeshBase.createTransactionNow();
        
        try {
            ownerData.unrelate( ownerProtectionDomain ); // This also checks that we don't accidentally unrelate
            reportError( "Attacker succeeded removing owner's data from owner's ProtectionDomain" );

        } catch( NotPermittedException ex ) {
            // no op
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Owner should be able to remove owner's ProtectionDomain from owner's data" );
        
        theAccessManager.setCaller( owner );
        tx = theMeshBase.createTransactionNow();
        
        try {
            ownerData.unrelate( ownerProtectionDomain ); // This also checks that we don't accidentally unrelate

        } catch( NotPermittedException ex ) {
            reportError( "Owner was unable to remove owner's ProtectionDomain from owner's data" );
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        MeshBaseSecurityTest3 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new MeshBaseSecurityTest3( args );
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
    public MeshBaseSecurityTest3(
            String [] args )
        throws
            Exception
    {
        super( MeshBaseSecurityTest3.class );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( MeshBaseSecurityTest3.class );
}
