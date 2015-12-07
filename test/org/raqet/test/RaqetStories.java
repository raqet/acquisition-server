/*
 * Copyright (c) 2015, Netherlands Forensic Institute
 * All rights reserved.
 */
package org.raqet.test;

import static java.util.Arrays.asList;

import java.util.List;

import org.jbehave.core.annotations.UsingEmbedder;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.StoryControls;
import org.jbehave.core.failures.FailingUponPendingStep;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.junit.runner.RunWith;
import org.raqet.test.steps.BeforeAfterSteps;
import org.raqet.test.steps.RaqetSteps;
import org.raqet.test.steps.UserRestApiSteps;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;

@RunWith(JUnitReportingRunner.class)
@UsingEmbedder(embedder = Embedder.class, generateViewAfterStories = true, ignoreFailureInStories = false, ignoreFailureInView = true, verboseFailures = true, threads = 1)
public final class RaqetStories extends JUnitStories {
    @Override
    public Configuration configuration() {
        return new MostUsefulConfiguration()
            .usePendingStepStrategy(new FailingUponPendingStep())
            .useStoryControls(new StoryControls().doSkipBeforeAndAfterScenarioStepsIfGivenStory(false))
            .useStoryLoader(new LoadFromClasspath(getClass()))
            .useStoryReporterBuilder(new StoryReporterBuilder().withDefaultFormats().withFormats(Format.CONSOLE, Format.TXT));
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        final RaqetConfig config = new RaqetConfig();
        return new InstanceStepsFactory(configuration(), new BeforeAfterSteps(config), new RaqetSteps(config), new UserRestApiSteps(config));
    }

    @Override
    public List<String> storyPaths() {
        return new StoryFinder().findPaths(CodeLocations.codeLocationFromClass(this.getClass()), asList("**/*.story"), null);
    }
}