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

package org.infogrid.jee.viewlet.blob;

import java.io.IOException;
import javax.servlet.ServletException;
import org.infogrid.jee.templates.BinaryStructuredResponseSection;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that displays one image. The image is held by a Property of the subject MeshObject.
 * The particular property can be specified as a URL argument. If none is specified, the
 * Viewlet will pick the first BlobValue property.
 */
public class BlobViewlet
        extends
            AbstractJeeViewlet
        implements
            JeeViewlet
{
    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static BlobViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects( mb );
        BlobViewlet              ret    = new BlobViewlet( viewed, c );

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
        return new DefaultViewletFactoryChoice( BlobViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( toView.getMeshBase(), c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected BlobViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Process the incoming RestfulRequest. Default implementation that can be
     * overridden by subclasses.
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
        MeshObject subject = theViewedMeshObjects.getSubject();
        
        String       propTypeName = restful.getSaneRequest().getArgument( PROPERTY_TYPE_PAR );
        PropertyType propType     = null;

        if( propTypeName != null ) {
            ModelBase mb = subject.getMeshBase().getModelBase();
            
            try {
                propType = mb.findPropertyTypeByIdentifier( mb.getMeshTypeIdentifierFactory().fromExternalForm( propTypeName ));

            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                throw new ServletException( ex );
            }

        } else {
            for( EntityType type1 : subject.getTypes()) {
                for( PropertyType type2 : type1.getAllPropertyTypes()) {
                    if( type2.getDataType() instanceof BlobDataType ) {
                        propType = type2;
                        break;
                    }
                }
                if( propType != null ) {
                    break;
                }
            }
        }

        BlobValue content = null;
        try {
            content = (BlobValue) subject.getPropertyValue( propType );

        } catch( IllegalPropertyTypeException ex ) {
            throw new ServletException( ex );
        } catch( NotPermittedException ex ) {
            throw new ServletException( ex );
        }

        if( content != null ) {
            BinaryStructuredResponseSection section = structured.getDefaultBinarySection();
            section.setContent( content.value() );
            section.setMimeType( content.getMimeType() );
        }
    }

    /**
     * Name of the URL parameter that identifies the PropertyType to show.
     */
    public static final String PROPERTY_TYPE_PAR = "propertytype";
}
