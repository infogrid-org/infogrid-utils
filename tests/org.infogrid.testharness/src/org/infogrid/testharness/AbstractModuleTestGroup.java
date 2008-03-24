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

package org.infogrid.testharness;

import org.infogrid.module.Module;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleRequirement;

import org.infogrid.util.logging.Log;


/**
 * This provides methods to easily run a group of tests specified as test Modules.
 */
public abstract class AbstractModuleTestGroup
{
    private static final Log log = Log.getLogInstance( AbstractModuleTestGroup.class ); // our own, private logger

    /**
     * Constructor.
     */
    private AbstractModuleTestGroup()
    {
        // no op
    }

    /**
     * Run a list of tests.
     *
     * @param testSpecs the tests
     */
    protected static void runTests(
            ModuleTestSpec [] testSpecs )
    {
        for( int i=0 ; i<testSpecs.length ; ++i ) {
            try {
                testSpecs[i].run();
            } catch( Throwable t ) {
                log.error( "FAIL", t );

                System.exit( -1 );
            }
        }
    }
    
    /**
     * Captures one test.
     */
    public static class ModuleTestSpec
    {
        /**
         * Constructor.
         *
         * @param testName name of the test
         * @param testModuleRequirement ModuleRequirement indicating the test to be run
         * @param testArgs the arguments for the test
         */
        public ModuleTestSpec(
                String            testName,
                ModuleRequirement testModuleRequirement,
                String []         testArgs )
        {
            theTestName              = testName;
            theTestModuleRequirement = testModuleRequirement;
            theTestArgs              = testArgs;
        }
        
        /**
         * Constructor.
         *
         * @param testModuleRequirement ModuleRequirement indicating the test to be run
         * @param testArgs the arguments for the test
         */
        public ModuleTestSpec(
                ModuleRequirement testModuleRequirement,
                String []         testArgs )
        {
            this( testModuleRequirement.toString(), testModuleRequirement, testArgs );
        }
        
        /**
         * Constructor for a test without arguments.
         *
         * @param testModuleRequirement ModuleRequirement indicating the test to be run
         */
        public ModuleTestSpec(
                ModuleRequirement testModuleRequirement )
        {
            this( testModuleRequirement.toString(), testModuleRequirement, new String[0] );
        }
        
        /**
         * Run the test described by this TestSpec.
         *
         * @throws Throwable any type of exception thrown while executing the test
         */
        public void run()
            throws
                Throwable
        {
            ModuleRegistry theModuleRegistry = ModuleRegistryContext.getModuleRegistry();
            ModuleAdvertisement [] candidates = theModuleRegistry.determineResolutionCandidates( theTestModuleRequirement );
            if( candidates.length == 0 ) {
                throw new IllegalArgumentException( "Cannot find Module for " + theTestModuleRequirement );
            }
            if( candidates.length > 1 ) {
                throw new IllegalArgumentException( "More than one Module for " + theTestModuleRequirement );
            }

            Module testModule = theModuleRegistry.resolve( candidates[0] );

            testModule.configureRecursively( null );
            testModule.activateRecursively();
            testModule.run( theTestArgs );
        }

        /**
         * The name of the test.
         */
        protected String theTestName;
        
        /**
         * Captures the Module that runs the rest.
         */
        protected ModuleRequirement theTestModuleRequirement;
        
        /**
         * The arguments into the test.
         */
        protected String [] theTestArgs;
    }
}
