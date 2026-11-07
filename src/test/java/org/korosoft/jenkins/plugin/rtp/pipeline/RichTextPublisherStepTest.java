package org.korosoft.jenkins.plugin.rtp.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;

@WithJenkins
class RichTextPublisherStepTest {
    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void keepsEveryRichTextPublishedInTheSameBuild() throws Exception {
        WorkflowRun run = runPipeline("node {\n  rtp nullAction: '1', stableText: 'Artifact 1'\n  rtp nullAction: '1', stableText: 'Artifact 2'\n}");

        assertEquals(List.of("Artifact 1", "Artifact 2"), richTexts(run));
    }

    @Test
    void replaceKeepsOnlyTheLatestRichText() throws Exception {
        WorkflowRun run = runPipeline("node {\n  rtp nullAction: '1', stableText: 'Artifact 1'\n  rtp nullAction: '1', stableText: 'Artifact 2', replace: true\n}");

        assertEquals(List.of("Artifact 2"), richTexts(run));
    }

    @Test
    void nullActionFromAVariableIsApplied() throws Exception {
        WorkflowRun run = runPipeline("node {\n  for (action in [0, 1, 2, 3, 4]) {\n"
                + "    rtp nullAction: \"${action}\", stableText: 'stable', unstableText: 'unstable', abortedText: 'aborted', failedText: 'failed'\n"
                + "  }\n}");

        assertEquals(List.of("stable", "unstable", "aborted", "failed"), richTexts(run));
    }

    private WorkflowRun runPipeline(String script) throws Exception {
        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "job");
        job.setDefinition(new CpsFlowDefinition(script, true));
        return jenkins.buildAndAssertSuccess(job);
    }

    private static List<String> richTexts(WorkflowRun run) {
        return run.getActions(BuildRichTextAction.class).stream()
                .map(BuildRichTextAction::getRichText)
                .collect(Collectors.toList());
    }
}
