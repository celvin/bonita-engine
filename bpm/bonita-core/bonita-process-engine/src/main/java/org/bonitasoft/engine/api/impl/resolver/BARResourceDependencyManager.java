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

package org.bonitasoft.engine.api.impl.resolver;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bar.BARResourceType;
import org.bonitasoft.engine.bar.BusinessArchiveResourceService;
import org.bonitasoft.engine.bar.SBARResource;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;

/**
 * @author Baptiste Mesta
 */
public abstract class BARResourceDependencyManager implements BusinessArchiveDependencyManager {
    protected final BusinessArchiveResourceService businessArchiveResourceService;

    public BARResourceDependencyManager(BusinessArchiveResourceService businessArchiveResourceService) {
        this.businessArchiveResourceService = businessArchiveResourceService;
    }

    void saveResources(BusinessArchive businessArchive, SProcessDefinition processDefinition, String folder, BARResourceType type) {
        final Map<String, byte[]> resources = businessArchive.getResources("^" + folder + "/.*$");
        for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
            businessArchiveResourceService.add(processDefinition.getId(), entry.getKey().substring((folder + "/").length()),
                    type, entry.getValue());
        }
    }

    void exportResourcesToBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder, BARResourceType document) {
        List<SBARResource> resources;
        int from = 0;
        do {
            resources = businessArchiveResourceService.getAll(processDefinitionId, document, from, 10);
            addToBusinessArchive(businessArchiveBuilder, resources);
            from += 10;
        } while (resources.size() == 10);
    }

    void addToBusinessArchive(BusinessArchiveBuilder businessArchiveBuilder, List<SBARResource> resources) {
        for (SBARResource resource : resources) {
            addToBusinessArchive(businessArchiveBuilder, new BarResource(resource.getName(), resource.getContent()));
        }
    }

    abstract void addToBusinessArchive(BusinessArchiveBuilder businessArchiveBuilder, BarResource resource);
}
