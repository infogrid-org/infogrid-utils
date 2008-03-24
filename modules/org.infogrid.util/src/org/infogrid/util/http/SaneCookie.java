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

package org.infogrid.util.http;

import java.util.Date;

/**
 * Interface to HTTP Cookies. This is an interface so we can implement a delegation
 * model to the Java servlet cookies. This interface is used both for incoming
 * and outgoing cookies.
 */
public interface SaneCookie
{
    /**
     * Obtain the name of the Cookie.
     *
     * @return the name of the Cookie
     */
    public String getName();

    /**
     * Obtain the value of the Cookie.
     *
     * @return the value of the Cookie
     */
    public String getValue();

    /**
     * Obtain the domain of the Cookie, if any.
     *
     * @return the domain of the Cookie
     */
    public String getDomain();

    /**
     * Obtain the path of the Cookie, if any.
     *
     * @return the path of the Cookie
     */
    public String getPath();

    /**
     * Obtain the expiration time of the Cookie, if any.
     *
     * @return the name of the Cookie
     */
    public Date getExpires();

    /**
     * Set this cookie to "please remove this cookie".
     */
    public void setRemoved();
}
