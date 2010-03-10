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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.text;

import java.util.Map;

/**
 * Collects parameters that may influence the formatting of a String using StringRepresentation.
 */
public interface StringRepresentationParameters
{
    /**
     * Obtain the number of parameters.
     *
     * @return the number of parameters
     */
    public int size();

    /**
     * Returns <tt>true</tt> if this is empty.
     *
     * @return <tt>true</tt> if this is empty.
     */
    boolean isEmpty();

    /**
     * Obtain a named value, or null.
     *
     * @param key the name of the value
     * @return the value, if any
     */
    public Object get(
            String key );

    /**
     * Create a copy of this instance, but with an additional the named value.
     *
     * @param key the name of the value
     * @param value the value
     * @return copy, with the named value
     */
    public StringRepresentationParameters with(
            String key,
            Object value );

    /**
     * Create a copy of this instance, but with the named values.
     *
     * @param map the named values
     * @return copy, with the named values
     */
    public StringRepresentationParameters with(
            Map<String,?> map );

    /**
     * Create a copy of this instance, but without the named value.
     *
     * @param key the name of the value
     * @return copy, without the named value
     */
    public StringRepresentationParameters without(
            String key );

    /**
     * Create a copy of this instance, but without the named values.
     *
     * @param keys the names of the values
     * @return copy, without the named values
     */
    public StringRepresentationParameters without(
            String [] keys );

    /**
     * The key into this object that identifies the desired maximum length of the produced String.
     */
    public final String MAX_LENGTH = "maxLength";

    /**
     * The key into this object that identifies whether or not colloquial output is desired.
     */
    public final String COLLOQUIAL = "colloquial";

    /**
     * The key into this object that identifies the variable to which an edited value is
     * assigned. The meaning of "variable" depends on the user interface technology. For
     * example, in a web application, it may refer to an HTTP POST parameter. This is only
     * relevant for StringRepresentations that are intended to be edited.
     */
    public final String EDIT_VARIABLE = "variable";

    /**
     * The key into this object that identifies the null value to display, if the object to
     * be rendered is null.
     */
    public final String NULL_STRING = "nullString";

    /**
     * The key into this object that represents a web application's context path.
     */
    public static final String WEB_CONTEXT_KEY = "web-context-path";

    /**
     * The key into this object that represents a desired target for a link, like in HTML href tags.
     */
    public static final String LINK_TARGET_KEY = "link-target";

    /**
     * The key into this object that represents a title for a link, like in HTML href tags.
     */
    public static final String LINK_TITLE_KEY = "link-title";

    /**
     * The key into this object that represents additional arguments to be appended to an HTML URL link.
     */
    public static final String HTML_URL_ADDITIONAL_ARGUMENTS = "html-url-additional-arguments";

}
