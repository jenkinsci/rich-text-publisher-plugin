package org.korosoft.jenkins.plugin.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RichTextPortletTest {
    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void escapesUnknownJobName() {
        String result = new RichTextPortlet("portlet", "<img src=x onerror=alert(1)>", false).getRichText();

        assertFalse(result.contains("<img"));
        assertTrue(result.contains("&lt;img"));
    }

    @Test
    void sanitizesRichTextOfTheJob() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("job");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        build.addAction(new BuildRichTextAction(build, "<b>ok</b><script>alert(1)</script>"));

        assertEquals("<b>ok</b>", new RichTextPortlet("portlet", "job", true).getRichText());
    }
}
