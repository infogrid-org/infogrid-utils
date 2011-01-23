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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.net.schemes;

import java.net.URI;
import java.net.URISyntaxException;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

/**
 * Represents the XRI identifier scheme for the DefaultNetMeshBaseIdentifierFactory.
 */
public class XriScheme
        extends
            AbstractScheme
{
    private static final Log log = Log.getLogInstance( XriScheme.class ); // our own, private logger

    /**
     * Constructor.
     */
    public XriScheme()
    {
        super( "XRI" );
    }

    /**
     * Detect whether a candidate identifier String strictly matches this scheme.
     *
     * @param context the identifier root that forms the context
     * @param candidate the candidate identifier
     * @return true if the candidate identifier strictly matches this scheme
     */
    public String matchesStrictly(
            String context,
            String candidate )
    {
        if( isXriGlobalContextSymbol( candidate.charAt( 0 ))) {
            return candidate;
        }
        if( candidate.startsWith( theXriResolverPrefix )) {
            String xri = candidate.substring( theXriResolverPrefix.length() );
            if( isXriGlobalContextSymbol( xri.charAt( 0 ) )) {
                return xri;
            }
        }
        return null;
    }

    /**
     * Attempt to convert this candidate identifier String into an identifier with this
     * scheme, taking creative license if needed. If successful, return the identifier,
     * null otherwise.
     *
     * @param context the identifier root that forms the context
     * @param candidate the candidate identifier
     * @param fact the NetMeshBaseIdentifierFactory on whose behalf we create this NetMeshBaseIdentifier
     * @return the successfully created identifier, or null otherwise
     */
    public NetMeshBaseIdentifier guessAndCreate(
            String                       context,
            String                       candidate,
            NetMeshBaseIdentifierFactory fact )
    {
        try {
            if( isXriGlobalContextSymbol( candidate.charAt( 0 ))) {
                return new NetMeshBaseIdentifier( fact, candidate, new URI( theXriResolverPrefix + candidate ), candidate, true );
            }

            if( candidate.startsWith( theXriResolverPrefix ) && isXriGlobalContextSymbol( candidate.charAt( theXriResolverPrefix.length() ))) {
                String xri = candidate.substring( theXriResolverPrefix.length() );
                return new NetMeshBaseIdentifier( fact, xri, new URI( theXriResolverPrefix + candidate ), candidate, true );
            }
        } catch( URISyntaxException ex ) {
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
        }
        return null;
    }

    /**
     * Determine whether a character is an XRI global context symbol.
     *
     * @param c the character
     * @return true if this character is an XRI global context symbol
     */
    public static boolean isXriGlobalContextSymbol(
            char c )
    {
        switch( c ) {
            case '=':
            case '@':
            case '+':
            case '$':
            case '!':
                return true;

            default:
                return false;
        }
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( XriScheme.class );

    /**
     * The XRI resolver location.
     */
    private static final String theXriResolverPrefix = theResourceHelper.getResourceStringOrDefault(
            "XriResolverPrefix",
            "http://xri.net/" );

}