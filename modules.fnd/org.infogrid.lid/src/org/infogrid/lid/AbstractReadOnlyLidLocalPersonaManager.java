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

package org.infogrid.lid;

import java.util.Map;
import org.infogrid.lid.credential.LidCredentialType;

/**
 * Factors out common functionality of LidLocalPersonaManagers that only support
 * read and not write operations..
 */
public abstract class AbstractReadOnlyLidLocalPersonaManager
        extends
            AbstractLidLocalPersonaManager
{
    /**
     * Create a LidLocalPersona.
     *
     * @param identifier the identifier for the to-be-created LidLocalPersona
     * @param attributes the attributes for the to-be-created LidLocalPersona
     * @param credentials the credentials for the to-be-created LidLocalPersona
     * @return nothing, as UnsupportedOperationException is always thrown 
     * @throws UnsupportedOperationException always thrown
     */
    public LidLocalPersona createLocalPersona(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
        throws
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

//    /**
//     * Change the credential associated with a given identifier.
//     *
//     * @param identifier the identifier for which the credential will be changed
//     * @param type the type of credential to be changed
//     * @param credential the new credential
//     * @throws UnsupportedOperationException always thrown
//     */
//    public void changeCredential(
//            String            identifier,
//            LidCredentialType type,
//            String            credential )
//        throws
//            UnsupportedOperationException
//    {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Delete a LidLocalPersona, given its identifier.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @throws UnsupportedOperationException always thrown
     */
    public void delete(
            String identifier )
        throws
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
}