/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.filter.UserFilterService;
import org.bonitasoft.engine.core.filter.exception.SUserFilterLoadingException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.filter.UserFilterException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class UserFilterBusinessArchiveDependencyManager implements BusinessArchiveDependencyManager {

    private final ReadSessionAccessor readSessionAccessor;
    private final UserFilterService userFilterService;

    public UserFilterBusinessArchiveDependencyManager(ReadSessionAccessor readSessionAccessor, UserFilterService userFilterService) {
        this.readSessionAccessor = readSessionAccessor;
        this.userFilterService = userFilterService;
    }

    @Override
    public boolean deploy(final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws UserFilterException {
        try {
            final long tenantId = readSessionAccessor.getTenantId();
            return userFilterService.loadUserFilters(processDefinition.getId(), tenantId);
        } catch (final SUserFilterLoadingException | STenantIdNotSetException e) {
            throw new UserFilterException(e);
        }
    }

    @Override
    public List<Problem> checkResolution(final SProcessDefinition processDefinition) {
        return Collections.emptyList();
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException {

    }

}
