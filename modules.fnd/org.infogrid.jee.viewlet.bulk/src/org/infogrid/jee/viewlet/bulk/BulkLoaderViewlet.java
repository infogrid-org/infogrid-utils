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

package org.infogrid.jee.viewlet.bulk;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.infogrid.util.context.Context;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.mesh.externalized.ExternalizedMeshObject;
import org.infogrid.mesh.externalized.xml.BulkExternalizedMeshObjectXmlEncoder;
import org.infogrid.meshbase.BulkLoadException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewedMeshObjects;

/**
 * This Viewlet allows the user to bulk-load data into a MeshBase.
 */
public class BulkLoaderViewlet
        extends
            SimpleJeeViewlet
{
    private static final Log log = Log.getLogInstance( BulkLoaderViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created Viewlet
     */
    public static BulkLoaderViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        BulkLoaderViewlet        ret    = new BulkLoaderViewlet( viewed, c );

        viewed.setViewlet( ret );
        return ret;
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected BulkLoaderViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }
    
    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public void performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        performPost( request, response );
    }
    
    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no FormTokenService has been used.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public void performBeforeMaybeSafeOrUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        performPost( request, response );
    }

    /**
     * Perform the post operation.
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    protected void performPost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        SaneRequest theSaneRequest = (SaneRequest) request.getDelegate().getAttribute( SaneServletRequest.SANE_SERVLET_REQUEST_ATTRIBUTE_NAME );
        
        String bulkXml = theSaneRequest.getPostArgument( LOAD_CONTENT_ARGUMENT_NAME );

        MeshBase    base = getSubject().getMeshBase();
        Transaction tx   = null;
        try {
            tx = base.createTransactionAsapIfNeeded();

            InputStream inStream = new ByteArrayInputStream( bulkXml.getBytes( "UTF-8" ));
            
            BulkExternalizedMeshObjectXmlEncoder theParser = new BulkExternalizedMeshObjectXmlEncoder();

            Iterator<? extends ExternalizedMeshObject> iter = theParser.bulkLoad(
                    inStream,
                    base );

            while( iter.hasNext() ) {
                base.getMeshBaseLifecycleManager().loadExternalizedMeshObject( iter.next() );
            }
            
        } catch( BulkLoadException ex ) {
            theBulkXml = bulkXml;
            throw new ServletException( ex );

        } catch( TransactionException ex ) {
            log.error( ex );
            theBulkXml = null;

        } catch( Exception ex ) {
            log.error( ex );
            theBulkXml = bulkXml;
            
        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }

    /**
     * Obtaint the XML to show.
     *
     * @return the XML to show
     */
    public String getBulkXml()
    {
        return theBulkXml;
    }
    
    /**
     * The bulk XML to show.
     */
    protected String theBulkXml;
    
    /**
     * Name of the HTTP Post argument that contains the XML to load.
     */
    public static final String LOAD_CONTENT_ARGUMENT_NAME = "bulkXml";
}
