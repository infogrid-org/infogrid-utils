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

package org.infogrid.jee.viewlet.meshbase.net;

import java.io.IOException;
import javax.servlet.ServletException;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.rest.net.NetRestfulRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.context.Context;
import org.infogrid.util.text.StringRepresentationParseException;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that displays one particular Proxy. This is not exactly REST-ful as
 * Proxies aren't nouns in InfoGrid, which is why we have to do a few tricks...
 */
public class ProxyViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static ProxyViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        ProxyViewlet             ret    = new ProxyViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            double matchQuality )
    {
        return new DefaultViewletFactoryChoice( ProxyViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected ProxyViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Override processing the incoming RestfulRequest.
     * 
     * @param restful the incoming RestfulRequest
     * @param toView the MeshObjectsToView, mostly for error reporting
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    @Override
    public void processRequest(
            RestfulRequest     restful,
            MeshObjectsToView  toView,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        NetRestfulRequest realRestful = (NetRestfulRequest) restful;
        
        try {
            Proxy pseudoSubject = realRestful.determineRequestedProxy();
            realRestful.getSaneRequest().setAttribute( "Proxy", pseudoSubject );

        } catch( MeshObjectAccessException ex ) {
            throw new ServletException( ex );
        } catch( NotPermittedException ex ) {
            throw new ServletException( ex );
        } catch( StringRepresentationParseException ex ) {
            throw new ServletException( ex );
        }
        
        super.processRequest( restful, toView, structured );
    }
}
