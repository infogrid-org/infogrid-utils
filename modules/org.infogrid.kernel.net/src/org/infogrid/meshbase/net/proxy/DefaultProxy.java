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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.util.logging.Log;

/**
 * The default implementation of Proxy.
 */
public class DefaultProxy
        extends
            AbstractProxy
{
    private static final Log log = Log.getLogInstance( DefaultProxy.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param ep the communications endpoint
     * @param mb the NetMeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @return the created DefaultProxy
     */
    public static DefaultProxy create(
            ProxyMessageEndpoint ep,
            NetMeshBase          mb,
            ProxyPolicy          policy )
    {
        DefaultProxy ret = new DefaultProxy( ep, mb, policy );

        if( log.isDebugEnabled() ) {
            log.debug( "Created " + ret, new RuntimeException( "marker" ));
        }
        return ret;
    }

    /**
     * Factory method to restore a Proxy from storage.
     *
     * @param ep the communications endpoint
     * @param mb the NetMeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     * @return the restored DefaultProxy
     */
    public static DefaultProxy restoreProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase          mb,
            ProxyPolicy          policy,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
    {
        DefaultProxy ret = new DefaultProxy( ep, mb, policy, timeCreated, timeUpdated, timeRead, timeExpires );

        if( log.isDebugEnabled() ) {
            log.debug( "Restored " + ret, new RuntimeException( "marker" ));
        }
        return ret;
    }

    /**
     * Constructor.
     *
     * @param ep the communications endpoint
     * @param mb the NetMeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     */
    protected DefaultProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase          mb,
            ProxyPolicy          policy )
    {
        super( ep, mb, policy );
    }

    /**
     * Constructor.
     *
     * @param ep the communications endpoint
     * @param mb the NetMeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     */
    protected DefaultProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase          mb,
            ProxyPolicy          policy,
            long                 timeCreated,
            long                 timeUpdated,
            long                 timeRead,
            long                 timeExpires )
    {
        super( ep, mb, policy );
        
        theTimeCreated = timeCreated;
        theTimeUpdated = timeUpdated;
        theTimeRead    = timeRead;
        theTimeExpires = timeExpires;
    }

    /**
     * Internal implementation method for messageReceived. Overriding this makes
     * debugging easier as we only get breakpoints from instances of this class.
     *
     * @param endpoint the MessageEndpoint sending this event
     * @param incoming the incoming message
     */
    @Override
    protected void internalMessageReceived(
            ReceivingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                           incoming )
    {
        super.internalMessageReceived( endpoint, incoming );
    }
}
