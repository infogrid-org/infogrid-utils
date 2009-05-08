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

package org.infogrid.meshworld;

import java.util.ArrayList;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.DefaultJspViewlet;
import org.infogrid.jee.viewlet.blob.BlobViewlet;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactoryChoice;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * ViewletFactory for the MeshWorld application's iframe.
 */
public class IframeMeshWorldViewletFactory
        extends
            AbstractViewletFactory
{
    private static final Log log = Log.getLogInstance( IframeMeshWorldViewletFactory.class ); // our own, private logger

    /**
     * Constructor.
     */
    public IframeMeshWorldViewletFactory()
    {
        super( JeeViewlet.class.getName() );
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView theObjectsToView )
    {
        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();

        MeshObject subject = theObjectsToView.getSubject();
        if( subject.isBlessedBy( WikiSubjectArea.WIKIOBJECT )) {
            ret.add( DefaultJspViewlet.choice( "org.infogrid.jee.viewlet.wikiobject.WikiObjectDisplayViewlet", ViewletFactoryChoice.GOOD_MATCH_QUALITY ));
            ret.add( DefaultJspViewlet.choice( "org.infogrid.jee.viewlet.wikiobject.WikiObjectEditViewlet", ViewletFactoryChoice.GOOD_MATCH_QUALITY+1.0f ));
        }
        for( PropertyType type : subject.getAllPropertyTypes()) {
            if( type.getDataType() instanceof BlobDataType ) {
                try {
                    BlobValue value = (BlobValue) subject.getPropertyValue( type );
                    if( value != null && BlobDataType.theJdkSupportedBitmapType.isAllowedMimeType( value.getMimeType() )) {
                        ret.add( BlobViewlet.choice( ViewletFactoryChoice.BAD_MATCH_QUALITY ));
                        break;
                    }
                } catch( IllegalPropertyTypeException ex ) {
                    log.error( ex );
                } catch( NotPermittedException ex ) {
                    // ignore: then we'll do without this Viewlet
                }
            }
        }
        ret.add( DefaultJspViewlet.choice( "org.infogrid.jee.viewlet.propertysheet.PropertySheetViewlet",     ViewletFactoryChoice.BAD_MATCH_QUALITY ));

        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }
}
