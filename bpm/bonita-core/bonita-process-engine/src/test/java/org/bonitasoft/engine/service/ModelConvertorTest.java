package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoDefinition;
import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.bpm.contract.ComplexInputDefinition;
import org.bonitasoft.engine.bpm.contract.ConstraintDefinition;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.SimpleInputDefinition;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.contract.impl.ComplexInputDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.ConstraintDefinitionImpl;
import org.bonitasoft.engine.bpm.contract.impl.SimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.core.process.definition.model.SComplexInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.SConstraintDefinition;
import org.bonitasoft.engine.core.process.definition.model.SContractDefinition;
import org.bonitasoft.engine.core.process.definition.model.SSimpleInputDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SComplexInputDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SConstraintDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SContractDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SSimpleInputDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.core.document.api.DocumentService;
import org.bonitasoft.engine.core.document.model.SMappedDocument;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowErrorEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowMessageEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowSignalEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.execution.state.CompletedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ModelConvertorTest {

    @Mock
    private FlowNodeStateManager manager;

    @Test
    public void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    public void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState("un_known_state");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState(null);
    }

    @Test
    public void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }

    private DocumentService createdMockedDocumentService() {
        final DocumentService documentService = mock(DocumentService.class);
        doReturn("url?fileName=document&contentStorageId=123").when(documentService).generateDocumentURL("document", "123");
        return documentService;
    }

    private SMappedDocument createMockedDocument() {
        final SMappedDocument documentMapping = mock(SMappedDocument.class);
        doReturn("document").when(documentMapping).getFileName();
        doReturn(123l).when(documentMapping).getDocumentId();
        doReturn("whateverUrl").when(documentMapping).getUrl();
        return documentMapping;
    }

    @Test
    public void toArchivedUserTaskInstance_sould_return_the_right_idenfiers() {
        final SAUserTaskInstanceImpl sInstance = new SAUserTaskInstanceImpl();
        sInstance.setRootContainerId(1L);
        sInstance.setParentContainerId(2L);
        sInstance.setLogicalGroup(0, 456789456798L);
        sInstance.setLogicalGroup(1, 1L);
        sInstance.setLogicalGroup(2, 456L);
        sInstance.setLogicalGroup(3, 2L);
        sInstance.setStateId(5);
        sInstance.setPriority(STaskPriority.NORMAL);

        when(manager.getState(5)).thenReturn(new CompletedActivityStateImpl());

        final ArchivedUserTaskInstance archivedUserTaskInstance = ModelConvertor.toArchivedUserTaskInstance(sInstance, manager);
        assertThat(archivedUserTaskInstance.getProcessDefinitionId()).isEqualTo(456789456798L);
        assertThat(archivedUserTaskInstance.getRootContainerId()).isEqualTo(1L);
        assertThat(archivedUserTaskInstance.getParentContainerId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getProcessInstanceId()).isEqualTo(2L);
        assertThat(archivedUserTaskInstance.getParentActivityInstanceId()).isEqualTo(456L);
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_with_generated_url_when_it_has_content() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(true).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("url?fileName=document&contentStorageId=123", document.getUrl());
    }

    @Test
    public void getDocument_from_process_instance_and_name_should_return_a_document_url_when_is_external_url() {

        final SMappedDocument documentMapping = createMockedDocument();
        final DocumentService documentService = createdMockedDocumentService();
        doReturn(false).when(documentMapping).hasContent();

        final Document document = ModelConvertor.toDocument(documentMapping, documentService);

        assertEquals("whateverUrl", document.getUrl());
    }

    @Test
    public void should_convert_server_definition_into_client_definition() {
        final CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() {
        final CustomUserInfoValue value = ModelConvertor.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    public void should_return_null_when_trying_to_convert_a_null_value() {
        final CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }

    @Test
    public void toEventTriggerInstance_cant_convert_ERROR_Type() {
        // Given
        final SThrowErrorEventTriggerInstance sEventTriggerInstance = new SThrowErrorEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_SIGNAL_Type() {
        // Given
        final SThrowMessageEventTriggerInstance sEventTriggerInstance = new SThrowMessageEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_MESSAGE_Type() {
        // Given
        final SThrowSignalEventTriggerInstance sEventTriggerInstance = new SThrowSignalEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_TERMINATE_Type() {
        // Given
        final SEventTriggerInstance sEventTriggerInstance = new SEventTriggerInstanceImpl() {

            private static final long serialVersionUID = 514899463254242741L;

            @Override
            public String getDiscriminator() {
                return null;
            }

            @Override
            public SEventTriggerType getEventTriggerType() {
                return SEventTriggerType.TERMINATE;
            }
        };

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_can_convert_TIMER_Type() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = (TimerEventTriggerInstance) ModelConvertor.toEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    public void toTimerEventTriggerInstance_can_convert() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");
        sTimerEventTriggerInstance.setId(9);

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = ModelConvertor.toTimerEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    public void convertSContractDefinition() {
        //given
        final SimpleInputDefinition expectedSimpleInput = new SimpleInputDefinitionImpl("name", Type.TEXT, "description");
        final ComplexInputDefinition expectedComplexInput = new ComplexInputDefinitionImpl("complex input", "complex description",
                Arrays.asList(expectedSimpleInput), null);
        final ConstraintDefinition expectedRule = new ConstraintDefinitionImpl("name", "expression", "explanation");
        expectedRule.getInputNames().add("input1");
        expectedRule.getInputNames().add("input2");

        //when
        final SContractDefinition contractDefinition = new SContractDefinitionImpl();
        final SConstraintDefinition sRule = new SConstraintDefinitionImpl(expectedRule);
        final SSimpleInputDefinition sSimpleInput = new SSimpleInputDefinitionImpl(expectedSimpleInput);
        final SComplexInputDefinition sComplexInput = new SComplexInputDefinitionImpl(expectedComplexInput);

        contractDefinition.getConstraints().add(sRule);
        contractDefinition.getSimpleInputs().add(sSimpleInput);
        contractDefinition.getComplexInputs().add(sComplexInput);

        final ContractDefinition contract = ModelConvertor.toContract(contractDefinition);

        //then
        assertThat(contract.getConstraints()).as("should convert rules").containsExactly(expectedRule);
        assertThat(contract.getSimpleInputs()).as("should convert simple inputs").containsExactly(expectedSimpleInput);
        assertThat(contract.getComplexInputs()).as("should convert complex inputs").containsExactly(expectedComplexInput);
    }

    @Test
    public void convertMultipleSContractDefinition() {
        //given
        final SimpleInputDefinition expectedSimpleInput = new SimpleInputDefinitionImpl("name", Type.TEXT, "description", true);
        final ComplexInputDefinition expectedComplexInput = new ComplexInputDefinitionImpl("complex input", "complex description", true,
                Arrays.asList(expectedSimpleInput), null);
        final ComplexInputDefinition expectedComplexWithComplexInput = new ComplexInputDefinitionImpl("complex in complext", "complex description", true,
                null, Arrays.asList(expectedComplexInput));

        final ConstraintDefinition expectedRule = new ConstraintDefinitionImpl("name", "expression", "explanation");
        expectedRule.getInputNames().add("input1");
        expectedRule.getInputNames().add("input2");

        //when
        final SContractDefinition contractDefinition = new SContractDefinitionImpl();
        final SConstraintDefinition sRule = new SConstraintDefinitionImpl(expectedRule);
        final SSimpleInputDefinition sSimpleInput = new SSimpleInputDefinitionImpl(expectedSimpleInput);
        final SComplexInputDefinition sComplexInput = new SComplexInputDefinitionImpl(expectedComplexWithComplexInput);

        contractDefinition.getConstraints().add(sRule);
        contractDefinition.getSimpleInputs().add(sSimpleInput);
        contractDefinition.getComplexInputs().add(sComplexInput);

        final ContractDefinition contract = ModelConvertor.toContract(contractDefinition);

        //then
        assertThat(contract.getConstraints()).as("should convert rules").containsExactly(expectedRule);
        assertThat(contract.getSimpleInputs()).as("should convert simple inputs").containsExactly(expectedSimpleInput);
        assertThat(contract.getComplexInputs()).as("should convert complex inputs").containsExactly(expectedComplexWithComplexInput);
    }

}
