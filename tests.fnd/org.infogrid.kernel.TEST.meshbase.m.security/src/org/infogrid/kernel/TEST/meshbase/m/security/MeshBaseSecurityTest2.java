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
 * Tests that only owners can assign new owners to MeshObjects.
 */
public class MeshBaseSecurityTest2
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
        log.info( "Setup" );
        
        Transaction tx = theMeshBase.createTransactionNow();
        
        MeshObject owner1    = life.createMeshObject();
        MeshObject owner2    = life.createMeshObject();
        MeshObject attacker1 = life.createMeshObject();
        MeshObject attacker2 = life.createMeshObject();

        theAccessManager.setCaller( owner1 );
        
        MeshObject data = life.createMeshObject();
        
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        checkIdentity( owner1, data.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource() ).getSingleMember(), "Not the same owner1" );
        
        
        //
        
        log.info( "Attacker cannot create new owner" );
        
        theAccessManager.setCaller( attacker1 );
        tx = theMeshBase.createTransactionNow();

        try {
            data.relateAndBless( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), attacker1 );
            
            reportError( "Attacker could add itself as an owner" );
        } catch( NotPermittedException ex ) {
            // noop
        }
        
        try {
            data.relateAndBless( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), attacker2 );
            
            reportError( "Attacker could add second attacker as an owner" );
        } catch( NotPermittedException ex ) {
            // noop
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Owner can add second owner" );
        
        theAccessManager.setCaller( owner1 );
        tx = theMeshBase.createTransactionNow();

        try {
            data.relateAndBless( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), owner2 );
            
        } catch( NotPermittedException ex ) {
            reportError( "Owner could not add a second owner" );
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();

        //
        
        log.info( "Owner can remove itself as owner" );
        
        theAccessManager.setCaller( owner1 );
        tx = theMeshBase.createTransactionNow();

        try {
            data.unblessRelationship( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), owner1 );
            
        } catch( NotPermittedException ex ) {
            reportError( "Owner could not remove itself as owner" );
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Ex-owner is just like attacker now" );
        
        theAccessManager.setCaller( owner1 );
        tx = theMeshBase.createTransactionNow();

        try {
            data.blessRelationship( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), owner1 );
            
            reportError( "Ex-owner could add itself as an owner" );
        } catch( NotPermittedException ex ) {
            // noop
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();

        //
        
        log.info( "Second Owner can add back in first owner" );
        
        theAccessManager.setCaller( owner2 );
        tx = theMeshBase.createTransactionNow();

        try {
            data.blessRelationship( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource(), owner1 );
            
        } catch( NotPermittedException ex ) {
            reportError( "Second owner could not add ex-owner" );
        }
        tx.commitTransaction();
        theAccessManager.unsetCaller();
        
        //
        
        log.info( "Make sure there are two owners" );
        
        checkEquals( data.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource() ).size(), 2, "wrong number of owners" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        MeshBaseSecurityTest2 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new MeshBaseSecurityTest2( args );
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
    public MeshBaseSecurityTest2(
            String [] args )
        throws
            Exception
    {
        super( MeshBaseSecurityTest2.class );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( MeshBaseSecurityTest2.class );
}
