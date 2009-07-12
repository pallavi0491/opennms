/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.stereotype.Repository;

@Repository
public interface NodeDao extends OnmsDao<OnmsNode, Integer> {
	
    /**
     * Get a node based on it's node ID or foreignSource:foreignId
     * @param lookupCriteria the criteria, either the node ID, or a colon-separated string of foreignSource:foreignId
     * @return the node
     */
    public abstract OnmsNode get(String lookupCriteria);
    
    public abstract Collection<OnmsNode> findByLabel(String label);
    
    public abstract Collection<OnmsNode> findNodes(OnmsDistPoller dp);
    
    public abstract OnmsNode getHierarchy(Integer id);
    
    public abstract Map<String, Integer> getForeignIdToNodeIdMap(String foreignSource);
    
    public abstract Collection<OnmsNode> findAllByVarCharAssetColumn(String columnName, String columnValue);
    
    public abstract Collection<OnmsNode> findAllByVarCharAssetColumnCategoryList(String columnName, String columnValue,
            Collection<OnmsCategory> categories);
    
    public abstract Collection<OnmsNode> findByCategory(OnmsCategory category);
    
    public abstract Collection<OnmsNode> findAllByCategoryList(Collection<OnmsCategory> categories);

    public abstract Collection<OnmsNode> findAllByCategoryLists(Collection<OnmsCategory> rowCatNames, Collection<OnmsCategory> colCatNames);
    
    /**
     * Returns a list of nodes ordered by label.
     */
    public abstract List<OnmsNode> findAll();

    public abstract List<OnmsNode> findByForeignSource(String foreignSource);
    
    public abstract OnmsNode findByForeignId(String foreignSource, String foreignId);

    public abstract int getNodeCountForForeignSource(String groupName);
    
    public abstract List<OnmsNode> findAllProvisionedNodes();
    
    public abstract List<OnmsIpInterface> findObsoleteIpInterfaces(Integer nodeId, Date scanStamp);

    public abstract void deleteObsoleteInterfaces(Integer nodeId, Date scanStamp);

    public abstract void updateNodeScanStamp(Integer nodeId, Date scanStamp);

    public abstract Collection<Integer> getNodeIds();
}
