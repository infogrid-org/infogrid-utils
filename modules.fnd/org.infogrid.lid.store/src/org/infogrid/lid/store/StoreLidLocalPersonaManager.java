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

package org.infogrid.lid.store;

import java.util.Map;
import org.infogrid.lid.local.AbstractLidLocalPersonaManager;
import org.infogrid.lid.local.LidLocalPersona;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.local.LidLocalPersonaExistsAlreadyException;
import org.infogrid.lid.local.LidLocalPersonaUnknownException;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.ObjectExistsAlreadyFactoryException;
import org.infogrid.util.PatientSmartFactory;
import org.infogrid.util.SmartFactory;

/**
 * Store implementation of LidLocalPersonaManager.
 */
public class StoreLidLocalPersonaManager
        extends
            AbstractLidLocalPersonaManager
{
    /**
     * Factory method.
     *
     * @param store the Store to store to
     * @param credentialTypeClassLoader the ClassLoader to use to instantiate LidCredentialTypes
     * @return the created StoreLidLocalPersonaManager
     */
    public static StoreLidLocalPersonaManager create(
            Store       store,
            ClassLoader credentialTypeClassLoader )
    {
        LidLocalPersonaMapper mapper = new LidLocalPersonaMapper( credentialTypeClassLoader );
        
        Factory<Identifier,StoreLidLocalPersona,AttributesCredentials> delegateFactory
                = new AbstractFactory<Identifier,StoreLidLocalPersona,AttributesCredentials>() {
                    public StoreLidLocalPersona obtainFor(
                            Identifier            identifier,
                            AttributesCredentials attCred )
                    {
                        StoreLidLocalPersona ret = new StoreLidLocalPersona(
                                identifier,
                                attCred.getAttributes(),
                                attCred.getCredentials() );
                        return ret;
                    }
                };

        StoreBackedSwappingHashMap<Identifier,StoreLidLocalPersona> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );
        
        SmartFactory<Identifier,StoreLidLocalPersona,AttributesCredentials> smartFactory
                = new PatientSmartFactory<Identifier,StoreLidLocalPersona,AttributesCredentials>( delegateFactory, storage );
        
        StoreLidLocalPersonaManager ret = new StoreLidLocalPersonaManager( smartFactory );
        return ret;
    }

    /**
     * Constructor, use factory method.
     * 
     * @param delegateFactory the underlying SmartFactory
     */
    protected StoreLidLocalPersonaManager(
            SmartFactory<Identifier,StoreLidLocalPersona,AttributesCredentials> delegateFactory )
    {
        theDelegateFactory = delegateFactory;
    }

    /**
     * Create a LidLocalPersona.
     *
     * @param identifier the identifier for the to-be-created LidLocalPersona
     * @param attributes the attributes for the to-be-created LidLocalPersona
     * @param credentials the credentials for the to-be-created LidLocalPersona
     * @return the LocalPersona that was created
     * @throws LidLocalPersonaExistsAlreadyException thrown if a LidLocalPersona with this identifier exists already
     * @throws UnsupportedOperationException thrown if this LidLocalPersonaManager does not permit the creation of new LidLocalPersonas
     */
    public LidLocalPersona createLocalPersona(
            Identifier                    identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
        throws
            LidLocalPersonaExistsAlreadyException,
            UnsupportedOperationException
    {
        AttributesCredentials attCred = new AttributesCredentials( attributes, credentials );
        
        try {
            LidLocalPersona ret = theDelegateFactory.obtainNewFor( identifier, attCred );
            return ret;

        } catch( ObjectExistsAlreadyFactoryException ex ) {
            throw new LidLocalPersonaExistsAlreadyException( (LidLocalPersona) ex.getExisting(), ex );

        } catch( FactoryException ex ) {
            throw new RuntimeException( ex );
        }
    }

    /**
     * Obtain a LidLocalPersona, given its identifier.
     *
     * @param identifier the identifier for which the LidLocalPersona will be retrieved
     * @return the found LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public LidLocalPersona find(
            Identifier identifier )
        throws
            LidLocalPersonaUnknownException
    {
        LidLocalPersona ret = theDelegateFactory.get( identifier );
        if( ret == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }
        return ret;
    }

    /**
     * Delete a LidLocalPersona, given its identifier.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @throws UnsupportedOperationException thrown if this LidLocalPersonaManager does not permit the deletion of LidLocalPersonas
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public void delete(
            Identifier identifier )
        throws
            UnsupportedOperationException,
            LidLocalPersonaUnknownException
    {
        LidLocalPersona found = theDelegateFactory.remove( identifier );
        if( found == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }
    }

    /**
     * The underlying SmartFactory. This is hidden so we can do access control and
     * expose the API we want.
     */
    protected SmartFactory<Identifier,StoreLidLocalPersona,AttributesCredentials> theDelegateFactory;
}
