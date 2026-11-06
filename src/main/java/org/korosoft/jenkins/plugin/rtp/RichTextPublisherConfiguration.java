package org.korosoft.jenkins.plugin.rtp;

import hudson.Extension;
import jenkins.model.GlobalConfiguration;
import jenkins.model.GlobalConfigurationCategory;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

@Extension
@Symbol("richTextPublisher")
public class RichTextPublisherConfiguration extends GlobalConfiguration {
    private boolean allowUnsafeHtml;

    public RichTextPublisherConfiguration() {
        load();
    }

    @Override
    public String getDisplayName() {
        return "Rich Text Publisher";
    }

    @Override
    public GlobalConfigurationCategory getCategory() {
        return GlobalConfigurationCategory.get(GlobalConfigurationCategory.Security.class);
    }

    public boolean isAllowUnsafeHtml() {
        return allowUnsafeHtml;
    }

    @DataBoundSetter
    public void setAllowUnsafeHtml(boolean allowUnsafeHtml) {
        this.allowUnsafeHtml = allowUnsafeHtml;
        save();
    }

    public static boolean isUnsafeHtmlAllowed() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins == null) {
            return false;
        }
        RichTextPublisherConfiguration configuration =
                jenkins.getExtensionList(RichTextPublisherConfiguration.class).get(RichTextPublisherConfiguration.class);
        return configuration != null && configuration.isAllowUnsafeHtml();
    }
}
