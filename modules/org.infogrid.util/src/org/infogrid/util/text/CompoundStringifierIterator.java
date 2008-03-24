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
import org.infogrid.util.ResourceHelper;

import java.util.Iterator;

/**
 * Iterator for a CompoundStringifier. This is basically an iterative implementation of a
 * depth-first search algorithm similar to how PROLOG works.
 */
class CompoundStringifierIterator<T>
        implements
            Iterator<StringifierParsingChoice<ArrayFacade<T>>>
{
    /**
     * Constructor.
     *
     * @param stringifier the CompoundStringifier to which this iterator belongs
     * @param components the CompoundStringifierComponent in the stringifier
     * @param rawString the String to parse
     * @param startIndex the start index (inclusive) of the String to parse
     * @param endIndex the end index (exclusive) of the String to parse
     * @param max the maximum number of elements to return
     * @param if true, only return StringifierParsingChoice that consume the entire String
     */
    public CompoundStringifierIterator(
            CompoundStringifier<T>             stringifier,
            CompoundStringifierComponent<T> [] components,
            String  rawString,
            int     startIndex,
            int     endIndex,
            int     max,
            boolean matchAll )
    {
        theStringifier  = stringifier;
        theComponents   = components;
        theRawString    = rawString;
        theStartIndex   = startIndex;
        theEndIndex     = endIndex;
        theMax          = max;
        theMatchAll     = matchAll;
        
        initializeIteratorsAndStatus();
        
        theDepth    = 0;
        theChildMax = Math.max( theMax, theResourceHelper.getResourceIntegerOrDefault( "ChildMax", 20 ) ); // 20 chars for Strings ...
        
        theIterators[ theDepth ] = theComponents[theDepth].parsingChoiceIterator(
                theRawString,
                0,
                theRawString.length(),
                theChildMax,
                theComponents.length == theDepth+1 ); // first one is the last one
        goNext();
    }
    
    /**
     * Internal helper method to avoid plastering the SuppressWarnings all over the place.
     */
    @SuppressWarnings(value={"unchecked"})
    protected void initializeIteratorsAndStatus()
    {
        theIterators = new Iterator[theComponents.length];
        theStatus    = new StringifierParsingChoice[theComponents.length];
    }

    
    /**
     * Are there more elements to be returned by this Iterator.
     *
     * @return true if there are more elements to be returned by this Iterator
     */
    public boolean hasNext()
    {
        return theNext != null;
    }
    
    /**
     * Obtain the next found StringifierParsingChoice.
     *
     * @return the next StringifierParsingChoice
     */
    public StringifierParsingChoice<ArrayFacade<T>> next()
    {
        StringifierParsingChoice<ArrayFacade<T>> ret = theNext;
        
        goNext();
        
        return ret;
    }

    /**
     * Iterate to the next element.
     */
    protected void goNext()
    {
        while( theDepth >=0 ) {
            while( theIterators[theDepth] != null && theIterators[theDepth].hasNext() ) {
                theStatus[theDepth] = theIterators[theDepth].next();

                if( theDepth == theComponents.length-1 ) {
                    // last component
                    if( !theMatchAll || theStatus[theDepth].getEndIndex() == theRawString.length() ) {
                        // found
                        theNext = new CompoundStringifierChildChoice<T>(
                                theStringifier,
                                theStatus.clone(),
                                0,
                                theRawString.length() );
                        return;
                    }
                }
                if( theDepth < theComponents.length-1 ) {
                    ++theDepth;
                    theIterators[ theDepth ] = theComponents[theDepth].parsingChoiceIterator(
                            theRawString,
                            theStatus[theDepth-1].getEndIndex(),
                            theRawString.length(),
                            theChildMax,
                            theMatchAll && ( theDepth == theIterators.length-1 ) ? true : false );
                }
            }
            --theDepth;
        }
        theNext = null;
    }
    
    /**
     * Always throws UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException
     */
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The Stringifier this Iterator belongs to. Making this explicit seems easier than a non-static inner class
     * with all these generics.
     */
    protected CompoundStringifier<T> theStringifier;
    
    /**
     * The CompoundStringifierComponent inside the stringifier to which this Iterator belongs to.
     */
    protected CompoundStringifierComponent<T> [] theComponents;

    /**
     * The String being parsed.
     */
    protected String theRawString;
    
    /**
     * The start index in the String from where to parse (inclusive).
     */
    protected int theStartIndex;
    
    /**
     * The end index in the String to where to parse (exclusive).
     */
    protected int theEndIndex;
    
    /**
     * The maximum number of elements to return.
     */
    protected int theMax;
    
    /**
     * The maximum number of elements to ask our children for.
     */
    protected int theChildMax;
    
    /**
     * If true, return only matches that match the entire String between start and end.
     */
    protected boolean theMatchAll;
    
    /**
     * Reflects the current depth of the state of the search algorithm.
     */
    protected int theDepth;
    
    /**
     * The current child iterators, ordered from left to right.
     */
    protected Iterator<? extends StringifierParsingChoice<? extends T>> [] theIterators;
    
    /**
     * The most-recently returned result of the child iterators, in same sequence as theIterators.
     */
    protected StringifierParsingChoice [] theStatus;
    
    /**
     * The next result to return from this Iterator.
     */
    protected StringifierParsingChoice<ArrayFacade<T>> theNext;
    
    /**
     * The ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( CompoundStringifierIterator.class );
}
