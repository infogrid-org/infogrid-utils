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

package org.infogrid.util.test;

import java.util.ArrayList;
import java.util.List;
import org.infogrid.util.ArrayListCursorIterator;
import org.infogrid.util.CompositeCursorIterator;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;
import org.junit.Test;

/**
 * Tests the CompoundCursorIterator.
 * There is only one element.
 */
public class CompositeCursorIteratorTest2
        extends
            AbstractCursorIteratorTest2
{
    @Test
    public void run()
        throws
            Exception
    {
        String [] testData = new String[] {
            "a", // [0]
        };

        ArrayList<String> collection1 = new ArrayList<String>();
        ArrayList<String> collection2 = new ArrayList<String>();
        ArrayList<String> collection3 = new ArrayList<String>();
        ArrayList<String> collection4 = new ArrayList<String>();

        // nothing in #1
        collection2.add( testData[0] );
        // nothing in #3 and #4

        ArrayListCursorIterator<String> iter1 = ArrayListCursorIterator.create( collection1, String.class );
        ArrayListCursorIterator<String> iter2 = ArrayListCursorIterator.create( collection2, String.class );
        ArrayListCursorIterator<String> iter3 = ArrayListCursorIterator.create( collection3, String.class );
        ArrayListCursorIterator<String> iter4 = ArrayListCursorIterator.create( collection4, String.class );

        List<CursorIterator<String>> iterators = new ArrayList<CursorIterator<String>>();
        iterators.add( iter1 );
        iterators.add( iter2 );
        iterators.add( iter3 );
        iterators.add( iter4 );

        CompositeCursorIterator<String> iter = CompositeCursorIterator.<String>create( iterators, String.class );

        runWith( testData[0], iter, log );
    }

    private static final Log log = Log.getLogInstance( CompositeCursorIteratorTest2.class  ); // our own, private logger
}
