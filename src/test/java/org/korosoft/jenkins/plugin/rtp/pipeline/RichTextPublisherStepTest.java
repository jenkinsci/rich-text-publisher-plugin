package org.korosoft.jenkins.plugin.rtp.pipeline;

import java.util.List;
import java.util.stream.Collectors;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.korosoft.jenkins.plugin.rtp.BuildRichTextAction;

public class RichTextPublisherStepTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void keepsEveryRichTextPublishedInTheSameBuild() throws Exception {
        WorkflowRun run = runPipeline("node {\n  rtp nullAction: '1', stableText: 'Artifact 1'\n  rtp nullAction: '1', stableText: 'Artifact 2'\n}");

        Assert.assertEquals(List.of("Artifact 1", "Artifact 2"), richTexts(run));
    }

    @Test
    public void replaceKeepsOnlyTheLatestRichText() throws Exception {
        WorkflowRun run = runPipeline("node {\n  rtp nullAction: '1', stableText: 'Artifact 1'\n  rtp nullAction: '1', stableText: 'Artifact 2', replace: true\n}");

        Assert.assertEquals(List.of("Artifact 2"), richTexts(run));
    }

    @Test
    public void nullActionFromAVariableIsApplied() throws Exception {
        WorkflowRun run = runPipeline("node {\n  def action = 1\n  rtp nullAction: \"${action}\", stableText: 'Artifact 1'\n}");

        Assert.assertEquals(List.of("Artifact 1"), richTexts(run));
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
