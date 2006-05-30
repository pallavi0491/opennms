//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.syslogd;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.queue.FifoQueue;
import org.opennms.core.queue.FifoQueueException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;



/**
 * This class encapsulates the execution context for processing events received
 * via UDP from remote agents. This is a separate event context to allow the
 * event receiver to do minimum work to avoid dropping packets from the agents.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.oculan.com">Oculan Corporation </a>
 * 
 */
final class SyslogProcessor implements Runnable {
    /**
     * The UDP receiver thread.
     */
    private Thread m_context;

    /**
     * The list of incomming events.
     */
    private List m_eventsIn;

    /**
     * The list of outgoing event-receipts by UUID.
     */
    private List m_eventsOut;

    /**
     * The list of registered event handlers.
     */
    private List m_handlers;

    /**
     * The stop flag
     */
    private volatile boolean m_stop;

    /**
     * The UDP socket for receipt and transmission of packets from agents.
     */
    private DatagramSocket m_dgSock;
    
    
    private FifoQueue m_queue;

    /**
     * The log prefix
     */
    private String m_logPrefix;
    
    private String m_localAddr;

    SyslogProcessor() {
        m_context = null;
        m_stop = false;
        
        
        m_logPrefix = Syslogd.LOG4J_CATEGORY;
        
        try {
            m_localAddr = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhE) {
            Category log = ThreadCategory.getInstance(getClass());
   
            m_localAddr = "localhost";
            log.error("<ctor>: Error looking up local hostname", uhE);
        }
        
    }

    /**
     * Returns true if the thread is still alive
     */
    boolean isAlive() {
        return (m_context == null ? false : m_context.isAlive());
    }

    /**
     * Stops the current context
     */
    void stop() throws InterruptedException {
        m_stop = true;
        if (m_context != null) {
            Category log = ThreadCategory.getInstance(getClass());
            if (log.isDebugEnabled())
                log.debug("Stopping and joining thread context " + m_context.getName());

            m_context.interrupt();
            m_context.join();

            if (log.isDebugEnabled())
                log.debug("Thread context stopped and joined");
        }
    }

    /**
     * The event processing execution context.
     */
    public void run() {
        // The runnable context
        //
        m_context = Thread.currentThread();

        // get a logger
        //
        ThreadCategory.setPrefix(m_logPrefix);
        Category log = ThreadCategory.getInstance(getClass());
        boolean isTracing = log.isDebugEnabled();

        if (m_stop) {
            if (isTracing)
                log.debug("Stop flag set before thread started, exiting");
            return;
        } else if (isTracing)
            log.debug("Thread context started");

        // This loop is labeled so that it can be
        // exited quickly when the thread is interrupted
        //
        RunLoop: while (!m_stop) {
            
            
                    ConvertToEvent o = null;

                    o = (ConvertToEvent)SyslogHandler.queueManager.getFromQueue();
                    

                    if (o != null) {
                        try {
                            log.debug("Processing a syslog to event dispatch" + o.toString());
                            
                            // print out the eui, source, and other
                            // important aspects
                            //
                            String uuid = o.getEvent().getUuid();
                            log.debug("Event {");
                            log.debug("  uuid  = " + (uuid != null && uuid.length() > 0 ? uuid : "<not-set>"));
                            log.debug("  uei   = " + o.getEvent().getUei());
                            log.debug("  src   = " + o.getEvent().getSource());
                            log.debug("  iface = " + o.getEvent().getInterface());
                            log.debug("  time  = " + o.getEvent().getTime());
                            log.debug("  Msg   = " + o.getEvent().getLogmsg().getContent());
                            log.debug("  Dst   = " + o.getEvent().getLogmsg().getDest());
                            Parm[] parms = (o.getEvent().getParms() == null ? null : o.getEvent().getParms().getParm());
                            if (parms != null) {
                                log.debug("  parms {");
                                for (int x = 0; x < parms.length; x++) {
                                    if ((parms[x].getParmName() != null) && (parms[x].getValue().getContent() != null)) {
                                        log.debug("    (" + parms[x].getParmName().trim() + ", " + parms[x].getValue().getContent().trim() + ")");
                                    }
                                }
                                log.debug("  }");
                            }
                            log.debug("}");    
                            EventIpcManagerFactory.getIpcManager().sendNow(o.getEvent());
                        } catch (Throwable t) {
                            log.error("Unexpected error processing SyslogMessage - Could not send", t);
                        }
                    }
                
        
            }
        
    } // end run()

    void setLogPrefix(String prefix) {
        m_logPrefix = prefix;
    }
    
    private void sendNewSuspectEvent(String trapInterface) {
        // construct event with 'trapd' as source
        Event event = new Event();
        event.setSource("syslogd");
        event.setUei(org.opennms.netmgt.EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI);
        event.setHost(m_localAddr);
        event.setInterface(trapInterface);
        event.setTime(org.opennms.netmgt.EventConstants.formatToString(new java.util.Date()));
        EventIpcManagerFactory.getIpcManager().sendNow(event);
        // send the event to eventd
        //m_eventMgr.sendNow(event);
    }
} // end SyslogProcessor Class

