package com.xreous.stepperng.variable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests for the X-Stepper-Argument / sequence-argument override surviving
 * pre/post-execution recomputation (bug 2). A step variable flagged {@code overridden}
 * must keep its injected value instead of being re-prompted (PromptVariable) or
 * re-captured from the response (RegexVariable).
 */
class ArgumentOverrideTest {

    @Test
    void regexOverrideSurvivesResponseCapture() {
        RegexVariable overridden = new RegexVariable("token", "TOKEN=(\\w+)");
        overridden.setValue("injected");
        overridden.setOverridden(true);

        overridden.updateFromResponse("HTTP/1.1 200 OK\r\n\r\nTOKEN=fromserver");

        assertEquals("injected", overridden.getValue(),
                "overridden regex variable must keep the injected value, not re-capture");
    }

    @Test
    void regexCaptureStillWorksWhenNotOverridden() {
        RegexVariable normal = new RegexVariable("token", "TOKEN=(\\w+)");
        normal.setValue("injected");
        // overridden defaults to false

        normal.updateFromResponse("HTTP/1.1 200 OK\r\n\r\nTOKEN=fromserver");

        assertEquals("fromserver", normal.getValue(),
                "non-overridden regex variable must still capture from the response");
        assertFalse(normal.isOverridden());
    }

    @Test
    void promptOverrideSkipsPrompt() {
        // Must run without a display: if the override guard failed, updateVariableBeforeExecution()
        // would try to open a Swing dialog and throw/hang instead of returning.
        System.setProperty("java.awt.headless", "true");

        PromptVariable prompt = new PromptVariable("password");
        prompt.setValue("injected");
        prompt.setOverridden(true);

        prompt.updateVariableBeforeExecution();

        assertTrue(prompt.isOverridden());
        assertEquals("injected", prompt.getValue(),
                "overridden prompt variable must keep the injected value, not prompt");
    }
}
