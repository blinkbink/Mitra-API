/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2007 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.ee.status;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;
import org.jpos.q2.iso.ChannelAdaptor;

public class ChannelMonitor extends Log implements TwoWayMonitorTask, Configurable {
    Configuration cfg;
    String serviceName;
    @Override
	public String checkService () {
        boolean rc = false;
        ChannelAdaptor channel;
        try {
            channel = (ChannelAdaptor) NameRegistrar.get (serviceName);
        } catch (NameRegistrar.NotFoundException e) {
            return StatusBase.ERROR + " channel '" + serviceName + "' not found.";
        }
        if (channel.isConnected())
            return StatusBase.OK + " Connected";
        else
            return StatusBase.ERROR + " Not connected";
    }
    @Override
	public String command (String command) {
        String response = "Unknown command";
        ChannelAdaptor channel;
        try {
            channel = (ChannelAdaptor) NameRegistrar.get (serviceName);
        } catch (NameRegistrar.NotFoundException e) {
            return "Channel '" + serviceName + "' not found";
        }
        if ("STOP".equalsIgnoreCase (command)) {
            channel.stop();
            response = "Channel stopped";
        } else if ("START".equalsIgnoreCase (command)) {
            channel.start();
            response = "Channel started";
        }
        return response;
    }
    @Override
	public void setConfiguration (Configuration cfg) 
        throws ConfigurationException
    {
        this.cfg = cfg;
        serviceName = cfg.get ("channel", null);
        if (serviceName == null)
            throw new ConfigurationException ("property 'channel' is null");
    }
}

