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

import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests URL encoding via the HTTP class.
 */
public class UrlEncodingTest1
{
    @Test
    public void run()
        throws
            Exception
    {
        String [][] testCases = {
            {
                "abc def",
                "abc%20def",
                "abc+def"
            },
            {
                "abc?def",
                "abc%3Fdef",
                "abc%3Fdef"
            },
            {
                "abc/def",
                "abc/def",
                "abc/def" // do not encode "/", Tomcat does not like that at all, it considers it a security issue
            },
            {
                "abc:def",
                "abc%3Adef",
                "abc%3Adef"
            },
            {
                "abc#def",
                "abc%23def",
                "abc%23def"
            },
        };

        for( int i=0 ; i<testCases.length ; ++i ) {
            log.debug( "Now testing " + i );
            
            String input = testCases[i][0];
            String safeUrl      = HTTP.encodeToValidUrl( input );
            String safeArgument = HTTP.encodeToValidUrlArgument( input );
            
            Assert.assertEquals( "SafeURL encoding failed for test case \"" + input + "\" (" + i + ")", testCases[i][1], safeUrl );
            Assert.assertEquals( "SafeURLArgument encoding failed for test case \"" + input + "\" (" + i + ")", testCases[i][2], safeArgument );
        }
    }

    private static final Log log = Log.getLogInstance( UrlEncodingTest1.class  ); // our own, private logger
}
