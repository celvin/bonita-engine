package org.bonitasoft.engine.event;

import static org.junit.Assert.assertFalse;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.SignalEventTriggerDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.Test;

public class SignalBoundaryEventTest extends AbstractEventTest {

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a user task having a boundary catch signal event")
    public void signalBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEvent("MySignal");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal with wrong name sent on a user task having a boundary catch signal event")
    public void signalBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MySignal1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, donaBenta);
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        // Thread.sleep(500);
        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a call activity having a boundary catch signal event")
    public void signalBoundaryEventOnCallActivityTriggered() throws Exception {
        final String signalName = "MySignal";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(calledProcessInstance, TestStates.ABORTED);
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(calledStep.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Test
    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "boundary",
            "event" }, jira = "ENGINE-502", story = "signal sent on a call activity having a boundary catch signal event")
    public void signalBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final String signalName = "MySignal";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTask("calledStep", processInstance);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());
        assignAndExecuteStep(calledStep, donaBenta.getId());

        final ActivityInstance step2 = waitForUserTask("step2", processInstance);
        waitForProcessToFinish(calledProcessInstance);

        getProcessAPI().sendSignal("MySignal");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2, donaBenta.getId());
        waitForProcessToFinish(processInstance);

        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event not triggered on sequential multi-instance", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, BoundaryEventDefinition.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Multi-instance", "Sequential" }, story = "Execute signal boundary event not triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Loop activity" }, story = "Execute signal boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        Thread.sleep(50);
        getProcessAPI().sendSignal("MySignal");

        final ActivityInstance executionStep = waitForUserTask(EXCEPTION_STEP, processInstance);
        assignAndExecuteStep(executionStep, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SignalEventTriggerDefinition.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal",
            "Boundary", "Loop activity" }, story = "Execute signal boundary event not triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void signalBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt("step1", processInstance.getId(), donaBenta.getId());
        }
        final ActivityInstance waitForUserTask = waitForUserTask("step2", processInstance);

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(waitForUserTask, donaBenta.getId());

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

}
