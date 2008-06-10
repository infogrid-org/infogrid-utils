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

package org.infogrid.jee.viewlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.infogrid.context.Context;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.servlet.BufferedServletResponse;
import org.infogrid.jee.servlet.UnsafePostException;
import org.infogrid.jee.viewlet.templates.StructuredResponse;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.AbstractViewlet;

/**
 * Factors out commonly used functionality for JeeViewlets.
 */
public abstract class AbstractJeeViewlet
        extends
            AbstractViewlet
        implements
            JeeViewlet
{
    private static final Log log = Log.getLogInstance( AbstractJeeViewlet.class ); // our own, private loger

    /**
     * Constructor, for subclasses only.
     * 
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected AbstractJeeViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }
    
    /**
     * Obtain the Html class name for this Viewlet that will be used for the enclosing <tt>div</tt> tag.
     * By default, it is the Java class name, having replaced all periods with hyphens.
     * 
     * @return the HTML class name
     */
    public String getHtmlClass()
    {
        String ret = getClass().getName();

        ret = ret.replaceAll( "\\.", "-" );
        
        return ret;
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the GET method has been requested.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the GET execution of the servlet.</p>
     * <p>Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public void performBeforeGet(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * <p>Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public void performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was <b>not</b> safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * <p>It is strongly recommended that JeeViewlets do not regularly process the incoming
     *    POST data, as the request is likely unsafe (e.g. a Cross-Site Request Forgery).</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws UnsafePostException thrown if the unsafe POST operation was not acceptable
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public void performBeforeUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            UnsafePostException,
            ServletException
    {
        throw new UnsafePostException( request );
    }

    /**
     * <p>Invoked after to the execution of the Servlet. It is the hook by which
     * the JeeViewlet can perform whatever operations needed after to the execution of the servlet, e.g.
     * logging. Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @param thrown if this is non-null, it is the Throwable indicating a problem that occurred
     *        either during execution of performBefore or of the servlet.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     */
    public void performAfter(
            RestfulRequest     request,
            StructuredResponse response,
            Throwable          thrown )
        throws
            ServletException
    {
        // noop on this level
    }

    /**
     * Obtain the path to the Servlet for this JeeViewlet. JeeViewlet may implement this in different ways,
     * such as be returning a path to a JSP. This returns the generic path, not a localized version.
     * 
     * @return the Servlet path
     */
    public String getServletPath()
    {
        String ret = constructDefaultDispatcherUrl( getClass() );
        return ret;
    }

    /**
     * Obtain the full URI of the incoming request.
     * 
     * @return the full URI, as String
     */
    public String getRequestURI()
    {
        RestfulRequest request = theCurrentRequest;
        String         ret;

        if( request != null ) {
            ret = request.getSaneRequest().getAbsoluteFullUri();
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Process the incoming RestfulRequest. Default implementation that can be
     * overridden by subclasses.
     * 
     * @param restful the incoming RestfulRequest
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public void processRequest(
            RestfulRequest     restful,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        synchronized( this ) {
            if( theCurrentRequest != null ) {
                throw new IllegalStateException( "Have current request already: " + theCurrentRequest );
            }
            theCurrentRequest = restful;
        }
        
        try {
            String servletPath = getServletPath();

            if( servletPath != null ) {
                InfoGridWebApp app = InfoGridWebApp.getSingleton();

                RequestDispatcher dispatcher = app.findLocalizedRequestDispatcher(
                        servletPath,
                        restful.getSaneRequest().acceptLanguageIterator(),
                        structured.getServletContext() );

                if( dispatcher != null ) {
                    runRequestDispatcher( dispatcher, restful, structured );
                } // FIXME? Should there be an else here, throwing an Exception?
            }

        } finally {
            synchronized( this ) {
                theCurrentRequest = null;
            }
        }        
    }

    /**
     * Invoke the RequestDispatcher and put the results in the default section of the StructuredResponse.
     * 
     * @param dispatcher the RequestDispatcher to invoke
     * @param restful the incoming request
     * @param structured the outgoing response
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public void runRequestDispatcher(
            RequestDispatcher  dispatcher,
            RestfulRequest     restful,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        BufferedServletResponse bufferedResponse = new BufferedServletResponse( structured.getDelegate() );

        dispatcher.include( restful.getDelegate(), bufferedResponse );

        byte [] bufferedBytes  = bufferedResponse.getBufferedServletOutputStreamOutput();
        String  bufferedString = bufferedResponse.getBufferedPrintWriterOutput();

        if( bufferedBytes != null ) {
            if( bufferedString != null ) {
                // don't know what to do here -- defaults to "string gets processed, bytes ignore"
                log.warn( "Have both String and byte content, don't know what to do: " + restful );
                structured.setDefaultSectionContent( bufferedString ); // do something is better than nothing

            } else {
                structured.setDefaultSectionContent( bufferedBytes );
            }

        } else if( bufferedString != null ) {
            structured.setDefaultSectionContent( bufferedString );
        } else {
            // do nothing
        }
        structured.setMimeType( bufferedResponse.getContentType() );
    }
                

    /**
     * Obtain the URL to which forms should be HTTP POSTed. This
     * can be overridden by subclasses.
     *
     * @return the URL
     */
    public String getPostUrl()
    {
        String relativePath  = theCurrentRequest.getDelegate().getRequestURI();

        // we need to replace # -- FIXME, is this right? HTTP.encodeUrl does seem to do too much
        String ret = relativePath.replace( "#", "%23" );
        
        // append lid-xpath
        String xpath = theCurrentRequest.getDelegate().getParameter( "lid-xpath" );
        if( xpath != null ) {
            ret = HTTP.appendArgumentToUrl( ret, "lid-xpath=" + HTTP.encodeUrl( xpath ));
        }
        // append lid-format
        String format = theCurrentRequest.getDelegate().getParameter( "lid-format" );
        if( format != null ) {
            ret = HTTP.appendArgumentToUrl( ret, "lid-format=" + HTTP.encodeUrl( format ));
        }
        return ret;
    }

    /**
     * This method converts the name of a Class (subclass of this one) into the default request URL
     * for the RequestDispatcher.
     * 
     * @param viewletClassName the class name of the JeeViewlet
     * @return the JSP URL.
     */
    protected String constructDefaultDispatcherUrl(
            String viewletClassName )
    {
        StringBuilder almost = new StringBuilder();
        almost.append( "/v/" );
        almost.append( viewletClassName.replace( '.', '/' ));
        
        String mime = theCurrentRequest.getRequestedMimeType();
        if( mime != null && mime.length() > 0 ) {
            // FIXME: does not handle * parameters right now
            almost.append( "/" );
            almost.append( mime );
            almost.append( "/" );
            
            int lastDot = viewletClassName.lastIndexOf( '.' );
            if( lastDot > 0 ) {
                almost.append( viewletClassName.substring( lastDot+1 ));
            } else {
                almost.append( viewletClassName );
            }
        }
        almost.append( ".jsp" );
        return almost.toString();
    }

    /**
     * This method converts a Class (subclass of this one) into the default request URL
     * for the RequestDispatcher.
     * 
     * @param viewletClass the class of the JeeViewlet
     * @return the JSP URL.
     */
    protected String constructDefaultDispatcherUrl(
            Class viewletClass )
    {
        return constructDefaultDispatcherUrl( viewletClass.getName() );
    }

    /**
     * The request currently being processed.
     */
    protected RestfulRequest theCurrentRequest;
}
