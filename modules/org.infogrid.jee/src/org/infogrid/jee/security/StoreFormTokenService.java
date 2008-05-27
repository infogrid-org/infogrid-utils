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

package org.infogrid.jee.security;

import org.infogrid.store.Store;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.util.StoreBackedMap;

/**
 * A simple implementation of FormTokenService that stores its tokens in a Store.
 */
public class StoreFormTokenService
        implements
            FormTokenService
{
    /**
     * Factory method.
     *
     * @param store the Store to store the tokens in
     * @return the created StoreFormTokenService
     */
    public static StoreFormTokenService create(
            Store store )
    {
        StoreEntryMapper<String,StoredFormToken> mapper = StoredFormTokenMapper.create();
        StoreBackedMap<String,StoredFormToken>   map    = StoreBackedMap.createWeak( mapper, store );

        StoreFormTokenService ret = new StoreFormTokenService( map );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param map manages the storage of tokens
     */
    protected StoreFormTokenService(
            StoreBackedMap<String,StoredFormToken> map )
    {
        theMap = map;
    }
    
    /**
     * Generate a new token.
     * 
     * @return the newly generated token
     */
    public String generateNewToken()
    {
        StoredFormToken token = StoredFormToken.createNew();

        theMap.put( token.getKey(), token );

        return token.getKey();
    }
    
    /**
     * Validate a presented token. It is up to the implementation to decide whether
     * or not to invalidate presented tokens.
     * 
     * @param key the key of the presented token
     * @return true if the token is valid, false if it is not valid or unknown.
     */
    public boolean validateToken(
            String key )
    {
        // regardless, we remove tokens passed into here
        StoredFormToken token = theMap.remove( key );
        if( token == null ) {
            return false;
        }
        if( !token.isStillValid() ) {
            return false;
        }
        return true;
    }

    /**
     * The Map that delegates to the Store.
     */
    protected StoreBackedMap<String,StoredFormToken> theMap;
}
