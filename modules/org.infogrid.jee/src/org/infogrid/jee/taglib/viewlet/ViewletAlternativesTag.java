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

package org.infogrid.jee.taglib.viewlet;

import org.infogrid.jee.taglib.InfoGridJspUtils;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeViewlet;

import org.infogrid.context.Context;
import org.infogrid.mesh.MeshObject;

import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactory;
import org.infogrid.viewlet.ViewletFactoryChoice;

import org.infogrid.util.logging.Log;
import org.infogrid.util.ResourceHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

import java.util.HashMap;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.viewlet.templates.JspStructuredResponseTemplate;
import org.infogrid.jee.viewlet.templates.StructuredResponse;
import org.infogrid.jee.viewlet.templates.TextHtmlStructuredResponseSection;

/**
 * Allows the user to select an alternate JeeViewlet to display the current subject.
 */
public class ViewletAlternativesTag
    extends
        AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( ViewletAlternativesTag.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ViewletAlternativesTag()
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
     * Process the start tag.
     *
     * @return evaluate or skip body
     * @throws JspException if a JSP exception has occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        StructuredResponse theResponse = (StructuredResponse) lookupOrThrow(
                JspStructuredResponseTemplate.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        // this needs to be simple lookup so the periods in the class name don't trigger nestedLookup
        RestfulRequest restful = (RestfulRequest) InfoGridJspUtils.simpleLookup(
                pageContext,
                RestfulRequest.class.getName(),
                getScope() );

        Viewlet currentViewlet = (Viewlet) lookupOrThrow(
                JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        
        MeshObject subject = currentViewlet.getSubject();
        Context    c       = currentViewlet.getContext();
        
        ViewletFactory factory = c.findContextObject( ViewletFactory.class );
        
        MeshObjectsToView toView = MeshObjectsToView.create( subject );

        try {
            ViewletFactoryChoice [] candidates = factory.determineFactoryChoicesOrderedByMatchQuality( toView );

            if( candidates.length > 0 ) {
                String nameInCss = getClass().getName().replace( '.', '-' );
                println( "<div class=\"" + nameInCss + "\" id=\"" + nameInCss + "\">" );
                print( "<h3><a href=\"javascript:toggle_viewlet_alternatives()\">" );
                print( theResourceHelper.getResourceString( "Title" ));
                println( "</a></h3>" );
                println( "<ul>" );

                String                 url = ((HttpServletRequest)pageContext.getRequest()).getRequestURI();
                HashMap<String,String> map = new HashMap<String,String>();
                
                for( int i=0 ; i<candidates.length ; ++i ) {
                    map.put( "lid-format", "viewlet:" + candidates[i].getName() );
                    print( "<li" );
                    if( candidates[i].getName().equals( currentViewlet.getName() )) {
                        print( " class=\"selected\"" );
                    }
                    print( ">" );
                    print( "<a href=\"" );
                    print( InfoGridJspUtils.constructHrefWithDifferentArguments( url, map ));
                    print( "\">" );
                    print( candidates[i].getUserVisibleName() );
                    print( "</a>" );
                    println( "</li>" );
                }
                println( "</ul>" );
                println( "</div>" );

                StringBuilder js = new StringBuilder();
                js.append( "<script src=\"" );
                js.append( restful.getContextPath() );
                js.append( "/v/" );
                js.append( getClass().getName().replace( '.' , '/' ));
                js.append( ".js" );
                js.append( "\" type=\"text/javascript\"></script>\n" );

                StringBuilder css = new StringBuilder();
                css.append( "<link rel=\"stylesheet\" href=\"" );
                css.append( restful.getContextPath() );
                css.append( "/v/" );
                css.append( getClass().getName().replace( '.' , '/' ));
                css.append( ".css" );
                css.append( "\" />\n" );
                
                theResponse.appendToSectionContent( TextHtmlStructuredResponseSection.HTML_HEAD_SECTION, js.toString() );
                theResponse.appendToSectionContent( TextHtmlStructuredResponseSection.HTML_HEAD_SECTION, css.toString() );
            
            }

        } catch( CannotViewException ex ) {
            log.warn( ex );
        }
        return SKIP_BODY;
    }
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( ViewletAlternativesTag.class );
}
