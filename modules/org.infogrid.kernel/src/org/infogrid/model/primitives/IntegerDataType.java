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

package org.infogrid.model.primitives;

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringifierException;

import java.io.ObjectStreamException;

/**
  * This is an integer DataType for PropertyValue with explicity minimum and maximum values.
  * This can carry a unit.
  */
public class IntegerDataType
        extends DataType
{
    /**
      * This is the default instance of this class. It represents
      * an int data type with no domain restriction.
      */
    public static final IntegerDataType theDefault
            = new IntegerDataType(
                    IntegerValue.create( Long.MIN_VALUE ),
                    IntegerValue.create( Long.MAX_VALUE ),
                    null,
                    null );

    /**
      * This is a default instance of this class. It represents
      * a positive or zero int data type.
      */
    public static final IntegerDataType thePositiveOrZeroDefault
            = new IntegerDataType(
                    IntegerValue.create( 0 ),
                    IntegerValue.create( Long.MAX_VALUE ),
                    null,
                    theDefault );

    /**
      * This is a default instance of this class. It represents
      * a positive int data type excluding zero.
      */
    public static final IntegerDataType thePositiveDefault
            = new IntegerDataType(
                    IntegerValue.create( 1 ),
                    IntegerValue.create( Long.MAX_VALUE ),
                    null,
                    thePositiveOrZeroDefault );

    /**
      * Construct one with minimum and maximum values (inclusive).
      * Use constants defined in java.lang.Integer to represent infinity.
      *
      * @param min the smallest allowed value
      * @param max the largest allowed value
      * @param superType the DataType that we refine, if any
      * @return the created IntegerDataType
      */
     public static IntegerDataType create(
            IntegerValue min,
            IntegerValue max,
            DataType     superType )
    {
        return new IntegerDataType( min, max, null, superType );
    }

    /**
      * Construct one with minimum and maximum values (inclusive) and a unit.
      * Use constants defined in java.lang.Integer to represent infinity.
      *
      * @param min the smallest allowed value
      * @param max the largest allowed value
      * @param u the unit
      * @param superType the DataType that we refine, if any
      * @return the created IntegerDataType
      */
     public static IntegerDataType create(
            IntegerValue min,
            IntegerValue max,
            UnitFamily   u,
            DataType     superType )
    {
        return new IntegerDataType( min, max, u, superType );
    }

    /**
     * Private constructor, use factory method instead.
     *
     * @param min the smallest allowed value
     * @param max the largest allowed value
     * @param u the unit
     * @param superType the DataType that we refine, if any
     */
    private IntegerDataType(
            IntegerValue min,
            IntegerValue max,
            UnitFamily   u,
            DataType     superType )
    {
        super( superType );

        this.theMin = min;
        this.theMax = max;

        this.theUnitFamily = u;

        if( theUnitFamily == null ) {
            if( theMin.getUnit() != null ) {
                throw new IllegalArgumentException( "Min value has unit, data type has none" );
            }
            if( theMax.getUnit() != null ) {
                throw new IllegalArgumentException( "Max value has unit, data type has none" );
            }
        } else {
            if( ! theMin.getUnit().getFamily().equals( theUnitFamily )) {
                throw new IllegalArgumentException( "Min value has wrong unit family" );
            }
            if( ! theMax.getUnit().getFamily().equals( theUnitFamily )) {
                throw new IllegalArgumentException( "Max value has wrong unit family" );
            }
        }
    }

    /**
      * Test for equality.
      *
      * @param other object to test against
      * @return true if the two objects are equal
      */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof IntegerDataType ) {
            IntegerDataType realOther = (IntegerDataType) other;
            return    ( theMin.equals( realOther.theMin ))
                   && ( theMax.equals(  realOther.theMax ))
                   && (   ( theUnitFamily == null && realOther.theUnitFamily == null )
                       || theUnitFamily.equals( realOther.theUnitFamily ));
        }
        return false;
    }

    /**
      * Test whether this DataType is a superset of or equals the argument
      * DataType. This is useful to test whether an assigment of
      * values is legal prior to attempting to do it.
      *
      * @param other the DataType we test against
      * @return if true, this DataType is a superset or equals the argument DataType
      */
    @Override
    public boolean isSupersetOfOrEquals(
            DataType other )
    {
        if( other instanceof IntegerDataType ) {
            IntegerDataType realOther = (IntegerDataType) other;
            return    ( theMin.isSmallerOrEquals( realOther.theMin ))
                   && ( realOther.theMax.isSmallerOrEquals( theMax ))
                   && (    ( theUnitFamily == null && realOther.theUnitFamily == null )
                        || theUnitFamily.equals( realOther.theUnitFamily ));
        }
        return false;
    }

    /**
     * Determine whether this PropertyValue conforms to this DataType.
     *
     * @param value the candidate PropertyValue
     * @return true if the candidate PropertyValue conforms to this type
     */
    public boolean conforms(
            PropertyValue value )
    {
        if( value instanceof IntegerValue ) {
            IntegerValue realValue = (IntegerValue) value;
            
            boolean ret = theMin.theValue <= realValue.value() && realValue.value() <= theMax.theValue;
            return ret;
        }
        return false;
    }

    /**
      * Determine whether a domain check shall be performed on
      * assignments. Default to false.
      *
      * @return if true, a domain check shall be performed prior to performing assignments
      */
    @Override
    public boolean getPerformDomainCheck()
    {
        return ( theMin.theValue != Long.MIN_VALUE ) || ( theMax.theValue != Long.MAX_VALUE );
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      *
      * This is used primarily for code-generation purposes.
      * FIXME add support for units
      *
      * @param varName the name of the variable containing the value
      * @return the boolean expression
      */
    @Override
    public String getJavaDomainCheckExpression(
            String varName )
    {
        if( theMin.theValue == Long.MIN_VALUE ) {
            if( theMax.theValue == Long.MAX_VALUE ) {
                return "true";
            } else {
                return "( " + varName + ".value() <= " + String.valueOf( theMax ) + " )";
            }
        } else {
            if( theMax.theValue == Long.MAX_VALUE ) {
                return "( " + varName + ".value() >= " + String.valueOf( theMin.value() ) + " )";
            } else {
                return "(    ( " + varName + ".value() >= " + String.valueOf( theMin.value() ) + " )"
                   +    " && ( " + varName + ".value() <= " + String.valueOf( theMax.value() ) + " ) )";
            }
        }
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public Class getCorrespondingJavaClass()
    {
        return IntegerValue.class;
    }

    /**
      * Instantiate this data type into a PropertyValue with a
      * reasonable default value.
      *
      * @return a PropertyValue with a reasonable default value that is an instance of this DataType
      */
    public PropertyValue instantiate()
    {
        return getDefaultValue();
    }

    /**
     * Obtain the default value of this DataType.
     *
     * @return the default value of this DataType
     */
    public PropertyValue getDefaultValue()
    {
        if( theUnitFamily == null ) {
            if( theMin.theValue <= 0 && theMax.theValue >= 0 ) {
                return IntegerValue.create( 0 );
            }
            return IntegerValue.create( theMin.theValue );
        } else {
            if( theMin.theValue <= 0 && theMax.theValue >= 0 ) {
                return IntegerValue.create( 0, theUnitFamily.getUnitsInFamily()[0] );
            }
            return IntegerValue.create( theMin.theValue, theUnitFamily.getUnitsInFamily()[0] );
        }
    }

    /**
      * Obtain the smallest allowed value.
      *
      * @return the smallest allowed value
      */
    public IntegerValue getMinimum()
    {
        return theMin;
    }

    /**
      * Obtain the largest allowed value.
      *
      * @return the largest allowed value
      */
    public IntegerValue getMaximum()
    {
        return theMax;
    }

    /**
      * Obtain the unit family, if any.
      *
      * @return the UnitFamily
      */
    public UnitFamily getUnitFamily()
    {
        return theUnitFamily;
    }

    /**
     * Correctly deserialize a static instance.
     *
     * @return the static instance if appropriate
     * @throws ObjectStreamException thrown if reading from the stream failed
     */
    public Object readResolve()
        throws
            ObjectStreamException
    {
        if( this.equals( theDefault )) {
            return theDefault;
        } else if( this.equals( thePositiveDefault )) {
            return thePositiveDefault;
        } else if( this.equals( thePositiveOrZeroDefault )) {
            return thePositiveOrZeroDefault;
        } else {
            return this;
        }
    }

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
    public String getJavaConstructorString(
            String classLoaderVar )
    {
        final String className = getClass().getName();

        if( this == theDefault )
            return className + DEFAULT_STRING;
        else if( this == thePositiveDefault )
            return className + ".thePositiveDefault";
        else if( this == thePositiveOrZeroDefault )
            return className + ".thePositiveOrZeroDefault";
        else
        {
            StringBuffer ret = new StringBuffer( className );
            ret.append( CREATE_STRING );

            if( theMin != null ) {
                ret.append( theMin.getJavaConstructorString( classLoaderVar, null ));
            } else {
                ret.append( NULL_STRING );
            }
            ret.append( COMMA_STRING );

            if( theMax != null ) {
                ret.append( theMax.getJavaConstructorString( classLoaderVar, null ));
            } else {
                ret.append( NULL_STRING );
            }

            ret.append( COMMA_STRING );

            if( theUnitFamily != null ) {
                ret.append( theUnitFamily.getJavaConstructorString( classLoaderVar ));
            } else {
                ret.append( NULL_STRING );
            }

            ret.append( COMMA_STRING );

            if( theSupertype != null ) {
                ret.append( theSupertype.getJavaConstructorString( classLoaderVar ));
            } else {
                ret.append( NULL_STRING );
            }

            ret.append( CLOSE_PAREN_STRING );
            return ret.toString();
        }
    }

    /**
     * Convert this PropertyValue to its String representation, using the representation scheme.
     *
     * @param representation the representation scheme
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation representation )
    {
        return representation.formatEntry(
                RESOURCEHELPER,
                DEFAULT_ENTRY,
                PropertyValue.toStringRepresentation( getDefaultValue(), representation ),
                PropertyValue.toStringRepresentation( theMin,            representation ),
                PropertyValue.toStringRepresentation( theMax,            representation ),
                theUnitFamily,
                theSupertype );
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
    public IntegerValue fromStringRepresentation(
            StringRepresentation representation,
            String                      s )
        throws
            PropertyValueParsingException
    {
        try {
            Object [] found = representation.parseEntry( IntegerValue.RESOURCEHELPER, IntegerValue.DEFAULT_ENTRY, s );

            IntegerValue ret;

            switch( found.length ) {
                case 1:
                    ret = IntegerValue.create( (Integer) found[0] );
                    break;

                default:
                    throw new PropertyValueParsingException( this, representation, s );
            }

            return ret;

        } catch( StringifierException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex );

        } catch( ClassCastException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex );
        }
    }

    /**
      * The value for the minimum.
      */
    protected IntegerValue theMin;

    /**
      * The value for the maximum.
      */
    protected IntegerValue theMax;

    /**
      * The unit family (if any).
      */
    protected UnitFamily theUnitFamily;
    
    /**
     * Our ResourceHelper.
     */
    static final ResourceHelper RESOURCEHELPER = IntegerValue.RESOURCEHELPER;
}
