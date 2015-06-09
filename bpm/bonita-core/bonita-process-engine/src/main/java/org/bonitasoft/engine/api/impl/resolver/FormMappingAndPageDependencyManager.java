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
 */
package org.bonitasoft.engine.api.impl.resolver;

import static org.bonitasoft.engine.form.FormMappingType.PROCESS_OVERVIEW;
import static org.bonitasoft.engine.form.FormMappingType.PROCESS_START;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.engine.api.impl.converter.PageModelConverter;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingDefinition;
import org.bonitasoft.engine.bpm.bar.form.model.FormMappingModel;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.impl.internal.ProblemImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.core.form.FormMappingService;
import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.form.FormMapping;
import org.bonitasoft.engine.form.FormMappingTarget;
import org.bonitasoft.engine.form.FormMappingType;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.page.SPageMapping;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * @author Laurent Leseigneur
 */
public class FormMappingAndPageDependencyManager implements BusinessArchiveDependencyManager {

    public static final String ERROR_MESSAGE = "error while resolving form mapping %s";
    private static final String regex = "^resources/customPages/(custompage_.*)\\.(zip)$";
    private final SessionService sessionService;
    private final SessionAccessor sessionAccessor;
    private final PageService pageService;
    private final TechnicalLoggerService technicalLoggerService;
    private final FormMappingService formMappingService;

    public FormMappingAndPageDependencyManager(SessionService sessionService, SessionAccessor sessionAccessor, PageService pageService,
            TechnicalLoggerService technicalLoggerService, FormMappingService formMappingService) {
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.pageService = pageService;
        this.technicalLoggerService = technicalLoggerService;
        this.formMappingService = formMappingService;
    }

    @Override
    public boolean deploy(final BusinessArchive businessArchive, final SProcessDefinition processDefinition)
            throws ProcessDeployException {

        deployProcessPages(businessArchive, processDefinition.getId(), sessionService.getLoggedUserFromSession(sessionAccessor));
        deployFormMappings(businessArchive, processDefinition.getId());
        return checkResolution(processDefinition).isEmpty();
    }

    public void deployProcessPages(BusinessArchive businessArchive, Long processDefinitionId, long userId) {
        final Map<String, byte[]> pageResources = getPageResources(businessArchive);
        for (final Map.Entry<String, byte[]> resource : pageResources.entrySet()) {
            try {
                deployPage(resource.getKey(), resource.getValue(), processDefinitionId, userId, pageService);
            } catch (SBonitaException e) {
                technicalLoggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Unable to deploy all pages", e);
            }
        }
    }

    protected Map<String, byte[]> getPageResources(BusinessArchive businessArchive) {
        return businessArchive.getResources(regex);
    }

    private void deployPage(String resourcePath, byte[] pageContent, Long processDefinitionId, long userId, PageService pageService) throws SBonitaException {
        final Matcher pathMatcher = getPathMatcher(resourcePath);
        if (pathMatcher.matches()) {
            final String pageName = pathMatcher.group(1);
            final String extension = pathMatcher.group(2);
            String contentName = pageName + "." + extension;
            final SPage sPage = pageService.getPageByNameAndProcessDefinitionId(pageName, processDefinitionId);
            if (sPage != null) {
                pageService.updatePageContent(sPage.getId(), pageContent, contentName);
            } else {
                final Properties pageProperties = pageService.readPageZip(pageContent);
                final PageCreator pageCreator = new PageCreator(pageName, contentName, ContentType.FORM, processDefinitionId)
                        .setDisplayName(pageProperties.getProperty(PageService.PROPERTIES_DISPLAY_NAME))
                        .setDescription(pageProperties.getProperty(PageService.PROPERTIES_DESCRIPTION));
                final SPage newPage = new PageModelConverter().constructSPage(pageCreator, userId);
                pageService.addPage(newPage, pageContent);
            }
        }
    }

    private Matcher getPathMatcher(String resourcePath) {
        final Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(resourcePath);
    }

    @Override
    public List<Problem> checkResolution(final SProcessDefinition processDefinition) {
        List<Problem> problems = new ArrayList<>();
        try {
            problems = checkPageProcessResolution(processDefinition);
        } catch (SBonitaReadException | SObjectNotFoundException e) {
            problems.add(new ProblemImpl(Problem.Level.ERROR, null, null, "unable to resolve form mapping dependencies"));
        }
        return problems;
    }

    @Override
    public void delete(SProcessDefinition processDefinition) throws SObjectModificationException {
        try {
            deleteFormMapping(processDefinition.getId());
            deleteProcessPages(processDefinition.getId());
        } catch (SBonitaReadException | SObjectNotFoundException e) {
            throw new SObjectModificationException("Unable to delete forms and page of the process definition <" + processDefinition.getName() + ">", e);
        }
    }

    @Override
    public void exportBusinessArchive(long processDefinitionId, BusinessArchiveBuilder businessArchiveBuilder) throws SBonitaException {
        final FormMappingModel formMappingModel = new FormMappingModel();
        final List<SFormMapping> formMappings = formMappingService.list(processDefinitionId, 0, Integer.MAX_VALUE);
        for (SFormMapping formMapping : formMappings) {
            final FormMapping client = ModelConvertor.toFormMapping(formMapping);
            String form = null;
            switch (client.getTarget()) {
                case INTERNAL:
                    final SPage page = pageService.getPage(client.getPageId());
                    form = page.getName();
                    break;
                case URL:
                    form = client.getURL();
                    break;
            }
            final FormMappingDefinition mapping = new FormMappingDefinition(form, client.getType(), client.getTarget(), client.getTask());
            formMappingModel.addFormMapping(mapping);
        }
        businessArchiveBuilder.setFormMappings(formMappingModel);
    }

