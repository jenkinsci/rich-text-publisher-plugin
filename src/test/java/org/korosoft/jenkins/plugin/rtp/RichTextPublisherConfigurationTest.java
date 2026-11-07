package org.korosoft.jenkins.plugin.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.htmlunit.html.HtmlForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.korosoft.jenkins.plugin.rtp.parser.HTMLMarkupParser;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class RichTextPublisherConfigurationTest {
    private static final String HTML = "<b>safe</b><script>alert(1)</script>";

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void htmlIsSanitizedByDefault() {
        assertFalse(configuration().isAllowUnsafeHtml());

        String parsed = new HTMLMarkupParser().parse(HTML);

        assertEquals("<b>safe</b>", parsed);
        assertEquals("<b>safe</b>", new BuildRichTextAction(null, HTML).getRichText());
    }

    @Test
    void allowUnsafeHtmlRendersHtmlAsIs() {
        configuration().setAllowUnsafeHtml(true);

        assertEquals(HTML, new HTMLMarkupParser().parse(HTML));
        assertEquals(HTML, new BuildRichTextAction(null, HTML).getRichText());
    }

    @Test
    void storedMessagesAreSanitizedOnceUnsafeHtmlIsDisallowedAgain() {
        configuration().setAllowUnsafeHtml(true);
        BuildRichTextAction stored = new BuildRichTextAction(null, new HTMLMarkupParser().parse(HTML));

        configuration().setAllowUnsafeHtml(false);

        assertEquals("<b>safe</b>", stored.getRichText());
    }

    @Test
    void settingRequiresAdministerOnTheSecurityPage() throws Exception {
        assertTrue(configuration().getCategory() instanceof GlobalConfigurationCategory.Security);
        assertEquals(Jenkins.ADMINISTER, configuration().getRequiredGlobalConfigPagePermission());

        HtmlForm form = jenkins.createWebClient().goTo("manage/configureSecurity").getFormByName("config");
        form.getInputByName("_.allowUnsafeHtml").setChecked(true);
        jenkins.submit(form);

        assertTrue(configuration().isAllowUnsafeHtml());
    }

    private RichTextPublisherConfiguration configuration() {
        return jenkins.jenkins.getExtensionList(RichTextPublisherConfiguration.class)
                .get(RichTextPublisherConfiguration.class);
    }
}
