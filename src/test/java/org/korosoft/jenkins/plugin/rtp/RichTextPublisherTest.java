package org.korosoft.jenkins.plugin.rtp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RichTextPublisherTest {
    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void replaceSurvivesConfigRoundtrip() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        RichTextPublisher publisher = new RichTextPublisher("Artifact 1", "", "", "", true, true, true, "HTML", "0");
        publisher.setReplace(true);
        project.getPublishersList().add(publisher);

        jenkins.configRoundtrip(project);

        assertTrue(project.getPublishersList().get(RichTextPublisher.class).isReplace());
    }
}
