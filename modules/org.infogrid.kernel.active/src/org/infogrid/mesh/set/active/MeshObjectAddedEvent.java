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

package org.infogrid.mesh.set.active;

import org.infogrid.mesh.MeshObject;
import org.infogrid.util.StringHelper;

/**
  * This indicates that a MeshObject was added to an ActiveMeshObjectSet.
  */
public class MeshObjectAddedEvent
        extends
            ActiveMeshObjectSetEvent
{
    /**
      * Constructor.
      *
      * @param _theSet the set that changed
      * @param _theAddedMeshObject the MeshObject that was added to the set
      */
    public MeshObjectAddedEvent(
            ActiveMeshObjectSet _theSet,
            MeshObject          _theAddedMeshObject )
    {
        super(_theSet);

        theAddedMeshObject = _theAddedMeshObject;
        theIndexOfAdded    = -1;
    }

    /**
      * Constructor.
      *
      * @param _theSet the set that changed,
      * @param _theAddedEntity the Entity that was added to the set
      * @param _indexOfAdded the index in the set at which it was added
      */
    public MeshObjectAddedEvent(
            ActiveMeshObjectSet _theSet,
            MeshObject          _theAddedMeshObject,
            int                 _indexOfAdded )
    {
        super(_theSet);

        theAddedMeshObject = _theAddedMeshObject;
        theIndexOfAdded    = _indexOfAdded;
    }

    /**
      * Obtain the added MeshObject.
      *
      * @return the added MeshObject
      */
    public MeshObject getAddedMeshObject()
    {
        return theAddedMeshObject;
    }

    /**
     * Obtain the index at which the MeshObject was added to the set. This applies
     * only to ordered sets.
     *
     * @return the index at which the MeshObject was added to the set.
     * @throws IllegalStateException if called for an unordered set
     */
    public int getIndexOfAdded()
    {
        if( theIndexOfAdded == -1 ) {
            throw new IllegalStateException( "unordered sets do not have indices" );
        }
        return theIndexOfAdded;
    }

    /**
     * Return in string form, for debugging.
     *
     * @return this object in string form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theAddedMeshObject"
                },
                new Object[] {
                    theAddedMeshObject
                });
    }

    /**
      * The added MeshObject.
      */
    protected MeshObject theAddedMeshObject;

    /**
     * The index at which the MeshObject was added, or -1.
     */
    protected int theIndexOfAdded;
}
