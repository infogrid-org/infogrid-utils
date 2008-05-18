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

package org.infogrid.meshbase.net.transaction;


import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObjectUtils;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.Proxy;

import org.infogrid.meshbase.transaction.CannotApplyChangeException;
import org.infogrid.meshbase.transaction.MeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.MeshTypeUtils;
import org.infogrid.model.primitives.RoleType;

/**
 * This event indicates that a NetMeshObject was added to the set of neighbors of a NetMeshObject.
 * In other words, the NetMeshObject now participates in one relationship more.
 */
public class NetMeshObjectNeighborAddedEvent
        extends
            MeshObjectNeighborAddedEvent
        implements
            NetMeshObjectNeighborChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param meshObject the MeshObject that is the source of the event
     * @param roleTypes the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors
     * @param oldNeighbors the set of neighbor MeshObjects prior to the event
     * @param deltaNeighbor the neighbor MeshObject affected by this event
     * @param newNeighbors the set of neighbor MeshObjects after the event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public NetMeshObjectNeighborAddedEvent(
            NetMeshObject         meshObject,
            RoleType []           roleTypes,
            NetMeshObject []      oldNeighbors,
            NetMeshObject         deltaNeighbor,
            NetMeshObject []      newNeighbors,
            NetMeshBaseIdentifier originIdentifier,
            long                  timeEventOccurred )
    {
        this(   meshObject,
                meshObject.getIdentifier(),
                roleTypes,
                MeshTypeUtils.meshTypeIdentifiers( roleTypes ),
                oldNeighbors,
                NetMeshObjectUtils.netMeshObjectIdentifiers( oldNeighbors ),
                new NetMeshObject[] { deltaNeighbor },
                new NetMeshObjectIdentifier[] { deltaNeighbor.getIdentifier() },
                newNeighbors,
                NetMeshObjectUtils.netMeshObjectIdentifiers( newNeighbors ),
                originIdentifier,
                timeEventOccurred,
                meshObject.getMeshBase() );
    }

    /**
     * Convenience constructor.
     * 
     * @param meshObject the MeshObject that is the source of the event
     * @param roleTypes the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors
     * @param oldNeighborIdentifiers the identifiers of the neighbor MeshObjects prior to the event
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param newNeighborIdentifiers the identifiers of the neighbor MeshObjects after the event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    public NetMeshObjectNeighborAddedEvent(
            NetMeshObject              meshObject,
            RoleType []                roleTypes,
            NetMeshObjectIdentifier [] oldNeighborIdentifiers,
            NetMeshObjectIdentifier    deltaNeighborIdentifier,
            NetMeshObjectIdentifier [] newNeighborIdentifiers,
            NetMeshBaseIdentifier      originIdentifier,
            long                       timeEventOccurred )
    {
        this(   meshObject,
                meshObject.getIdentifier(),
                roleTypes,
                MeshTypeUtils.meshTypeIdentifiers( roleTypes ),
                null,
                oldNeighborIdentifiers,
                null,
                new NetMeshObjectIdentifier[] { deltaNeighborIdentifier },
                null,
                newNeighborIdentifiers,
                originIdentifier,
                timeEventOccurred,
                meshObject.getMeshBase() );
    }

    /**
     * Constructor.
     * 
     * @param meshObjectIdentifier identifier of the MeshObject that is the source of the event
     * @param roleTypeIdentifiers identifiers of the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors
     * @param oldNeighborIdentifiers the Identifiers of the neighbor MeshObjects prior to the event
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param newNeighborIdentifiers the identifiers of the neighbor MeshObjects after the event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    public NetMeshObjectNeighborAddedEvent(
            NetMeshObjectIdentifier    meshObjectIdentifier,
            MeshTypeIdentifier []      roleTypeIdentifiers,
            NetMeshObjectIdentifier [] oldNeighborIdentifiers,
            NetMeshObjectIdentifier    deltaNeighborIdentifier,
            NetMeshObjectIdentifier [] newNeighborIdentifiers,
            NetMeshBaseIdentifier      originIdentifier,
            long                       timeEventOccurred,
            NetMeshBase                resolver )
    {
        this(   null,
                meshObjectIdentifier,
                null,
                roleTypeIdentifiers,
                null,
                oldNeighborIdentifiers,
                null,
                new NetMeshObjectIdentifier[] { deltaNeighborIdentifier },
                null,
                newNeighborIdentifiers,
                originIdentifier,
                timeEventOccurred,
                resolver );
    }

    /**
     * Constructor for the case where we don't have old and new values, only the delta.
     * This perhaps should trigger some exception if it is attempted to read old or
     * new values later. (FIXME?)
     * 
     * @param meshObjectIdentifier identifier of the MeshObject that is the source of the event
     * @param roleTypeIdentifiers identifiers of the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors
     * @param deltaNeighborIdentifier the identifier of the neighbor MeshObject affected by this event
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    public NetMeshObjectNeighborAddedEvent(
            NetMeshObjectIdentifier meshObjectIdentifier,
            MeshTypeIdentifier []   roleTypeIdentifiers,
            NetMeshObjectIdentifier deltaNeighborIdentifier,
            NetMeshBaseIdentifier   originIdentifier,
            long                    timeEventOccurred,
            NetMeshBase             resolver )
    {
        this(   null,
                meshObjectIdentifier,
                null,
                roleTypeIdentifiers,
                null,
                null,
                null,
                new NetMeshObjectIdentifier[] { deltaNeighborIdentifier },
                null,
                null,
                originIdentifier,
                timeEventOccurred,
                resolver );
    }
    
    /**
     * Main constructor.
     *
     * @param meshObject the MeshObject that is the source of the event (optional)
     * @param meshObjectIdentifier identifier of the MeshObject that is the source of the event (required)
     * @param roleTypes the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors (optional)
     * @param roleTypeIdentifiers identifiers of the RoleTypes affected on the source MeshObject, with respect to the deltaNeighbors (required)
     * @param oldNeighbors the set of neighbor MeshObjects prior to the event (optional)
     * @param oldNeighborIdentifiers the identifiers of the neighbor MeshObjects prior to the event (required)
     * @param deltaNeighbors the set of neighbor MeshObjects affected by this event (optional)
     * @param deltaNeighborIdentifiers the identifiers of the neighbor MeshObjects affected by this event (required)
     * @param newNeighbors the set of neighbor MeshObjects after the event (optional)
     * @param newNeighborIdentifiers the identifiers of the neighbor MeshObjects after the event (required)
     * @param originIdentifier identifier of the NetMeshBase from where this NetChange arrived, if any
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     * @param resolver the MeshBase against which the MeshObjectIdentifiers are currently resolved, if any
     */
    protected NetMeshObjectNeighborAddedEvent(
            NetMeshObject              meshObject,
            NetMeshObjectIdentifier    meshObjectIdentifier,
            RoleType []                roleTypes,
            MeshTypeIdentifier[]       roleTypeIdentifiers,
            NetMeshObject []           oldNeighbors,
            NetMeshObjectIdentifier [] oldNeighborIdentifiers,
            NetMeshObject []           deltaNeighbors,
            NetMeshObjectIdentifier [] deltaNeighborIdentifiers,
            NetMeshObject []           newNeighbors,
            NetMeshObjectIdentifier [] newNeighborIdentifiers,
            NetMeshBaseIdentifier      originIdentifier,
            long                       timeEventOccurred,
            NetMeshBase                resolver )
    {
        super(  meshObject,
                meshObjectIdentifier,
                roleTypes,
                roleTypeIdentifiers,
                oldNeighbors,
                oldNeighborIdentifiers,
                deltaNeighbors,
                deltaNeighborIdentifiers,
                newNeighbors,
                newNeighborIdentifiers,
                timeEventOccurred,
                resolver );

        theOriginNetworkIdentifier = originIdentifier;
    }

    /**
     * Obtain the MeshObject affected by this Change.
     *
     * @return obtain the MeshObject affected by this Change
     */
    @Override
    public NetMeshObject getAffectedMeshObject()
    {
        return (NetMeshObject) super.getAffectedMeshObject();
    }

    /**
     * Obtain the MeshObjectIdentifier of the MeshObject affected by this Change.
     *
     * @return the MeshObjectIdentifier of the NetMeshObject affected by this Change
     */
    @Override
    public NetMeshObjectIdentifier getAffectedMeshObjectIdentifier()
    {
        return (NetMeshObjectIdentifier) super.getAffectedMeshObjectIdentifier();
    }

    /**
     * Obtain the neighbor that changed.
     *
     * @return the neighbor MeshObject
     */
    @Override
    public NetMeshObject getNeighborMeshObject()
    {
        return (NetMeshObject) super.getNeighborMeshObject();
    }

    /**
     * <p>Apply this NetChange to a NetMeshObject in this MeshBase. This method
     *    is intended to make it easy to replicate NetChanges that were made to a
     *    replica of one NetMeshObject in one NetMeshBase to another replica
     *    of the NetMeshObject in another NetMeshBase.</p>
     *
     * <p>This method will attempt to create a Transaction if none is present on the
     * current Thread.</p>
     *
     * @param base the NetMeshBase in which to apply the NetChange
     * @return the NetMeshObject to which the NetChange was applied
     * @throws CannotApplyChangeException thrown if the NetChange could not be applied, e.g because
     *         the affected NetMeshObject did not exist in MeshBase base
     * @throws TransactionException thrown if a Transaction didn't exist on this Thread and
     *         could not be created
     */
    public NetMeshObject applyToReplicaIn(
            NetMeshBase base )
        throws
            CannotApplyChangeException,
            TransactionException
    {
        setResolver( base );

        Transaction tx = null;

        NetMeshObject              otherObject = null; // make compiler happy
        NetMeshObjectIdentifier [] relatedOtherObjects;
        RoleType []                roleTypes;

        try {
            tx = base.createTransactionNowIfNeeded();

            otherObject         = (NetMeshObject) getSource();
            relatedOtherObjects = (NetMeshObjectIdentifier []) getDeltaValueIdentifier();
            roleTypes           = getProperty();

            for( int i=0 ; i<relatedOtherObjects.length ; ++i ) {
                otherObject.rippleRelate( relatedOtherObjects[i] );
                if( roleTypes != null && roleTypes.length > 0 ) {
                    otherObject.rippleBless( roleTypes, relatedOtherObjects[i] );
                }
            }

        } catch( TransactionException ex ) {
            throw ex;

        } catch( Throwable ex ) {
            throw new CannotApplyChangeException.ExceptionOccurred( base, ex );

        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
        return otherObject;
    }

    /**
     * Determine whether this NetChange should be forwarded through the given, outgoing Proxy.
     * If specified, {@link #getOriginNetworkIdentifier} specifies where the NetChange came from.
     *
     * @param outgoingProxy the potential outgoing Proxy
     * @return true if the NetChange should be forwarded torwards the outgoingProxy
     */
    public boolean shouldBeSent(
            Proxy outgoingProxy )
    {
        return Utils.hasReplicaInDirection( this, outgoingProxy, theOriginNetworkIdentifier );
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     *
     * @return the NetMeshBaseIdentifier, if any
     */
    public final NetMeshBaseIdentifier getOriginNetworkIdentifier()
    {
        return theOriginNetworkIdentifier;
    }

    /**
     * The NetMeshBaseIdentifier of the NetMeshBase from where this NetChange arrived.
     * This may or may not be the NetMeshBase where the Change originated, as it might be
     * passed through several NetMeshBases until it arrived here. This may be null if
     * the Change originated locally.
     */
    protected NetMeshBaseIdentifier theOriginNetworkIdentifier;
}
