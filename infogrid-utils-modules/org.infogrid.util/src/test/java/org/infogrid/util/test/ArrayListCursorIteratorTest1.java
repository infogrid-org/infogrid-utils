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
import org.infogrid.util.ArrayListCursorIterator;
import org.infogrid.util.logging.Log;
import org.junit.Test;

/**
 * Tests the ArrayListCursorIterator. This is the substantially the same test as
 * ArrayCursorIteratorTest1.
 */
public class ArrayListCursorIteratorTest1
        extends
            AbstractCursorIteratorTest1
{
    @Test
    public void run()
        throws
            Exception
    {
        String [] testData = new String[] {
            "a", // [0]
            "b", // [1]
            "c", // [2]
            "d", // [3]
            "e", // [4]
            "f", // [5]
            "g", // [6]
            "h"  // [7]
        };
        
        ArrayList<String> testCollection = new ArrayList<String>();
        for( String current : testData ) {
            testCollection.add( current );
        }

        ArrayListCursorIterator<String> iter = ArrayListCursorIterator.create( testCollection, String.class );

        runWith( testData, iter, log );
    }

    private static final Log log = Log.getLogInstance( ArrayListCursorIteratorTest1.class ); // our own, private logger
}
