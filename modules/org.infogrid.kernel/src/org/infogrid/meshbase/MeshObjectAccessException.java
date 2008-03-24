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

package org.infogrid.meshbase;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.PartialResultException;

/**
 * Thrown if something went wrong when trying to access a MeshObject. The underlying
 * cause may indicate what went wrong.
 */
public class MeshObjectAccessException
        extends
            AbstractLocalizedException
        implements
            PartialResultException<MeshObject[]>
{
    /**
     * Constructor.
     *
     * @param mb the MeshBase in which the Exception occurred
     * @param partialResult a partial result, if any, available at the time the Exception occurred
     * @param failedPaths the access path that was used
     * @param cause the underlying cause for the Exception
     */
    public MeshObjectAccessException(
            MeshBase                mb,
            MeshObject []           partialResult,
            MeshObjectIdentifier [] failedPaths,
            Throwable               cause )
    {
        super( cause );

        theMeshBase          = mb;
        thePartialResult     = partialResult;
        theFailedIdentifiers = failedPaths;
    }

    /**
     * Obtain the MeshBase in which the Exception occurred.
     *
     * @return the MeshBase in which the Exception occurred
     */
    public MeshBase getMeshBase()
    {
        return theMeshBase;
    }

    /**
     * Determine whether a partial result is available.
     *
     * @return true if a partial result is available
     */
    public boolean isPartialResultAvailable()
    {
        return thePartialResult != null;
    }

    /**
     * Obtain the partial result, if any.
     *
     * @return the partial result, if any
     */
    public MeshObject [] getBestEffortResult()
    {
        return thePartialResult;
    }

    /**
     * Convert to string form, for debugging.
     *
     * @return string form of this object, for debugging
     */
    @Override
    public String toString()
    {
        StringBuilder almostRet = new StringBuilder( super.toString() );
        almostRet.append( "{ mb: " );
        almostRet.append( theMeshBase );
        almostRet.append( ", cause: " );
        almostRet.append( getCause() );
        almostRet.append( " }" );
        return almostRet.toString();
    }

    /**
     * Obtain the parameters with which the internationalized string
     * will be parameterized.
     *
     * @return the parameters with which the internationalized string will be parameterized
     */
    public Object [] getLocalizationParameters()
    {
        String [] ret = new String[ theFailedIdentifiers.length ];

        for( int i=0 ; i<theFailedIdentifiers.length ; ++i ) {
            ret[i] = theFailedIdentifiers[0].toExternalForm();
        }
        return ret;
    }

    /**
     * The MeshBase in which this Exception occurred.
     */
    protected transient MeshBase theMeshBase;

    /**
     * The partial result (if any).
     */
    protected transient MeshObject [] thePartialResult;

    /**
     * The identifiers of the MeshObjects whose access failed.
     */
    protected MeshObjectIdentifier [] theFailedIdentifiers;
}