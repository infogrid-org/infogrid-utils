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

package org.infogrid.jee.viewlet;

import java.io.IOException;
import javax.servlet.ServletException;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;

/**
 * <p>A software component of an application's JEE user interface.
 *    Conceptually, the user interface of an InfoGrid web application consists of Viewlets.
 *    These Viewlets can be supported by Servlets and/or JSPs, which can rely on the fact that a specific
 *    JeeViewlet exists for them &quot;behind the,&quot;; this makes programming much simpler.
 *    Among them, it makes it easy to deliver the same JeeViewlet functionality in multiple
 *    locales.</p>
 * <p>JEE Viewlets are somewhat comparable with Java portlets, but much simpler and built around
 *    a REST-ful, identity-aware design.</p>
 * <p>A JeeViewlet typically has a subject, which is given as a <code>MeshObject</code>. For example,
 *    a JeeViewlet showing an electronic business card might have the owner of the business card
 *    as the subject.</p>
 * <p>The <code>JeeViewlet</code> interface is supported by all InfoGrid Viewlets.</p>
 */
public interface JeeViewlet
        extends
            Viewlet
{
    /**
     * Obtain the current state of the Viewlet.
     *
     * @return the current state of the Viewlet
     */
    public JeeViewletState getViewletState();

    /**
     * Obtain all possible states of this Viewlet. This may depend on the current MeshObjectsToView
     * (e.g. whether the user may edit a MeshObject or not).
     *
     * @return the possible ViewletStates
     */
    public JeeViewletState [] getPossibleViewletStates();

    /**
     * Obtain the Html class name for this Viewlet that will be used for the enclosing <tt>div</tt> tag.
     * 
     * @return the HTML class name
     */
    public String getHtmlClass();

    /**
     * <p>Invoked prior to the execution of the Servlet if the GET method has been requested.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the GET execution of the servlet.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     * @see #performBeforeMaybeSafeOrUnsafePost
     * @see #performAfter
     */
    public void performBeforeGet(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException;

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeUnsafePost
     * @see #performBeforeMaybeSafeOrUnsafePost
     * @see #performAfter
     */
    public void performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException;

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
     * @see #performBeforeMaybeSafeOrUnsafePost
     * @see #performAfter
     */
    public void performBeforeUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            UnsafePostException,
            ServletException;

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no FormTokenService has been used.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * <p>It is strongly recommended that JeeViewlets do not regularly process the incoming
     *    POST data, as the request is likely unsafe (e.g. a Cross-Site Request Forgery).</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public void performBeforeMaybeSafeOrUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException;
    
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
            ServletException;

    /**
     * Process the incoming RestfulRequest.
     * 
     * @param request the incoming RestfulRequest
     * @param toView the MeshObjectsToView, mostly for error reporting
     * @param response the StructuredResponse into which to write the result
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if writing the output failed
     */
    public void processRequest(
            RestfulRequest     request,
            MeshObjectsToView  toView,
            StructuredResponse response )
        throws
            ServletException,
            IOException;
            
    /**
     * Obtain the URL to which forms should be HTTP POSTed.
     *
     * @return the URL
     */
    public String getPostUrl();

    /**
     * Name of the Request attribute that contains the current JeeViewlet instance.
     */
    public static final String VIEWLET_ATTRIBUTE_NAME = "Viewlet";
    
    /**
     * Name of the Request attribute that contains the REST-ful subject MeshObject.
     */
    public static final String SUBJECT_ATTRIBUTE_NAME = "Subject";

    /**
     * Name of the viewlet parameter that contains the current JeeViewletState.
     */
    public static final String VIEWLET_STATE_NAME = "ViewletState";

    /**
     * Name of the viewlet parameter that contains the desired JeeViewletStateTransition.
     */
    public static final String VIEWLET_STATE_TRANSITION_NAME = "ViewletStateTransition";
}    
