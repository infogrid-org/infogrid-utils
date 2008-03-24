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

import java.util.Calendar;
import java.util.Date;

/**
  * This is a time stamp value for PropertyValues. Its values generally
  * are represented in UTC, we will support different time zones at some time
  * in the future.
  */
public final class TimeStampValue
        extends
            PropertyValue
{
    /**
     * Factory method.
     *
     * @param year the number of years
     * @param month the number of months
     * @param day the number of days
     * @param hour the number of hours
     * @param minute the number of minutes
     * @param second the snumber of seconds, plus fractions
     * @return the created TimePeriodValue
     */
    public static TimeStampValue create(
            short year,
            short month,
            short day,
            short hour,
            short minute,
            float second )
    {
        return new TimeStampValue( year, month, day, hour, minute, second );
    }

    /**
     * Factory method.
     *
     * @param value time in the format returned by System.currentTimeMillis()
     * @return the created TimePeriodValue
     */
    public static TimeStampValue create(
            long value )
    {
        Calendar theDate = Calendar.getInstance();

        theDate.setTime( new Date(value) ); // needed for JDK 1.3 compatibility

        return create( theDate );
    }

    /**
     * Factory method.
     *
     * @param value value as a Calendar
     * @return the created TimeStampValue
     */
    public static TimeStampValue create(
            Calendar value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }

        return new TimeStampValue(
                (short) value.get( Calendar.YEAR ),
                (short) ( value.get( Calendar.MONTH ) + 1 ), // Calendar counts from 0
                (short) value.get( Calendar.DAY_OF_MONTH ), // Calendar counts from 1
                (short) value.get( Calendar.HOUR_OF_DAY ),
                (short) value.get( Calendar.MINUTE ),
                value.get( Calendar.SECOND ) + (0.001f * value.get( Calendar.MILLISECOND )) );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value value as a Calendar
     * @return the created TimeStampValue, or null
     */
    public static TimeStampValue createOrNull(
                Calendar value )
    {
        if( value == null ) {
            return null;
        }

        return new TimeStampValue(
                (short) value.get( Calendar.YEAR ),
                (short) value.get( Calendar.MONTH + 1 ), // Calendar counts from 0
                (short) value.get( Calendar.DAY_OF_MONTH ), // Calendar counts from 1
                (short) value.get( Calendar.HOUR_OF_DAY ),
                (short) value.get( Calendar.MINUTE ),
                value.get( Calendar.SECOND ) + (0.001f * value.get( Calendar.MILLISECOND )) );
    }

    /**
     * Factory method.
     *
     * @param value value as a Date
     * @return the created TimeStampValue
     */
    public static TimeStampValue create(
            Date value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime( value );

        return create( cal );
    }

    /**
     * Factory method, or return null if the argument is null.
     *
     * @param value value as a Date
     * @return the created TimeStampValue, or null
     */
    public static TimeStampValue createOrNull(
            Date value )
    {
        if( value == null ) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime( value );

        return create( cal );
    }

    /**
      * Private constructor, use factory method.
      *
      * @param year the number of years
      * @param month the number of months
      * @param day the number of days
      * @param hour the number of hours
      * @param minute the number of minutes
      * @param second the snumber of seconds, plus fractions
      */
    private TimeStampValue(
            short year,
            short month,
            short day,
            short hour,
            short minute,
            float second )
    {
        if( month < 1 || month > 12 ) {
            throw new InvalidTimeStampValueException.Month( year, month, day, hour, minute, second );
        }
        if( day < 1 || day > 31 ) {
            throw new InvalidTimeStampValueException.Day( year, month, day, hour, minute, second );
        }
        if( hour < 0 || hour > 23 ) {
            throw new InvalidTimeStampValueException.Hour( year, month, day, hour, minute, second );
        }
        if( minute < 0 || minute > 59 ) {
            throw new InvalidTimeStampValueException.Minute( year, month, day, hour, minute, second );
        }
        if( second < 0. || second >= 60. ) {
            throw new InvalidTimeStampValueException.Second( year, month, day, hour, minute, second );
        }

        this.theYear   = year;
        this.theMonth  = month;
        this.theDay    = day;
        this.theHour   = hour;
        this.theMinute = minute;
        this.theSecond = second;
    }

    /**
      * Determine year value.
      *
      * @return the years
      */
    public short getYear()
    {
        return theYear;
    }

    /**
      * Determine month value.
      *
      * @return the months
      */
    public short getMonth()
    {
        return theMonth;
    }

    /**
      * Determine day value.
      *
      * @return the days
      */
    public short getDay()
    {
        return theDay;
    }

    /**
      * Determine hour value.
      *
      * @return the hours
      */
    public short getHour()
    {
        return theHour;
    }

    /**
      * Determine minute value.
      *
      * @return the minutes
      */
    public short getMinute()
    {
        return theMinute;
    }

    /**
      * Determine second value.
      *
      * @return the seconds plus fractions
      */
    public float getSecond()
    {
        return theSecond;
    }

    /**
     * Obtain the underlying value.
     *
     * @return the underlying value
     */
    public String value()
    {
        return toString();
    }

    /**
     * Obtain this as a java.util.Date.
     *
     * @return this value as java.util.Date
     */
    public Date getAsDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.set( theYear, theMonth, theDay, theHour, theMinute, (int) theSecond );

        // FIXME -- millis?

        return cal.getTime();
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
        if( otherValue instanceof TimeStampValue ) {
            return (theYear   == ((TimeStampValue)otherValue).theYear)
                && (theMonth  == ((TimeStampValue)otherValue).theMonth)
                && (theDay    == ((TimeStampValue)otherValue).theDay)
                && (theHour   == ((TimeStampValue)otherValue).theHour)
                && (theMinute == ((TimeStampValue)otherValue).theMinute)
                && (theSecond == ((TimeStampValue)otherValue).theSecond);
        }
        return false;
    }

    /**
     * Convert to long representation like System.currentTimeMillis().
     *
     * @return long representation of this TimeStampValue
     */
    public long getAsLong()
    {
        return getAsDate().getTime();
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( theYear );
        buf.append( "/" );
        buf.append( theMonth );
        buf.append( "/" );
        buf.append( theDay );
        buf.append( " " );
        buf.append( theHour );
        buf.append( ":" );
        buf.append( theMinute );
        buf.append( ":" );
        buf.append( theSecond );
        return buf.toString();
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
        StringBuffer buf = new StringBuffer( 256 );
        buf.append( getClass().getName() );
        buf.append( ".create( (short) " );
        buf.append( theYear );
        buf.append( ", (short) " );
        buf.append( theMonth );
        buf.append( ", (short) " );
        buf.append( theDay );
        buf.append( ", (short) " );
        buf.append( theHour );
        buf.append( ", (short) " );
        buf.append( theMinute );
        buf.append( ", (float) " );
        buf.append( theSecond );
        buf.append( " )" );
        return buf.toString();
    }

    /**
     * This creates an instance initialized with the current time.
     *
     * @return the current time
     */
    public static TimeStampValue now()
    {
        Calendar currentDate = Calendar.getInstance();
        return new TimeStampValue(
                (short) currentDate.get( Calendar.YEAR ),
                (short) ( currentDate.get( Calendar.MONTH ) + 1 ),   // month counts from zero
                (short) currentDate.get( Calendar.DAY_OF_MONTH ),
                (short) currentDate.get( Calendar.HOUR_OF_DAY ),
                (short) currentDate.get( Calendar.MINUTE ),
                currentDate.get( Calendar.SECOND )
                    + currentDate.get( Calendar.MILLISECOND )/1000.0f );
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
        TimeStampValue realOther = (TimeStampValue) o;

        return compare( this, realOther );
    }

    /**
     * Compare two instances of this class according to the rules of java.util.Comparator.
     *
     * @param valueOne the first value
     * @param valueTwo the second value
     * @return 0 if both are the same, -1 if the first is smaller than the second, 1 if the second is smaller that the first
     */
    public static int compare(
            TimeStampValue valueOne,
            TimeStampValue valueTwo )
    {
        if( valueOne == null ) {
            if( valueTwo == null ) {
                return 0;
            } else {
                return +1;
            }
        } else {
            if( valueTwo == null ) {
                return -1;
            } else {
                int delta;
                delta = valueOne.theYear - valueTwo.theYear;
                if( delta != 0 ) {
                    return delta;
                }
                delta = valueOne.theMonth - valueTwo.theMonth;
                if( delta != 0 ) {
                    return delta;
                }
                delta = valueOne.theDay - valueTwo.theDay;
                if( delta != 0 ) {
                    return delta;
                }
                delta = valueOne.theHour - valueTwo.theHour;
                if( delta != 0 ) {
                    return delta;
                }
                delta = valueOne.theMinute - valueTwo.theMinute;
                if( delta != 0 ) {
                    return delta;
                }

                if( valueOne.theSecond < valueTwo.theSecond ) {
                    return -1;
                } else if( valueOne.theSecond == valueTwo.theSecond ) {
                    return 0;
                } else {
                    return +1;
                }
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
        return representation.formatEntry(
                RESOURCEHELPER,
                DEFAULT_ENTRY,
                theYear,
                theMonth,
                theDay,
                theHour,
                theMinute,
                theSecond,
                (int) theSecond,
                ((int) ( theSecond * 1000 )) % 1000 );
    }

    /**
      * The real year value.
      */
    protected short theYear;

    /**
      * The real month value.
      */
    protected short theMonth;

    /**
      * The real day value.
      */
    protected short theDay;

    /**
      * The real hour value.
      */
    protected short theHour;

    /**
      * The real minute value.
      */
    protected short theMinute;

    /**
      * The real second value.
      */
    protected float theSecond;

    /**
     * Our ResourceHelper.
     */
    static final ResourceHelper RESOURCEHELPER = ResourceHelper.getInstance( TimeStampValue.class );    
}