    protected void deleteFormMapping(Long processDefinitionId) throws SBonitaReadException, SObjectModificationException {
        List<SFormMapping> formMappings;
        do {
            formMappings = formMappingService.list(processDefinitionId, 0, 100);
            for (SFormMapping formMapping : formMappings) {
                formMappingService.delete(formMapping);
            }
        } while (formMappings.size() == 100);
    }

    private void deleteProcessPages(Long processDefinitionId) throws SBonitaReadException, SObjectModificationException, SObjectNotFoundException {
        List<SPage> sPages;
        do {
            sPages = pageService.getPageByProcessDefinitionId(processDefinitionId, 0, 100);
            for (SPage sPage : sPages) {
                pageService.deletePage(sPage.getId());
            }
        } while (sPages.size() == 100);
    }

    protected List<Problem> checkPageProcessResolution(SProcessDefinition sProcessDefinition) throws SBonitaReadException,
            SObjectNotFoundException {
        final List<Problem> problems = new ArrayList<>();
        List<SFormMapping> formMappings;
        do {
            formMappings = formMappingService.list(sProcessDefinition.getId(), 0, 100);
            for (SFormMapping formMapping : formMappings) {
                checkFormMappingResolution(formMapping, problems);
            }
        } while (formMappings.size() == 100);
        return problems;
    }

    protected void checkFormMappingResolution(SFormMapping formMapping, List<Problem> problems)
            throws SBonitaReadException, SObjectNotFoundException {
        if (isMappingRelatedToCustomPage(formMapping)) {
            SPageMapping pageMapping = formMapping.getPageMapping();
            if (pageMapping == null) {
                addProblem(formMapping, problems);
                return;
            }
            final Long pageId = pageMapping.getPageId();
            if (pageId == null || pageService.getPage(pageId) == null) {
                addProblem(formMapping, problems);
            }
        }
    }

    private void addProblem(SFormMapping formMapping, List<Problem> problems) {
        problems.add(new ProblemImpl(Problem.Level.ERROR, formMapping.getProcessElementName(), "form mapping", String.format(ERROR_MESSAGE,
                formMapping.toString())));
    }

    private boolean isMappingRelatedToCustomPage(SFormMapping formMapping) {
        return FormMappingTarget.INTERNAL.name().equals(formMapping.getTarget());
    }

    public void deployFormMappings(final BusinessArchive businessArchive, final long processDefinitionId)
            throws ProcessDeployException {
        final List<FormMappingDefinition> formMappings = businessArchive.getFormMappingModel().getFormMappings();
        final List<ActivityDefinition> activities = businessArchive.getProcessDefinition().getFlowElementContainer().getActivities();
        try {
            // Deals with human tasks declared in process definition:
            for (final ActivityDefinition activity : activities) {
                if (isHumanTask(activity)) {
                    // create mapping as declared in form mapping:
                    createFormMapping(formMappingService, processDefinitionId, getFormMappingForHumanTask(activity.getName(), formMappings),
                            FormMappingType.TASK.getId(),
                            activity.getName());
                }
            }
            // Deals with the process start / process overview forms:
            createFormMapping(formMappingService, processDefinitionId, getFormMappingForType(formMappings, PROCESS_START), PROCESS_START.getId(), null);
            createFormMapping(formMappingService, processDefinitionId, getFormMappingForType(formMappings, PROCESS_OVERVIEW), PROCESS_OVERVIEW.getId(), null);
        } catch (final SObjectCreationException | SBonitaReadException e) {
            throw new ProcessDeployException(e);
        }
    }

    private void createFormMapping(FormMappingService formMappingService, long processDefinitionId, FormMappingDefinition formMappingDefinition, Integer type,
            String taskName)
            throws SObjectCreationException, SBonitaReadException {
        if (formMappingDefinition != null) {
            createSFormMapping(formMappingService, processDefinitionId, formMappingDefinition);
        } else {
            formMappingService.create(processDefinitionId, taskName, type, FormMappingTarget.UNDEFINED.name(), null);
        }
    }

    private SFormMapping createSFormMapping(FormMappingService formMappingService, long processDefinitionId, FormMappingDefinition formMappingDefinition)
            throws SObjectCreationException,
            SBonitaReadException {
        return formMappingService.create(processDefinitionId, formMappingDefinition.getTaskname(), formMappingDefinition.getType().getId(),
                formMappingDefinition.getTarget().name(), formMappingDefinition.getForm());
    }

    private boolean isHumanTask(final ActivityDefinition activity) {
        return activity instanceof HumanTaskDefinition;
    }

    /**
     * @return the found mapping for the given human task, or null is not found
     */
    private FormMappingDefinition getFormMappingForHumanTask(final String name, final List<FormMappingDefinition> formMappings) {
        for (final FormMappingDefinition formMapping : formMappings) {
            if (name.equals(formMapping.getTaskname())) {
                return formMapping;
            }
        }
        return null;
    }

    private FormMappingDefinition getFormMappingForType(final List<FormMappingDefinition> formMappings, final FormMappingType type) {
        for (final FormMappingDefinition formMapping : formMappings) {
            if (type == formMapping.getType()) {
                return formMapping;
            }
        }
        return null;
    }
}
