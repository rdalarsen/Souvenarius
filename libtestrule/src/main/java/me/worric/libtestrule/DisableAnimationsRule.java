package me.worric.libtestrule;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

public class DisableAnimationsRule implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                disableAnimations();
                try {
                    base.evaluate();
                } finally {
                    enableAnimations();
                }
            }
        };
    }

    private void disableAnimations() throws IOException {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global transition_animation_scale 0");
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global window_animation_scale 0");
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global animator_duration_scale 0");
    }

    private void enableAnimations() throws IOException {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global transition_animation_scale 1");
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global window_animation_scale 1");
        UiDevice.getInstance(instrumentation)
                .executeShellCommand("settings put global animator_duration_scale 1");
    }



}
