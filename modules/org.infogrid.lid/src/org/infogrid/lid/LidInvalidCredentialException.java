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

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if a credential was provided that was invalid.
 */
public class LidInvalidCredentialException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param identifier the identifier for which an invalid credential was provided
     */
    public LidInvalidCredentialException(
            String identifier )
    {
        theIdentifier = identifier;
    }
    
    /**
     * Constructor.
     * 
     * @param identifier the identifier for which an invalid credential was provided
     * @param cause the underlying cause, if any
     */
    public LidInvalidCredentialException(
            String    identifier,
            Throwable cause )
    {
        super( cause );

        theIdentifier = identifier;
    }
    
    /**
     * Obtain the identifier for which an invalid credential was provided.
     * 
     * @return the identifier
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theIdentifier };
    }

    /**
     * The identifier for which an invalid credential was provided.
     */
    protected String theIdentifier;        
}

