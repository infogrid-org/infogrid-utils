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

package org.infogrid.jee.taglib.meshbase;

import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.InfoGridJspUtils;
import org.infogrid.meshbase.MeshBase;

import javax.servlet.jsp.JspException;

/**
 * <p>Tag that links/hyperlinks to a NetMeshBase.</p>
 */
public class MeshBaseLinkTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public MeshBaseLinkTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshBaseName         = null;
        theRootPath             = null;
        theAddArguments         = null;
        theStringRepresentation = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshBase property.
     * 
     * @return value of the meshBase property
     * @see #setMeshBaseName
     */
    public String getMeshBaseName()
    {
        return theMeshBaseName;
    }

    /**
     * Set value of the meshBase property.
     * 
     * @param newValue new value of the meshBase property
     * @see #getMeshBaseName
     */
    public void setMeshBaseName(
            String newValue )
    {
        theMeshBaseName = newValue;
    }

    /**
     * Obtain value of the rootPath property.
     *
     * @return value of the rootPath property
     * @see #setRootPath
     */
    public String getRootPath()
    {
        return theRootPath;
    }

    /**
     * Set value of the rootPath property.
     *
     * @param newValue new value of the rootPath property
     * @see #getRootPath
     */
    public void setRootPath(
            String newValue )
    {
        theRootPath = newValue;
    }

    /**
     * Obtain value of the addArguments property.
     *
     * @return value of the addArguments property
     * @see #setAddArguments
     */
    public String getAddArguments()
    {
        return theAddArguments;
    }

    /**
     * Set value of the addArguments property.
     *
     * @param newValue new value of the addArguments property
     * @see #getAddArguments
     */
    public void setAddArguments(
            String newValue )
    {
        theAddArguments = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Do the start tag operation.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshBase mb = (MeshBase) lookupOrThrow( theMeshBaseName );
        
        String text = InfoGridJspUtils.formatMeshBaseLinkStart( pageContext, mb, theRootPath, theStringRepresentation );
        print( text );

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Do the end tag operation.
     *
     * @return evaluate or skip page
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException
    {
        MeshBase mb = (MeshBase) lookupOrThrow( theMeshBaseName );

        String text = InfoGridJspUtils.formatMeshBaseLinkEnd( pageContext, mb, theRootPath, theStringRepresentation );
        print( text );

        return EVAL_PAGE;
    }
    
    /**
     * Name of the bean that holds the MeshBase.
     */
    protected String theMeshBaseName;
    
    /**
     * The HTTP path prepended to the HREF, e.g. http://example.com/foo/bar/?obj=
     */
    protected String theRootPath;
    
    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
