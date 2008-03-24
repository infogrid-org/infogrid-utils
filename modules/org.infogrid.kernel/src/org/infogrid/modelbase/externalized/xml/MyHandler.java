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

package org.infogrid.modelbase.externalized.xml;

import org.infogrid.mesh.set.ByTypeMeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSelector;

import org.infogrid.model.primitives.AttributableMeshType;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.primitives.BooleanDataType;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.ColorDataType;
import org.infogrid.model.primitives.ColorValue;
import org.infogrid.model.primitives.DataType;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.EnumeratedDataType;
import org.infogrid.model.primitives.ExtentDataType;
import org.infogrid.model.primitives.ExtentValue;
import org.infogrid.model.primitives.FloatDataType;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerDataType;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.L10Map;
import org.infogrid.model.primitives.L10MapImpl;
import org.infogrid.model.primitives.MultiplicityDataType;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.model.primitives.PointDataType;
import org.infogrid.model.primitives.PointValue;
import org.infogrid.model.primitives.PropertyTypeGroup;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RelationshipType;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.primitives.StringDataType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.SubjectArea;
import org.infogrid.model.primitives.TimePeriodDataType;
import org.infogrid.model.primitives.TimePeriodValue;
import org.infogrid.model.primitives.TimeStampDataType;
import org.infogrid.model.primitives.TimeStampValue;

import org.infogrid.model.traversal.SelectiveTraversalSpecification;
import org.infogrid.model.traversal.SequentialCompoundTraversalSpecification;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalToPropertySpecification;

import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.externalized.xml.PropertyValueXmlEncoder;

import org.infogrid.modelbase.MeshTypeLifecycleManager;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ProjectedPropertyTypePatcher;

import org.infogrid.modelbase.externalized.ExternalizedAttributableMeshType;
import org.infogrid.modelbase.externalized.ExternalizedAttributes;
import org.infogrid.modelbase.externalized.ExternalizedEntityType;
import org.infogrid.modelbase.externalized.ExternalizedEnum;
import org.infogrid.modelbase.externalized.ExternalizedMeshObjectSelector;
import org.infogrid.modelbase.externalized.ExternalizedModuleRequirement;
import org.infogrid.modelbase.externalized.ExternalizedProjectedPropertyType;
import org.infogrid.modelbase.externalized.ExternalizedPropertyType;
import org.infogrid.modelbase.externalized.ExternalizedPropertyTypeGroup;
import org.infogrid.modelbase.externalized.ExternalizedRelationshipType;
import org.infogrid.modelbase.externalized.ExternalizedRoleType;
import org.infogrid.modelbase.externalized.ExternalizedSubjectArea;
import org.infogrid.modelbase.externalized.ExternalizedSubjectAreaDependency;
import org.infogrid.modelbase.externalized.ExternalizedTraversalSpecification;
import org.infogrid.modelbase.externalized.ExternalizedTraversalToPropertySpecification;

import org.infogrid.module.ModuleRequirement;

import org.infogrid.util.logging.Log;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * This is the handler for the SAX callbacks during model XML parsing.
 */
