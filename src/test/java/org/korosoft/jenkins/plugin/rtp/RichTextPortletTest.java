package org.korosoft.jenkins.plugin.rtp;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class RichTextPortletTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void escapesUnknownJobName() {
        String result = new RichTextPortlet("portlet", "<img src=x onerror=alert(1)>", false).getRichText();

        Assert.assertFalse(result.contains("<img"));
        Assert.assertTrue(result.contains("&lt;img"));
    }

    @Test
    public void sanitizesRichTextOfTheJob() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("job");
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);
        build.addAction(new BuildRichTextAction(build, "<b>ok</b><script>alert(1)</script>"));

        Assert.assertEquals("<b>ok</b>", new RichTextPortlet("portlet", "job", true).getRichText());
    }
}
