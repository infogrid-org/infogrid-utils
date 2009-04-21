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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.security.FormTokenService;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * Inserts the hidden input field with the token to make a safe form. This is only
 * needed for those HTML forms that are not generated with SafeFormTag.
 */
public class SafeFormHiddenInputTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public SafeFormHiddenInputTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        String toInsert = hiddenInputTagString( pageContext );
        if( toInsert != null ) {
            print( toInsert );
        }
        return EVAL_PAGE;
    }

    /**
     * Construct the String to insert into the HTML.
     *
     * @param pageContext the PageContext of the current page
     * @return the String
     */
    public static String hiddenInputTagString(
            PageContext pageContext )
    {

        String value = (String) pageContext.getAttribute( FORM_TOKEN_NAME );

        if( value == null ) {
            FormTokenService service = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( FormTokenService.class );
            if( service != null ) {
                // no service, no output
                value = service.generateNewToken();
            }
            if( value != null ) {
                pageContext.getRequest().setAttribute( FORM_TOKEN_NAME, value );
            }
        }
        if( value != null ) {
            StringBuilder ret = new StringBuilder();

            ret.append( "<input name=\"" );
            ret.append( SafeUnsafePostFilter.INPUT_FIELD_NAME );
            ret.append( "\" type=\"hidden\" value=\"" );
            ret.append( value );
            ret.append( "\" />" );
            return ret.toString();

        } else {
            return null;
        }
    }

    /**
     * Name of the buffered token in the page context.
     */
    public static final String FORM_TOKEN_NAME
            = SaneRequestUtils.classToAttributeName( SafeFormHiddenInputTag.class, "token" );
}
