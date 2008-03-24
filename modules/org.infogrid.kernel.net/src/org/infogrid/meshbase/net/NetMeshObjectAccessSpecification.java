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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.DefaultAnetMeshObjectIdentifier;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;

import java.io.Serializable;
import java.net.URI;

/**
 * <p>A path to a remote MeshObject, comprised of zero or more NetworkIdentifiers.
 * It can be roughly compared to a UUCP path, or a server trace in a mail message.</p>
 * 
 * <p>A NetMeshObjectAccessSpecification always identifies a MeshObject. However, sometimes everal MeshObjects
 * reside at the same NetMeshBaseIdentifier, such as when the NetMeshBaseIdentifier refers to a file
 * that contains several MeshObjects. This class also provides capabilities to diambiguate
 * MeshObjects at the same NetMeshBaseIdentifier.</p>
 */
public class NetMeshObjectAccessSpecification
        implements
            Serializable
{
    private static final Log log = Log.getLogInstance( NetMeshObjectAccessSpecification.class ); // our own, private logger
    
    /**
     * Factory method to create a NetMeshObjectAccessSpecification to a locally available MeshObject. This
     * is a degenerate form of NetMeshObjectAccessSpecification, but useful for API consistency.
     * 
     * @param extName Identifier of the local MeshObject
     * @return created NeNetMeshBasePath
     */
    public static NetMeshObjectAccessSpecification createToLocalObject(
            NetMeshObjectIdentifier extName )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( extName.getNetMeshBaseIdentifier() ) },
                extName );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier oneElementName )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName ) },
                null );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default MeshObject.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier    oneElementName,
            NetMeshObjectIdentifier  extName )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName ) },
                extName );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default scope.
     * 
     * @param oneElementName the NNetMeshBaseIdentifier
     * @param scope the scope
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier oneElementName,
            ScopeSpecification    scope )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, scope ) },
                null );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier  oneElementName,
            CoherenceSpecification coherence )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, coherence ) },
                null );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default MeshObject and a non-default scope.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @param scope the scope
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            ScopeSpecification      scope )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, scope ) },
                extName );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default MeshObject and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            CoherenceSpecification  coherence )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, coherence ) },
                extName );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default scope and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param scope the scope
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier  oneElementName,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, scope, coherence ) },
                null );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * specifying a non-default MeshObject, a non-default scope and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @param scope the scope
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            ScopeSpecification      scope,
            CoherenceSpecification  coherence )
    {
        return new NetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        NetMeshBaseAccessSpecification.create( oneElementName, scope, coherence ) },
                extName );
    }

    /**
     * Factory method to create a multi-element NetMeshObjectAccessSpecification from a series of NetMeshBaseIdentifier.
     * 
     * @param elements the NetworkIdentifiers, in sequence
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseAccessSpecification [] elements )
    {
        return new NetMeshObjectAccessSpecification(
                elements,
                null );
    }

    /**
     * Factory method to create a multi-element NetMeshObjectAccessSpecification from a series of NetMeshBaseIdentifier,
     * specifying a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseIdentifier, in sequence
     * @param extName Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseAccessSpecification [] elements,
            NetMeshObjectIdentifier           extName )
    {
        return new NetMeshObjectAccessSpecification(
                elements,
                extName );
    }

    /**
     * Factory method to create a multi-element NetMeshObjectAccessSpecification from a series of NetMeshBaseIdentifier.
     * 
     * @param elements the NetworkIdentifiers, in sequence
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier [] elements )
    {
        return new NetMeshObjectAccessSpecification(
                NetMeshBaseAccessSpecification.create( elements ),
                null );
    }

    /**
     * Factory method to create a multi-element NetMeshObjectAccessSpecification from a series of NetMeshBaseIdentifier,
     * specifying a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseIdentifier, in sequence
     * @param extName Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public static NetMeshObjectAccessSpecification create(
            NetMeshBaseIdentifier [] elements,
            NetMeshObjectIdentifier  extName )
    {
        return new NetMeshObjectAccessSpecification(
                NetMeshBaseAccessSpecification.create( elements ),
                extName );
    }
    
    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a URL.
     * 
     * 
     * @param remoteLocation the URI identifying the remote MeshObject
     * @return created NeNetMeshBasePath
     */
    public static NetMeshObjectAccessSpecification create(
            URI remoteLocation )
    {
        NetMeshBaseIdentifier oneElementName = NetMeshBaseIdentifier.create( remoteLocation );
        return create( oneElementName );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a URL with a non-default
     * CoherenceSpecification.
     * 
     * 
     * @param remoteLocation the URI identifying the remote MeshObject
     * @param coherence the CoherenceSpecification
     * @return created NetNetMeshBasePath
     */
    public static NetMeshObjectAccessSpecification create(
            URI                    remoteLocation,
            CoherenceSpecification coherence )
    {
        NetMeshBaseIdentifier oneElementName = NetMeshBaseIdentifier.create( remoteLocation );
        return create( oneElementName, coherence );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a URL with a non-default
     * ScopeSpecification.
     * 
     * 
     * @param remoteLocation the URI identifying the remote MeshObject
     * @param scope the scope
     * @return created NetNetMeshBasePath
     */
    public static NetMeshObjectAccessSpecification create(
            URI                remoteLocation,
            ScopeSpecification scope )
    {
        NetMeshBaseIdentifier oneElementName = NetMeshBaseIdentifier.create( remoteLocation );
        return create( oneElementName, scope );
    }

    /**
     * Factory method to create a single-element NetMeshObjectAccessSpecification from a URL with a non-default
     * ScopeSpecification and a non-default CoherenceSpecification.
     * 
     * 
     * @param remoteLocation the URI identifying the remote MeshObject
     * @param scope the scope
     * @param coherence the CoherenceSpecification
     * @return created NetwNetMeshBasePath
     */
    public static NetMeshObjectAccessSpecification create(
            URI                    remoteLocation,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        NetMeshBaseIdentifier oneElementName = NetMeshBaseIdentifier.create( remoteLocation );
        return create( oneElementName, scope, coherence );
    }
    
    /**
     * A convenience method to prefix an array of NetworkPaths with the same NetMeshBaseIdentifier.
     * 
     * 
     * @param prefix the prefix for the NetworkPaths
     * @param paths the array of NetworkPaths that needs to be prefixed
     * @return an array with the prefixed paths in the same order
     */
    public static NetMeshObjectAccessSpecification [] withPrefix(
            NetMeshBaseAccessSpecification      prefix,
            NetMeshObjectAccessSpecification [] paths )
    {
        NetMeshObjectAccessSpecification [] ret = new NetMeshObjectAccessSpecification[ paths.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = new NetMeshObjectAccessSpecification(
                    ArrayHelper.append( prefix, paths[i].getAccessPath(), NetMeshBaseAccessSpecification.class ),
                    paths[i].getNetMeshObjectIdentifier() );
        }
        return ret;
    }

    /**
     * Constructor.
     *
     * @param namePath  the sequence of network locations to traverse to find one where we can access the MeshObject
     * @param extName   the identifier of the MeshObject there, if different from the default
     * @param scope     the scope parameter indicating how many MeshObjects shall be replcated at the same time
     * @param coherence the requirement for information coherence (e.g. how often updates shall be sent)
     */
    private NetMeshObjectAccessSpecification(
            NetMeshBaseAccessSpecification [] accessPath,
            NetMeshObjectIdentifier           extName )
    {
        theAccessPath       = accessPath != null ? accessPath : new NetMeshBaseAccessSpecification[0];
        theRemoteIdentifier = extName;
        
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            if( theAccessPath[i] == null ) {
                throw new IllegalArgumentException( "No AccessPath component in NetMeshObjectAccessSpecification must be null" );
            }
        }
    }

    /**
     * Obtain the NetMeshBaseIdentifier path.
     * 
     * @return the path we traverse to the MeshObject we want to access. May be of length 0.
     */
    public NetMeshBaseAccessSpecification [] getAccessPath()
    {
        return theAccessPath;
    }

    /**
     * Obtain the Identifier of the MeshObject that we are looking for in the remote MeshBase, if different from the default.
     *
     * @return the Identifier of the MeshObject that we are looking for, if different from the default
     */
    public NetMeshObjectIdentifier getNoneDefaultNetMeshObjectIdentifier()
    {
        return theRemoteIdentifier;
    }

    /**
     * Obtain the Identifier of the MeshObject that we are looking for in the remote MeshBase.
     * Calculate it if it is the default.
     *
     * @return the Identifier of the MeshObject that we are looking for
     */
    public NetMeshObjectIdentifier getNetMeshObjectIdentifier()
    {
        NetMeshObjectIdentifier ret;
        if( theRemoteIdentifier != null ) {
            ret = theRemoteIdentifier;

        } else  if( theAccessPath == null || theAccessPath.length == 0 ) {
            // FIXME -- not sure this should reference an "A" implementation here
            ret = DefaultAnetMeshObjectIdentifier.create( null, null );

        } else {
            ret = DefaultAnetMeshObjectIdentifier.create( theAccessPath[ theAccessPath.length-1 ].getNetMeshBaseIdentifier(), null );
        }
        return ret;
    }

    /**
     * Obtain an externalized version of this NetMeshObjectAccessSpecification.
     * 
     * @return external form of this NetMeshObjectAccessSpecification similar to URL.toExternalForm()
     */
    public String toExternalForm()
    {
        StringBuilder almostRet = new StringBuilder( 100 ); // fudge number

        String sep = "";
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            almostRet.append( sep );
            almostRet.append( theAccessPath[i].toExternalForm() );
            sep = "!";
        }
        if( theRemoteIdentifier != null ) {
            almostRet.append( "#" );
            almostRet.append( escapeHash( theRemoteIdentifier.toExternalForm() ));
        }

        return almostRet.toString();
    }

    /**
     * Helper method to escape the hash sign.
     *
     * @param s String with hash
     * @return String with escaped hash
     */
    protected static String escapeHash(
            String s )
    {
        int           len = s.length();
        StringBuilder ret = new StringBuilder( len + 10 ); // fudge
        for( int i=0 ; i<len ; ++i ) {
            char c = s.charAt( i );
            switch( c ) {
                case '#':
                    ret.append( ESCAPED_HASH );
                    break;
                default:
                    ret.append( c );
                    break;
            }            
        }
        return ret.toString();
    }

    /**
     * Helper method to descape the hash site.
     *
     * @param s String with escaped hash
     * @return String with regular hash
     */
    protected static String descapeHash(
            String s )
    {
        int           len = s.length();
        StringBuilder ret = new StringBuilder( len );
        
        int startAt = 0;
        int foundAt;
        while( ( foundAt = s.indexOf( ESCAPED_HASH, startAt )) >= 0 ) {
            String sub = s.substring( startAt, foundAt );
            ret.append( sub );
            ret.append( '#' );
            foundAt += ESCAPED_HASH.length();
        }
        String sub = s.substring( startAt );
        ret.append( sub );

        return ret.toString();
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare against
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof NetMeshObjectAccessSpecification )) {
            return false;
        }
        NetMeshObjectAccessSpecification realOther = (NetMeshObjectAccessSpecification) other;
        
        if( theAccessPath.length != realOther.theAccessPath.length ) {
            return false;
        }
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            if( !theAccessPath[i].equals( realOther.theAccessPath[i] )) {
                return false;
            }
        }
        if( theRemoteIdentifier != null ) {
            if( !theRemoteIdentifier.equals( realOther.theRemoteIdentifier )) {
                return false;
            }
        } else if( realOther.theRemoteIdentifier != null ) {
            return false;
        }
        return true;
    }

    /**
     * Convert to String, for debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theAccessPath",
                    "theNonDefaultRemoteIdentifier"
                },
                new Object[] {
                    theAccessPath,
                    theRemoteIdentifier
                },
                StringHelper.LOG_FLAGS.SHOW_NON_NULL | StringHelper.LOG_FLAGS.SHOW_NON_ZERO );
    }

    /**
     * Helper method to obtain the remote Identifiers of an array of NetMeshObjectAccessSpecifications.
     *
     * @param specs the NetMeshObjectAccessSpecifications
     * @return the NetMeshObjectIdentifiers contained therein
     */
    public static NetMeshObjectIdentifier [] identifiersOf(
            NetMeshObjectAccessSpecification [] specs )
    {
        NetMeshObjectIdentifier [] ret = new NetMeshObjectIdentifier[ specs.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = specs[i].getNetMeshObjectIdentifier();
        }
        return ret;
    }

    /**
     * The NetMeshBaseIdentifier path.
     */
    protected NetMeshBaseAccessSpecification [] theAccessPath;

    /**
     * If a non-default MeshObject shall be accessed, this captures the Identifier
     * of that MeshObject at the remote location.
     */
    protected NetMeshObjectIdentifier theRemoteIdentifier;

    /**
     * To save memory, this constant is allocated here and used wherever appropriate.
     */
    public static final NetMeshObjectAccessSpecification [] EMPTY_ARRAY = {};

    /**
     * The escaped hash sign.
     */
    private static final String ESCAPED_HASH = "&#35;";
}
