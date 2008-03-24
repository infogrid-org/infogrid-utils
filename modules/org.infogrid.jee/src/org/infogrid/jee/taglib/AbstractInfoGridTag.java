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

package org.infogrid.jee.taglib;

import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import java.io.IOException;

/**
 * <p>Factors out common functionality for regular Tags. Also redefines the JEE Tag
 * API, which really makes no sense whatsoever.</p>
 *
 * <p>Support for the following attributes is provided to all subclasses:</p>
 * <table class="infogrid-border">
 *  <thead>
 *   <tr>
 *    <th>Attribute</th>
 *    <th>Meaning</th>
 *    <th>Mandatory?</th>
 *   </tr>
 *  </thead>
 *  <tbody>
 *   <tr>
 *    <td><code>filter</code></td>
 *    <td>Filter output for characters that are sensitive in HTML.</td>
 *    <td>optional</td>
 *   </tr>
 *   <tr>
 *    <td><code>ignore</code></td>
 *    <td>If true, ignore missing beans and simply output nothing. Otherwise, throw a <code>JspException</code></td>
 *    <td>optional</td>
 *   </tr>
 *   <tr>
 *    <td><code>scope</code></td>
 *    <td>The scope (page, request, session, application) in which beans are being looked up</td>
 *    <td>optional</td>
 *   </tr>
 *  </tbody>
 * </table>
 *
 * <p>If you change this file, you MUST also change AbstractInfoGridBodyTag, which largely
 * defines the same methods with the same code.</p>
 */
public abstract class AbstractInfoGridTag
    extends
        TagSupport
{
    /**
     * This has a constructor to invoke initializeToDefaults()
     */
    protected AbstractInfoGridTag()
    {
        initializeToDefaults(); // may invoke subclass invocation
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    protected void initializeToDefaults()
    {
        theFilter = null;
        theIgnore = null;
        theScope  = null; // means "search"
    }

    /**
     * Obtain value of the filter property.
     *
     * @return value of the filter property
     * @see #setFilter
     */
    public final String getFilter()
    {
        return theFilter;
    }

    /**
     * Set value of the filter property.
     *
     * @param newValue new value of the filter property
     * @see #getFilter
     */
    public final void setFilter(
            String newValue )
    {
        theFilter = newValue;
    }

    /**
     * Obtain value of the ignore property.
     *
     * @return value of the ignore property
     * @see #setIgnore
     */
    public final String getIgnore()
    {
        return theIgnore;
    }

    /**
     * Set value of the ignore property.
     *
     * @param newValue new value of the ignore property
     * @see #getIgnore
     */
    public final void setIgnore(
            String newValue )
    {
        theIgnore = newValue;
    }

    /**
     * Obtain value of the scope property.
     *
     * @return value of the scope property
     * @see #setScope
     */
    public final String getScope()
    {
        return theScope;
    }

    /**
     * Set value of the scope property.
     *
     * @param newValue new value of the scope property
     * @see #getScope
     */
    public final void setScope(
            String newValue )
    {
        theScope = newValue;
    }

    /**
     * Do the start tag operation. This method is final; subclasses implement the
     * {@link #realDoStartTag realDoStartTag} method.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public final int doStartTag()
        throws
            JspException
    {
        try {
            int ret = realDoStartTag();
            
            return ret;
            
        } catch( IgnoreException ex ) {
            return SKIP_BODY;

        } catch( IOException ex ) {
            throw new JspException( "Exception while processing " + getClass().getName() + ".doStartTag", ex );
        }
    }

    /**
     * Our implementation of doStartTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected abstract int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException;

    /**
     * Do the end tag operation. This method is final; subclasses implement the
     * {@link #realDoEndTag realDoEndTag} method.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public final int doEndTag()
        throws
            JspException
    {
        try {
            int ret = realDoEndTag();
            
            return ret;
            
        } catch( IgnoreException ex ) {
            return EVAL_PAGE;

        } catch( IOException ex ) {
            throw new JspException( "Exception while processing " + getClass().getName() + ".doEndTag", ex );
        }
    }

    /**
     * Our implementation of doEndTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        return EVAL_PAGE; // reasonable default
    }

    /**
     * Format a PropertyValue.
     *
     * @param value the PropertValue
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @return the String to display
     */
    protected final String formatValue(
            PageContext   pageContext,
            PropertyValue value,
            String        nullString,
            String        stringRepresentation )
    {
        return InfoGridJspUtils.formatPropertyValue( pageContext, value, nullString, stringRepresentation );
    }

    /**
     * Look up a bean in the scope given by the scope attribute.
     *
     * @param name name of the bean
     * @return the bean
     * @throws JspException thrown if a processing error occurred
     */
    protected final Object lookup(
            String name )
        throws
            JspException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }
        Object ret = InfoGridJspUtils.nestedLookup( pageContext, name, theScope );
        return ret;
    }

    /**
     * Look up a bean in the scope given by the scope attribute, and
     * throw JspException if not found.
     *
     * @param name name of the bean
     * @return the bean
     * @throws JspException if the bean was not found
     */
    protected final Object lookupOrThrow(
            String name )
        throws
            JspException,
            IgnoreException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }

        Object ret;
        if( InfoGridJspUtils.isTrue( theIgnore )) {
            ret = InfoGridJspUtils.nestedLookupOrThrow( pageContext, name, theScope );
        } else {
            ret = InfoGridJspUtils.nestedLookup( pageContext, name, theScope );
        }
        return ret;
    }

    /**
     * Look up a bean in the specified scope, and throw JspException if not found.
     *
     * @param name name of the bean
     * @return the bean
     * @throws JspException if the bean was not found
     */
    protected final Object lookupOrThrow(
            String name,
            String propertyName )
        throws
            JspException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }
        
        Object ret;
        if( InfoGridJspUtils.isTrue( theIgnore )) {
            ret = InfoGridJspUtils.nestedLookupOrThrow( pageContext, name, propertyName, theScope );
        } else {
            ret = InfoGridJspUtils.nestedLookup( pageContext, name, propertyName, theScope );
        }
        return ret;
    }

    /**
     * Find a PropertyType, or throw an Exception. This will consider the
     * EntityTypes that the MeshObject is currently blessed with, and look for
     * a PropertyType with the given name.
     *
     * @param obj the MeshObject
     * @param name name of the property
     */
    protected PropertyType findPropertyTypeOrThrow(
            MeshObject obj,
            String     name )
        throws
            JspException
    {
        return InfoGridJspUtils.findPropertyTypeOrThrow( obj, name );
    }

    /**
     * Print out some text.
     *
     * @param text the text
     * @throws JspException
     */
    protected final void print(
            String text )
        throws
            JspException
    {
        InfoGridJspUtils.print( pageContext, InfoGridJspUtils.isTrue( theFilter ), text );
    }

    /**
     * Print out some text, followed by a line feed.
     *
     * @param text the text
     * @throws JspException
     */
    protected final void println(
            String text )
        throws
            JspException
    {
        InfoGridJspUtils.println( pageContext, InfoGridJspUtils.isTrue( theFilter ), text );
    }

    /**
     * May be overridden by subclasses.
     */
    protected void internalRelease()
    {
        // noop on this level
    }

    /**
     * Release resources.
     */
    @Override
    public final void release()
    {
        internalRelease();
        initializeToDefaults();
    }

    /**
     * Filter the rendered output for characters that are sensitive in HTML?
     */
    private String theFilter;

    /**
     * Should we ignore missing beans and simply output nothing?
     */
    private String theIgnore;

    /**
     * The scope to be searched to retrieve beans.
     */
    private String theScope;
}
