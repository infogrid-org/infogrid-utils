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

package org.infogrid.mesh.a;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.a.DefaultAMeshObjectIdentifierFactory;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringRepresentation;

/**
 * Implements MeshObjectIdentifier for the in-memory MeshBase.
 */
public class DefaultAMeshObjectIdentifier
        implements
            MeshObjectIdentifier
{
    /**
     * Factory method.
     *
     * @param localId the localId of the to-be-created ReferenceValue
     * @return the created ReferenceValue
     * @throws IllegalArgumentException thrown if a non-null localId contains a period.
     */
    public static DefaultAMeshObjectIdentifier create(
            String localId )
    {
        if( localId == null || localId.length() == 0 ) {
            return DefaultAMeshObjectIdentifierFactory.HOME_OBJECT;
        }

        return new DefaultAMeshObjectIdentifier( localId );
    }

    /**
     * Private constructor.
     * 
     * @param localId the localId of the to-be-created MeshObjectIdentifier
     */
    protected DefaultAMeshObjectIdentifier(
            String localId )
    {
        theLocalId = localId;
    }

    /**
     * Obtain the localId component.
     *
     * @return the localId component
     */
    public String getLocalId()
    {
        return theLocalId;
    }

    /**
     * Determine whether this MeshObjectIdentifier identifies a Home Object.
     *
     * @return true if it identifies a Home Object
     */
    public boolean identifiesHomeObject()
    {
        return toExternalForm().length() == 0;
    }

    /**
      * Determine hashCode.
      *
      * For speed reasons, we only use the localId. This is probably good enough.
      *
      * @return the hash code
      */
    @Override
    public final int hashCode()
    {
        return toExternalForm().hashCode();
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return true if the objects are equal
      */
    @Override
    public final boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof MeshObjectIdentifier ) {
            MeshObjectIdentifier realValue = (MeshObjectIdentifier) otherValue;
            
            if( !toExternalForm().equals( realValue.toExternalForm() )) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        return "&" + toExternalForm();
    }

    /**
     * Obtain an external form for this ReferenceValue, similar to
     * URL's getExternalForm(). This returns an empty String for local home objects.
     *
     * @return external form of this ReferenceValue
     */
    public String toExternalForm()
    {
        if( theLocalId != null ) {
            return theLocalId;
        } else {
            return "";
        }
    }

    /**
     * Re-construct a ReferenceValue from an external form.
     *
     * @param raw the external form of the ReferenceValue
     * @return the created ReferenceValue
     */
    public static MeshObjectIdentifier fromExternalForm(
            String raw )
    {
        if( raw == null ) {
            return null;
        }
        return DefaultAMeshObjectIdentifier.create( raw );
    }

    /**
     * Convert this PropertyValue to its String representation, using the representation scheme.
     *
     * @param representation the representation scheme
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation representation )
    {
        String externalForm = toExternalForm();

        String key;
        if( identifiesHomeObject() ) {
            key = HOME_DEFAULT_ENTRY;
        } else {
            key = DEFAULT_ENTRY;
        }

        String ret = representation.formatEntry(
                ResourceHelper.getInstance( getClass() ), // dispatch to the right subtype
                key,
                externalForm );

        return ret;
    }

    /**
     * The real value for the localId.
     */
    protected String theLocalId;

    /**
     * The default entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "String";

    /**
     * The home entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String HOME_DEFAULT_ENTRY = "HomeString";

    /**
     * Our ResourceHelper.
     */
    public static final ResourceHelper RESOURCEHELPER = ResourceHelper.getInstance( DefaultAMeshObjectIdentifier.class );
}
