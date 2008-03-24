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

package org.infogrid.kernel.active.TEST;

import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest1;
import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest2;
import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest3;
import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest4;
import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest5;
import org.infogrid.kernel.active.TEST.objectset.ActiveMeshObjectSetTest6;
import org.infogrid.kernel.active.TEST.traversalpathset.ActiveTraversalPathSetTest1;
import org.infogrid.kernel.active.TEST.traversalpathset.ActiveTraversalPathSetTest2;

import org.infogrid.testharness.AbstractTestGroup;

/**
 *
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
        TestSpec [] tests = {
                new TestSpec( ActiveMeshObjectSetTest1.class ), // ok
                new TestSpec( ActiveMeshObjectSetTest2.class ), // ok
                new TestSpec( ActiveMeshObjectSetTest3.class ), // ok
                new TestSpec( ActiveMeshObjectSetTest4.class ), // ok
                new TestSpec( ActiveMeshObjectSetTest5.class ), // ok
                new TestSpec( ActiveMeshObjectSetTest6.class ), // ok
//                new TestSpec( ActiveMeshObjectSetTest7.class ), // FIXME
//                new TestSpec( ActiveMeshObjectSetTest8.class ), // FIXME

                new TestSpec( ActiveTraversalPathSetTest1.class ),
                new TestSpec( ActiveTraversalPathSetTest2.class )
        };

        runTests( tests );
    }
}

