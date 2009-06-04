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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates.defaultapp;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if the application could not be initialized. The cause expresses more detail.
 */
public class AppInitializationException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     *
     * @param cause the cause of the exception
     */
    public AppInitializationException(
            Throwable cause )
    {
        super( null, cause );
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { getCause() };
    }
}
