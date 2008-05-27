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

package org.infogrid.jee.servlet;

import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>Filter that makes sure InfoGrid initialization has been performed prior to processing
 *    requests.</p>
 * <p>The following Filter parameters are available in the <code>web.xml</code> file:</p>
 * <table class="infogrid-border">
 *  <thead>
 *   <tr>
 *    <td>Parameter Name</td>
 *    <td>Description</td>
 *    <td>Required?</td>
 *   </tr>
 *  </thead>
 *  <tbody>
 *   <tr>
 *    <td><code>org.infogrid.jee.app.InfoGridWebApp</code></td>
 *    <td>Filter parameter specifying the name of the class to use as the
 *        {@link org.infogrid.jee.app.InfoGridWebApp InfoGridWebApp}.
 *        This class must have a static factory method with the signature
 *        <code>public InfoGridWebApp create( String )</code>, where the String
 *        parameter is the name of the <code>DataSource</code> in the JNDI directory.</td>
 *    <td>Required</td>
 *   </tr>
 *   <tr>
 *    <td><code>DefaultMeshBaseIdentifier</code></td>
 *    <td>Filter parameter specifying the MeshBaseIdentifier of the default MeshBase in the
 *        application.</td>
 *    <td>Required</td>
 *   </tr>
 *  </tbody>
 * </table>
 */
public class InitializationFilter
        implements
            Filter
{
    private static Log log; // initialized later

    /**
     * Constructor.
     */
    public InitializationFilter()
    {
        // noop
    }

    /**
     * Perform the main filter method.
     * 
     * @param request The incoming servlet request
     * @param response The outgoing servlet response
     * @param chain The filter chain to which this Filter belongs
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        HttpServletRequest  realRequest  = (HttpServletRequest)  request;
        HttpServletResponse realResponse = (HttpServletResponse) response;

        Throwable thrown = null;
        try {
            initializeInfoGridWebApp();

            SaneRequest lidRequest = SaneServletRequest.create( realRequest );
            // doing both doesn't hurt, and there are arguments in favor of both
            realRequest.setAttribute( SaneRequest.class.getName(), lidRequest );
            realRequest.setAttribute( SaneServletRequest.class.getName(), lidRequest );
        
            request.setAttribute( CONTEXT_PARAMETER, realRequest.getContextPath() );

            StringBuilder fullContext = new StringBuilder();
            fullContext.append( lidRequest.getProtocol() ).append( "://" );
            fullContext.append( lidRequest.getHttpHost() );
            fullContext.append( realRequest.getContextPath() );

            request.setAttribute( FULLCONTEXT_PARAMETER, fullContext.toString() );

            chain.doFilter( request, response );

        } catch( Throwable t ) {
            thrown = t;

        } finally {
            // this funny construct does not make us re-throw the exception

            if( thrown != null ) {
                if( log == null ) {
                    log = Log.getLogInstance( InitializationFilter.class );
                }
                log.error( thrown );
                if( thrown instanceof ServletException ) {
                    log.error( "root cause: ", ((ServletException)thrown).getRootCause() );
                }

                // these three lines of code are for the debugger's benefit only
                StringWriter w = new StringWriter();
                thrown.printStackTrace( new PrintWriter( w ));
                String error = w.getBuffer().toString();

                realResponse.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            }
        }
    }
    
    /**
     * Initialize the InfoGridWebApp if needed.
     *
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected void initializeInfoGridWebApp()
        throws
            ServletException
    {
        // the Exception trick gets around us having to do a synchronized
        InfoGridWebApp theApp = InfoGridWebApp.getSingleton();
        if( theApp == null ) {
            String className = theFilterConfig.getInitParameter( INFOGRID_WEB_APP_CLASS_NAME_PARAMETER );
            if( className == null || className.length() == 0 ) {
                throw new ServletException( "Cannot initialize InfoGridWebApp: no " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " parameter given in web.xml file" );
            }

            try {                
                Class  appClass      = Class.forName( className );
                Method factoryMethod = appClass.getMethod( "create", String.class );

                theApp = (InfoGridWebApp) factoryMethod.invoke( null, theDefaultMeshBaseIdentifier );

            } catch( ClassNotFoundException ex ) {
                throw new ServletException( "Cannot find class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex  );

            } catch( NoSuchMethodException ex ) {
                throw new ServletException( "Cannot find method \"create\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

            } catch( IllegalAccessException ex ) {
                throw new ServletException( "Cannot access method \"create\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

            } catch( InvocationTargetException ex ) {
                throw new ServletException( "Cannot execute method \"create\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );
            }

            try {
                InfoGridWebApp.setSingleton( theApp );

            } catch( IllegalStateException ex ) {
                // have one already, that's fine (a parallel thread was faster)
            }
                
        }
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
    }
    
    /**
     * Initialization method for this filter.
     * 
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig = filterConfig;
        
        theDefaultMeshBaseIdentifier = theFilterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER );
        if( theDefaultMeshBaseIdentifier == null || theDefaultMeshBaseIdentifier.length() == 0 ) {
            throw new ServletException( getClass().getName() + "'s configuration required the " + DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER + " filter parameter" );
        }
    }

    /**
     * Name of the Filter parameter that contains the name of the InfoGridWebApp (sub-)class to use.
     */
    public static final String INFOGRID_WEB_APP_CLASS_NAME_PARAMETER = InfoGridWebApp.class.getName();

    /**
     * Name of the String in the RequestContext that contains the identifier of the default
     * MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER = "DefaultMeshBaseIdentifier";

    /**
     * Name of the String in the RequestContext that is the context path of the application.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path from the root of the current host, not including the host.
     * @see FULLCONTEXT_PARAMETER
     */
    public static final String CONTEXT_PARAMETER = "CONTEXT";
    
    /**
     * Name of the String in the RequestContext that is the contextPath of the application.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path including protocol, host and port.
     * @see CONTEXT_PARAMETER
     */
    public static final String FULLCONTEXT_PARAMETER = "FULLCONTEXT";
    
    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
    
    /**
     * The default MeshBaseIdentifier, in String form,
     */
    protected String theDefaultMeshBaseIdentifier;
}
