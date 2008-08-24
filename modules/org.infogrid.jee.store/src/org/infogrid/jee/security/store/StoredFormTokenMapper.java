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
package org.infogrid.jee.security.store;

import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.StoreValue;
import org.infogrid.store.StoreValueDecodingException;
import org.infogrid.store.StoreValueEncodingException;

/**
 * Knows how to encode/decode StoredFormTokens. Currently, the token actually does
 * not carry any data other than key, timeCreated, timeExpires.
 */
public class StoredFormTokenMapper
        implements
            StoreEntryMapper<String,StoredFormToken>
{
    /**
     * Factory method.
     *
     * @return the created StoredFormTokenMapper
     */
    public static StoredFormTokenMapper create()
    {
        StoredFormTokenMapper ret = new StoredFormTokenMapper();
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     */
    protected StoredFormTokenMapper()
    {
        // nothing right now
    }

    /**
     * Map a key to a String value that can be used for the Store.
     *
     * @param key the key object
     * @return the corresponding String value that can be used for the Store
     */
    public String keyToString(
            String key )
    {
        return key;
    }

    /**
     * Map a String value that can be used for the Store to a key object.
     *
     * @param stringKey the key in String form
     * @return the corresponding key object
     */
    public String stringToKey(
            String stringKey )
    {
        return stringKey;
    }

    /**
     * Map a StoreValue to a value.
     *
     * @param key the key to the StoreValue
     * @param value the StoreValue
     * @return the value
     * @throws StoreValueDecodingException thrown if the StoreValue could not been decoded
     */
    public StoredFormToken decodeValue(
            String     key,
            StoreValue value )
        throws
            StoreValueDecodingException
    {
        StoredFormToken ret = StoredFormToken.restore( key, value.getTimeCreated(), value.getTimeExpires() );
        return ret;
    }
    
    /**
     * Obtain the preferred encoding id of this StoreEntryMapper.
     * 
     * @return the preferred encoding id
     */
    public String getPreferredEncodingId()
    {
        return getClass().getName();
    }

    /**
     * Obtain the time a value was created.
     *
     * @param value the time a value was created.
     * @return the time created, in System.currentTimeMillis() format
     */
    public long getTimeCreated(
            StoredFormToken value )
    {
        return value.getTimeCreated();
    }

    /**
     * Obtain the time a value was last updated.
     *
     * @param value the time a value was last updated.
     * @return the time updated, in System.currentTimeMillis() format
     */
    public long getTimeUpdated(
            StoredFormToken value )
    {
        return value.getTimeCreated();
    }

    /**
     * Obtain the time a value was last read.
     *
     * @param value the time a value was last read.
     * @return the time read, in System.currentTimeMillis() format
     */
    public long getTimeRead(
            StoredFormToken value )
    {
        return -1L; // FIXME?
    }

    /**
     * Obtain the time a value will expire.
     *
     * @param value the time a value will expire.
     * @return the time will expire, in System.currentTimeMillis() format
     */
    public long getTimeExpires(
            StoredFormToken value )
    {
        return value.getTimeExpires();
    }

    /**
     * Obtain the value as a byte array.
     *
     * @param value the value
     * @return the byte array
     * @throws StoreValueEncodingException thrown if the value could not been encoded
     */
    public byte [] asBytes(
            StoredFormToken value )
        throws
            StoreValueEncodingException
    {
        return EMPTY;
    }
    
    /**
     * THe default content.
     */
    private static final byte [] EMPTY = new byte[0];
}
