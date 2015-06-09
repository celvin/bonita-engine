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

package org.bonitasoft.engine.core;

import java.io.IOException;

import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.commons.exceptions.SAlreadyExistsException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;

/**
 * @author Baptiste Mesta
 */
public class BusinessArchiveServiceImpl implements BusinessArchiveService {

    private final ProcessDefinitionService processDefinitionService;
    private final DependencyService dependencyService;
    private final DependencyResolver dependencyResolver;
    private final TechnicalLoggerService logger;
    private final ReadSessionAccessor readSessionAccessor;

    public BusinessArchiveServiceImpl(ProcessDefinitionService processDefinitionService, DependencyService dependencyService,
            DependencyResolver dependencyResolver, TechnicalLoggerService logger, ReadSessionAccessor readSessionAccessor) {
        this.processDefinitionService = processDefinitionService;
        this.dependencyService = dependencyService;
        this.dependencyResolver = dependencyResolver;
        this.logger = logger;
        this.readSessionAccessor = readSessionAccessor;
    }

    @Override
    public SProcessDefinition deploy(BusinessArchive businessArchive) throws SObjectCreationException, SAlreadyExistsException {

        final DesignProcessDefinition designProcessDefinition = businessArchive.getProcessDefinition();

        SProcessDefinition sProcessDefinition;
        try {
            checkIfExists(designProcessDefinition);
            sProcessDefinition = processDefinitionService.store(designProcessDefinition);

            unzipBar(businessArchive, sProcessDefinition, getTenantId());
            final boolean isResolved = dependencyResolver.resolveDependencies(businessArchive, sProcessDefinition);
            if (isResolved) {
                dependencyResolver.resolveAndCreateDependencies(businessArchive, processDefinitionService, dependencyService,
                        sProcessDefinition);
            }
        } catch (SAlreadyExistsException e) {
            throw e;
        } catch (final BonitaHomeNotSetException | IOException | SBonitaException e) {
            throw new SObjectCreationException(e);
        }
        info(sProcessDefinition);
        return sProcessDefinition;
    }

    void checkIfExists(DesignProcessDefinition designProcessDefinition) throws SBonitaReadException, SAlreadyExistsException {
        try {
            processDefinitionService.getProcessDefinitionId(designProcessDefinition.getName(), designProcessDefinition.getVersion());
            throw new SAlreadyExistsException("The process " + designProcessDefinition.getName() + " in version " + designProcessDefinition.getVersion()
                    + " already exists.");
        } catch (final SProcessDefinitionNotFoundException e) {
            // ok
        }
    }

    void info(SProcessDefinition sProcessDefinition) {
        if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(this.getClass(), TechnicalLogSeverity.INFO, "The user <" + SessionInfos.getUserNameFromSession() + "> has installed process <"
                    + sProcessDefinition.getName() + "> in version <" + sProcessDefinition.getVersion() + "> with id <" + sProcessDefinition.getId() + ">");
        }
    }

    long getTenantId() throws STenantIdNotSetException {
        return readSessionAccessor.getTenantId();
    }

    @Override
    public BusinessArchive export(long processDefinitionId) {
        return null;
    }

    protected void unzipBar(final BusinessArchive businessArchive, final SProcessDefinition sProcessDefinition, final long tenantId)
            throws BonitaHomeNotSetException, IOException {
        BonitaHomeServer.getInstance().getProcessManager().writeBusinessArchive(tenantId, sProcessDefinition.getId(), businessArchive);
    }
}
