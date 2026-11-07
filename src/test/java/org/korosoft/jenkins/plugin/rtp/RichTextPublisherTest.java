package org.korosoft.jenkins.plugin.rtp;

import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class RichTextPublisherTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void replaceSurvivesConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        RichTextPublisher publisher = new RichTextPublisher("Artifact 1", "", "", "", true, true, true, "HTML", "0");
        publisher.setReplace(true);
        project.getPublishersList().add(publisher);

        jenkins.configRoundtrip(project);

        Assert.assertTrue(project.getPublishersList().get(RichTextPublisher.class).isReplace());
    }
}
