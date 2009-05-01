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

package org.infogrid.model.primitives;

import java.io.Serializable;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationParameters;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.SimpleStringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierUnformatFactory;

/**
  * This represents a data type for properties. This is an abstract class;
  * a more suitable subclass shall be instantiated.
  * A DataType can refine another DataType and usually does.
  *
  * Note: This is different from a value for properties:
  * Instances of (a subtype of) DataType represent the notion of a type,
  * such as "a positive integer", while instances of (a subtype of)
  * PropertyValue represent a value, such as "7".
  */
public abstract class DataType
        implements
            HasStringRepresentation,
            StringifierUnformatFactory,
            Serializable
{
    /**
      * Constructor for subclasses only.
      *
      * @param supertype the DataType that we refine, if any
      */
    protected DataType(
            DataType supertype )
    {
        theSupertype = supertype;
    }

    /**
      * Obtain the name of the DataType.
      *
      * @return the name of the DataType
      */
    public final String getName()
    {
        return this.getClass().getName();
    }

    /**
      * Test for equality.
      *
      * @param other object to test against
      * @return true if the two objects are equal
      */
    @Override
    public abstract boolean equals(
            Object other );

    /**
      * Test whether this DataType is a superset of or equals the argument
      * DataType. This is useful to test whether an assigment of
      * values is legal prior to attempting to do it. By default, this delegates
      * to the equals method.
      *
      * @param other the DataType we test against
      * @return if true, this DataType is a superset or equals the argument DataType
      */
    public boolean isSupersetOfOrEquals(
            DataType other )
    {
        return equals( other );
    }

    /**
     * Determine whether this PropertyValue conforms to this DataType.
     *
     * @param value the candidate PropertyValue
     * @return true if the candidate PropertyValue conforms to this type
     */
    public abstract boolean conforms(
            PropertyValue value );

    /**
      * Determine whether a domain check shall be performed on
      * assignments. Default to false.
      *
      * @return if true, a domain check shall be performed prior to performing assignments
      */
    public boolean getPerformDomainCheck()
    {
        return false;
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      * For example, on an integer data type with minimum and maximum
      * values, one would call it with argument "theValue", and it could
      * return: "( theValue > 2 && theValue < 7 )", where "2" and "7" are
      * the values of the min and max attributes of the integer data type.
      *
      * This is used primarily for code-generation purposes.
      *
      * @param varName the name of the variable containing the value
      * @return the Java language  expression
      */
    public String getJavaDomainCheckExpression(
            String varName )
    {
        return "true";
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public abstract Class getCorrespondingJavaClass();

    /**
     * Obtain a value expression in the Java language that invokes the constructor
     * of factory method of the underlying concrete class, thereby creating or
     * reusing an instance of the underlying concrete class that is identical
     * to the instance on which this method was invoked.
     *
     * This is used primarily for code-generation purposes.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @return the Java language expression
     */
    public abstract String getJavaConstructorString(
            String classLoaderVar );

    /**
      * Instantiate this data type into a PropertyValue with a
      * reasonable default value.
      *
      * @return a PropertyValue with a reasonable default value that is an instance of this DataType
      */
    public abstract PropertyValue instantiate();

    /**
     * If this DataType is a refinement of another, find which DataType it refines.
     *
     * @return the refined DataType, if any
     */
    public final DataType getSupertype()
    {
        return theSupertype;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * Obtain a PropertyValue that corresponds to this PropertyType, based on the String representation
     * of the PropertyValue.
     * 
     * @param representation the StringRepresentation in which the String s is given
     * @param s the String
     * @return the PropertyValue
     * @throws PropertyValueParsingException thrown if the String representation could not be parsed successfully
     */
    public abstract PropertyValue fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            PropertyValueParsingException;

    /**
     * Emit String representation of a null PropertyValue of this PropertyType.
     *
     * @param representation the representation scheme
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return the String representation
     */
    public String nullValueStringRepresentation(
            StringRepresentation           representation,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        PropertyValue defaultValue = instantiate();

        Object editVariable;
        Object meshObject;
        Object propertyType;
        Object nullString;
        if( pars != null ) {
            editVariable = pars.get( StringRepresentationParameters.EDIT_VARIABLE );
            meshObject   = pars.get( ModelPrimitivesStringRepresentationParameters.MESH_OBJECT );
            propertyType = pars.get( ModelPrimitivesStringRepresentationParameters.PROPERTY_TYPE );
            nullString   = pars.get( StringRepresentationParameters.NULL_STRING );
        } else {
            editVariable = null;
            meshObject   = null;
            propertyType = null;
            nullString   = null;
        }

        return representation.formatEntry(
                defaultValue.getClass(),
                NULL_ENTRY,
                pars,
        /* 0 */ editVariable,
        /* 1 */ meshObject,
        /* 2 */ propertyType,
        /* 3 */ this,
        /* 4 */ PropertyValue.toStringRepresentation(
                        defaultValue,
                        representation.getStringRepresentationDirectory().get( SimpleStringRepresentationDirectory.TEXT_PLAIN_NAME ),
                        context,
                        pars ),
        /* 5 */ nullString );
     }

    /**
     * The supertype of this DataType (if any).
     */
    protected DataType theSupertype;

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String CREATE_STRING = ".create( ";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String COMMA_STRING = ", ";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String NULL_STRING = "null";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String QUOTE_STRING = "\"";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String DEFAULT_STRING = ".theDefault";

    /**
     * String constant used in our subclasses in order to avoid using up more memory than necessary.
     */
    public static final String CLOSE_PARENTHESIS_STRING = " )";

    /**
     * The default entry in the resource files for a DataType, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "Type";

    /**
     * The default entry in the resource files for a null value of this DataType, prefixed by the StringRepresentation's prefix.
     */
    public static final String NULL_ENTRY = "Null";
}
