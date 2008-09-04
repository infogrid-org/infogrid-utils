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

package org.infogrid.jee.app;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.util.context.Context;
import org.infogrid.util.text.SimpleStringRepresentationContext;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * <p>An InfoGrid web application. This needs to be subclassed.</p>
 * <p>The application developer must instantiate this subclass exactly
 *    once per application and set the resulting instance with the
 *    {@link InfoGridWebApp#setSingleton InfoGridWebApp.setSingleton} method.</p>
 * <p>If the {@link org.infogrid.jee.servlet.InitializationFilter InitializationFilter}
 *    is used, this can be accomplished simply by declaring the name of the
 *    application class (e.g. <code>org.infogrid.jee.app.InfoGridWebApp</code>)
 *    as a parameter in the <code>web.xml</code> file. See documentation for
 *    {@link org.infogrid.jee.servlet.InitializationFilter InitializationFilter}.</p>
 */
public abstract class InfoGridWebApp
{
    /**
     * Set the singleton instance.
     *
     * @param single the singleton instance
     * @throws IllegalStateException if a singleton instance was set previously
     */
    public static void setSingleton(
            InfoGridWebApp single )
        throws
            IllegalStateException
    {
        if( theSingleton != null ) {
            throw new IllegalStateException( "Singleton set already: " + theSingleton );
        }
        theSingleton = single;
    }

    /**
     * Obtain the singleton instance.
     *
     * @return the singleton instance
     */
    public static InfoGridWebApp getSingleton()
    {
        return theSingleton;
    }

    /**
     * Constructor, for subclasses.
     *
     * @param applicationContext the main application Context. THis context holds all the
     *        well-known objects needed by the application
     */
    protected InfoGridWebApp(
            Context applicationContext )
    {        
        theApplicationContext = applicationContext;
    }

    /**
     * Obtain the main application Context for this application.
     *
     * @return the Context
     */
    public final Context getApplicationContext()
    {
        return theApplicationContext;
    }

    /**
     * Find the a RequestDispatcher for the request with this ServletPath, localized
     * according to the user's language preferences. This uses a similar algorithm as for Java's
     * <code>ResourceBundle.getBundle</code>. It can be overridden by subclasses.
     * 
     * @param servletName the generic servlet path
     * @param localeIterator Iterator over the user's Locale preferences, in sequence
     * @param context the ServletContext to use
     * @return the found RequestDispatcher, or null.
     */
    public RequestDispatcher findLocalizedRequestDispatcher(
            String           servletName,
            Iterator<Locale> localeIterator,
            ServletContext   context )
    {
        String found = null;
        
        String servletBaseName;
        String servletExtension;
        int    period = servletName.lastIndexOf( "." );
        if( period >= 0 ) {
            servletBaseName  = servletName.substring( 0, period );
            servletExtension = servletName.substring( period ); // include the period
        } else {
            servletBaseName  = servletName;
            servletExtension = "";
        }
        
        if( localeIterator != null ) {
            ArrayList<Locale> consideredAlready        = new ArrayList<Locale>();
            ArrayList<Locale> countriesStillToConsider = new ArrayList<Locale>();
            ArrayList<Locale> languagesStillToConsider = new ArrayList<Locale>();
            
            while( localeIterator.hasNext() ) {
                Locale current = localeIterator.next();

                StringBuilder candidate = new StringBuilder();
                candidate.append( servletBaseName );
                
                String language = current.getLanguage();
                String country  = current.getCountry();
                String variant  = current.getVariant();
                if( language == null || language.length() == 0 ) {
                    continue; // just the default
                }
                candidate.append( '_' ).append( language );
                if( country != null && country.length() > 1 ) {
                    candidate.append( '_' ).append( country );
                    
                    if( variant != null && variant.length() > 1 ) {
                        countriesStillToConsider.add( new Locale( language, country ));
                        candidate.append( '_' ).append( variant );
                        
                    } else {
                        languagesStillToConsider.add( new Locale( language ));
                    }
                }
                candidate.append( servletExtension );

                String candidateString = candidate.toString();
                URL    resource        = null;

                try {
                    resource = context.getResource( candidateString );
                    
                } catch( MalformedURLException ex ) {
                    // in this case, skip
                }
                if( resource != null ) {
                    found = candidateString;
                    break; // found
                } else {
                    consideredAlready.add( current );
                }
            }
            
            // now saved countries
            if( found == null ) {
                Iterator<Locale> iter = countriesStillToConsider.iterator();
                while( iter.hasNext() ) {
                    Locale current = iter.next();

                    if( consideredAlready.contains( current )) {
                        continue; // did this already
                    }
                    StringBuilder candidate = new StringBuilder();
                    candidate.append( servletBaseName );
                
                    candidate.append( '_' ).append( current.getLanguage() );
                    candidate.append( '_' ).append( current.getCountry() );
                    candidate.append( servletExtension );

                    String candidateString = candidate.toString();
                    URL    resource        = null;

                    try {
                        resource = context.getResource( candidateString );

                    } catch( MalformedURLException ex ) {
                        // in this case, skip
                    }
                    if( resource != null ) {
                        found = candidateString;
                        break; // found
                    } else {
                        consideredAlready.add( current );
                    }
                }
            }
            // now saved languages
            if( found == null ) {
                Iterator<Locale> iter = languagesStillToConsider.iterator();
                while( iter.hasNext() ) {
                    Locale current = iter.next();

                    if( consideredAlready.contains( current )) {
                        continue; // did this already
                    }
                    StringBuilder candidate = new StringBuilder();
                    candidate.append( servletBaseName );
                
                    candidate.append( '_' ).append( current.getLanguage() );
                    candidate.append( servletExtension );

                    String candidateString = candidate.toString();
                    if( doesResourceExist( candidateString, context )) {
                        found = candidateString;
                        break;
                    } else {
                        consideredAlready.add( current );
                    }                    
                }
            }
        }

        if( found != null ) {
            return context.getRequestDispatcher( found );
        }
        // make sure the default exists.
        if( doesResourceExist( servletName, context )) {            
            return context.getRequestDispatcher( servletName );
        } else {
            return null;
        }
    }

    /**
     * Determine whether a resource exists.
     *
     * @param resourcePath path to the resource
     * @param context the ServletContext to use
     * @return true if a resource exists with this path in the ServletContext
     */
    protected boolean doesResourceExist(
            String         resourcePath,
            ServletContext context )
    {
        URL resource = null;

        try {
            resource = context.getResource( resourcePath );

        } catch( MalformedURLException ex ) {
            // in this case, skip
        }
        if( resource != null ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Construct a StringRepresentationContext appropriate for this application. This may
     * be overridden by subclasses.
     * 
     * @param request the incoming request
     * @return the created StringRepresentationContext
     */
    public StringRepresentationContext constructStringRepresentationContext(
            HttpServletRequest request )
    {
        HashMap<String,Object> contextObjects = new HashMap<String,Object>();
        contextObjects.put( StringRepresentationContext.WEB_CONTEXT_KEY, request.getContextPath() );
 
        StringRepresentationContext ret = SimpleStringRepresentationContext.create( contextObjects );
        return ret;
    }

    /**
     * Name of a Request-level attribute that contains the problems that have occurred, as a
     * List&lt;Throwable&gt;.
     */
    public static final String PROCESSING_PROBLEM_EXCEPTION_NAME = InfoGridWebApp.class.getName() + "-RequestProblems";

    /**
     * The application context.
     */
    protected Context theApplicationContext;

    /**
     * The singleton instance of this class.
     */
    private static InfoGridWebApp theSingleton;    
}
