/*
 * $Id$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package com.sun.javatest.agent;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * A factory for creating connections to be used by agents running in "active" mode.
 */
public class ActiveConnectionFactory implements ConnectionFactory
{
    /**
     * Create a factory for creating connections to be used by agents running
     * in "active" mode.
     * @param host The host to which the connections should connect.
     * @param port The port on the host to which the connections should connect.
     */
    public ActiveConnectionFactory(String host, int port) {
        if (host == null)
            throw new NullPointerException();

        if (host.length() == 0 || port <= 0)
            throw new IllegalArgumentException();

        this.host = host;
        this.port = port;
    }

    /**
     * Get the host to which connections returned by this factory will connect.
     * @return the host to which connections will be made
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the port to which connections returned by this factory will connect.
     * @return the port to which connections will be made
     */
    public int getPort() {
        return port;
    }

    public Connection nextConnection() throws ConnectionFactory.Fault {
        try {
            return new SocketConnection(new Socket(host, port));
        }
        catch (UnknownHostException e) {
            throw new ConnectionFactory.Fault(e, true);
        }
        catch (IOException e) {
            throw new ConnectionFactory.Fault(e, false);
        }
    }

    public void close() {
    }

    private String host;
    private int port;
}
