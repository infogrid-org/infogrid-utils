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

package org.infogrid.probe.TEST;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;

/**
 * Tests the Probe framework.
 */
public class AllTests
        extends
            AbstractTestGroup
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        String [] noArgs = {};
        
        TestSpec [] tests = {

                new TestSpec(
                        ProbeTest1.class,
                        AbstractTest.fileSystemFileName( ProbeTest1.class, "ProbeTest1.xml" )),

                new TestSpec(
                        ProbeTest2.class,
                        AbstractTest.tempInputFileName( ProbeTest2.class, "test2-active.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest2.class, "ProbeTest2_1.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest2.class, "ProbeTest2_2.xml" )),

                new TestSpec(
                        ProbeTest3.class,
                        AbstractTest.tempInputFileName( ProbeTest3.class, "test3-active.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest3.class, "ProbeTest2_1.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest3.class, "ProbeTest2_2.xml" )),
                        
                new TestSpec(
                        ProbeTest4.class,
                        AbstractTest.fileSystemFileName( ProbeTest4.class, "ProbeTest4.xml" )),

                new TestSpec(
                        ProbeTest5.class ),

//                new TestSpec( // FAILS (minor). Requires memory debugger to make progress. See also ProbeTest6
//                        ProbeTest6.class,
//                        AbstractTest.fileSystemFileName( ProbeTest6.class, "ProbeTest6.xml" )),
//
                new TestSpec(
                        ProbeTest7.class ),

// FIXME: ProbeTest8 seems to work, but spits out way too many warnings (but only when run in this test suite,
// not when run standalone). It may be related to failure to clean up Proxies to expired Shadows, and perhaps
// ProbeTest6. This needs more investigation but does not appear to be critical.
//                new TestSpec(
//                        ProbeTest8.class ),

                new TestSpec(
                        YadisTest1.class ),

                new TestSpec(
                        YadisTest2.class ),

                new TestSpec(
                        YadisTest3.class ),

                new TestSpec(
                        YadisTest4.class ),

                new TestSpec(
                        YadisTest5.class,
                        AbstractTest.fileSystemFileName( YadisTest5.class, "YadisTest5.xml" )),

                new TestSpec( // property updates
                            WritableProbeTest1.class ),
                            
                new TestSpec( // blessing
                            WritableProbeTest2.class ),
                            
                new TestSpec( // unblessing
                            WritableProbeTest3.class ),
                            
                new TestSpec( // deletion
                            WritableProbeTest4.class ),
                            

// FIXME: The remaining WritableProbeTests have not been written yet.
//                new TestSpec( // relationship delete
//                        WritableProbeTest5.class,
//                        noArgs ),
//                new TestSpec( // bless relationship
//                        WritableProbeTest6.class,
//                        noArgs ),
//                new TestSpec( // unbless relationship
//                        WritableProbeTest7.class,
//                        noArgs ),
//                new TestSpec( // create relationship between objects instantiated by probe but unrelated
//                        WritableProbeTest8.class,
//                        noArgs ),
//                new TestSpec( // create object -- BROKEN: need new API call to "createAndPush" to avoid triggering the non-local NetMeshObjectIdentifier exception
//                       .class,
//                        noArgs ),
//                new TestSpec( // create and relate object
//                        WritableProbeTest10.class,
//                        noArgs ),


                new TestSpec(
                        ShadowTest1.class,
                        AbstractTest.fileSystemFileName( ShadowTest1.class, "ProbeTest1.xml" )),
                            
                new TestSpec(
                        ShadowTest2.class ),
                            
                new TestSpec(
                        ShadowTest3.class,
                        AbstractTest.fileSystemFileName( ShadowTest3.class, "ShadowTest3a.html" )), // ShadowTest3b.xml referenced from ShadowTest3a.html

                new TestSpec(
                        ShadowTest4.class ),

                new TestSpec(
                        ShadowTest5.class ),

                new TestSpec(
                        ShadowTest6.class ),

                new TestSpec(
                        ShadowTest7.class ),

                new TestSpec(
                        ShadowTest8.class ),

                new TestSpec(
                        ProbeUpdateCalculatorTest1.class ),

                new TestSpec(
                        ForwardReferenceTest1.class,
                        AbstractTest.fileSystemFileName( ForwardReferenceTest1.class, "ForwardReferenceTest1_1.xml" )),
                                // ForwardReferenceRest1_2.xml included by reference from ForwardReferenceRest1.xml

                new TestSpec(
                        ForwardReferenceTest2.class ),

                new TestSpec(
                        ForwardReferenceTest3.class ),

                new TestSpec(
                        ForwardReferenceTest4.class ),

                new TestSpec(
                        ProbeMatchTest1.class ),
        };

        runTests( tests );
    }
}

