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

import java.awt.geom.Point2D;

/**
  * This is a point-in-space value for PropertyValues.
  */
public final class PointValue
        extends
            PropertyValue
{
    /**
     * Factory method.
     *
     * @param value the value as Point2D
     * @return the created PointValue
     * @throws IllegalArgumentException if null value is given
     */
    public static PointValue create(
            Point2D value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        return new PointValue( value.getX(), value.getY() );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value the value as Point2D
     * @return the created PointValue, or null
     * @throws IllegalArgumentException if null value is given
     */
    public static PointValue createOrNull(
            Point2D value )
    {
        if( value == null ) {
            return null;
        }
        return new PointValue( value.getX(), value.getY() );
    }

    /**
     * Factory method.
     *
     * @param xx xx dimension of the point
     * @param yy yy dimension of the point
     * @return the created PointValue
     */
    public static PointValue create(
            double xx,
            double yy )
    {
        return new PointValue( xx, yy );
    }

    /**
      * Private constructor.
      *
      * @param xx xx dimension of the point
      * @param yy yy dimension of the point
      */
    private PointValue(
            double xx,
            double yy )
    {
        this.x = xx;
        this.y = yy;
    }

    /**
      * Obtain value as Point2D.
      *
      * @return value as Point2D
      */
    public Point2D value()
    {
        return new Point2D.Double( x, y );
    }

    /**
      * Determine X coordinate.
      *
      * @return the X coordinate
      */
    public double getX()
    {
        return x;
    }

    /**
      * Determine Y coordinate.
      *
      * @return the Y coordinate
      */
    public double getY()
    {
        return y;
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return true if the objects are equal
      */
    @Override
    public boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof PointValue ) {
            return (x == ((PointValue)otherValue).x)
                && (y == ((PointValue)otherValue).y);
        }
        return false;
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        return "(" + x + ";" + y + ")";
    }

    /**
     * Obtain a string which is the Java-language constructor expression reflecting this value.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @param typeVar  name of the variable containing the DatatType that goes with the to-be-created instance.
     * @return the Java-language constructor expression
     */
    public String getJavaConstructorString(
            String classLoaderVar,
            String typeVar )
    {
        StringBuffer buf = new StringBuffer( 64 );
        buf.append( getClass().getName());
        buf.append( DataType.CREATE_STRING );
        buf.append( x );
        buf.append( ", " );
        buf.append( y );
        buf.append( DataType.CLOSE_PAREN_STRING );
        return buf.toString();
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param   o the PropertyValue to be compared.
     * @return  a negative integer, zero, or a positive integer as this object
     *		is less than, equal to, or greater than the specified object.
     *
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this Object.
     */
    public int compareTo(
            PropertyValue o )
    {
        PointValue realOther = (PointValue) o;

        if( x < realOther.x ) {
            if( y < realOther.y ) {
                return -1;
            } else {
                return +2; // not comparable convention: +2
            }
        } else if( x == realOther.x ) {
            if( y == realOther.y ) {
                return 0;
            } else {
               return +2; // not comparable convention: +2
            }
        } else {
            if( y > realOther.y ) {
                return +1;
            } else {
                return +2; // not comparable convention: +2
            }
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
        return representation.formatEntry( RESOURCEHELPER, DEFAULT_ENTRY, x, y );
    }

    /**
      * The real value for the x component.
      */
    protected double x;

    /**
      * The real value for the y component.
      */
    protected double y;

    /**
     * Our ResourceHelper.
     */
    static final ResourceHelper RESOURCEHELPER = ResourceHelper.getInstance( PointValue.class );    
}
