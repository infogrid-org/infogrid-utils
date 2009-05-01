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

import java.io.ObjectStreamException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParseException;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierUnformatFactory;

/**
  * An enumerated DataType for PropertyValues. It requires the explicit specification of
  * a domain.
  */
public class EnumeratedDataType
        extends
            DataType
        implements
            StringifierUnformatFactory,
            CanBeDumped
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Default instance.
      */
    public static EnumeratedDataType theDefault = new EnumeratedDataType();

    /**
     * Factory method.
     *
     * @param domain the programmatic domain of this EnumeratedDataType
     * @param userNameMap in the same sequence as the domain, the domain in internationalized form
     * @param userDescriptionMap in the same sequence as the domain, user-displayable descriptions of the various values
     * @param superType the DataType that we refine, if any
     * @return the created EnumeratedDataType
     */
    public static EnumeratedDataType create(
            String [] domain,
            L10Map [] userNameMap,
            L10Map [] userDescriptionMap,
            DataType  superType )
    {
        return new EnumeratedDataType( domain, userNameMap, userDescriptionMap, superType );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param domain the programmatic domain of this EnumeratedDataType
     * @param userNameMap in the same sequence as the domain, the domain in internationalized form
     * @param userDescriptionMap in the same sequence as the domain, user-displayable descriptions of the various values
     * @param superType the DataType that we refine, if any
     */
    private EnumeratedDataType(
            String [] domain,
            L10Map [] userNameMap,
            L10Map [] userDescriptionMap,
            DataType  superType )
    {
        super( superType );

        if( domain == null || domain.length == 0 ) {
            throw new IllegalArgumentException( "Cannot have empty domain for EnumeratedDataType" );
        }
        if( userNameMap == null || userNameMap.length != domain.length ) {
            throw new IllegalArgumentException( "UserNameMap must match length of EnumeratedDataType domain" );
        }
        if( userDescriptionMap != null && userDescriptionMap.length != domain.length ) {
            throw new IllegalArgumentException( "UserDescriptionMap must either be null, or match length of EnumeratedDataType domain" );
        }
        theDomain = new EnumeratedValue[ domain.length ];
        for( int i=0 ; i<domain.length ; ++i ) {
            L10Map nameMap        = userNameMap[i]; // may not be null
            L10Map descriptionMap = userDescriptionMap != null ? userDescriptionMap[i] : null;

            theDomain[i] = EnumeratedValue.create( this, domain[i], nameMap, descriptionMap );
        }
    }

    /**
     * Special constructor for the supertype singleton.
     */
    private EnumeratedDataType()
    {
        super( null );
        
        theDomain = new EnumeratedValue[0];
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
        if( this == other ) {
            return true;
        }

        if( other instanceof EnumeratedDataType ) {
            EnumeratedDataType realOther = (EnumeratedDataType) other;
            if( theDomain == null ) {
                if( realOther.theDomain == null ) {
                    return true; // FIXME? Why doesn't the first line of this method catch this? Need singleton deserialization support ...?!?
                } else {
                    return false;
                }
            }
            if( realOther.theDomain == null ) {
                return false;
            }

            // quick test first
            if( theDomain.length != realOther.theDomain.length ) {
                return false;
            }

            // then look at all values
            for( int i=0 ; i<realOther.theDomain.length ; ++i ) {
                boolean found = false;
                for( int j=0 ; j<theDomain.length ; ++j ) {
                    if( realOther.theDomain[i].equals( theDomain[j] )) {
                        found = true;
                        break;
                    }
                }
                if( ! found ) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
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
        if( other instanceof EnumeratedDataType ) {
            EnumeratedDataType realOther = (EnumeratedDataType) other;

            // quick test first
            if( theDomain.length < realOther.theDomain.length ) {
                return false;
            }
            // then look at all values
            for( int i=0 ; i<realOther.theDomain.length ; ++i ) {
                boolean found = false;
                for( int j=0 ; j<theDomain.length ; ++j ) {
                    if( realOther.theDomain[i].equals( theDomain[j] )) {
                        found = true;
                        break;
                    }
                }
                if( ! found ) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
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
        if( value instanceof EnumeratedValue ) {
            EnumeratedValue realValue = (EnumeratedValue) value;

            for( int i=0 ; i<theDomain.length ; ++i ) {
                if( realValue.equals( (Object) theDomain[i].value() )) { // EnumeratedValue compared to String is okay
                    return true;
                }
            }
            return false;
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
        return true;
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      *
      * This is used primarily for code-generation purposes.
      *
      * @param varName the name of the variable containing the value
      * @return the boolean expression
      */
    @Override
    public String getJavaDomainCheckExpression(
            String varName )
    {
        StringBuffer ret = new StringBuffer( theDomain.length * 10 );
        ret.append( "( " );
        for( int i=0 ; i<theDomain.length ; ++i ) {
            if( i>0 ) {
                ret.append( " || " );
            }
            ret.append( varName );
            ret.append( ".equals( \"" );
            ret.append( theDomain[i].value() );
            ret.append( "\" )" );
        }
        ret.append( CLOSE_PARENTHESIS_STRING );
        return ret.toString();
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public Class getCorrespondingJavaClass()
    {
        return EnumeratedValue.class;
    }

    /**
      * Obtain the domain with programmatic values.
      *
      * @return the domain with programmatic values
      */
    public EnumeratedValue [] getDomain()
    {
        return theDomain;
    }

    /**
      * Instantiate this data type into a PropertyValue with a
      * reasonable default value.
      *
      * @return a PropertyValue with a reasonable default value that is an instance of this DataType
      */
    public EnumeratedValue instantiate()
    {
        return theDomain[0];
    }

    /**
     * Obtain the default value of this DataType.
     *
     * @return the default value of this DataType
     */
    public EnumeratedValue getDefaultValue()
    {
        return theDomain[0];
    }

    /**
     * Given a programmatic string as a key, this selects the right EnumeratedValue from our domain.
     *
     * @param key the selector to find the right element of this domain
     * @return the found EnumeratedValue
     * @throws UnknownEnumeratedValueException.Key if the EnumeratedValue cannot be found
     */
    public EnumeratedValue select(
            String key )
        throws
            UnknownEnumeratedValueException.Key
    {
        EnumeratedValue ret = selectOrNull( key );
        if( ret != null ) {
            return ret;
        } else {
            throw new UnknownEnumeratedValueException.Key( this, key );
        }
    }

    /**
     * Given a programmatic string as a key, this selects the right EnumeratedValue from our domain,
     * or returns null if not found.
     *
     * @param key the selector to find the right element of this domain
     * @return the found EnumeratedValue, or null
     */
    public EnumeratedValue selectOrNull(
            String key )
    {
        for( int i=0 ; i<theDomain.length ; ++i ) {
            if( theDomain[i].equals( (Object) key )) { // compare EnumeratedValue with String is okay
                return theDomain[i];
            }
        }
        return null;
    }

    /**
     * Given a user-visible name as a key, this selects the right EnumeratedValue from our domain.
     *
     * @param userVisibleName the selector to find the right element of this domain
     * @return the found EnumeratedValue
     * @throws UnknownEnumeratedValueException.UserVisible if the EnumeratedValue cannot be found
     */
    public EnumeratedValue selectByUserVisibleName(
            String userVisibleName )
        throws
            UnknownEnumeratedValueException.UserVisible
    {
        EnumeratedValue ret = selectByUserVisibleNameOrNull( userVisibleName );
        if( ret != null ) {
            return ret;
        } else {
            throw new UnknownEnumeratedValueException.UserVisible( this, userVisibleName );
        }
    }

    /**
     * Given a user-visible name as a key, this selects the right EnumeratedValue from our domain,
     * or returns null if not found.
     *
     * @param userVisibleName the selector to find the right element of this domain
     * @return the found EnumeratedValue, or null
     */
    public EnumeratedValue selectByUserVisibleNameOrNull(
            String userVisibleName )
    {
        for( int i=0 ; i<theDomain.length ; ++i ) {
            if( userVisibleName.equals( theDomain[i].getUserVisibleName().value() )) {
                return theDomain[i];
            }
        }
        return null;
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

        if( this == theDefault ) {
            return className + DEFAULT_STRING;
        } else {
            // we make the assumption that there is a domain

            StringBuffer ret = new StringBuffer( className );
            ret.append( CREATE_STRING );
            ret.append( "new String[] { " );
            for( int i=0 ; i<theDomain.length ; ++i ) {
                ret.append( QUOTE_STRING );
                ret.append( theDomain[i].value() );
                ret.append( QUOTE_STRING );
                if( i<theDomain.length-1 ) {
                    ret.append( COMMA_STRING );
                }
            }
            ret.append( " }, new " ).append( L10Map.class.getName() ).append( "[] { " );
            for( int i=0 ; i<theDomain.length ; ++i ) {
                if( theDomain[i].getUserVisibleNameMap() != null ) {
                    ret.append( theDomain[i].getUserVisibleNameMap().getJavaConstructorString( classLoaderVar, null ));
                } else {
                    ret.append( NULL_STRING );
                }
                if( i<theDomain.length-1 ) {
                    ret.append( COMMA_STRING );
                }
            }
            ret.append( " }, new " ).append( L10Map.class.getName() ).append( "[] { " );
            for( int i=0 ; i<theDomain.length ; ++i ) {
                if( theDomain[i].getUserVisibleDescriptionMap() != null ) {
                    ret.append( theDomain[i].getUserVisibleDescriptionMap().getJavaConstructorString( classLoaderVar, null ));
                } else {
                    ret.append( NULL_STRING );
                }
                if( i<theDomain.length-1 ) {
                    ret.append( COMMA_STRING );
                }
            }
            ret.append( " }" );
            ret.append( COMMA_STRING );

            if( theSupertype != null ) {
                ret.append( theSupertype.getJavaConstructorString( classLoaderVar ));
            } else {
                ret.append( NULL_STRING );
            }
            ret.append( CLOSE_PARENTHESIS_STRING );

            return ret.toString();
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theDomain"
                },
                new Object[] {
                    theDomain
                });

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
        } else {
            return this;
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        return rep.formatEntry(
                EnumeratedValue.class,
                DEFAULT_ENTRY,
                pars,
                getDefaultValue(),
                // PropertyValue.toStringRepresentation( getDefaultValue(), rep, context, pars ),
                this,
                theDomain,
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
    public EnumeratedValue fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            PropertyValueParsingException
    {
        try {
            Object [] found = representation.parseEntry( EnumeratedValue.class, EnumeratedValue.DEFAULT_ENTRY, s, this );

            EnumeratedValue ret;

            switch( found.length ) {
                case 4:
                    ret = (EnumeratedValue) found[3];
                    break;

                default:
                    throw new PropertyValueParsingException( this, representation, s );
            }

            return ret;

        } catch( StringRepresentationParseException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex.getFormatString(), ex );

//        } catch( UnknownEnumeratedValueException ex ) {
//            throw new PropertyValueParsingException( this, representation, s, ex );
//
        } catch( ClassCastException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex );
        }
    }
    
    /**
      * The value of the domain.
      */
    protected EnumeratedValue [] theDomain;
}
