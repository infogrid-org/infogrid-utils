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
import org.infogrid.mesh.MeshObjectUtils;

import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.RoleType;

import org.infogrid.util.ArrayHelper;

/**
 * This event indicates that a MeshObject was removed from the set of neighbors of a MeshObject.
 * In other words, the MeshObject now participates in one relationship less.
 */
public class MeshObjectNeighborRemovedEvent
        extends
            AbstractMeshObjectNeighborChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param meshObject the MeshObject that is the source of the event
     * @param oldNeighbors the set of neighbor MeshObjects prior to the event
     * @param deltaNeighbor the neighbor MeshObject affected by this event
     * @param newNeighbors the set of neighbor MeshObjects after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectNeighborRemovedEvent(
            MeshObject       meshObject,
            MeshObject []    oldNeighbors,
            MeshObject       deltaNeighbor,
            MeshObject []    newNeighbors,
            long             timeEventOccurred )
    {
        this(   meshObject,
                meshObject.getIdentifier(),
                oldNeighbors,
                MeshObjectUtils.meshObjectIdentifiers( oldNeighbors ),
                new MeshObject[] { deltaNeighbor },
                new MeshObjectIdentifier[] { deltaNeighbor.getIdentifier() },
                newNeighbors,
                MeshObjectUtils.meshObjectIdentifiers( newNeighbors ),
                timeEventOccurred );
    }

    /**
     * Constructor.
     * 
     * @param meshObject the MeshObject that is the source of the event
     * @param oldNeighborIdentifiers the identifiers of the neighbor MeshObjects prior to the event
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param newNeighborIdentifiers the identifiers of the neighbor MeshObjects after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectNeighborRemovedEvent(
            MeshObject              meshObject,
            MeshObjectIdentifier [] oldNeighborIdentifiers,
            MeshObjectIdentifier    deltaNeighborIdentifier,
            MeshObjectIdentifier [] newNeighborIdentifiers,
            long                    timeEventOccurred )
    {
        this(   meshObject,
                meshObject.getIdentifier(),
                null,
                oldNeighborIdentifiers,
                null,
                new MeshObjectIdentifier[] { deltaNeighborIdentifier },
                null,
                newNeighborIdentifiers,
                timeEventOccurred );
    }

    /**
     * Constructor.
     * 
     * @param meshObjectIdentifier the identifier of the MeshObject that is the source of the event
     * @param oldNeighborIdentifiers the identifiers of the neighbor MeshObjects prior to the event
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param newNeighborIdentifiers the identifiers of the neighbor MeshObjects after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public MeshObjectNeighborRemovedEvent(
            MeshObjectIdentifier    meshObjectIdentifier,
            MeshObjectIdentifier [] oldNeighborIdentifiers,
            MeshObjectIdentifier    deltaNeighborIdentifier,
            MeshObjectIdentifier [] newNeighborIdentifiers,
            long                    timeEventOccurred )
    {
        this(   null,
                meshObjectIdentifier,
                null,
                oldNeighborIdentifiers,
                null,
                new MeshObjectIdentifier[] { deltaNeighborIdentifier },
                null,
                newNeighborIdentifiers,
                timeEventOccurred );
    }

    /**
     * Main constructor.
     *
     * @param meshObject the MeshObject that is the source of the event (optional)
     * @param meshObjectIdentifier Identifier of the MeshObject that is the source of the event (required)
     * @param oldNeighbors the set of neighbor MeshObjects prior to the event (optional)
     * @param oldNeighborIdentifiers the Identifiers of the neighbor MeshObjects prior to the event (required)
     * @param deltaNeighbors the set of neighbor MeshObjects affected by this event (optional)
     * @param deltaNeighborIdentifiers the Identifiers of the neighbor MeshObjects affected by this event (required)
     * @param newNeighbors the set of neighbor MeshObjects after the event (optional)
     * @param newNeighborIdentifiers the Identifiers of the neighbor MeshObjects after the event (required)
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected MeshObjectNeighborRemovedEvent(
            MeshObject              meshObject,
            MeshObjectIdentifier    meshObjectIdentifier,
            MeshObject []           oldNeighbors,
            MeshObjectIdentifier [] oldNeighborIdentifiers,
            MeshObject []           deltaNeighbors,
            MeshObjectIdentifier [] deltaNeighborIdentifiers,
            MeshObject []           newNeighbors,
            MeshObjectIdentifier [] newNeighborIdentifiers,
            long                    timeEventOccurred )
    {
        super(  meshObject,
                meshObjectIdentifier,
                null,
                null,
                oldNeighbors,
                oldNeighborIdentifiers,
                deltaNeighbors,
                deltaNeighborIdentifiers,
                newNeighbors,
                newNeighborIdentifiers,
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
            MeshBase base )
        throws
            CannotApplyChangeException,
            TransactionException
    {
        setResolver( base );

        Transaction tx = null;

        MeshObject    otherObject; // declaring this out here makes debugging much easier
        MeshObject [] relatedOtherObjects;
        RoleType []   roleTypes;

        try {
            tx = base.createTransactionNowIfNeeded();

            otherObject         = getSource();
            relatedOtherObjects = getDeltaValue();
            roleTypes           = getProperty(); // FIXME not really needed

            for( int i=0 ; i<relatedOtherObjects.length ; ++i ) {
                otherObject.unrelate( relatedOtherObjects[i] );
            }

            return otherObject;

        } catch( TransactionException ex ) {
            throw ex;

        } catch( Throwable ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( base, ex );

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
        if( !( other instanceof MeshObjectNeighborRemovedEvent )) {
            return false;
        }
        MeshObjectNeighborRemovedEvent realOther = (MeshObjectNeighborRemovedEvent) other;
        
        if( !getSourceIdentifier().equals( realOther.getSourceIdentifier() )) {
            return false;
        }
        if( !ArrayHelper.hasSameContentOutOfOrder( getPropertyIdentifier(), realOther.getPropertyIdentifier(), true )) {
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
