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

package org.infogrid.meshbase.transaction;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.meshbase.MeshBase;

import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.MeshTypeUtils;

import org.infogrid.util.ArrayHelper;

/**
 * Event that indicates a MeshObject was unblessed from one or more EntityTypes.
 */
public class MeshObjectTypeRemovedEvent
        extends
            AbstractMeshObjectTypeChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the MeshObject whose type changed
     * @param oldValues the old set of EntityTypes, prior to the event
     * @param deltaValues the EntityTypes that were added
     * @param newValues the new set of EntityTypes, after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectTypeRemovedEvent(
            MeshObject        source,
            EntityType []     oldValues,
            EntityType []     deltaValues,
            EntityType []     newValues,
            long              timeEventOccurred )
    {
        super(  source,
                source.getIdentifier(),
                oldValues,
                MeshTypeUtils.meshTypeIdentifiers( oldValues ),
                deltaValues,
                MeshTypeUtils.meshTypeIdentifiers( deltaValues ),
                newValues,
                MeshTypeUtils.meshTypeIdentifiers( newValues ),
                timeEventOccurred );
    }

    /**
     * Constructor for the case where we don't have old and new values, only the delta.
     * This perhaps should trigger some exception if it is attempted to read old or
     * new values later. (FIXME?)
     * 
     * @param sourceIdentifier the identifier for the MeshObject whose type changed
     * @param deltaValues the EntityTypes that were removed
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectTypeRemovedEvent(
            MeshObjectIdentifier  sourceIdentifier,
            MeshTypeIdentifier [] deltaValues,
            long                  timeEventOccurred )
    {
        super(  null,
                sourceIdentifier,
                null,
                null,
                null,
                deltaValues,
                null,
                null,
                timeEventOccurred );        
    }
    
    /**
     * Constructor.
     *
     * @param sourceIdentifier the identifier of the MeshObject whose type changed
     * @param oldValueIdentifiers the identifiers of the old set of EntityTypes, prior to the event
     * @param deltaValueIdentifiers the identifiers of the EntityTypes that were removed
     * @param newValueIdentifiers the identifiers of the new set of EntityTypes, after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectTypeRemovedEvent(
            MeshObjectIdentifier  sourceIdentifier,
            MeshTypeIdentifier [] oldValueIdentifiers,
            MeshTypeIdentifier [] deltaValueIdentifiers,
            MeshTypeIdentifier [] newValueIdentifiers,
            long                  timeEventOccurred )
    {
        super(  null,
                sourceIdentifier,
                null,
                oldValueIdentifiers,
                null,
                deltaValueIdentifiers,
                null,
                newValueIdentifiers,
                timeEventOccurred );
    }

    /**
     * <p>Apply this Change to a MeshObject in this MeshBase. This method
     *    is intended to make it easy to reproduce Changes that were made in
     *    one MeshBase to MeshObjects in another MeshBase.</p>
     *
     * <p>This method will attempt to create a Transaction if none is present on the
     * current Thread.</p>
     *
     * @param base the MeshBase in which to apply the Change
     * @return the MeshObject to which the Change was applied
     * @throws CannotApplyChangeException thrown if the Change could not be applied, e.g because
     *         the affected MeshObject did not exist in MeshBase base
     * @throws TransactionException thrown if a Transaction didn't exist on this Thread and
     *         could not be created
     */
    public MeshObject applyTo(
            MeshBase otherMeshBase )
        throws
            CannotApplyChangeException,
            TransactionException
    {
        setResolver( otherMeshBase );

        Transaction tx = null;
        try {
            tx = otherMeshBase.createTransactionNowIfNeeded();

            MeshObject otherObject = getSource();

            EntityType [] types = getDeltaValue();
            otherObject.unbless( types );

            return otherObject;

        } catch( TransactionException ex ) {
            throw ex;

        } catch( Throwable ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( otherMeshBase, ex );

        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }
    
    /**
     * Determine equality.
     *
     * @param other the Object to compare with
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof MeshObjectTypeRemovedEvent )) {
            return false;
        }
        MeshObjectTypeRemovedEvent realOther = (MeshObjectTypeRemovedEvent) other;
        
        if( !super.getSourceIdentifier().equals( realOther.getSourceIdentifier() )) {
            return false;
        }        
        if( !ArrayHelper.hasSameContentOutOfOrder( getDeltaValueIdentifier(), realOther.getDeltaValueIdentifier(), true )) {
            return false;
        }
        if( getTimeEventOccurred() != realOther.getTimeEventOccurred() ) {
            return false;
        }
        return true;
    }
}
