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

import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.util.IdentifierFactory;
import org.infogrid.util.text.StringRepresentation;

import java.net.URISyntaxException;

/**
 * Factory for MeshObjectIdentifiers.
 */
public interface MeshObjectIdentifierFactory
        extends
             IdentifierFactory
{
    /**
     * Determine the Identifier of the Home Object.
     *
     * @return the Identifier
     */
    public abstract MeshObjectIdentifier getHomeMeshObjectIdentifier();

    /**
     * Create a unique Identifier for a MeshObject that can be used to create a MeshObject
     * with the associated MeshBaseLifecycleManager.
     *
     * @return the created Identifier
     */
    public abstract MeshObjectIdentifier createMeshObjectIdentifier();

    /**
     * Create a MeshObjectIdentifier from an external form.
     *
     * @param raw the external form
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public MeshObjectIdentifier fromExternalForm(
            String raw )
        throws
            URISyntaxException;

    /**
     * Convert this StringRepresentation back to an Identifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshObjectIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public MeshObjectIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException;
}
