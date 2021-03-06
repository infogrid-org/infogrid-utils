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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.text;

import java.util.Iterator;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;

/**
 * Stringifies a single Integer.
 */
public class IntegerStringifier
        extends
            NumberStringifier<Integer>
{
    /**
     * Factory method.
     * 
     * @return the created IntegerStringifier
     */
    public static IntegerStringifier create()
    {
        return new IntegerStringifier( -1, 10 );
    }

    /**
     * Factory method for an IntegerStringifier that attempts to display N digits, inserting leading zeros if needed.
     * 
     * @param digits the number of digits to display
     * @return the created IntegerStringifier
     */
    public static IntegerStringifier create(
            int digits )
    {
        return new IntegerStringifier( digits, 10 );
    }

    /**
     * Factory method for an IntegerStringifier that attempts to display N digits, inserting leading zeros if needed.
     *
     * @param digits the number of digits to display
     * @param radix the radix to use, e.g. 16 for hexadecimal
     * @return the created IntegerStringifier
     */
    public static IntegerStringifier create(
            int digits,
            int radix )
    {
        return new IntegerStringifier( digits, radix );
    }

    /**
     * Constructor. Use factory method.
     *
     * @param digits the number of digits to display
     * @param radix the radix to use, e.g. 16 for hexadecimal
     */
    protected IntegerStringifier(
            int digits,
            int radix )
    {
        super( digits, radix );
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation. Always provided.
     * @return the formatted String
     */
    public String format(
            String                         soFar,
            Integer                        arg,
            StringRepresentationParameters pars )
    {
        return super.format( soFar, arg.longValue(), pars );
    }
    
    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @param factory the factory needed to create the parsed values, if any
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    @Override
    public Integer unformat(
            String                     rawString,
            StringifierUnformatFactory factory )
        throws
            StringifierParseException
    {
        try {
            Integer ret;
            if( theDigits != -1 && rawString.length() > theDigits ) {
                ret = Integer.parseInt( rawString.substring( 0, theDigits ), theRadix );
            } else {
                ret = Integer.parseInt( rawString, theRadix );
            }

            return ret;

        } catch( NumberFormatException ex ) {
            throw new StringifierParseException( this, rawString, ex );
        }
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
     * @param factory the factory needed to create the parsed values, if any
     * @return the Iterator
     */
    @Override
    public Iterator<StringifierParsingChoice<Integer>> parsingChoiceIterator(
            final String                     rawString,
            final int                        startIndex,
            final int                        endIndex,
            final int                        max,
            final boolean                    matchAll,
            final StringifierUnformatFactory factory )
    {
        if( matchAll ) {
            for( int i = 0 ; i < endIndex-startIndex ; ++i ) {
                if( !validChar( i+startIndex, startIndex, endIndex, rawString ) ) {
                    return ZeroElementCursorIterator.<StringifierParsingChoice<Integer>>create();
                }
            }
            return OneElementIterator.<StringifierParsingChoice<Integer>>create(
                    new StringifierValueParsingChoice<Integer>( startIndex, endIndex, Integer.parseInt( rawString.substring( startIndex, endIndex ), theRadix )));

        } else {
            final int finalEnd = theDigits == -1 ? rawString.length() : ( startIndex + theDigits );
            if( theDigits != -1 && startIndex + theDigits > endIndex ) {
                // need theDigits but don't have enough
                return ZeroElementCursorIterator.<StringifierParsingChoice<Integer>>create();
            }

            return new Iterator<StringifierParsingChoice<Integer>>() {
                /** Constructor */
                {
                    if( theDigits == -1 ) {
                        char c = rawString.charAt( startIndex );
                        if( c == '-' || c == '+' ) {
                            currentEnd = startIndex + 1;
                        } else {
                            currentEnd = startIndex;
                        }
                    } else {
                        currentEnd = startIndex + theDigits -1;
                    }
                }

                /**
                 * Does the iterator have a next element?
                 *
                 * @return true if the iterator has a next element
                 */
                public boolean hasNext()
                {
                    if( counter >= max ) {
                        return false;
                    }
                    if( currentEnd >= finalEnd ) {
                        return false;
                    }
                    if( !validChar( currentEnd, startIndex, endIndex, rawString ) ) {
                        return false;
                    }
                    return true;
                }

                /**
                 * Obtain the next element in the iteration.
                 *
                 * @return the next element
                 */
                public StringifierParsingChoice<Integer> next()
                {
                    ++currentEnd;

                    boolean isNegative   = false;
                    int     currentValue = 0;

                    for( int i=startIndex ; i<currentEnd ; ++i ) {
                        char c = rawString.charAt( i );
                        switch( c ) {
                            case '-':
                                isNegative = true;
                                break;

                            case '+':
                                c = rawString.charAt( currentEnd );
                                break;

                            default:
                                currentValue = currentValue*theRadix + Character.digit( c, theRadix );
                                break;
                        }
                    }
                    ++counter;
                    return new StringifierValueParsingChoice<Integer>( startIndex, currentEnd, isNegative ? (-currentValue) : currentValue );
                }

                /**
                 * Throws UnsupportedOperationException at all times.
                 *
                 * @throws UnsupportedOperationException at all times.
                 */
                public void remove()
                    throws
                        UnsupportedOperationException
                {
                    throw new UnsupportedOperationException();
                }

                /**
                 * The current end, incremented every iteration.
                 */
                protected int currentEnd;
                
                /**
                 * Counts the number of iterations returned already.
                 */
                protected int counter = 0;
            };
        }
    }
}
