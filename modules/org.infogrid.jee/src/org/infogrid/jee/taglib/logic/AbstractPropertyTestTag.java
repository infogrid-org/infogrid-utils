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

package org.infogrid.jee.taglib.logic;

import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

import javax.servlet.jsp.JspException;

/**
 * <p>Abstract superclass for all tags evaluating a <code>PropertyValue</code>.</p>
 */
public abstract class AbstractPropertyTestTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractPropertyTestTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName   = null;
        thePropertyTypeName = null;
        thePropertyType     = null;
        
        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the propertyTypeName property.
     *
     * @return value of the propertyTypeName property
     * @see #setPropertyTypeName
     */
    public final String getPropertyTypeName()
    {
        return thePropertyTypeName;
    }

    /**
     * Set value of the propertyTypeName property.
     *
     * @param newValue new value of the propertyTypeName property
     * @see #getPropertyTypeName
     */
    public final void setPropertyTypeName(
            String newValue )
    {
        thePropertyTypeName = newValue;
    }
    
    /**
     * Obtain value of the propertyType property.
     *
     * @return value of the propertyType property
     * @see #setPropertyType
     */
    public final String getPropertyType()
    {
        return thePropertyType;
    }

    /**
     * Set value of the propertyType property.
     *
     * @param newValue new value of the propertyType property
     * @see #getPropertyType
     */
    public final void setPropertyType(
            String newValue )
    {
        thePropertyType = newValue;
    }

    /**
     * Do the start tag operation.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluatate the condition. If it returns true, we include, in the output,
     * the content contained in this tag. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Determine the PropertyValue to be used in the test.
     *
     * @return the PropertyValue
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected PropertyValue evaluate()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj  = (MeshObject) lookupOrThrow( theMeshObjectName );

        PropertyType  type  = null;
        PropertyValue value = null;
        
        if( thePropertyType != null ) {
            if( thePropertyTypeName != null ) {
                throw new JspException( "Must not specify both propertyTypeName and propertyType" );
            }
            
            type = findPropertyTypeOrThrow( obj, thePropertyType );

        } else if( thePropertyTypeName == null ) {
            throw new JspException( "Must specify either propertyTypeName or propertyType" );
        }

        if( value == null ) {
            if( type == null ) {
                type = (PropertyType) lookupOrThrow( thePropertyTypeName );
            }

            try {
                value = obj.getPropertyValue( type );

            } catch( Exception ex ) {
                throw new JspException( ex );
            }        
        }
        return value;
    }

    /**
     * String containing the name of the bean that is the MeshObject whose property is considered in the test.
     */
    protected String theMeshObjectName;

    /**
     * String containing the name of the bean that is the PropertyType.
     */
    protected String thePropertyTypeName;

    /**
     * Identifier of the PropertyType. This is mutually exclusive with thePropertyTypeName.
     */
    protected String thePropertyType;
}
