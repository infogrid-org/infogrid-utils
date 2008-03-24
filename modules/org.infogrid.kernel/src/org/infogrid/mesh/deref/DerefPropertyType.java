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

package org.infogrid.mesh.deref;

import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;

import org.infogrid.meshbase.transaction.TransactionException;

import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

/**
  * This is a pair of a MeshObject and a PropertyType,
  * representing a property of this MeshObject.
  */
public final class DerefPropertyType
        extends
            DerefPropertyTypeOrGroup
{
    private static final Log log = Log.getLogInstance( DerefPropertyType.class); // our own, private logger

    /**
      * Construct one. This constructor does not (and should not, due to possible blessing later)
      * check whether this MeshObject actually carries the specified PropertyType.
      *
      * @param mo the MeshObject
      * @param pt the PropertyType
      */
    public DerefPropertyType(
            MeshObject   mo,
            PropertyType pt )
    {
        super( mo );
        thePropertyType = pt;
    }

    /**
      * Obtain the PropertyType.
      *
      * @return the PropertyType
      */
    public PropertyType getPropertyType()
    {
        return thePropertyType;
    }

    /**
      * Obtain the value of the property for this MeshObject.
      *
      * @return the value of the property for this MeshObject
      * @throws NotPermittedException thrown if the caller did not have sufficient access rights
      * @throws IllegalPropertyTypeException thrown if the MeshObject does not currently carry the PropertyType
      * @see #setValue
      */
    public PropertyValue getValue()
       throws
            NotPermittedException,
            IllegalPropertyTypeException
    {
        return theMeshObject.getPropertyValue( thePropertyType );
    }

    /**
     * Set the value of the property for this MeshObject. This will
     * log an error to the logger if the PropertyType is not carried by the
     * MeshObject.
     * 
     * @param newValue the new property value on this MeshObject
     * @throws NotPermittedException thrown if the caller did not have sufficient access rights
     * @throws IllegalPropertyValueException thrown if the specified value is an illegal value
     *         for this property
     * @throws IllegalPropertyTypeException thrown if the MeshObject does not currently carry the PropertyType
     * @throws TransactionException thrown if this method is not invoked between proper
     *         Transaction boundaries
     * @see #getValue
     */
    public void setValue(
            PropertyValue newValue )
        throws
            NotPermittedException,
            IllegalPropertyValueException,
            IllegalPropertyTypeException,
            TransactionException
    {
        theMeshObject.setPropertyValue( thePropertyType, newValue );
    }

    /**
     * This is a convenience function that sets the value of the property
     * to a reasonable default. The reasonable default is obtained from the
     * PropertyType, and if not present, from the underlying DataType.
     * This is particularly useful when a default value is needed when the
     * property was previously null for this MeshObject.
     * 
     * @throws NotPermittedException thrown if the caller did not have sufficient access rights
     * @throws TransactionException thrown if this method is not invoked between
     *         proper Transaction boundaries
     */
    public void setDefaultValue()
        throws
            NotPermittedException,
            TransactionException
    {
        try {
            PropertyValue defaultValue = thePropertyType.getDefaultValue();
            if( defaultValue == null ) {
                defaultValue = thePropertyType.getDataType().instantiate();
            }
            setValue( defaultValue );

        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );
        }
    }

    /**
      * Determine equality.
      *
      * @param other the object to check against
      * @return true if the two objects are equal
      */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof DerefPropertyType ) {
            DerefPropertyType realOther = (DerefPropertyType) other;

            return    theMeshObject.equals( realOther.theMeshObject )
                   && thePropertyType.equals( realOther.thePropertyType );
        }
        return false;
    }

    /**
     * Convert to string, for debugging only.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theMeshObject",
                    "thePropertyType"
                },
                new Object[] {
                    theMeshObject,
                    thePropertyType
                });
    }

    /**
      * The PropertyType.
      */
    protected PropertyType thePropertyType;
}
