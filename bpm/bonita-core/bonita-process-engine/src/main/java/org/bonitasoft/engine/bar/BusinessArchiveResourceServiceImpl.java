/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.engine.bar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveResourceServiceImpl implements BusinessArchiveResourceService {

    private final HashMap<String, byte[]> documents = new HashMap<>();
    private final HashMap<String, byte[]> external = new HashMap<>();
    private final HashMap<String, byte[]> connectors = new HashMap<>();
    private final HashMap<String, byte[]> userFilters = new HashMap<>();

    @Override
    public void add(long processDefinitionId, String name, BARResourceType type, byte[] content) {
        switch (type) {

            case DOCUMENT:
                documents.put(processDefinitionId+name, content);
                break;
            case EXTERNAL:
                external.put(processDefinitionId+name, content);
                break;
            case CONNECTOR:
                connectors.put(processDefinitionId+name, content);
                break;
            case USER_FILTER:
                userFilters.put(processDefinitionId+name, content);
                break;
        }
    }

    @Override
    public void removeAll(long processDefinitionId) {

    }

    @Override
    public List<SBARResource> getAll(long processDefinitionId, BARResourceType type, int from, int numberOfElements) {
        switch (type) {
            case DOCUMENT:
                return getSbarResources(processDefinitionId, this.documents);
            case EXTERNAL:
                return getSbarResources(processDefinitionId, this.external);
            case CONNECTOR:
                return getSbarResources(processDefinitionId, this.connectors);
            case USER_FILTER:
                return getSbarResources(processDefinitionId, this.userFilters);
        }
        return null;
    }

    List<SBARResource> getSbarResources(long processDefinitionId, HashMap<String, byte[]> map) {
        final ArrayList<SBARResource> sbarResources = new ArrayList<>();
        for (Map.Entry<String, byte[]> doc : map.entrySet()) {
            if(doc.getKey().startsWith(String.valueOf(processDefinitionId))){
                final SBARResource e = new SBARResource();
                e.setName(doc.getKey().substring(String.valueOf(processDefinitionId).length()));
                e.setContent(doc.getValue());
                sbarResources.add(e);
            }
        }
        return sbarResources;
    }
}
