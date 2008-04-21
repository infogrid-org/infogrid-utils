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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.MeshObjectAccessException;

/**
 * Thrown if access to a remote MeshObject was not possible.
 */
public class NetMeshObjectAccessException
        extends
            MeshObjectAccessException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which the Exception occurred
     * @param partialResult a partial result, if any, available at the time the Exception occurred
     * @param failedPaths the NetMeshObjectAccessSpecifications that were used
     * @param cause the underlying cause for the Exception
     */
    public NetMeshObjectAccessException(
            NetMeshBase                         mb,
            NetMeshObject []                    partialResult,
            NetMeshObjectAccessSpecification [] failedPaths,
            Throwable                           cause )
    {
        super( mb, partialResult, NetMeshObjectAccessSpecification.identifiersOf( failedPaths ), cause );
        
        theFailedPaths = failedPaths;
    }

    /**
     * Obtain the MeshBase in which the Exception occurred.
     *
     * @return the MeshBase in which the Exception occurred
     */
    @Override
    public NetMeshBase getMeshBase()
    {
        return (NetMeshBase) super.getMeshBase();
    }

    /**
     * Obtain the partial result, if any.
     *
     * @return the partial result, if any
     */
    @Override
    public NetMeshObject [] getBestEffortResult()
    {
        return (NetMeshObject []) super.getBestEffortResult();
    }

    /**
     * Obtain the failed NetMeshObjectAccessSpecifications.
     *
     * @return the NetMeshObjectAccessSpecifications
     */
    public NetMeshObjectAccessSpecification [] getFailedAccessSpecifications()
    {
        return theFailedPaths;
    }

    /**
     * Obtain the parameters with which the internationalized string
     * will be parameterized.
     *
     * @return the parameters with which the internationalized string will be parameterized
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        String [] ret = new String[ theFailedPaths.length ];

        for( int i=0 ; i<theFailedPaths.length ; ++i ) {
            // we have to pick something that the user can understand
            NetMeshBaseAccessSpecification [] path = theFailedPaths[i].getAccessPath();
            if( path != null && path.length > 0 ) {
                ret[i] = path[0].toExternalForm();
            } else {
                ret[i] = theFailedPaths[i].getNetMeshObjectIdentifier().toExternalForm();
            }
        }
        return ret;
    }

    /**
     * The paths to the MeshObjects(s) whose access threw this Exception.
     */
    protected NetMeshObjectAccessSpecification [] theFailedPaths;
}