public class MyHandler
        extends
            PropertyValueXmlEncoder
{
    private static final Log log = Log.getLogInstance(MyHandler.class); // our own, private logger

    /**
      * Constructor.
      *
      * @param inst the MeshTypeLifecycleManager to use to instantiate the model
      * @param modelBase the ModelBase into which to instantiate the memodel
      * @param catalogClassLoader the ClassLoader for the XML catalog
      * @param errorPrefix a prefix for error messages
      */
    public MyHandler(
            MeshTypeLifecycleManager inst,
            ModelBase                modelBase,
            ClassLoader              catalogClassLoader,
            String                   errorPrefix )
    {
        theInstantiator       = inst;
        theModelBase          = modelBase;
        theCatalogClassLoader = catalogClassLoader;
        theErrorPrefix        = errorPrefix;
    }

    /**
     * This causes us to instantiate the MeshTypes that we have read and buffered.
     *
     * @param theClassLoader the ClassLoader to use
     * @return the instantiated SubjectAreas
     * @throws MeshTypeNotFoundException thrown if a required MeshType could not be found
     */
    public SubjectArea [] instantiateExternalizedObjects(
            ClassLoader theClassLoader )
        throws
            MeshTypeNotFoundException
    {
        SubjectArea [] ret = new SubjectArea[ theSubjectAreas.size() ];

        for( int i=theSubjectAreas.size()-1 ; i>=0 ; --i ) {
            ret[i] = instantiateSubjectArea( theSubjectAreas.get(i), theClassLoader );
        }
        return ret;
    }

    /**
     * SAX says a new element starts.
     *
     * @param namespaceURI URI of the namespace
     * @param localName the local name
     * @param qName the qName
     * @param attrs the Attributes at this element
     */
    @Override
    public void startElement(
            String     namespaceURI,
            String     localName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".startElement( " + namespaceURI + ", " + localName + ", " + qName + " ...)" );
        }
        
        theCharacters = null;

        try {
            int token = XmlModelTokens.getTokenFromKeyword( namespaceURI, qName );

            ExternalizedSubjectArea            theSubjectArea;
            ExternalizedEntityType             theEntityType;
            ExternalizedRelationshipType       theRelationshipType;
            ExternalizedRoleType               theRoleType;
            ExternalizedPropertyType           thePropertyType;
            ExternalizedPropertyTypeGroup      thePropertyTypeGroup;
            ExternalizedProjectedPropertyType  theProjectedPropertyType;
            ExternalizedEnum                   theEnum;

            Object temp;

            switch( token ) {

                case XmlModelTokens.MODEL_TOKEN:
                    break;
                case XmlModelTokens.SUBJECT_AREA_TOKEN:
                    theSubjectArea = new ExternalizedSubjectArea();
                    theSubjectArea.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    theStack.push( theSubjectArea );
                    break;
                case XmlModelTokens.DEPENDSON_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.SUBJECT_AREA_REFERENCE_TOKEN:
                    theStack.push( new ExternalizedSubjectAreaDependency());
                    break;
                case XmlModelTokens.MODULE_REFERENCE_TOKEN:
                    theStack.push( new ExternalizedModuleRequirement());
                    break;
                case XmlModelTokens.ENTITY_TYPE_TOKEN:
                    theSubjectArea = (ExternalizedSubjectArea) theStack.peek();
                    theEntityType = new ExternalizedEntityType();
                    theEntityType.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    theSubjectArea.addEntityType( theEntityType );
                    theStack.push( theEntityType );
                    break;
                case XmlModelTokens.RELATIONSHIP_TYPE_TOKEN:
                    theSubjectArea = (ExternalizedSubjectArea) theStack.peek();
                    theRelationshipType = new ExternalizedRelationshipType();
                    theRelationshipType.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    theSubjectArea.addRelationshipType( theRelationshipType );
                    theStack.push( theRelationshipType );
                    break;
                case XmlModelTokens.PROPERTY_TYPE_TOKEN:
                    temp = theStack.peek();
                    thePropertyType = new ExternalizedPropertyType();
                    thePropertyType.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addPropertyType( thePropertyType );
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.addPropertyType( thePropertyType );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    theStack.push( thePropertyType );
                    break;
                case XmlModelTokens.PROJECTED_PROPERTY_TYPE_TOKEN:
                    temp = theStack.peek();
                    theProjectedPropertyType = new ExternalizedProjectedPropertyType();
                    theProjectedPropertyType.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addProjectedPropertyType( theProjectedPropertyType );
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.addProjectedPropertyType( theProjectedPropertyType );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    theStack.push( theProjectedPropertyType );
                    break;
                case XmlModelTokens.PROPERTY_TYPE_GROUP_TOKEN:
                    temp = theStack.peek();
                    thePropertyTypeGroup = new ExternalizedPropertyTypeGroup();
                    thePropertyTypeGroup.setIdentifier( createTypeIdentifierFrom( attrs.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ) ));
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addPropertyTypeGroup( thePropertyTypeGroup );
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.addPropertyTypeGroup( thePropertyTypeGroup );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    theStack.push( thePropertyTypeGroup );
                    break;
                case XmlModelTokens.PROPERTY_TYPE_GROUP_MEMBER_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.INPUT_PROPERTY_SPECIFICATION_TOKEN:
                    theStack.push( new ExternalizedTraversalToPropertySpecification());
                    break;
                case XmlModelTokens.TRAVERSAL_SPECIFICATION_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.SEQUENTIAL_TRAVERSAL_SPECIFICATION_TOKEN:
                    theStack.push( new ExternalizedTraversalSpecification.Sequential() );
                    break;
                case XmlModelTokens.SELECTIVE_TRAVERSAL_SPECIFICATION_TOKEN:
                    theStack.push( new ExternalizedTraversalSpecification.Selective() );
                    break;
                case XmlModelTokens.MESH_OBJECT_SELECTOR_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.BY_TYPE_MESH_OBJECT_SELECTOR_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.PROPERTY_TYPE_REFERENCE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.ROLE_TYPE_REFERENCE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.NAME_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.MINVERSION_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.VERSION_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.USERNAME_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.USERDESCRIPTION_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.SUPERTYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.OVERRIDE_CODE_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.PROJECTION_CODE_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.IS_ABSTRACT_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.MAYBE_USED_AS_FORWARD_REFERENCE:
                    // noop
                    break;
                case XmlModelTokens.IS_OPTIONAL_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.IS_READONLY_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.IS_SIGNIFICANT_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.CODEGEN_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.DATATYPE_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.DEFAULT_VALUE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.SOURCE_TOKEN:
                    theRelationshipType     = (ExternalizedRelationshipType) theStack.peek();
                    theRoleType             = new ExternalizedRoleType();
                    theRelationshipType.setSource( theRoleType );
                    theStack.push( theRoleType );
                    break;
                case XmlModelTokens.DESTINATION_TOKEN:
                    theRelationshipType      = (ExternalizedRelationshipType) theStack.peek();
                    theRoleType              = new ExternalizedRoleType();
                    theRelationshipType.setDestination( theRoleType );
                    theStack.push( theRoleType );
                    break;
                case XmlModelTokens.SOURCE_DESTINATION_TOKEN:
                    theRelationshipType         = (ExternalizedRelationshipType) theStack.peek();
                    theRoleType                 = new ExternalizedRoleType();
                    theRelationshipType.setSourceDestination( theRoleType );
                    theStack.push( theRoleType );
                    break;
                case XmlModelTokens.ENTITY_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.REFINES_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.TOP_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.TO_OVERRIDE_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.GUARD_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.SEQUENCE_NUMBER_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.ICON_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.BLOB_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.BOOLEAN_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.COLOR_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.ENUMERATED_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    theStack.push( new ArrayList() ); // we put our domain elements into there
                    break;
                case XmlModelTokens.EXTENT_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.FLOAT_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.FLOAT_MATRIX_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.INTEGER_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.MULTIPLICITY_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.POINT_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.STRING_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.TIME_PERIOD_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.TIME_STAMP_DATATYPE_TOKEN:
                    theStack.push( new ExternalizedAttributes( attrs ));
                    break;
                case XmlModelTokens.MULTIPLICITY_VALUE_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.ENUM_TOKEN:
                    theStack.push( new ExternalizedEnum());
                    break;
                case XmlModelTokens.DECLARES_METHOD_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.IMPLEMENTS_METHOD_TOKEN:
                    // noop
                    break;

                default:
                    if( namespaceURI == null || namespaceURI.length() == 0 ) {
                        error( "Unknown token: <" + qName + ">" );
                    } else {
                        error( "Unknown token: <" + namespaceURI + ":" + qName + ">" );
                    }
            }
        } catch( RuntimeException ex ) {
            error( ex );
        }
    }

    /**
     * SAX says an element ends.
     *
     * @param namespaceURI the URI of the namespace
     * @param localName the local name
     * @param qName the qName
     */
    @Override
    public void endElement(
            String namespaceURI,
            String localName,
            String qName )
        throws
            SAXException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".endElement( " + namespaceURI + ", " + localName + ", " + qName + " )" );
        }
        try {
            int token = XmlModelTokens.getTokenFromKeyword( namespaceURI, qName );

            ExternalizedSubjectArea                      theSubjectArea;
            ExternalizedSubjectAreaDependency            theSubjectAreaDependency;
            ExternalizedModuleRequirement                 theModuleRequirement;
            ExternalizedEntityType                       theEntityType;
            ExternalizedRelationshipType                 theRelationshipType;
            ExternalizedRoleType                         theRoleType;
            ExternalizedPropertyType                     thePropertyType;
            ExternalizedPropertyTypeGroup                thePropertyTypeGroup;
            ExternalizedProjectedPropertyType            theProjectedPropertyType;
            ExternalizedAttributes                       theAttributes;
            ExternalizedEnum                             theEnum;
            ExternalizedTraversalToPropertySpecification theTraversalToPropertySpecification;
            ExternalizedTraversalSpecification           theTraversalSpecification;
            ExternalizedMeshObjectSelector               theMeshObjectSelector;

            ArrayList<Object> theCollection;
            DataType          theDataType;
            Object            temp;

            switch( token ) {
                case XmlModelTokens.MODEL_TOKEN:
                    break;
                case XmlModelTokens.SUBJECT_AREA_TOKEN:
                    theSubjectArea = (ExternalizedSubjectArea) theStack.pop();
                    theSubjectAreas.add( theSubjectArea );
                    break;
                case XmlModelTokens.DEPENDSON_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.SUBJECT_AREA_REFERENCE_TOKEN:
                    temp = theStack.pop();
                    if( temp instanceof ExternalizedSubjectAreaDependency ) {
                        theSubjectAreaDependency = (ExternalizedSubjectAreaDependency) temp;
                    } else {
                        theSubjectAreaDependency = (ExternalizedSubjectAreaDependency) theStack.pop();
                    }
                    theSubjectArea           = (ExternalizedSubjectArea) theStack.peek();
                    theSubjectArea.addSubjectAreaDependency( theSubjectAreaDependency );
                    break;
                case XmlModelTokens.MODULE_REFERENCE_TOKEN:
                    temp = theStack.pop();
                    if( temp instanceof ExternalizedModuleRequirement ) { // this is an ugly hack, but somehow it produces empty whitespace chars here
                        theModuleRequirement = (ExternalizedModuleRequirement) temp;
                    } else {
                        theModuleRequirement = (ExternalizedModuleRequirement) theStack.pop();
                    }
                    theSubjectArea       = (ExternalizedSubjectArea) theStack.peek();
                    theSubjectArea.addModuleRequirement( theModuleRequirement );
                    break;
                case XmlModelTokens.ENTITY_TYPE_TOKEN:
                    theEntityType  = (ExternalizedEntityType) theStack.pop();
                    break;
                case XmlModelTokens.RELATIONSHIP_TYPE_TOKEN:
                    theRelationshipType = (ExternalizedRelationshipType) theStack.pop();
                    break;
                case XmlModelTokens.PROPERTY_TYPE_TOKEN:
                    thePropertyType = (ExternalizedPropertyType) theStack.pop();
                    break;
                case XmlModelTokens.PROJECTED_PROPERTY_TYPE_TOKEN:
                    theProjectedPropertyType = (ExternalizedProjectedPropertyType) theStack.pop();
                    break;
                case XmlModelTokens.PROPERTY_TYPE_GROUP_TOKEN:
                    thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) theStack.pop();
                    break;
                case XmlModelTokens.PROPERTY_TYPE_GROUP_MEMBER_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        thePropertyTypeGroup.addGroupMember( theAttributes.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.INPUT_PROPERTY_SPECIFICATION_TOKEN:
                    theTraversalToPropertySpecification = (ExternalizedTraversalToPropertySpecification) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedProjectedPropertyType ) {
                        theProjectedPropertyType = (ExternalizedProjectedPropertyType) temp;
                        theProjectedPropertyType.addTraversalToPropertySpecification( theTraversalToPropertySpecification );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.TRAVERSAL_SPECIFICATION_TOKEN:
                    theTraversalSpecification = (ExternalizedTraversalSpecification) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedTraversalSpecification.Selective ) {
                        ((ExternalizedTraversalSpecification.Selective)temp).setTraversalSpecification( theTraversalSpecification );
                    } else if( temp instanceof ExternalizedTraversalSpecification.Sequential ) {
                        ((ExternalizedTraversalSpecification.Sequential)temp).addStep( theTraversalSpecification );
                    } else if( temp instanceof ExternalizedTraversalToPropertySpecification ) {
                        ((ExternalizedTraversalToPropertySpecification)temp).setTraversalSpecification( theTraversalSpecification );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.SEQUENTIAL_TRAVERSAL_SPECIFICATION_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.SELECTIVE_TRAVERSAL_SPECIFICATION_TOKEN:
                    // noop
                    break;
                case XmlModelTokens.MESH_OBJECT_SELECTOR_TOKEN: // FIXME: does not deal with startSelector
                    theMeshObjectSelector = (ExternalizedMeshObjectSelector) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedTraversalSpecification.Selective ) {
                        ((ExternalizedTraversalSpecification.Selective)temp).setEndSelector( theMeshObjectSelector );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.BY_TYPE_MESH_OBJECT_SELECTOR_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theMeshObjectSelector = new ExternalizedMeshObjectSelector.ByType();
                    ((ExternalizedMeshObjectSelector.ByType)theMeshObjectSelector).identifier       = createTypeIdentifierFrom( theAttributes.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ));
                    ((ExternalizedMeshObjectSelector.ByType)theMeshObjectSelector).subtypesAllowed = "true".equalsIgnoreCase( theAttributes.getValue( XmlModelTokens.SUBTYPE_ALLOWED_KEYWORD ));
                    theStack.push( theMeshObjectSelector );
                    break;
                case XmlModelTokens.PROPERTY_TYPE_REFERENCE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedTraversalToPropertySpecification ) {
                        ((ExternalizedTraversalToPropertySpecification)temp).addPropertyType( createTypeIdentifierFrom( theAttributes.getValue( XmlModelTokens.IDENTIFIER_KEYWORD )));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.ROLE_TYPE_REFERENCE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theTraversalSpecification = new ExternalizedTraversalSpecification.Role();
                    ((ExternalizedTraversalSpecification.Role)theTraversalSpecification).setIdentifierString( theAttributes.getValue( XmlModelTokens.IDENTIFIER_KEYWORD ));
                    theStack.push( theTraversalSpecification );
                    break;
                case XmlModelTokens.NAME_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedSubjectArea ) {
                        theSubjectArea = (ExternalizedSubjectArea) temp;
                        theSubjectArea.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        thePropertyTypeGroup.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedEnum ) {
                        theEnum = (ExternalizedEnum) temp;
                        theEnum.setValue( theCharacters.toString());
                    } else if( temp instanceof ExternalizedSubjectAreaDependency )  {
                        theSubjectAreaDependency = (ExternalizedSubjectAreaDependency) temp;
                        theSubjectAreaDependency.setName( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedModuleRequirement )  {
                        theModuleRequirement = (ExternalizedModuleRequirement) temp;
                        theModuleRequirement.setName( createStringValueFrom( theCharacters ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.MINVERSION_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedSubjectAreaDependency ) {
                        theSubjectAreaDependency = (ExternalizedSubjectAreaDependency) temp;
                        theSubjectAreaDependency.setMinVersion( createStringValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedModuleRequirement ) {
                        theModuleRequirement = (ExternalizedModuleRequirement) temp;
                        theModuleRequirement.setMinVersion( createStringValueFrom( theCharacters ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.VERSION_TOKEN:
                    temp  = theStack.peek();
                    if( temp instanceof ExternalizedSubjectArea ) {
                        theSubjectArea = (ExternalizedSubjectArea) temp;
                        theSubjectArea.setVersion( createStringValueFrom( theCharacters ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.USERNAME_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp          = theStack.peek();
                    if( temp instanceof ExternalizedSubjectArea ) {
                        theSubjectArea = (ExternalizedSubjectArea) temp;
                        theSubjectArea.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        thePropertyTypeGroup.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedRoleType ) {
                        theRoleType = (ExternalizedRoleType) temp;
                        theRoleType.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else if( temp instanceof ExternalizedEnum ) {
                        theEnum = (ExternalizedEnum) temp;
                        theEnum.addUserName( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createStringValueFrom( theCharacters.toString() ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.USERDESCRIPTION_TOKEN:
                    theAttributes   = (ExternalizedAttributes) theStack.pop();

                    temp = theStack.peek();
                    if( temp instanceof ExternalizedSubjectArea ) {
                        theSubjectArea = (ExternalizedSubjectArea) temp;
                        theSubjectArea.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedPropertyType )  {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        thePropertyTypeGroup.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedRoleType ) {
                        theRoleType = (ExternalizedRoleType) temp;
                        theRoleType.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else if( temp instanceof ExternalizedEnum ) {
                        theEnum = (ExternalizedEnum) temp;
                        theEnum.addUserDescription( theAttributes.getValue( XmlModelTokens.LOCALE_KEYWORD ), createBlobValueFrom( theCharacters.toString(), BlobValue.TEXT_HTML_MIME_TYPE ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.SUPERTYPE_TOKEN:
                    MeshTypeIdentifier v = createTypeIdentifierFrom( theCharacters.toString() );
                    theStack.pop(); // the ExternalizedAttributes -- ignored because we have a string identifying the supertype
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addSuperType( v );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.OVERRIDE_CODE_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.setRawOverrideCode( theCharacters.toString());
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.PROJECTION_CODE_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedProjectedPropertyType ) {
                        theProjectedPropertyType = (ExternalizedProjectedPropertyType) temp;
                        theProjectedPropertyType.setRawProjectionCode( theCharacters.toString());
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.IS_ABSTRACT_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.setIsAbstract( BooleanValue.TRUE );
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        theRelationshipType.setIsAbstract( BooleanValue.TRUE );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.MAYBE_USED_AS_FORWARD_REFERENCE:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.setMayBeUsedAsForwardReference( BooleanValue.TRUE );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.IS_OPTIONAL_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.setIsOptional( BooleanValue.TRUE );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.IS_READONLY_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.setIsReadOnly( BooleanValue.TRUE );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.IS_SIGNIFICANT_TOKEN:
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.setIsSignificant( BooleanValue.TRUE );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.CODEGEN_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedSubjectArea ) {
                        theSubjectArea = (ExternalizedSubjectArea) temp;
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.INTERFACE_KEYWORD ))) {
                            theSubjectArea.setGenerateInterface( BooleanValue.FALSE );
                        }
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.IMPLEMENTATION_KEYWORD ))) {
                            theSubjectArea.setGenerateImplementation( BooleanValue.FALSE );
                        }
                    } else if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.INTERFACE_KEYWORD ))) {
                            theEntityType.setGenerateInterface( BooleanValue.FALSE );
                        }
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.IMPLEMENTATION_KEYWORD ))) {
                            theEntityType.setGenerateImplementation( BooleanValue.FALSE );
                        }
                    } else if( temp instanceof ExternalizedRelationshipType ) {
                        theRelationshipType = (ExternalizedRelationshipType) temp;
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.INTERFACE_KEYWORD ))) {
                            theRelationshipType.setGenerateInterface( BooleanValue.FALSE );
                        }
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.IMPLEMENTATION_KEYWORD ))) {
                            theRelationshipType.setGenerateImplementation( BooleanValue.FALSE );
                        }
                    } else if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.INTERFACE_KEYWORD ))) {
                            thePropertyType.setGenerateInterface( BooleanValue.FALSE );
                        }
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.IMPLEMENTATION_KEYWORD ))) {
                            thePropertyType.setGenerateImplementation( BooleanValue.FALSE ); 
                        }
                    } else if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.INTERFACE_KEYWORD ))) {
                            thePropertyTypeGroup.setGenerateInterface( BooleanValue.FALSE );
                        }
                        if( "false".equals( theAttributes.getValue( XmlModelTokens.IMPLEMENTATION_KEYWORD ))) {
                            thePropertyTypeGroup.setGenerateImplementation( BooleanValue.FALSE );
                        }
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.DATATYPE_TOKEN:
                    theDataType = (DataType) theStack.pop();
                    temp        = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.setDataType( theDataType );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.DEFAULT_VALUE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp          = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        if( theAttributes.getValue( XmlModelTokens.CODE_KEYWORD ) != null ) {
                            thePropertyType.setDefaultValueCode( BlobValue.create( theCharacters.toString(), BlobValue.TEXT_PLAIN_MIME_TYPE ));
                        } else {
                            thePropertyType.setDefaultValue( constructDefaultValue( theCharacters.toString(), thePropertyType ));
                        }
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.SOURCE_TOKEN:
                    theRoleType        = (ExternalizedRoleType) theStack.pop();
                    break;
                case XmlModelTokens.DESTINATION_TOKEN:
                    theRoleType        = (ExternalizedRoleType) theStack.pop();
                    break;
                case XmlModelTokens.SOURCE_DESTINATION_TOKEN:
                    theRoleType        = (ExternalizedRoleType) theStack.pop();
                    break;
                case XmlModelTokens.ENTITY_TOKEN:
                    theRoleType        = (ExternalizedRoleType) theStack.peek();
                    theRoleType.setEntityType( createTypeIdentifierFrom( theCharacters ));
                    break;
                case XmlModelTokens.REFINES_TOKEN:
                    theRoleType        = (ExternalizedRoleType) theStack.peek();
                    theRoleType.addRefines( theCharacters.toString() );
                    break;
                case XmlModelTokens.TOP_TOKEN:
 log.error( "unexpected TOP -- thought we got rid of it" );
                    theRoleType        = (ExternalizedRoleType) theStack.peek();
                    // theRoleType.top    = createTypeIdentifierFrom( theCharacters );
                    break;
                case XmlModelTokens.TO_OVERRIDE_TOKEN:
                    temp               = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.addToOverride( theCharacters.toString() );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.GUARD_TOKEN:
                    temp            = theStack.peek();
                    if( temp instanceof ExternalizedRoleType ) {
                        theRoleType = (ExternalizedRoleType) temp;
                        theRoleType.addRoleTypeGuardClassName( theCharacters.toString() );
                    } else if( temp instanceof ExternalizedEntityType ) {
                        theEntityType = (ExternalizedEntityType) temp;
                        theEntityType.addLocalEntityTypeGuardClassName( theCharacters.toString() );
                    } else if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.addLocalPropertyTypeGuardClassName( theCharacters.toString() );
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.SEQUENCE_NUMBER_TOKEN:
                    temp            = theStack.peek();
                    if( temp instanceof ExternalizedPropertyType ) {
                        thePropertyType = (ExternalizedPropertyType) temp;
                        thePropertyType.setSequenceNumber( createFloatValueFrom( theCharacters ));
                    } else if( temp instanceof ExternalizedPropertyTypeGroup ) {
                        thePropertyTypeGroup = (ExternalizedPropertyTypeGroup) temp;
                        thePropertyTypeGroup.setSequenceNumber( createFloatValueFrom( theCharacters ));
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.ICON_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    temp = theStack.peek();
                    if( temp instanceof ExternalizedEntityType ) {
                        // use file extension to determine mime type
                        String path = theAttributes.getValue( XmlModelTokens.PATH_KEYWORD );
                        int lastPeriod = path.lastIndexOf( '.' );
                        if( lastPeriod > 0 ) {
                            theEntityType = (ExternalizedEntityType) temp;
                            theEntityType.setRelativeIconPath( path );
                            theEntityType.setIconMimeType( "image/" + path.substring( lastPeriod+1 ));
                        }
                    } else {
                        error( theErrorPrefix + "unexpected type: " + temp );
                    }
                    break;
                case XmlModelTokens.BLOB_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( determineDataType( BlobDataType.class, theAttributes, XmlModelTokens.DEFAULT_KEYWORD, BlobDataType.theDefault ));
                    break;
                case XmlModelTokens.BOOLEAN_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( BooleanDataType.theDefault );
                    break;
                case XmlModelTokens.COLOR_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( ColorDataType.theDefault );
                    break;
                case XmlModelTokens.ENUMERATED_DATATYPE_TOKEN:
                    theCollection = toArrayList(             theStack.pop());
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    if( !theCollection.isEmpty() ) {
                        String [] domain           = new String[ theCollection.size() ];
                        L10Map [] userNames        = new L10Map[ domain.length ];
                        L10Map [] userDescriptions = new L10Map[ domain.length ];

                        for( int i=0 ; i<domain.length ; ++i ) {
                            theEnum = (ExternalizedEnum) theCollection.get( i );
                            domain[i]           = theEnum.getValue();
                            userNames[i]        = L10MapImpl.create( theEnum.getUserNames(),        StringValue.create( theEnum.getValue() ) );
                            userDescriptions[i] = L10MapImpl.create( theEnum.getUserDescriptions(), null );
                        }
                        theDataType = EnumeratedDataType.create( domain, userNames, userDescriptions, EnumeratedDataType.theDefault );
                    } else {
                        theDataType = EnumeratedDataType.theDefault; // should not really happen
                    }
                    theStack.push( theDataType );
                    break;
                case XmlModelTokens.EXTENT_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( ExtentDataType.theDefault );
                    break;
                case XmlModelTokens.FLOAT_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( determineDataType( FloatDataType.class, theAttributes, XmlModelTokens.DEFAULT_KEYWORD, FloatDataType.theDefault ));
                    break;
                case XmlModelTokens.INTEGER_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( determineDataType( IntegerDataType.class, theAttributes, XmlModelTokens.DEFAULT_KEYWORD, IntegerDataType.theDefault ));
                    break;
                case XmlModelTokens.MULTIPLICITY_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( MultiplicityDataType.theDefault );
                    break;
                case XmlModelTokens.POINT_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( PointDataType.theDefault );
                    break;
                case XmlModelTokens.STRING_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( StringDataType.theDefault );
                    break;
                case XmlModelTokens.TIME_PERIOD_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( TimePeriodDataType.theDefault );
                    break;
                case XmlModelTokens.TIME_STAMP_DATATYPE_TOKEN:
                    theAttributes = (ExternalizedAttributes) theStack.pop();
                    theStack.push( TimeStampDataType.theDefault );
                    break;
                case XmlModelTokens.MULTIPLICITY_VALUE_TOKEN:
                    theRoleType      = (ExternalizedRoleType) theStack.peek();
                    theRoleType.setMultiplicity( (MultiplicityValue) createMultiplicityValueFrom( theCharacters ));
                    break;
                case XmlModelTokens.ENUM_TOKEN:
                    theEnum          = (ExternalizedEnum)  theStack.pop();
                    theCollection    = toArrayList( theStack.peek() );
                    theCollection.add( theEnum );
                    break;
                case XmlModelTokens.DECLARES_METHOD_TOKEN:
                    theEntityType   = (ExternalizedEntityType) theStack.peek();
                    theEntityType.addDeclaredMethod( theCharacters.toString() );
                    break;
                case XmlModelTokens.IMPLEMENTS_METHOD_TOKEN:
                    theEntityType   = (ExternalizedEntityType) theStack.peek();
                    theEntityType.addImplementedMethod( theCharacters.toString() );
                    break;

                default:
                    if( namespaceURI == null || namespaceURI.length() == 0 ) {
                        error( "Unknown token: </" + qName + ">" );
                    } else {
                        error( "Unknown token: </" + namespaceURI + ":" + qName + ">" );
                    }
            }
        } catch( RuntimeException ex ) {
           error( ex );
        }
    }

    /**
     * We need this helper method to isolate a non-safe cast.
     *
     * @param o the Object to cast
     * @return the same Object, cast 
     */
    @SuppressWarnings("unchecked")
    private static final ArrayList<Object> toArrayList(
            Object o )
    {
        return (ArrayList<Object>) o;
    }            

    /**
      * SAX wants us to resolve an XML entity.
      *
      * @param publicId the public identifier of the entity
      * @param systemId the system identifier of the entity
      * @return an InputSource to the XML entity, or null if it could not be resolved
      */
    @Override
    public InputSource resolveEntity(
            String publicId,
            String systemId )
    {
        if( theCatalogClassLoader != null && XmlModelTokens.PUBLIC_ID_DTD.equals( publicId )) {
            InputStream modelStream = theCatalogClassLoader.getResourceAsStream( XmlModelTokens.LOCAL_MODEL_DTD_PATH );

            if( modelStream != null ) {
                return new InputSource( modelStream );
            }
            log.error( theErrorPrefix + "Cannot find DTD at " + XmlModelTokens.LOCAL_MODEL_DTD_PATH );
        }
        return null; // default behavior
    }

    /**
     * Create a MeshTypeIdentifier from a String.
     * 
     * @param raw the String
     * @return the created MeshTypeIdentifier
     */
    protected MeshTypeIdentifier createTypeIdentifierFrom(
            CharSequence raw )
    {
        if( raw == null ) {
            return null;
        }
        MeshTypeIdentifier ret = theModelBase.getMeshTypeIdentifierFactory().fromExternalForm( raw.toString() );
        return ret;
    }

    /**
     * Create a StringValue from a String.
     *
     * @param raw the String
     * @return the created StringValue
     */
    protected StringValue createStringValueFrom(
            String raw )
    {
        return StringValue.create( raw );
    }

    /**
     * Create a StringValue from a StringBuffer.
     *
     * @param raw the StringBuffer
     * @return the created StringValue
     */
    protected StringValue createStringValueFrom(
            StringBuffer raw )
    {
        return StringValue.create( raw.toString().trim() );
    }

    /**
     * Create a FloatValue from a String.
     *
     * @param raw the String
     * @return the created FloatValue
     */
    protected FloatValue createFloatValueFrom(
            String raw )
    {
        double d = Double.parseDouble( raw );
        return FloatValue.create( d );
    }

    /**
     * Create a FloatValue from a StringBuffer.
     *
     * @param raw the StringBuffer
     * @return the created FloatValue
     */
    protected FloatValue createFloatValueFrom(
            StringBuffer raw )
    {
        return createFloatValueFrom( raw.toString() );
    }

    /**
     * Create a BlobValue from a String.
     *
     * @param raw the String
     * @param mime the MIME type for the BlobValue
     * @return the created BlobValue
     */
    protected BlobValue createBlobValueFrom(
            String raw,
            String mime )
    {
        if( raw == null || raw.length() == 0 ) {
            return null;
        }
        return BlobValue.create( raw, mime );
    }

    /**
     * Create a MultiplicityValue from a String.
     *
     * @param raw the String
     * @return the created MultiplicityValue
     */
    protected MultiplicityValue createMultiplicityValueFrom(
            String raw )
    {
        int colon = raw.indexOf( ':' );
        String minString = raw.substring( 0, colon );
        String maxString = raw.substring( colon+1 );

        int min;
        int max;

        if( "N".equalsIgnoreCase( minString )) {
            min = MultiplicityValue.N;
        } else {
            min = Integer.parseInt( raw.substring( 0, colon ));
        }

        if( "N".equalsIgnoreCase( maxString )) {
            max = MultiplicityValue.N;
        } else {
            max = Integer.parseInt( raw.substring( colon+1 ));
        }
        return MultiplicityValue.create( min, max );
    }

    /**
     * Create a MultiplicityValue from a StringBuffer.
     *
     * @param raw the StringBuffer
     * @return the created MultiplicityValue
     */
    protected MultiplicityValue createMultiplicityValueFrom(
            StringBuffer raw )
    {
        return createMultiplicityValueFrom( raw.toString() );
    }

    /**
      * Construct a PropertyValue as a default value given this String
      * representation and what we know about the PropertyType at this time.
      *
      * @param raw the String
      * @param ma the ExternalizedPropertyType
      * @return the created PropertyValue
      */
    protected PropertyValue constructDefaultValue(
            String                   raw,
            ExternalizedPropertyType pt )
    {
        PropertyValue ret;
        DataType      type = pt.getDataType();

        if( type instanceof BlobDataType ) {
            ret = BlobValue.create( raw );

        } else if( type instanceof BooleanDataType ) {
            char firstChar = raw.charAt( 0 );
            if( firstChar == 'T' || firstChar == 't' ) {
                ret = BooleanValue.create( true );
            } else if( firstChar == 'F' || firstChar == 'f' ) {
                ret = BooleanValue.create( false );
            } else {
                ret = BooleanValue.create( false );
                log.error( "illegal encoding in boolean value: " + raw );
            }

        } else if( type instanceof ColorDataType ) {
            int value = Integer.parseInt( raw );
            ret = ColorValue.create( value );

        } else if( type instanceof EnumeratedDataType ) {
            ret = ((EnumeratedDataType)type).select( raw );

        } else if( type instanceof ExtentDataType ) {
            int colon = raw.indexOf( ':' );
            double a = Double.parseDouble( raw.substring( 0, colon ));
            double b = Double.parseDouble( raw.substring( colon+1 ));
            ret = ExtentValue.create( a, b );

        } else if( type instanceof FloatDataType ) {
            ret = FloatValue.create( Double.parseDouble( raw ));

        } else if( type instanceof IntegerDataType ) {
            ret = IntegerValue.create( Integer.parseInt( raw ));

        } else if( type instanceof MultiplicityDataType ) {
            int colon = raw.indexOf( ':' );
            int a = Integer.parseInt( raw.substring( 0, colon ));
            int b = Integer.parseInt( raw.substring( colon+1 ));
            ret = MultiplicityValue.create( a, b );

        } else if( type instanceof PointDataType ) {
            int colon = raw.indexOf( ':' );
            int a = Integer.parseInt( raw.substring( 0, colon ));
            int b = Integer.parseInt( raw.substring( colon+1 ));
            ret = PointValue.create( a, b );

        } else if( type instanceof StringDataType ) {
            ret = StringValue.create( raw );

        } else if( type instanceof TimePeriodDataType ) {
            try {
                int [] values = new int[6];
                int oldSlash = 0;
                int slash;
                int index;

                for( index=0 ; index<values.length ; ++index ) {
                    slash = raw.indexOf( '/', oldSlash );
                    if( slash < 0 ) {
                        values[index] = Integer.parseInt( raw.substring( oldSlash ));
                        break;
                    }
                    values[index] = Integer.parseInt( raw.substring( oldSlash, slash ));
                    oldSlash = slash+1;
                }
                // FIXME -- using integer for float seconds
                ret = TimePeriodValue.create(
                        (short) ((index >=5 ) ? values[ index-5 ] : 0 ),
                        (short) ((index >=4 ) ? values[ index-4 ] : 0 ),
                        (short) ((index >=3 ) ? values[ index-3 ] : 0 ),
                        (short) ((index >=2 ) ? values[ index-2 ] : 0 ),
                        (short) ((index >=1 ) ? values[ index-1 ] : 0 ),
                        (float) ((index >=0 ) ? values[ index ] : 0 ) );

            } catch( NumberFormatException ex ) {
                throw new NumberFormatException( "Error when attempting to parse TimePeriodValue '" + raw + "'" );
            }

        } else if( type instanceof TimeStampDataType ) {
            try {
                int [] values = new int[6];
                int oldSlash = 0;
                int slash;
                int index;

                for( index=0 ; index<values.length ; ++index ) {
                    slash = raw.indexOf( '/', oldSlash );
                    if( slash < 0 ) {
                        values[index] = Integer.parseInt( raw.substring( oldSlash ));
                        break;
                    }
                    values[index] = Integer.parseInt( raw.substring( oldSlash, slash ));
                    oldSlash = slash+1;
                }
                // FIXME -- using integer for float seconds
                ret = TimeStampValue.create(
                        (short) ((index >=5 ) ? values[ index-5 ] : 0 ),
                        (short) ((index >=4 ) ? values[ index-4 ] : 0 ),
                        (short) ((index >=3 ) ? values[ index-3 ] : 0 ),
                        (short) ((index >=2 ) ? values[ index-2 ] : 0 ),
                        (short) ((index >=1 ) ? values[ index-1 ] : 0 ),
                        (float) ((index >=0 ) ? values[ index ] : 0 ) );

            } catch( NumberFormatException ex ) {
                throw new NumberFormatException( "Error when attempting to parse TimeStampDataType '" + raw + "'" );
            }

        } else {
            ret = null;
            log.error( "unknown data type " + type );
        }

        return ret;
    }

    /**
     * Internal helper to determine the correct instanceof a subtype of DataType, given the parameters.
     *
     * @param theDataTypeClass the Class whose instance we are looking for
     * @param theAttributes the ExternalizedAttributes containing information about this DataType
     * @param nameOfParameter the name of the parameter
     * @param theDefault the default DataType
     * @return the created / found DataType
     */
    protected DataType determineDataType(
            Class                  theDataTypeClass,
            ExternalizedAttributes theAttributes,
            String                 nameOfParameter,
            DataType               theDefault )
    {
        String fieldName = theAttributes.getValue( nameOfParameter );
        if( fieldName == null ) {
            return theDefault;
        }

        // use reflection to resolve to the specified default
        try {
            Field theField = theDataTypeClass.getDeclaredField( fieldName );
            if( theField == null ) {
                error( "Cannot find Field " + theDataTypeClass + "." + fieldName );
                return theDefault;
            }

            Object fieldValue = theField.get( null ); // must be static
            if( fieldValue instanceof DataType ) {
                return (DataType) fieldValue;
            } else {
                error( "Field " + theDataTypeClass + "." + fieldName + " holds object " + fieldValue + ", not DataType" );
                return theDefault;
            }
        } catch( NoSuchFieldException ex ) {
            log.error( theErrorPrefix, ex ); // good enough for the time being, FIXME
            return theDefault;
        } catch( IllegalAccessException ex ) {
            log.error( theErrorPrefix, ex ); // good enough for the time being, FIXME
            return theDefault;
        }
    }

    /**
     * Internal helper that instantiates all the data in this SubjectArea.
     *
     * @param theExternalizedSubjectArea the to-be-instantiated SubjectArea in buffered form
     * @param theClassLoader the ClassLoader to use for this SubjectArea
     * @return the instantiated SubjectArea
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected SubjectArea instantiateSubjectArea(
            ExternalizedSubjectArea theExternalizedSubjectArea,
            ClassLoader             theClassLoader )
        throws
            MeshTypeNotFoundException
    {
        SubjectArea [] theSubjectAreaDependencies = new SubjectArea[ theExternalizedSubjectArea.getSubjectAreaDependencies().size() ];

        int i=0;
        for( ExternalizedSubjectAreaDependency theExternalizedSubjectAreaDependency
                : theExternalizedSubjectArea.getSubjectAreaDependencies() )
        {
            StringValue minVersion = theExternalizedSubjectAreaDependency.getMinVersion();
            theSubjectAreaDependencies[i++] = theModelBase.findSubjectArea(
                    theExternalizedSubjectAreaDependency.getName().value(),
                    minVersion != null ? minVersion.value() : null );
        }

        i = 0;
        ModuleRequirement [] theModuleRequirements = new ModuleRequirement[ theExternalizedSubjectArea.getModuleRequirements().size() ];
        for( ExternalizedModuleRequirement theExternalizedModuleRequirement
                : theExternalizedSubjectArea.getModuleRequirements() )
        {
            StringValue minVersion = theExternalizedModuleRequirement.getMinVersion();
            theModuleRequirements[i++] = ModuleRequirement.create1(
                    theExternalizedModuleRequirement.getName().value(),
                    minVersion != null ? minVersion.value() : null );
        }

        SubjectArea theSubjectArea = theInstantiator.createSubjectArea(
                constructIdentifier( theExternalizedSubjectArea ),
                theExternalizedSubjectArea.getName(),
                theExternalizedSubjectArea.getVersion(),
                userTableToL10Map( theExternalizedSubjectArea.getUserNames(),        theExternalizedSubjectArea.getName(), StringDataType.theDefault ),
                userTableToL10Map( theExternalizedSubjectArea.getUserDescriptions(), null,                                 BlobDataType.theTextAnyType ),
                theSubjectAreaDependencies,
                theModuleRequirements,
                theClassLoader,
                theExternalizedSubjectArea.getGenerateInterface(),
                theExternalizedSubjectArea.getGenerateImplementation() );

        HashMap<ExternalizedEntityType,ProjectedPropertyTypePatcher[]> patchMap = new HashMap<ExternalizedEntityType,ProjectedPropertyTypePatcher[]>();

        for( ExternalizedEntityType theExternalizedEntityType
                : theExternalizedSubjectArea.getEntityTypes() )
        {
            ProjectedPropertyTypePatcher [] patchers = instantiateEntityType( theExternalizedEntityType, theExternalizedSubjectArea, theSubjectArea );
            patchMap.put( theExternalizedEntityType, patchers );
        }
        for( ExternalizedRelationshipType theExternalizedRelationshipType
                : theExternalizedSubjectArea.getRelationshipTypes() )
        {
            instantiateRelationshipType( theExternalizedRelationshipType, theExternalizedSubjectArea, theSubjectArea );
        }
        for( ExternalizedEntityType theExternalizedEntityType
                : theExternalizedSubjectArea.getEntityTypes() )
        {
            ProjectedPropertyTypePatcher [] patchers = patchMap.get( theExternalizedEntityType );
            setInputPropertyForEntityType( theExternalizedEntityType, patchers, theExternalizedSubjectArea, theSubjectArea );
        }
        return theSubjectArea;
    }

    /**
     * Internal helper that instantiates one EntityType.
     *
     * @param theExternalizedEntityType the to-be-instantiated object in buffered form
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the newly instantiated object lives
     * @param theSubjectArea the SubjectArea in which the newly instantiated object lives
     * @throws ModuleException a required ModelModule could not be loaded
     */
    protected ProjectedPropertyTypePatcher [] instantiateEntityType(
            ExternalizedEntityType  theExternalizedEntityType,
            ExternalizedSubjectArea theExternalizedSubjectArea,
            SubjectArea             theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        AttributableMeshType [] theSupertypes
                = new AttributableMeshType[ theExternalizedEntityType.getSuperTypes().size() ];
        for( int i=0 ; i<theSupertypes.length ; ++i ) {
            MeshTypeIdentifier currentRef = theExternalizedEntityType.getSuperTypes().get( i );

            theSupertypes[i] = (AttributableMeshType) theModelBase.findMeshTypeByIdentifier( currentRef );

            if( theSupertypes[i] == null ) {
                throw new IllegalArgumentException( "Cannot find supertype with ID " + currentRef + " for EntityType with ID " + theExternalizedEntityType.getIdentifier() );
            }
        }

        BlobValue icon;
        if( theExternalizedEntityType.getRelativeIconPath() != null ) {
            icon = BlobValue.createByLoadingFrom( theSubjectArea.getClassLoader(), theExternalizedEntityType.getRelativeIconPath(), theExternalizedEntityType.getIconMimeType() );
        } else {
            icon = null;
        }
        BlobValue overrideCode = createBlobValueFrom( theExternalizedEntityType.getRawOverrideCode(), BlobValue.TEXT_PLAIN_MIME_TYPE );

        EntityType theEntityType = theInstantiator.createEntityType(
                    constructIdentifier( theExternalizedSubjectArea, theExternalizedEntityType ),
                    theExternalizedEntityType.getName(),
                    userTableToL10Map( theExternalizedEntityType.getUserNames(),        theExternalizedEntityType.getName(), StringDataType.theDefault ),
                    userTableToL10Map( theExternalizedEntityType.getUserDescriptions(), null,                                BlobDataType.theTextAnyType ),
                    icon,
                    theSubjectArea,
                    theSupertypes,
                    overrideCode,
                    theExternalizedEntityType.getLocalEntityTypeGuardClassNames(),
                    BlobValue.createMultiple( theExternalizedEntityType.getDeclaredMethods() ),
                    BlobValue.createMultiple( theExternalizedEntityType.getImplementedMethods() ),
                    theExternalizedEntityType.getIsAbstract(),
                    theExternalizedEntityType.getMayBeUsedAsForwardReference(),
                    theExternalizedEntityType.getIsSignificant(),
                    theExternalizedEntityType.getGenerateInterface(),
                    theExternalizedEntityType.getGenerateImplementation() );

        for( ExternalizedPropertyType theExternalizedPropertyType
                : theExternalizedEntityType.getPropertyTypes() )
        {
            instantiatePropertyType(
                    theExternalizedPropertyType,
                    theExternalizedEntityType,
                    theEntityType,
                    theExternalizedSubjectArea,
                    theSubjectArea );
        }

        // for projected property, we need to do two passes:
        // 1) create, without InputPropertySpecifications,
        // 2) set InputPropertySpecifications
        // Otherwise we cannot deal with inevitable forward references. We do #1 here, and #2 after
        // the RelationshipTypes are instantiated
        ProjectedPropertyTypePatcher [] ret = new ProjectedPropertyTypePatcher[ theExternalizedEntityType.getProjectedPropertyTypes().size() ];
        
        int i=0;
        for( ExternalizedPropertyType theExternalizedProjectedPropertyType
                : theExternalizedEntityType.getProjectedPropertyTypes() )
        {
            ret[ i++ ] = instantiateProjectedPropertyType(
                    (ExternalizedProjectedPropertyType) theExternalizedProjectedPropertyType,
                    theExternalizedEntityType,
                    theEntityType,
                    theExternalizedSubjectArea,
                    theSubjectArea );
        }

        if( theExternalizedEntityType.getPropertyTypeGroups() != null && ! theExternalizedEntityType.getPropertyTypeGroups().isEmpty() ) {
            for( ExternalizedPropertyTypeGroup theExternalizedPropertyTypeGroup
                    : theExternalizedEntityType.getPropertyTypeGroups() )
            {
                instantiatePropertyTypeGroup(
                        theExternalizedPropertyTypeGroup,
                        theExternalizedEntityType,
                        theEntityType,
                        theExternalizedSubjectArea,
                        theSubjectArea );
            }
        }
        return ret;
    }

    /**
     * Internal helper that sets the TraversalToPropertySpecifications of one EntityType.
     *
     * @param theExternalizedEntityType the EntityType in buffered form
     * @param theExternalizedSubjectArea the SubjectArea in buffered form to which the EntityType belongs
     * @param theSubjectArea the SubjectArea to which the EntityType belongs
     * @throws ModuleException thrown if a required Module could not be loaded
     */
    protected void setInputPropertyForEntityType(
            ExternalizedEntityType          theExternalizedEntityType,
            ProjectedPropertyTypePatcher [] patchers,
            ExternalizedSubjectArea         theExternalizedSubjectArea,
            SubjectArea                     theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        EntityType theEntityType = (EntityType) theModelBase.findMeshTypeByIdentifier(
                constructIdentifier( theExternalizedSubjectArea, theExternalizedEntityType ));

        int i=0;
        for( ExternalizedPropertyType theExternalizedProjectedPropertyType
                : theExternalizedEntityType.getProjectedPropertyTypes() )
        {
            setInputPropertyInProjectedPropertyType(
                    (ExternalizedProjectedPropertyType) theExternalizedProjectedPropertyType,
                    theExternalizedEntityType,
                    theEntityType,
                    patchers[i],
                    theExternalizedSubjectArea,
                    theSubjectArea );
        }
    }

    /**
     * Internal helper that instantiates one RelationshipType and all it contains.
     *
     * @param theExternalizedRelationshipType the to-be-instantiated object in buffered form
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the newly instantiated object lives
     * @param theSubjectArea the SubjectArea in which the newly instantiated object lives
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected void instantiateRelationshipType(
            ExternalizedRelationshipType theExternalizedRelationshipType,
            ExternalizedSubjectArea      theExternalizedSubjectArea,
            SubjectArea                  theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        RelationshipType theRelationshipType;

        if( theExternalizedRelationshipType.getSourceDestination() == null ) {
            // normal case: binary RelationshipType
            RoleType [] theSourceSupertypes      = new RoleType[ theExternalizedRelationshipType.getSource().getRefines().size() ];
            RoleType [] theDestinationSupertypes = new RoleType[ theExternalizedRelationshipType.getDestination().getRefines().size() ];

            for( int i=0 ; i<theSourceSupertypes.length ; ++i ) {
                String current         = (String) theExternalizedRelationshipType.getSource().getRefines().get( i );
                theSourceSupertypes[i] = deserializeRoleType( current, theModelBase );

                if( theSourceSupertypes[i] == null ) {
                    throw new IllegalArgumentException( "Cannot find RoleType with external form " + current );
                }
            }
            for( int i=0 ; i<theDestinationSupertypes.length ; ++i ) {
                String current              = (String) theExternalizedRelationshipType.getDestination().getRefines().get( i );
                theDestinationSupertypes[i] = deserializeRoleType( current, theModelBase );

                if( theDestinationSupertypes[i] == null ) {
                    throw new IllegalArgumentException( "Cannot find RoleType with external form " + current );
                }
            }

            MeshTypeIdentifier srcName  = theExternalizedRelationshipType.getSource().getEntityType();
            MeshTypeIdentifier destName = theExternalizedRelationshipType.getDestination().getEntityType();
            
            EntityType src  = (srcName  != null) ? theModelBase.findEntityTypeByIdentifier( srcName ) : null;
            EntityType dest = (destName != null) ? theModelBase.findEntityTypeByIdentifier( destName ) : null;

            theRelationshipType = theInstantiator.createRelationshipType(
                    constructIdentifier( theExternalizedSubjectArea, theExternalizedRelationshipType ),
                    theExternalizedRelationshipType.getName(),
                    userTableToL10Map( theExternalizedRelationshipType.getUserNames(),        theExternalizedRelationshipType.getName(), StringDataType.theDefault ),
                    userTableToL10Map( theExternalizedRelationshipType.getUserDescriptions(), null,                                      BlobDataType.theTextAnyType ),
                    theSubjectArea,
                    theExternalizedRelationshipType.getSource().getMultiplicity(),
                    theExternalizedRelationshipType.getDestination().getMultiplicity(),
                    src,
                    dest,
                    theSourceSupertypes,
                    theDestinationSupertypes,
                    theExternalizedRelationshipType.getSource().getRoleTypeGuardClassNames(),
                    theExternalizedRelationshipType.getDestination().getRoleTypeGuardClassNames(),
                    theExternalizedRelationshipType.getIsAbstract(),
                    theExternalizedRelationshipType.getGenerateInterface(),
                    theExternalizedRelationshipType.getGenerateImplementation() );
        } else {
            // unary RelationshipType

            RoleType [] theSrcDestSupertypes = new RoleType[ theExternalizedRelationshipType.getSourceDestination().getRefines().size() ];

            for( int i=0 ; i<theSrcDestSupertypes.length ; ++i ) {
                String current          = (String) theExternalizedRelationshipType.getSourceDestination().getRefines().get( i );
                theSrcDestSupertypes[i] = deserializeRoleType( current, theModelBase );

                if( theSrcDestSupertypes[i] == null ) {
                    throw new IllegalArgumentException( "Cannot find RoleType with external form " + current );
                }
            }

            EntityType srcdest = (EntityType) theModelBase.findMeshTypeByIdentifier( theExternalizedRelationshipType.getSourceDestination().getEntityType() );

            theRelationshipType = theInstantiator.createRelationshipType(
                    constructIdentifier( theExternalizedSubjectArea, theExternalizedRelationshipType ),
                    theExternalizedRelationshipType.getName(),
                    userTableToL10Map( theExternalizedRelationshipType.getUserNames(),        theExternalizedRelationshipType.getName(), StringDataType.theDefault ),
                    userTableToL10Map( theExternalizedRelationshipType.getUserDescriptions(), null,                                      BlobDataType.theTextAnyType ),
                    theSubjectArea,
                    theExternalizedRelationshipType.getSourceDestination().getMultiplicity(),
                    srcdest,
                    theSrcDestSupertypes,
                    theExternalizedRelationshipType.getSourceDestination().getRoleTypeGuardClassNames(),
                    theExternalizedRelationshipType.getIsAbstract(),
                    theExternalizedRelationshipType.getGenerateInterface(),
                    theExternalizedRelationshipType.getGenerateImplementation() );
            return;
        }

        for( ExternalizedPropertyType theExternalizedPropertyType
                : theExternalizedRelationshipType.getPropertyTypes() )
        {
            instantiatePropertyType(
                    theExternalizedPropertyType,
                    theExternalizedRelationshipType,
                    theRelationshipType,
                    theExternalizedSubjectArea,
                    theSubjectArea );
        }
        for( ExternalizedPropertyType theExternalizedProjectedPropertyType
                : theExternalizedRelationshipType.getProjectedPropertyTypes())
        {
            instantiateProjectedPropertyType(
                    (ExternalizedProjectedPropertyType) theExternalizedProjectedPropertyType,
                    theExternalizedRelationshipType,
                    theRelationshipType,
                    theExternalizedSubjectArea,
                    theSubjectArea );
        }
        if(    theExternalizedRelationshipType.getPropertyTypeGroups() != null
            && !theExternalizedRelationshipType.getPropertyTypeGroups().isEmpty() )
        {
            for( ExternalizedPropertyTypeGroup theExternalizedPropertyTypeGroup
                    : theExternalizedRelationshipType.getPropertyTypeGroups() )
            {
                instantiatePropertyTypeGroup(
                        theExternalizedPropertyTypeGroup,
                        theExternalizedRelationshipType,
                        theRelationshipType,
                        theExternalizedSubjectArea,
                        theSubjectArea );
            }
        }
    }

    /**
     * Internal helper that instantiates one PropertyType.
     *
     * @param theExternalizedPropertyType the to-be-instantiated object in buffered form
     * @param theExternalizedAmo the ExternalizedAttributableMeshType to which the newly instantiated PropertyType belongs
     * @param theAmo the AttributableMeshType to which the newly instantiated PropertyType belongs
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the newly instantiated object lives
     * @param theSubjectArea the SubjectArea in which the newly instantiated object lives
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected void instantiatePropertyType(
            ExternalizedPropertyType         theExternalizedPropertyType,
            ExternalizedAttributableMeshType theExternalizedAmo,
            AttributableMeshType             theAmo,
            ExternalizedSubjectArea          theExternalizedSubjectArea,
            SubjectArea                      theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        MeshTypeIdentifier identifier = constructIdentifier( theExternalizedSubjectArea, theExternalizedAmo, theExternalizedPropertyType );

        if(    theExternalizedPropertyType.getToOverrides() == null
            || theExternalizedPropertyType.getToOverrides().isEmpty() )
        {
            // no override
            PropertyType thePropertyType = theInstantiator.createPropertyType(
                    identifier,
                    theExternalizedPropertyType.getName(),
                    userTableToL10Map( theExternalizedPropertyType.getUserNames(),        theExternalizedPropertyType.getName(), StringDataType.theDefault ),
                    userTableToL10Map( theExternalizedPropertyType.getUserDescriptions(), null,                                  BlobDataType.theTextAnyType ),
                    theAmo,
                    theSubjectArea,
                    theExternalizedPropertyType.getDataType(),
                    theExternalizedPropertyType.getDefaultValue(),
                    theExternalizedPropertyType.getDefaultValueCode(),
                    theExternalizedPropertyType.getLocalPropertyTypeGuardClassNames(),
                    theExternalizedPropertyType.getIsOptional(),
                    theExternalizedPropertyType.getIsReadOnly(),
                    theExternalizedPropertyType.getGenerateInterface(),
                    theExternalizedPropertyType.getGenerateImplementation(),
                    theExternalizedPropertyType.getSequenceNumber() );
        } else {
            // override
            PropertyType [] toOverride = new PropertyType[ theExternalizedPropertyType.getToOverrides().size() ];
            for( int i=0 ; i<toOverride.length ; ++i )
            {
                String             current    = (String) theExternalizedPropertyType.getToOverrides().get( i );
                MeshTypeIdentifier currentRef = createTypeIdentifierFrom( current );

                toOverride[i] = theModelBase.findPropertyTypeByIdentifier( currentRef );
                if( toOverride[i] == null ) {
                    throw new IllegalArgumentException( "Could not find PropertyType with id " + currentRef + " in order to override in object with id " + identifier );
                }
            }
            PropertyType thePropertyType = theInstantiator.createOverridingPropertyType(
                    toOverride,
                    identifier,
                    userTableToL10Map( theExternalizedPropertyType.getUserDescriptions(), null, BlobDataType.theTextAnyType ),
                    theAmo,
                    theSubjectArea,
                    theExternalizedPropertyType.getDataType(),
                    theExternalizedPropertyType.getDefaultValue(),
                    theExternalizedPropertyType.getDefaultValueCode(),
                    theExternalizedPropertyType.getLocalPropertyTypeGuardClassNames(),
                    theExternalizedPropertyType.getIsOptional(),
                    theExternalizedPropertyType.getIsReadOnly(),
                    theExternalizedPropertyType.getGenerateInterface(),
                    theExternalizedPropertyType.getGenerateImplementation() );
        }
    }

    /**
     * Internal helper that sets the InputTraversalToPropertySpecifications for ProjectedPropertyTypes.
     *
     * @param theExternalizedProjectedPropertyType the ProjectedPropertyType in buffered form
     * @param theExternalizedAmo the ExternalizedAttributableMeshType to which the ProjectedPropertyType belongs
     * @param theAmo the AttributableMeshType to which the ProjectedPropertyType belongs
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the object lives
     * @param theSubjectArea the SubjectArea in which the object lives
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected void setInputPropertyInProjectedPropertyType(
            ExternalizedProjectedPropertyType  theExternalizedProjectedPropertyType,
            ExternalizedAttributableMeshType   theExternalizedAmo,
            AttributableMeshType               theAmo,
            ProjectedPropertyTypePatcher       thePatcher,
            ExternalizedSubjectArea            theExternalizedSubjectArea,
            SubjectArea                        theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        TraversalToPropertySpecification [] mais
                = new TraversalToPropertySpecification[ theExternalizedProjectedPropertyType.getTraversalToPropertySpecifications().size() ];

        for( int i=0 ; i<mais.length ; ++i ) {
            ExternalizedTraversalToPropertySpecification current = (ExternalizedTraversalToPropertySpecification) theExternalizedProjectedPropertyType.getTraversalToPropertySpecifications().get( i );
            TraversalSpecification traversalSpec = deserializeTraversalSpecification( current.getTraversalSpecification() );

            PropertyType [] mas = new PropertyType[ current.getPropertyTypes().size() ];
            for( int j=0 ; j<mas.length ; ++j ) {
                MeshTypeIdentifier current2 = current.getPropertyTypes().get( j );
                mas[j] = (PropertyType) theModelBase.findMeshTypeByIdentifier( current2 );
            }
            mais[i] = TraversalToPropertySpecification.create(
                   traversalSpec,
                   mas );
        }

        thePatcher.setInputProperties( mais );
    }

    /**
     * Internal helper, invoked recursively, to deserialize a TraversalSpecification.
     *
     * @param theExternalizedTraversalSpec the to-be-deserialized TraversalSpecification
     * @return the TraversalSpecification
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected TraversalSpecification deserializeTraversalSpecification(
            ExternalizedTraversalSpecification theExternalizedTraversalSpec )
        throws
            MeshTypeNotFoundException
    {
        TraversalSpecification ret = null;
        if( theExternalizedTraversalSpec == null ) {
            ret = null;

        } else if( theExternalizedTraversalSpec instanceof ExternalizedTraversalSpecification.Selective ) {
            ExternalizedTraversalSpecification.Selective realExternalizedSpec = (ExternalizedTraversalSpecification.Selective) theExternalizedTraversalSpec;

            MeshObjectSelector startSelector;
            MeshObjectSelector endSelector;

            if( realExternalizedSpec.getStartSelector() != null ) {
                startSelector = deserializeMeshObjectSelector( realExternalizedSpec.getStartSelector() );
            } else {
                startSelector = null;
            }
            if( realExternalizedSpec.getEndSelector() != null ) {
                endSelector = deserializeMeshObjectSelector( realExternalizedSpec.getEndSelector() );
            } else {
                endSelector = null;
            }
            TraversalSpecification qualified = deserializeTraversalSpecification( realExternalizedSpec.getTraversalSpecification() );

            ret = SelectiveTraversalSpecification.create( startSelector, qualified, endSelector );

        } else if( theExternalizedTraversalSpec instanceof ExternalizedTraversalSpecification.Sequential ) {
            ExternalizedTraversalSpecification.Sequential realExternalizedSpec = (ExternalizedTraversalSpecification.Sequential) theExternalizedTraversalSpec;

            TraversalSpecification [] steps = new TraversalSpecification[ realExternalizedSpec.getSteps().size() ];
            for( int i=0 ; i<steps.length ; ++i ) {
                steps[i] = deserializeTraversalSpecification( (ExternalizedTraversalSpecification) realExternalizedSpec.getSteps().get( i ));
            }

            ret = SequentialCompoundTraversalSpecification.create( steps );

        } else if( theExternalizedTraversalSpec instanceof ExternalizedTraversalSpecification.Role ) {
            ExternalizedTraversalSpecification.Role realExternalizedSpec = (ExternalizedTraversalSpecification.Role) theExternalizedTraversalSpec;

            ret = deserializeRoleType( realExternalizedSpec.getIdentifierString(), theModelBase  );

        } else {
            log.error( theErrorPrefix + "unexpected type: " + theExternalizedTraversalSpec );
        }

        return ret;
    }

    /**
     * Internal helper to deserialize a MeshObjectSelector.
     *
     * @param theExternalizedSelector the to-be-deserialized MeshObjectSelector
     * @return the MeshObjectSelector
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected MeshObjectSelector deserializeMeshObjectSelector(
            ExternalizedMeshObjectSelector theExternalizedSelector )
        throws
            MeshTypeNotFoundException
    {
        MeshObjectSelector ret = null;

        if( theExternalizedSelector instanceof ExternalizedMeshObjectSelector.ByType ) {
            ExternalizedMeshObjectSelector.ByType realExternalizedSelector = (ExternalizedMeshObjectSelector.ByType) theExternalizedSelector;

            EntityType type = theModelBase.findEntityTypeByIdentifier( realExternalizedSelector.identifier );

            ret = ByTypeMeshObjectSelector.create( type, realExternalizedSelector.subtypesAllowed );
        } else {
            log.error( theErrorPrefix + "unexpected type: " + theExternalizedSelector );
        }
        return ret;
    }

    /**
     * Internal helper to deserialize a RoleType.
     *
     */
    protected RoleType deserializeRoleType(
            String    rawString,
            ModelBase mb )
        throws
            MeshTypeNotFoundException
    {
        boolean isSource      = false;
        boolean isDestination = false;
        boolean isTop         = false;

        MeshTypeIdentifier relationshipName;

        if( rawString.endsWith( RoleType.SOURCE_POSTFIX )) {
            isSource = true;
            relationshipName = createTypeIdentifierFrom(
                    rawString.substring( 0, rawString.length() - RoleType.SOURCE_POSTFIX.length() ));

        } else if( rawString.endsWith( RoleType.DESTINATION_POSTFIX )) {
            isDestination = true;
            relationshipName = createTypeIdentifierFrom(
                    rawString.substring( 0, rawString.length() - RoleType.DESTINATION_POSTFIX.length() ));

        } else if( rawString.endsWith( RoleType.TOP_SINGLETON_POSTFIX )) {
            isTop = false;
            relationshipName = createTypeIdentifierFrom(
                    rawString.substring( 0, rawString.length() - RoleType.TOP_SINGLETON_POSTFIX.length() ));

        } else {
            throw new IllegalArgumentException( "don't know what this is: " + rawString );
        }

        RelationshipType rt = mb.findRelationshipTypeByIdentifier( relationshipName );
        if( rt == null ) {
            throw new IllegalArgumentException( "Cannot find RoleType with ID " + rawString );
        }

        if( isSource || isTop ) {
            return rt.getSource();
        } else {
            return rt.getDestination();
        }
    }

    /**
     * Internal helper that instantiates one ProjectedPropertyType
     *
     * @param theExternalizedProjectedPropertyType the to-be-instantiated object in buffered form
     * @param theExternalizedAmo the ExternalizedAttributableMeshType to which the newly instantiated PropertyTypeGroup belongs
     * @param theAmo the AttributableMeshType to which the newly instantiated PropertyTypeGroup belongs
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the newly instantiated object lives
     * @param theSubjectArea the SubjectArea in which the newly instantiated object lives
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected ProjectedPropertyTypePatcher instantiateProjectedPropertyType(
            ExternalizedProjectedPropertyType theExternalizedProjectedPropertyType,
            ExternalizedAttributableMeshType  theExternalizedAmo,
            AttributableMeshType              theAmo,
            ExternalizedSubjectArea           theExternalizedSubjectArea,
            SubjectArea                       theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        // no mais here, see setMaisInProjectedPropertyType

        MeshTypeIdentifier identifier = constructIdentifier( theExternalizedSubjectArea, theExternalizedAmo, theExternalizedProjectedPropertyType );

        BlobValue projectionCode = createBlobValueFrom( theExternalizedProjectedPropertyType.getRawProjectionCode(), BlobValue.TEXT_PLAIN_MIME_TYPE );

        if(    theExternalizedProjectedPropertyType.getToOverrides() == null
            || theExternalizedProjectedPropertyType.getToOverrides().isEmpty() )
        {
            // no override
            ProjectedPropertyTypePatcher ret = theInstantiator.createProjectedPropertyType(
                    identifier,
                    theExternalizedProjectedPropertyType.getName(),
                    userTableToL10Map( theExternalizedProjectedPropertyType.getUserNames(),        theExternalizedProjectedPropertyType.getName(), StringDataType.theDefault ),
                    userTableToL10Map( theExternalizedProjectedPropertyType.getUserDescriptions(), null,                                           BlobDataType.theTextAnyType ),
                    theAmo,
                    theSubjectArea,
                    theExternalizedProjectedPropertyType.getDataType(),
                    theExternalizedProjectedPropertyType.getDefaultValue(),
                    theExternalizedProjectedPropertyType.getDefaultValueCode(),
                    null,
                    projectionCode,
                    theExternalizedProjectedPropertyType.getGenerateInterface(),
                    theExternalizedProjectedPropertyType.getGenerateImplementation(),
                    theExternalizedProjectedPropertyType.getSequenceNumber() );

            return ret;

        } else {
            // override
            PropertyType [] toOverride = new PropertyType[ theExternalizedProjectedPropertyType.getToOverrides().size() ];
            for( int i=0 ; i<toOverride.length ; ++i ) {
                String             current    = (String) theExternalizedProjectedPropertyType.getToOverrides().get( i );
                MeshTypeIdentifier currentRef = createTypeIdentifierFrom( current );

                toOverride[i] = theModelBase.findPropertyTypeByIdentifier( currentRef );
                if( toOverride[i] == null ) {
                    throw new IllegalArgumentException( "Could not find PropertyType with id " + currentRef + " in order to override in object with id " + identifier );
                }
            }
            ProjectedPropertyTypePatcher ret = theInstantiator.createOverridingProjectedPropertyType(
                    toOverride,
                    identifier,
                    userTableToL10Map( theExternalizedProjectedPropertyType.getUserDescriptions(), null, BlobDataType.theTextAnyType ),
                    theAmo,
                    theSubjectArea,
                    theExternalizedProjectedPropertyType.getDataType(),
                    theExternalizedProjectedPropertyType.getDefaultValue(),
                    theExternalizedProjectedPropertyType.getDefaultValueCode(),
                    null,
                    projectionCode,
                    theExternalizedProjectedPropertyType.getGenerateInterface(),
                    theExternalizedProjectedPropertyType.getGenerateImplementation() );

            return ret;

        }
    }

    /**
     * Internal helper that instantiates one PropertyTypeGroup.
     *
     * @param theExternalizedPropertyTypeGroup the to-be-instantiated object in buffered form
     * @param theExternalizedAmo the ExternalizedAttributableMeshType to which the newly instantiated PropertyTypeGroup belongs
     * @param theAmo the AttributableMeshType to which the newly instantiated PropertyTypeGroup belongs
     * @param theExternalizedSubjectArea the ExternalizedSubjectArea in which the newly instantiated object lives
     * @param theSubjectArea the SubjectArea in which the newly instantiated object lives
     * @throws ModuleException thrown if a required ModelModule could not be loaded
     */
    protected void instantiatePropertyTypeGroup(
            ExternalizedPropertyTypeGroup    theExternalizedPropertyTypeGroup,
            ExternalizedAttributableMeshType theExternalizedAmo,
            AttributableMeshType             theAmo,
            ExternalizedSubjectArea          theExternalizedSubjectArea,
            SubjectArea                      theSubjectArea )
        throws
            MeshTypeNotFoundException
    {
        MeshTypeIdentifier identifier = constructIdentifier( theExternalizedSubjectArea, theExternalizedAmo, theExternalizedPropertyTypeGroup );

        PropertyType [] mas = new PropertyType[ theExternalizedPropertyTypeGroup.getGroupMembers().size() ];
        for( int i=0 ; i<mas.length ; ++i ) {
            String             current    = (String) theExternalizedPropertyTypeGroup.getGroupMembers().get( i );
            MeshTypeIdentifier currentRef = createTypeIdentifierFrom( current );

            mas[i] = theModelBase.findPropertyTypeByIdentifier( currentRef );
            if( mas[i] == null ) {
                throw new IllegalArgumentException( "Cannot include null PropertyType in PropertyTypeGroup " + identifier );
            }
        }

        PropertyTypeGroup thePropertyTypeGroup = theInstantiator.createPropertyTypeGroup(
                identifier,
                theExternalizedPropertyTypeGroup.getName(),
                userTableToL10Map( theExternalizedPropertyTypeGroup.getUserNames(),        theExternalizedPropertyTypeGroup.getName(), StringDataType.theDefault ),
                userTableToL10Map( theExternalizedPropertyTypeGroup.getUserDescriptions(), null,                                       BlobDataType.theTextAnyType ),
                theAmo,
                theSubjectArea,
                mas,
                theExternalizedPropertyTypeGroup.getGenerateInterface(),
                theExternalizedPropertyTypeGroup.getGenerateImplementation(),
                theExternalizedPropertyTypeGroup.getSequenceNumber() );
    }

    /**
     * Internal helper to convert a HashMap of locale-value mappings into an L10Map.
     *
     * @param theMap the HashMap of locale-value mappings
     * @param defaultValue the default value of the PropertyType
     * @param theType the data type of the PropertyType
     * @return appropriate L10Map
     */
    protected L10Map userTableToL10Map(
            Map<String,PropertyValue> theMap,
            PropertyValue             defaultValue,
            DataType                  theType )
    {
        return L10MapImpl.create( theMap, defaultValue );
    }

    /**
     * For better debugging, we have this method.
     *
     * @param msg the error message
     */
    protected void error(
            String msg )
    {
        log.error( theErrorPrefix + msg + '\n' + parseStackToString() );
    }

    /**
     * Obtain current content of the parse stack (for debugging).
     *
     * @return String representation of the current parse stack
     */
    public String parseStackToString()
    {
        StringBuffer buf = new StringBuffer( 100 ); // fudge
        buf.append( "Parse Stack:" );
        Iterator theIter = theStack.iterator();
        while( theIter.hasNext() ) {
            buf.append( "\n    " );
            buf.append( theIter.next() );
        }
        buf.append( "\n(End parse stack)" );
        return buf.toString();
    }
    
    /**
     * Determine a MeshTypeIdentifier appropriate for a SubjectArea.
     *
     * @param sa the SubjectArea
     * @return a suitable Identifier
     */
    public MeshTypeIdentifier constructIdentifier(
            ExternalizedSubjectArea sa )
    {
        StringBuilder idString = new StringBuilder();

        idString.append( sa.getName().value() );
        if( sa.getVersion() != null ) {
            idString.append( "_v" );
            idString.append( sa.getVersion().value() );
        }
        
        return createTypeIdentifierFrom( idString.toString() );
    }
    
    /**
     * Determine a MeshTypeIdentifier appropriate for an AttributableMeshObject
     *
     * @param sa the SubjectArea in which this AttributableMeshType lives
     * @param amo the AttributableMeshObject
     * @return a suitable Identifier
     */
    public MeshTypeIdentifier constructIdentifier(
            ExternalizedSubjectArea          sa,
            ExternalizedAttributableMeshType amo )
    {
        if( amo.getIdentifier() != null ) {
            return amo.getIdentifier();
        }

        StringBuilder idString = new StringBuilder();

        idString.append( sa.getName().value() );
        if( sa.getVersion() != null ) {
            idString.append( "_v" );
            idString.append( sa.getVersion().value() );
        }
        idString.append( "#" );
        idString.append( amo.getName().value() );
        
        return createTypeIdentifierFrom( idString.toString() );
    }

    /**
     * Determine a MeshTypeIdentifier appropriate for a PropertyType.
     *
     * @param sa the SubjectArea in which this AttributableMeshType lives
     * @param amo the AttributableMeshObject in which this PropertyType lives
     * @param pt the PropertyType
     * @return a suitable Identifier
     */
    public MeshTypeIdentifier constructIdentifier(
            ExternalizedSubjectArea          sa,
            ExternalizedAttributableMeshType amo,
            ExternalizedPropertyType         pt )
    {
        if( pt.getIdentifier() != null ) {
            return pt.getIdentifier();
        }

        StringBuilder idString = new StringBuilder();

        idString.append( sa.getName().value() );
        if( sa.getVersion() != null ) {
            idString.append( "_v" );
            idString.append( sa.getVersion().value() );
        }
        idString.append( "#" );
        idString.append( amo.getName().value() );
        
        idString.append( '/' );
        idString.append( pt.getName().value() );

        return createTypeIdentifierFrom( idString.toString() );
    }

    /**
     * Determine a MeshTypeIdentifier appropriate for a PropertyTypeGroup.
     *
     * @param sa the SubjectArea in which this AttributableMeshType lives
     * @param amo the AttributableMeshObject in which this PropertyTypeGroup lives
     * @param pt the PropertyTypeGroup
     * @return a suitable Identifier
     */
    public MeshTypeIdentifier constructIdentifier(
            ExternalizedSubjectArea          sa,
            ExternalizedAttributableMeshType amo,
            ExternalizedPropertyTypeGroup    pt )
    {
        if( amo.getIdentifier() != null ) {
            return amo.getIdentifier();
        }

        StringBuilder idString = new StringBuilder();

        idString.append( sa.getName().value() );
        if( sa.getVersion() != null ) {
            idString.append( "_v" );
            idString.append( sa.getVersion().value() );
        }
        idString.append( "#" );
        idString.append( amo.getName().value() );
        
        idString.append( '/' );
        idString.append( pt.getName().value() );

        return createTypeIdentifierFrom( idString.toString() );
    }

    /**
     * The MeshTypeLifecycleManager that we use to instantiate the objects we find.
     */
    protected MeshTypeLifecycleManager theInstantiator;

    /**
     * The ClassLoader which we use to load the XML catalog, if any.
     */
    protected ClassLoader theCatalogClassLoader;

    /**
     * Our ModelBase into which we ultimately write and against which we resolve.
     */
    protected ModelBase theModelBase;

    /**
     * The set of SubjectAreas that we have parsed and found.
     */
    protected ArrayList<ExternalizedSubjectArea> theSubjectAreas = new ArrayList<ExternalizedSubjectArea>();
}
