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

package org.infogrid.probe.shadow;

import java.util.EventObject;

/**
  * This is a ShadowModelObjectRepository-specific event. The actual meaning
  * of the event is determined by which of the listener methods have been invoked.
  */
public class ShadowMeshBaseEvent
    extends
        EventObject
{
    /**
     * Constructor. No problem occurred.
     *
     * @param meshBase the ShadowMeshBase that sent this event
     */
    public ShadowMeshBaseEvent(
            ShadowMeshBase meshBase )
    {
        this( meshBase, null );
    }

    /**
     * Constructor. A problem occurred.
     *
     * @param meshBase the ShadowMeshBase that sent this event
     * @param problem the Throwable indicating the problem that occurred
     */
    public ShadowMeshBaseEvent(
            ShadowMeshBase meshBase,
            Throwable      problem )
    {
        super( meshBase );

        theProblem = problem;
    }

    /**
     * Obtain the problem that occurred, if any.
     *
     * @return the Throwable indicating the problem, if any.
     */
    public final Throwable getProblem()
    {
        return theProblem;
    }

    /**
     * The problem that occurred, if any.
     */
    protected Throwable theProblem;
}
