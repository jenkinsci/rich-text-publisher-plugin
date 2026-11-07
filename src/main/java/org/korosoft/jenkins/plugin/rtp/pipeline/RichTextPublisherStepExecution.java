package org.korosoft.jenkins.plugin.rtp.pipeline;

import javax.inject.Inject;

import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.korosoft.jenkins.plugin.rtp.RichTextPublisher;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;

public class RichTextPublisherStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

	private static final long serialVersionUID = 1L;

	@StepContextParameter
    private transient TaskListener listener;
	
	@StepContextParameter
	private transient FilePath workspace;
	
	@StepContextParameter
    private transient Run<?,?> build;

    @StepContextParameter
    private transient Launcher launcher;
    
    @Inject
    private transient RichTextPublisherStep step;
	
	@Override
	protected Void run() throws Exception {
		
		RichTextPublisher rtp = new RichTextPublisher(step.getStableText(), step.getUnstableText(), step.getFailedText(), step.getAbortedText(), step.getUnstableAsStable(), step.getFailedAsStable(), step.getAbortedAsStable(), step.getParserName(), step.getNullAction());
		rtp.setReplace(step.isReplace());
		rtp.perform(build, workspace, launcher, listener);
		return null;
	}
}