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

package org.infogrid.kernel.TEST.differencer;

import org.infogrid.context.Context;
import org.infogrid.context.SimpleContext;

import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;

import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.Change;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.RelationshipType;
import org.infogrid.model.primitives.SubjectArea;

import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;

import org.infogrid.testharness.AbstractTest;

import org.infogrid.util.logging.Log;

/**
 *
 */
public abstract class AbstractDifferencerTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    protected AbstractDifferencerTest(
            Class testClass )
        throws
            MeshTypeNotFoundException
    {
        super( localFile( testClass, "/ResourceHelper" ),
               localFile( testClass, "/Log.properties" ));

        SubjectArea sa = theModelBase.findSubjectArea( "org.infogrid.model.Test", null );
        
        EntityType typeA = theModelBase.findEntityType( sa, "A" );
        typeAA   = theModelBase.findEntityType(       sa,     "AA" );
        typeB    = theModelBase.findEntityType(       sa,     "B" );
        typeC    = theModelBase.findEntityType(       sa,     "C" );
        typeR    = theModelBase.findRelationshipType( sa,     "R" );
        typeRR   = theModelBase.findRelationshipType( sa,     "RR" );
        typeAR1A = theModelBase.findRelationshipType( sa,     "AR1A" );
        typeX    = theModelBase.findPropertyType(     typeA,  "X" );
        typeY    = theModelBase.findPropertyType(     typeAA, "Y" );
        typeZ    = theModelBase.findPropertyType(     typeB,  "Z" );
        typeU    = theModelBase.findPropertyType(     typeB,  "U" );
    }
    
    /**
     * Helper method to createCopy a MeshObject
     */
    protected MeshObject createMeshObject(
            MeshBaseLifecycleManager   life,
            MeshObjectIdentifier       identifier,
            EntityType                 type,
            long                       now )
        throws
            IsAbstractException,
            TransactionException,
            MeshObjectIdentifierNotUniqueException,
            NotPermittedException
    {
        MeshObject ret = life.createMeshObject( identifier, type, now, now, now, -1L );
        return ret;
    }

    /**
     * Helper method to createCopy a MeshObject
     */
    protected MeshObject createMeshObject(
            MeshBaseLifecycleManager life,
            MeshObjectIdentifier          identifier,
            long                     now )
        throws
            MeshObjectIdentifierNotUniqueException,
            TransactionException,
            NotPermittedException
    {
        MeshObject ret = life.createMeshObject( identifier, now, now, now, -1L );
        return ret;
    }

    /**
     * Helper to print the contents of a MeshBase.
     */
    protected void printMeshBase(
            Log              useThis,
            IterableMeshBase theMeshBase )
    {
        if( useThis.isDebugEnabled() ) {
            useThis.debug( "MeshBase content: " + theMeshBase.size() + " MeshObjects" );

            int i=0;
            for( MeshObject current : theMeshBase ) {
                useThis.debug( " " + i + ": " + current );
                ++i;
            }
        }
    }

    /**
     * Helper to print a ChangeSet.
     */
    protected void printChangeSet(
            Log       useThis,
            ChangeSet theChangeSet )
    {
        if( useThis.isDebugEnabled() ) {
            Change [] theChanges = theChangeSet.getChanges();

            useThis.debug( "found " + theChanges.length + " changes" );

            for( int i=0 ; i<theChanges.length ; ++i ) {
                Change current = theChanges[i];
                useThis.debug( " " + i + ": " + current );
            }
        }
    }

    /**
     * The ModelBase.
     */
    protected ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * Buffered MeshTypes.
     */
    protected EntityType       typeAA;
    protected EntityType       typeB;
    protected EntityType       typeC;
    protected RelationshipType typeR;
    protected RelationshipType typeRR;
    protected RelationshipType typeAR1A;
    protected PropertyType     typeX;
    protected PropertyType     typeY;
    protected PropertyType     typeZ;
    protected PropertyType     typeU;

    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );
}
