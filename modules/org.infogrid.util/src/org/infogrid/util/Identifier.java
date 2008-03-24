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

import org.infogrid.util.text.StringRepresentation;

/**
 * An abstract interface to capture the semantics of identifiers.
 */
public interface Identifier
{
    /**
     * Obtain an external form for this Identifier, similar to
     * URL's getExternalForm().
     *
     * @return external form of this Identifier
     */
    public abstract String toExternalForm();

    /**
     * Convert this Identifier to its String representation, using the given
     * StringRepresentation.
     *
     * @param representation the StringRrepresentation to use
     * @return the String representation
     */
    public String toStringRepresentation(
            StringRepresentation representation );
}
