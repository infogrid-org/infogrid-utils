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

package org.infogrid.util.text;

import org.infogrid.util.ArrayFacade;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;

import java.util.Iterator;

/**
 * The constant blocks of text inside a (compound) MessageStringifier.
 * 
 * @param T the type of the Objects to be stringified
 */
public class ConstantStringifierComponent<T>
        implements
            CompoundStringifierComponent<T>
{
    /**
     * Constructor.
     *
     * @param s the constant String
     */
    public ConstantStringifierComponent(
            String s )
    {
        theString = s;
    }

    /**
     * Format zero or one Objects in the ArrayFacade. The implementation here
     * only returns the constant String.
     *
     * @param arg the Object to format
     * @return the formatted String
     */
    public String format(
            ArrayFacade<T> arg )
    {
        // regardless of argument, we always return the same
        return theString;
    }

    /**
     * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
     * parse the String. The iterator returns zero elements if the String could not be parsed
     * by this Stringifier.
     *
     * @param rawString the String to parse
     * @param startIndex the position at which to parse rawString
     * @param endIndex the position at which to end parsing rawString
     * @param max the maximum number of choices to be returned by the Iterator.
     * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
     *                 If false, return other matches that only match the beginning of the String.
     * @return the Iterator
     */
    public Iterator<StringifierParsingChoice<T>> parsingChoiceIterator(
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
        if( rawString.regionMatches( startIndex, theString, 0, theString.length() )) {
            return OneElementIterator.<StringifierParsingChoice<T>>create(
                    new StringifierParsingChoice<T>( startIndex, startIndex + theString.length() ) {
                        public T unformat() {
                            // this doesn't return any value
                            return null;
                        }
                    }
                );
        } else {
            return ZeroElementCursorIterator.<StringifierParsingChoice<T>>create();
        }
    }

    /**
     * The constant String.
     */
    protected String theString;
}