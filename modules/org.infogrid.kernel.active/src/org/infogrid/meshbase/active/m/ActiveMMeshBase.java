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

package org.infogrid.meshbase.active.m;

import org.infogrid.context.Context;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.set.active.ActiveMeshObjectSetFactory;
import org.infogrid.mesh.set.active.m.ActiveMMeshObjectSetFactory;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.meshbase.a.DefaultAMeshObjectIdentifierFactory;
import org.infogrid.meshbase.m.MMeshBase;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.CachingMap;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.logging.Log;

/**
  * An MMeshBase that allows active sets.
  */
public class ActiveMMeshBase
        extends
            MMeshBase
{
    private static final Log log = Log.getLogInstance( ActiveMMeshBase.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param modelBase the ModelBase with the type definitions we use
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param theHomeObjectTypes the EntityTypes with which the home object should be blessed
     * @param c the Context in which this MeshBase will run
     */
    public static ActiveMMeshBase create(
            MeshBaseIdentifier identifier,
            ModelBase          modelBase,
            AccessManager      accessMgr,
            Context            c )
    {
        MCachingHashMap<MeshObjectIdentifier,MeshObject> cache = MCachingHashMap.create();

        DefaultAMeshObjectIdentifierFactory identifierFactory = DefaultAMeshObjectIdentifierFactory.create();
        ActiveMMeshObjectSetFactory         setFactory        = ActiveMMeshObjectSetFactory.create( MeshObject.class, MeshObjectIdentifier.class );

        ActiveMMeshBase ret = new ActiveMMeshBase( identifier, identifierFactory, setFactory, modelBase, accessMgr, cache, c );
        setFactory.setMeshBase( ret );

        ret.initializeHomeObject();

        if( log.isDebugEnabled() ) {
            log.debug( "created " + ret );
        }

        return ret;
    }

    /**
     * Constructor.
     *
     * @param identifier the MeshBaseIdentifier of this MeshBase
     * @param identifierFactory the factory for MeshObjectIdentifiers appropriate for this MeshBase
     * @param setFactory the factory for MeshObjectSets appropriate for this MeshBase
     * @param modelBase the ModelBase containing type information
     * @param accessMgr the AccessManager that controls access to this MeshBase
     * @param cache the CachingMap that holds the MeshObjects in this MeshBase
     * @param context the Context in which this MeshBase runs.
      */
    protected ActiveMMeshBase(
            MeshBaseIdentifier                          identifier,
            MeshObjectIdentifierFactory                 identifierFactory,
            ActiveMeshObjectSetFactory                  setFactory,
            ModelBase                                   modelBase,
            AccessManager                               accessMgr,
            CachingMap<MeshObjectIdentifier,MeshObject> cache,
            Context                                     context )
    {
        super( identifier, identifierFactory, setFactory, modelBase, accessMgr, cache, context );
    }

    /**
     * Obtain a factory for MeshObjectSets.
     * 
     * @return the factory
     */
    @Override
    public ActiveMeshObjectSetFactory getMeshObjectSetFactory()
    {
        return (ActiveMeshObjectSetFactory) super.getMeshObjectSetFactory();
    }
}
