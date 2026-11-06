package org.korosoft.jenkins.plugin.rtp;

import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlForm;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.korosoft.jenkins.plugin.rtp.parser.HTMLMarkupParser;
import org.jvnet.hudson.test.JenkinsRule;

public class RichTextPublisherConfigurationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private static final String HTML = "<b>safe</b><script>alert(1)</script>";

    @Test
    public void htmlIsSanitizedByDefault() {
        Assert.assertFalse(configuration().isAllowUnsafeHtml());

        String parsed = new HTMLMarkupParser().parse(HTML);

        Assert.assertEquals("<b>safe</b>", parsed);
        Assert.assertEquals("<b>safe</b>", new BuildRichTextAction(null, HTML).getRichText());
    }

    @Test
    public void allowUnsafeHtmlRendersHtmlAsIs() {
        configuration().setAllowUnsafeHtml(true);

        Assert.assertEquals(HTML, new HTMLMarkupParser().parse(HTML));
        Assert.assertEquals(HTML, new BuildRichTextAction(null, HTML).getRichText());
    }

    @Test
    public void storedMessagesAreSanitizedOnceUnsafeHtmlIsDisallowedAgain() {
        configuration().setAllowUnsafeHtml(true);
        BuildRichTextAction stored = new BuildRichTextAction(null, new HTMLMarkupParser().parse(HTML));

        configuration().setAllowUnsafeHtml(false);

        Assert.assertEquals("<b>safe</b>", stored.getRichText());
    }

    @Test
    public void settingRequiresAdministerOnTheSecurityPage() throws Exception {
        Assert.assertTrue(configuration().getCategory() instanceof GlobalConfigurationCategory.Security);
        Assert.assertEquals(Jenkins.ADMINISTER, configuration().getRequiredGlobalConfigPagePermission());

        HtmlForm form = jenkins.createWebClient().goTo("manage/configureSecurity").getFormByName("config");
        form.getInputByName("_.allowUnsafeHtml").setChecked(true);
        jenkins.submit(form);

        Assert.assertTrue(configuration().isAllowUnsafeHtml());
    }

    private RichTextPublisherConfiguration configuration() {
        return jenkins.jenkins.getExtensionList(RichTextPublisherConfiguration.class)
                .get(RichTextPublisherConfiguration.class);
    }
}
