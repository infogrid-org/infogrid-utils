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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import org.infogrid.util.PortableIcon;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * This is a Binary Large Object (BLOB) value for PropertyValues.
 * It can use any MIME-type to specify the formatting,
 * but practically only some are supported by the rest of the code.
 *
 * This has several inner classes as subtypes. One uses a byte[] representation,
 * another String, and a third and new one is a "delayed-loading" one.
 */
public abstract class BlobValue
        extends
            PropertyValue
{
    private static final Log log = Log.getLogInstance( BlobValue.class ); // our own, private logger
    private static final long serialVersionUID = 1382201442397779167L;    // helps with serialization

    /**
     * Factory method to construct one from unformatted, plain text.
     *
     * @param value the unformatted text
     * @return the created BlobValue
     */
    public static BlobValue create(
            String value )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }

        return new BlobValue.StringBlob( value, TEXT_PLAIN_MIME_TYPE );
    }

    /**
     * Factory method to construct one from unformatted, plain text, or to return
     * null if the argument is null.
     *
     * @param value the unformatted text
     * @return the created BlobValue, or null
     */
    public static BlobValue createOrNull(
            String value )
    {
        if( value == null ) {
            return null;
        }

        return new BlobValue.StringBlob( value, TEXT_PLAIN_MIME_TYPE );
    }

    /**
     * Factory method to construct one with formatted text according
     * to a certain MIME type in "abc/def" format.
     *
     * @param value the formatted text
     * @param mimeType the MIME type of the text, in "abc/def" format
     * @return the created BlobValue
     */
    public static BlobValue create(
            String value,
            String mimeType )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        if( mimeType == null ) {
            mimeType = NULL_MIME_TYPE;
        }

        return new BlobValue.StringBlob( value, mimeType );
    }

    /**
     * Factory method to construct one with formatted text according
     * to a certain MIME type in "abc/def" format, or to return null if the
     * value argument is null.
     *
     * @param value the formatted text
     * @param mimeType the MIME type of the text, in "abc/def" format
     * @return the created BlobValue, or null
     */
    public static BlobValue createOrNull(
            String value,
            String mimeType )
    {
        if( value == null ) {
            return null;
        }

        if( mimeType == null ) {
            mimeType = NULL_MIME_TYPE;
        }

        return new BlobValue.StringBlob( value, mimeType );
    }

    /**
      * Factory method to construct one with formatted text according
      * to a certain MIME type in "abc/def" format.
      *
      * @param value the formatted text
      * @param mimeType the MIME type of the text, in "abc/def" format
      * @return the created BlobValue
      */
    public static BlobValue create(
            byte [] value,
            String  mimeType )
    {
        if( value == null ) {
            throw new IllegalArgumentException( "null value" );
        }
        if( mimeType == null ) {
            mimeType = NULL_MIME_TYPE;
        }

        return new BlobValue.ByteBlob( value, mimeType );
    }

    /**
     * Factory method to construct one with formatted text according
     * to a certain MIME type in "abc/def" format, or to return null
     * if the argument is null.
     *
     * @param value the formatted text
     * @param mimeType the MIME type of the text, in "abc/def" format
     * @return the created BlobValue, or null
     */
    public static BlobValue createOrNull(
            byte [] value,
            String  mimeType )
    {
        if( value == null ) {
            return null;
        }
        if( mimeType == null ) {
            mimeType = NULL_MIME_TYPE;
        }

        return new BlobValue.ByteBlob( value, mimeType );
    }

    /**
     * Factory method to construct one with a certain MIME type by loading it from
     * a certain path relative to a given ClassLoader.
     *
     * @param loader the ClassLoader through which this is loaded
     * @param loadFrom the location relative to loader from which we load
     * @param mimeType the MIME type of the BlobValue, in "abc/def" format
     * @return the created BlobValue
     */
    public static BlobValue createByLoadingFrom(
            ClassLoader loader,
            String      loadFrom,
            String      mimeType )
    {
        if( loadFrom == null ) {
            throw new IllegalArgumentException( "null loadFrom" );
        }
        if( mimeType == null ) {
            throw new IllegalArgumentException( "null mime type" );
        }

        return new BlobValue.DelayedByteBlob( loader, loadFrom, mimeType );
    }

    /**
     * Factory method to construct one with a certain MIME type by loading it from
     * a certain path relative to the default ClassLoader.
     *
     * @param loadFrom the location relative to the default ClassLoader
     * @param mimeType the MIME type of the BlobValue, in "abc/def" format
     * @return the created BlobValue
     */
    public static BlobValue createByLoadingFrom(
            String      loadFrom,
            String      mimeType )
    {
        return new BlobValue.DelayedByteBlob( null, loadFrom, mimeType );
    }

    /**
     * Factory method for a new BlobValue by loading it from an input stream.
     *
     * @param theStream the stream from which the BlobValue shall be loaded
     * @param mimeType the MIME type of the BlobValue, in "abc/def" format
     * @return the created BlobValue
     * @throws IOException thrown if there is a read error from the stream
     */
    public static BlobValue create(
            InputStream theStream,
            String      mimeType )
        throws
            IOException
    {
        byte [] theValue = new byte[0];

        while( true ) {
            byte [] buf = new byte[ 512 ];
            int length = theStream.read( buf );

            if( length < 0 ) {
                break;
            }

            byte [] newValue = new byte[ theValue.length + length ];
            int i;
            for( i=0 ; i<theValue.length ; ++i ) {
                newValue[i] = theValue[i];
            }
            for( int j=0 ; j<length ; ++i, ++j ) {
                newValue[i] = buf[j];
            }

            theValue = newValue;

            if( length < buf.length ) {
                break;
            }
        }
        return new BlobValue.ByteBlob( theValue, mimeType );
    }

    /**
     * Helper method to create an array of BlobValues from Strings.
     *
     * @param raw the array of Strings, or null
     * @return the corresponding array of raw, or null
     */
    public static BlobValue [] createMultiple(
            String [] raw )
    {
        if( raw == null ) {
            return null;
        }
        BlobValue [] ret = new BlobValue[ raw.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = BlobValue.create( raw[i] );
        }
        return ret;
    }

    /**
     * Helper method to create an array of BlobValues from byte arrays.
     *
     * @param raw the array of byte arrays, or null
     * @param mimeType the MIME type of the byte arrays, in the same sequence
     * @return the corresponding array of raw, or null
     */
    public static BlobValue [] createMultiple(
            byte [][] raw,
            String    mimeType )
    {
        if( raw == null ) {
            return null;
        }
        BlobValue [] ret = new BlobValue[ raw.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = BlobValue.create( raw[i], mimeType );
        }
        return ret;
    }

    /**
     * Inverse operation of loadFrom. Saves binary data back to a stream.
     *
     * @param theStream the stream to write this object to
     * @throws IOException thrown if a write error occurred
     */
    public abstract void saveTo(
            OutputStream theStream )
        throws
            IOException;

    /**
     * Protected constructor for subclasses only.
     *
     * @param mimeType the MIME type of the BlobValue, in "abc/def" format
     */
    protected BlobValue(
            String mimeType )
    {
        this.theMimeType = mimeType;
    }

    /**
     * Obtain the content of this BlobValue in byte[] format.
     *
     * @return the content of this BlobValue in byte[] format
     */
    public abstract byte[] value();

    /**
      * Obtain the content of this BlobValue in String format. This method will
      * interpret the BlobValue as a string in Unicode format, regardless.
      *
      * @return the content of this BlobValue in String format
      */
    public abstract String getAsString();

    /**
     * Obtain the MIME type of this BlobValue.
     *
     * @return the MIME type of this BlobValue in "abc/def" forma
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * If this is non-null, this BlobValue is a delayed-loading Blob and the return value
     * is the relative path from which the content shall be loaded.
     *
     * @return the relative path from where the content shall be loaded
     */
    public String delayedLoadingFrom()
    {
        return null;
    }

    /**
     * Convenience method to turn this BlobValue into an Icon.
     *
     * @return the content of this BlobValue as an icon.
     * @throws IllegalArgumentException if this is not an icon BlobValue
     */
    public abstract PortableIcon getAsIcon()
        throws
            IllegalArgumentException;

    /**
     * Determine whether these two BlobValues have the same MIME type.
     *
     * @param other the BlobValue to test against
     * @return if true, this and other have the same MIME type
     */
    public boolean mimeTypesEquals(
            BlobValue other )
    {
        if( theMimeType == null ) {
            return other.theMimeType == null;
        }

        if( ! theMimeType.equals( other.theMimeType )) {
            return false;
        }

        return true;
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
        // type cast to ensure ClassCastException
        if( equals( (BlobValue) o )) {
            return 0;
        } else {
            return +2; // not comparable convention: +2
        }
    }

   /**
    * Encode byte data into hex format.
    *
    * @param data the data to convert into hex format
    * @return the content of data in hex format
    */
    public static String encodeHex(
            byte[] data )
    {
        StringBuffer almostRet = new StringBuffer( data.length*2 );

        for( int i=0; i<data.length; ++i ) {
            int current = ( data[i] >> 4 ) & 0xf;
            if( current <= 9 ) {
                almostRet.append( (char) ('0' + current ));
            } else {
                almostRet.append( (char) ('a' + current - 10 ));
            }
            current = data[i] & 0xf;
            if( current <= 9 ) {
                almostRet.append( (char) ('0' + current ));
            } else {
                almostRet.append( (char) ('a' + current - 10 ));
            }
        }
        return almostRet.toString();
   }

   /**
    * Decode byte data from hex format.
    *
    * @param raw the data in hex format
    * @return the original data, decoded
    */
    public static byte [] decodeHex(
            String raw )
    {
        byte [] ret = new byte[ raw.length() / 2 ];
        char [] c   = raw.toCharArray();

        for( int i=0 ; i<ret.length ; ++i ) {
            char current = c[2*i];
            if( current >= '0' && current <= '9' ) {
                ret[i] = (byte) ( (( current - '0' ) & 0xf) << 4 );
            } else if( current >= 'a' && current <= 'f' ) {
                ret[i] = (byte) ( (( current - 'a' + 10 ) & 0xf) << 4 );
            } else if( current >= 'A' && current <= 'F' ) {
                ret[i] = (byte) ( (( current - 'A' + 10 ) & 0xf) << 4 );
            } else {
                throw new IllegalArgumentException();
            }
            current = c[2*i+1];
            if( current >= '0' && current <= '9' ) {
                ret[i] |= ( current - '0' ) & 0xf;
            } else if( current >= 'a' && current <= 'f' ) {
                ret[i] |= ( current - 'a' + 10 ) & 0xf;
            } else if( current >= 'A' && current <= 'F' ) {
                ret[i] |= ( current - 'A' + 10 ) & 0xf;
            } else {
                throw new IllegalArgumentException();
            }
        }
        return ret;
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        if( getMimeType().startsWith( "text" )) {
            return rep.formatEntry(
                    getClass(),
                    "TextString",
                    theMimeType,
                    value(),
                    getAsString());
        } else {
            return rep.formatEntry(
                    getClass(),
                    "ByteString",
                    theMimeType,
                    value() );
        }
    }
        
    /**
      * The MIME type of this BlobValue.
      */
    protected String theMimeType;

    /**
     * Pre-defined MIME type for text/plain.
     */
    public static final String TEXT_PLAIN_MIME_TYPE = "text/plain";

    /**
     * Pre-defined MIME type for text/html.
     */
    public static final String TEXT_HTML_MIME_TYPE = "text/html";

    /**
     * Pre-defined MIME type for image/gif.
     */
    public static final String IMAGE_GIF_MIME_TYPE = "image/gif";

    /**
     * Pre-defined MIME type for image/jpg.
     */
    public static final String IMAGE_JPG_MIME_TYPE = "image/jpg";

    /**
     * Pre-defined MIME type for image/png.
     */
    public static final String IMAGE_PNG_MIME_TYPE = "image/png";

    /**
     * Pre-defined MIME type for unknown.
     */
    public static final String NULL_MIME_TYPE = "application/octet-stream";

    /**
     * This private subclass of BlobValue stores the data as a String.
     */
    protected static final class StringBlob
            extends
                BlobValue
    {
        private static final long serialVersionUID = 6861928152343433099L;

        /**
         * Private constructor.
         *
         * @param value the content of the BlobValue
         * @param mimeType the MIME type of the BlobValue in "abc/def" format
         */
        private StringBlob(
                String value,
                String mimeType )
        {
            super( mimeType );

            this.theValue = value;
        }

        /**
         * Obtain the content of this BlobValue in byte[] format.
         *
         * @return the content of this BlobValue in byte[] format
         */
        public byte [] value()
        {
            return theValue.getBytes();
        }

        /**
         * Obtain the content of this BlobValue in String format.
         *
         * @return the content of this BlobValue in String format
         */
        public String getAsString()
        {
            return theValue;
        }

        /**
         * Convenience method to turn this into an Icon. We cannot do this for this
         * subtype.
         *
         * @return never provided, always throws exception
         * @throws IllegalArgumentException always thrown
         */
        public PortableIcon getAsIcon()
            throws
                IllegalArgumentException
        {
            throw new IllegalArgumentException( "StringBlob cannot be converted into icon: " + theValue );
        }

        /**
         * Inverse operation of loadFrom(). Saves data back to a stream.
         *
         * @param theStream the stream to write to
         * @throws IOException thrown if a write error occurred
         */
        public void saveTo(
                OutputStream theStream )
            throws
                IOException
        {
            theStream.write( value() );
        }

        /**
         * Determine equality of two objects.
         *
         * @param otherValue the object to test against
         * @return if true, the values equal each other
         */
        @Override
        public boolean equals(
                Object otherValue )
        {
            // the very cheap one first
            if( this == otherValue ) {
                return true;
            }

            if( otherValue instanceof StringBlob ) {
                StringBlob realOtherValue = (StringBlob) otherValue;

                if( !mimeTypesEquals( realOtherValue )) {
                    return false;
                }

                // they are different if one is null and the other isn't
                if( theValue == null ) {
                    return realOtherValue.theValue == null;
                }
                if( realOtherValue.theValue == null ) {
                    return false;
                }

                return theValue.equals( realOtherValue.theValue );
            }
            return false;
        }

        /**
         * Obtain a string which is the Java-language constructor expression reflecting this value.
         *
         * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
         * @param typeVar  name of the variable containing the DataType that goes with the to-be-created instance.
         * @return the Java-language constructor expression
         */
        public String getJavaConstructorString(
                String classLoaderVar,
                String typeVar )
        {
            StringBuffer sb = new StringBuffer( 60 ); // fudge

            sb.append( BlobValue.class.getName());
            sb.append( DataType.CREATE_STRING ).append( "\"" );

            for( int i=0 ; i<theValue.length() ; ++i ) {
                char c = theValue.charAt( i );
                if( c == '\n' ) {
                    sb.append( "\\n" );
                } else if( c == '"' ) {
                    sb.append( "\\\"" );
                } else if( Character.isISOControl( c )) {
                    String v = String.valueOf( (int) c );

                    sb.append( "\\u" );
                    for( int j=v.length() ; j<4 ; ++j ) {
                        sb.append( '0' );
                    }
                    sb.append( v );
                }
                else {
                    sb.append( c );
                }
            }
            sb.append( "\" " );

            if ( theMimeType != null ) {
                sb.append(", \"");
                sb.append( theMimeType );
                sb.append("\"");
            }
            sb.append( " )" );
            return sb.toString();
        }

        /**
          * Return im string form, for debugging.
          *
          * @return string form of this instance
          */
        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer( 50 ); // fudge

            sb.append( "\"" );
            sb.append( getAsString() );
            sb.append( "\"" );

            sb.append( "[" + getMimeType() + "]" );
            return new String( sb );
        }

        /**
         * The real value.
         */
        private String theValue;
    }

    /**
     * This private subclass of BlobValue stores the data as a byte []. It has hooks to make
     * it really easy to subclass it into a delayed loading.
     */
    protected static class ByteBlob
            extends
                BlobValue
    {
        private static final long serialVersionUID = -515235083305202648L;

        /**
         * Construct one.
         *
         * @param value the content of the BlobValue
         * @param mimeType the MIME type of the BlobValue in "abc/def" form
         */
        private ByteBlob(
                byte [] value,
                String  mimeType )
        {
            super( mimeType );

            this.theValue = value;
        }

        /**
         * Constructor for subtypes only.
         *
         * @param mimeType the MIME type of the BlobValue in "abc/def" form
         */
        protected ByteBlob(
                String mimeType )
        {
            super( mimeType );
        }

        /**
         * Obtain the content of this BlobValue in byte[] format.
         *
         * @return the content in byte[] format
         */
        public byte [] value()
        {
            return theValue;
        }

        /**
         * Obtain the content of this BlobValue in String format. This method will
         * interpret the BlobValue as a string in Unicode format, regardless.
         *
         * @return the content in String format
         */
        public String getAsString()
        {
            return new String( theValue );
        }

        /**
         * Convenience method to turn this into an Icon.
         *
         * @return the content of this BlobValue as Icon
         * @throws IllegalArgumentException thrown if this does not represent an image
         */
        public PortableIcon getAsIcon()
        {
            if( ! getMimeType().startsWith( "image" )) {
                throw new IllegalArgumentException( "BlobValue is not an image, mime type is " + getMimeType() );
            }
            try {
                return PortableIcon.create( theValue );

            } catch( Throwable t ) {
                log.warn( "Could not create ImageIcon", t );
                return null;
            }
        }

        /**
         * Inverse operation of loadFrom(). Saves data back to a stream.
         *
         * @param theStream the stream to which this BlobValue is written
         * @throws IOException thrown if an error occurs during writing
         */
        public void saveTo(
                OutputStream theStream )
            throws
                IOException
        {
            theStream.write( theValue );
        }

        /**
         * Determine equality of two objects.
         *
         * @param otherValue the value to test against
         * @return true if both values are equal
         */
        @Override
        public boolean equals(
                Object otherValue )
        {
            // the very cheap one first
            if( this == otherValue ) {
                return true;
            }

            if( otherValue instanceof ByteBlob ) {
                ByteBlob realOtherValue = (ByteBlob) otherValue;

                if( !mimeTypesEquals( realOtherValue )) {
                    return false;
                }

                // they are different if one is null and the other isn't
                if( theValue == null ) {
                    return realOtherValue.theValue == null;
                }
                if( realOtherValue.theValue == null ) {
                    return false;
                }

                // they are different if their lengths are different
                if( theValue.length != realOtherValue.theValue.length ) {
                    return false;
                }
                for( int i=0 ; i<theValue.length ; ++i ) {
                    if( theValue[i] != realOtherValue.theValue[i] ) {
                        return false;
                    }
                }
                return true;

            } else if( otherValue instanceof DelayedByteBlob ) {

                DelayedByteBlob realOtherValue = (DelayedByteBlob) otherValue;

                if( !mimeTypesEquals( realOtherValue )) {
                    return false;
                }

                realOtherValue.ensureLoaded();

                // they are different if one is null and the other isn't
                if( theValue == null ) {
                    return realOtherValue.theValue == null;
                }
                if( realOtherValue.theValue == null ) {
                    return false;
                }

                // they are different if their lengths are different
                if( theValue.length != realOtherValue.theValue.length ) {
                    return false;
                }
                for( int i=0 ; i<theValue.length ; ++i ) {
                    if( theValue[i] != realOtherValue.theValue[i] ) {
                        return false;
                    }
                }
                return true;
            }
            return false;
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
            StringBuffer sb = new StringBuffer( 60 ); // fudge
            sb.append( getClass().getName() );
            sb.append( DataType.CREATE_STRING );
            sb.append( "new byte[] { " );

            for ( int i=0; i<theValue.length; ++i ) {
                if( i>0 ) {
                    sb.append( "," );
                }
                sb.append( (int) theValue[i] );
            }
            sb.append( " } " );

            if ( theMimeType != null ) {
                sb.append(", \"");
                sb.append( theMimeType );
                sb.append("\"");
            }
            sb.append( DataType.CLOSE_PAREN_STRING );

            return sb.toString();
        }

        /**
          * Return im string form, for debugging.
          *
          * @return string form of this instance
          */
        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer( 50 ); // fudge

            sb.append( "<" );
            sb.append( getClass().getName() );
            sb.append( "{ " );
            byte [] v = value();
            sb.append( "value: " );
            if( v != null ) {
                sb.append( "\"x\'" );
                sb.append( encodeHex( value() ) );
                sb.append( "\'\"");
            } else {
                sb.append( "null" );
            }

            sb.append( ", [" ).append( getMimeType() ).append( "]" );
            sb.append( " }>" );

            return sb.toString();
        }

        /**
         * The real value.
         */
        protected byte [] theValue;
    }

    /**
     * This private subclass of BlobValue stores the data as a byte [] and loads only on demand.
     */
    protected static final class DelayedByteBlob
            extends
                BlobValue
    {
        private static final Log log = Log.getLogInstance( DelayedByteBlob.class ); // our own, private logger

        private static final long serialVersionUID = 3750605570020464233L;

        /**
         * Private constructor.
         *
         * @param loader the ClassLoader relative to which we load
         * @param loadFrom the location relative to loader from which we load
         * @param mimeType the MIME type of the BlobValue, in "abc/def" format
         */
        private DelayedByteBlob(
                ClassLoader loader,
                String      loadFrom,
                String      mimeType )
        {
            super( mimeType );

            this.theClassLoader = loader;
            this.theLoadFrom    = loadFrom;
            this.theValue       = null; // indicates we have not loaded yet
        }

        /**
         * Determine value as byte [].
         *
         * @return value as byte[]
         */
        public byte [] value()
        {
            ensureLoaded();
            return theValue;
        }

        /**
         * Obtain the ClassLoader through which we attempt to load.
         *
         * @return the ClassLoader through which we attempt to load
         */
        public ClassLoader getClassLoader()
        {
            return theClassLoader;
        }

        /**
         * Obtain the content of this BlobValue in String format. This method will
         * interpret the BlobValue as a string in Unicode format, regardless.
         *
         * @return the value in String format
         */
        public String getAsString()
        {
            ensureLoaded();
            if( theValue != null ) {
                return new String( theValue );
            } else {
                return null;
            }
        }

        /**
         * Convenience method to turn this into an Icon.
         *
         * @return the value as an Icon
         * @throws IllegalArgumentException thrown if this is not an Icon
         */
        public PortableIcon getAsIcon()
        {
            if( ! getMimeType().startsWith( "image" )) {
                throw new IllegalArgumentException( "BlobBalue is not an image, mime type is " + getMimeType() );
            }

            ensureLoaded();

            if( theValue == null ) {
                return null;
            }

            try {
                return PortableIcon.create( theValue );

            } catch( Throwable t ) {
                log.warn( "Could not create ImageIcon", t );
                return null;
            }
        }

        /**
         * Inverse operation of loadFrom(). Saves data back to a stream.
         *
         * @param theStream the stream to write to
         * @throws IOException thrown if an error occurred during write
         */
        public void saveTo(
                OutputStream theStream )
            throws
                IOException
        {
            ensureLoaded();
            theStream.write( theValue );
        }

        /**
         * Determine the relative path from where we load.
         *
         * @return the relative path from where we load
         */
        @Override
        public String delayedLoadingFrom()
        {
            return theLoadFrom;
        }

        /**
         * This hook is overridden here to make sure we have loaded our value.
         */
        protected void ensureLoaded()
        {
            if( theValue != null ) {
                return; // done already
            }
            try {
                // do load
                ClassLoader l         = ( theClassLoader != null ) ? theClassLoader : Thread.currentThread().getContextClassLoader();
                InputStream theStream = l.getResourceAsStream( theLoadFrom );

                if( theStream == null ) {
                    log.error( "Cannot load resource " + theLoadFrom + ", class loader is " + l );
                    return;
                }

                while( true ) {

                    byte [] buf = new byte[ 512 ];
                    int length = theStream.read( buf );

                    if( length < 0 ) {
                        break;
                    }

                    if( theValue != null ) {
                        byte [] newValue = new byte[ theValue.length + length ];
                        int i;
                        for( i=0 ; i<theValue.length ; ++i ) {
                            newValue[i] = theValue[i];
                        }
                        for( int j=0 ; j<length ; ++j, ++i ) {
                            newValue[i] = buf[j];
                        }
                        theValue = newValue;
                    } else {
                        theValue = buf;
                    }

                    if( length > buf.length ) {
                        break;
                    }
                }

            } catch( IOException ex ) {
                log.error( ex );
            }
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
            StringBuffer sb = new StringBuffer( 60 ); // fudge

            sb.append( BlobValue.class.getName() );
            sb.append( ".createByLoadingFrom( " );
            sb.append( classLoaderVar );
            sb.append( " , \"" );
            sb.append( theLoadFrom );
            sb.append( "\"" );

            if ( theMimeType != null ) {
                sb.append(", \"");
                sb.append( theMimeType );
                sb.append("\"");
            }
            sb.append( DataType.CLOSE_PAREN_STRING );

            return sb.toString();
        }

        /**
          * Return as a text representation, for debugging.
          *
          * @return return as a text representation
          */
        @Override
        public String toString()
        {
            StringBuffer sb = new StringBuffer( 50 ); // fudge

            sb.append( "<" );
            sb.append( getClass().getName() );
            sb.append( "{ " );

            sb.append( "from: " );
            sb.append( theLoadFrom );

            sb.append( ", value: " );
            sb.append( theValue );

            sb.append( ", [" ).append( getMimeType()).append( "]" );
            sb.append( " }>" );

            return sb.toString();
        }

        /**
         * Determine equality of two objects.
         *
         * @param otherValue the value to test against
         * @return true if both values equal
         */
        @Override
        public boolean equals(
                Object otherValue )
        {
            // the very cheap one first
            if( this == otherValue ) {
                return true;
            }

            if( otherValue instanceof DelayedByteBlob ) {

                DelayedByteBlob realOtherValue = (DelayedByteBlob) otherValue;

                if( !mimeTypesEquals( realOtherValue )) {
                    return false;
                }

                // they are different if one is null and the other isn't
                if( theValue == null ) {
                    return realOtherValue.theValue == null;
                }
                if( realOtherValue.theValue == null ) {
                    return false;
                }

                if( theLoadFrom.equals( realOtherValue.theLoadFrom )) {
                    return true;
                }

                realOtherValue.ensureLoaded();

                // they are different if their lengths are different
                if( theValue.length != realOtherValue.theValue.length ) {
                    return false;
                }
                for( int i=0 ; i<theValue.length ; ++i ) {
                    if( theValue[i] != realOtherValue.theValue[i] ) {
                        return false;
                    }
                }
                return true;
            }

            if( otherValue instanceof ByteBlob ) {
                ByteBlob realOtherValue = (ByteBlob) otherValue;

                if( !mimeTypesEquals( realOtherValue )) {
                    return false;
                }

                ensureLoaded();

                // they are different if one is null and the other isn't
                if( theValue == null ) {
                    return realOtherValue.theValue == null;
                }
                if( realOtherValue.theValue == null ) {
                    return false;
                }

                // they are different if their lengths are different
                if( theValue.length != realOtherValue.theValue.length ) {
                    return false;
                }
                for( int i=0 ; i<theValue.length ; ++i ) {
                    if( theValue[i] != realOtherValue.theValue[i] ) {
                        return false;
                    }
                }
                return true;

            }
            return false;
        }

        /**
         * Override serialization, in order to remove ClassLoader dependencies.
         *
         * @return a ByteBlob replacing this one for the purpose of serialization
         * @throws ObjectStreamException thrown if a write error occurs.
         */
        private Object writeReplace()
            throws
                ObjectStreamException
        {
            return new ByteBlob( value(), getMimeType() );
        }

        /**
         * The ClassLoader through which we attempt to load.
         */
        private ClassLoader theClassLoader;

        /**
         * The relative path from which we load the real value.
         */
        private String theLoadFrom;

        /**
         * The real value.
         */
        protected byte [] theValue;
    }
}
