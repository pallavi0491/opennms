/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.core.jms;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ActiveMQ component with support for retrieving credentials from  SCV. 
 *
 * @author jwhite
 */
public class ScvEnabledActiveMQConnectionFactory extends ActiveMQConnectionFactory {
    public static final Logger LOG = LoggerFactory.getLogger(ScvEnabledActiveMQConnectionFactory.class);

    public ScvEnabledActiveMQConnectionFactory(String brokerUrl, SecureCredentialsVault scv, String scvAlias) {
        this.setBrokerURL(brokerUrl);
        final Credentials amqCredentials = scv.getCredentials(scvAlias);
        if (amqCredentials == null) {
            LOG.warn("No credentials found in SCV for alias '{}'. Using default credentials.", scvAlias);
            setUserName("admin");
            setPassword("admin");
        } else {
            setUserName(amqCredentials.getUsername());
            setPassword(amqCredentials.getPassword());
        }
    }
}
