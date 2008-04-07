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

package org.infogrid.util;

import java.text.MessageFormat;

/**
 * Superclass for RuntimeExceptions that knows how to internationalize themselves.
 * Given that Exceptions carry all their data, it is a lot easier to to
 * ask the Exception how to internationalize itself, then to write outside
 * code to do so.
 */
public abstract class AbstractLocalizedRuntimeException
    extends
        RuntimeException
    implements
        LocalizedException
{
    /**
     * Constructor with no message.
     */
    public AbstractLocalizedRuntimeException()
    {
    }

    /**
     * Constructor with a message.
     *
     * @param msg the message
     */
    public AbstractLocalizedRuntimeException(
            String msg )
    {
        super( msg );
    }

    /**
     * Constructor with no message but a cause.
     *
     * @param cause the Throwable that caused this Exception
     */
    public AbstractLocalizedRuntimeException(
            Throwable cause )
    {
        super( cause );
    }

    /**
     * Constructor one with a message and a cause.
     *
     * @param msg the message
     * @param cause the Exception that caused this Exception
     */
    public AbstractLocalizedRuntimeException(
            String    msg,
            Throwable cause )
    {
        super( msg, cause );
    }

    /**
     * Determine the correct internationalized string that can be shown to the
     * user when the LocalizedException is thrown.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        Throwable cause = getCause();
        if( cause != null && cause instanceof LocalizedException ) {
             return ((LocalizedException)cause).getLocalizedMessage( formatter );
        }

        ResourceHelper theHelper = findResourceHelperForLocalizedMessage();        
        String         message   = theHelper.getResourceStringOrDefault( findMessageParameter(), null );
        
        Class c = getClass();

        while( message == null && ! Object.class.equals( c )) {
            c = c.getSuperclass();

            theHelper = ResourceHelper.getInstance( c );
            message   = theHelper.getResourceStringOrDefault( MESSAGE_PARAMETER, null );
        }
        if( message == null ) {
            message = getClass().getName();
        }
        Object [] params = getLocalizationParameters();
        if( params != null ) {
            
            Object [] formattedParams;
            if( formatter != null ) {
                formattedParams = new Object[ params.length ];
                for( int i=0 ; i<formattedParams.length ; ++i ) {
                    formattedParams[i] = formatter.asLocalizedString( params[i] );
                }
            } else {
                formattedParams = params;
            }
            
            try {
                message = MessageFormat.format( message, formattedParams );

            } catch( IllegalArgumentException ex ) {
                message = message + "(error while formatting translated message)";
            }
        }
        return message;
    }

    /**
     * Obtain localized message, per JDK 1.5.
     *
     * @return localized message
     */
    @Override
    public String getLocalizedMessage()
    {
        return getLocalizedMessage( null );
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public abstract Object [] getLocalizationParameters();

    /**
     * Allow subclasses to override which ResourceHelper to use.
     *
     * @return the ResourceHelper to use
     */
    protected ResourceHelper findResourceHelperForLocalizedMessage()
    {
        return ResourceHelper.getInstance( getClass() );
    }
    
    /**
     * Allow subclasses to override which ResourceHelper to use.
     *
     * @return the ResourceHelper to use
     */
    protected ResourceHelper findResourceHelperForLocalizedMessageViaEnclosingClass()
    {
        String className = getClass().getName();
        String key;
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            className = className.substring( 0, dollar );
        }
        return ResourceHelper.getInstance( className, getClass().getClassLoader() );
    }
    
    /**
     * Allow subclasses to override which key to use in the Resource file for the message.
     *
     * @return the key
     */
    protected String findMessageParameter()
    {
        return MESSAGE_PARAMETER;
    }

    /**
     * This method can be invoked by subclasses to obtain a suitable message key
     * for the same resource file for all inner classes.
     *
     * @return the key
     */
    protected String findMessageParameterViaEnclosingClass()
    {
        String className = getClass().getName();
        String key;
        int    dollar = className.indexOf( '$' );
        if( dollar >= 0 ) {
            key = className.substring( dollar+1 ) + "-" + MESSAGE_PARAMETER;
        } else {
            key = MESSAGE_PARAMETER;
        }
        return key;
    }
}
