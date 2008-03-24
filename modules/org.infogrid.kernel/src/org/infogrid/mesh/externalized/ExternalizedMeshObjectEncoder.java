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

package org.infogrid.mesh.externalized;

import org.infogrid.model.primitives.externalized.DecodingException;
import org.infogrid.model.primitives.externalized.EncodingException;

import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This interface is supported by classes that know how to serialize and deserialize
 * ExternalizedMeshObject.
 */
public interface ExternalizedMeshObjectEncoder
{
    /**
     * Serialize an ExternalizedMeshObject to an OutputStream.
     * 
     * @param obj the input ExternalizedMeshObject
     * @param out the OutputStream to which to append the ExternalizedMeshObject
     * @throws EncodingException thrown if a problem occurred during encoding
     */
    public void encodeExternalizedMeshObject(
            ExternalizedMeshObject obj,
            OutputStream           out )
        throws
            EncodingException,
            IOException;

    /**
     * Deserialize an ExternalizedMeshObject from a byte stream.
     *
     * @param s the InputStream from which to read
     * @param life the MeshBaseLifecycleManager appropriate to create an appropriate ExternalizedMeshObject
     * @return return the just-instantiated AMeshObject, as convenience
     * @throws DecodingException thrown if a problem occurred during decoding
     */
    public ExternalizedMeshObject decodeExternalizedMeshObject(
            InputStream                                 s,
            ParserFriendlyExternalizedMeshObjectFactory externalizedMeshObjectFactory,
            MeshObjectIdentifierFactory                 meshObjectIdentifierFactory,
            MeshTypeIdentifierFactory                   meshTypeIdentifierFactory )
        throws
            DecodingException,
            IOException;

    /**
     * Obtain an encodingId that reflects this ExternalizedMeshObjectEncoder.
     * 
     * @return the encodingId.
     */
    public String getEncodingId();
}
