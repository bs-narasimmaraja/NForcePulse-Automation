package com.qa.nforcepulse.utilities;

import com.qa.nforcepulse.testbase.TestBase;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestStepFinished;
import org.openqa.selenium.NoAlertPresentException;

public class StepListener extends TestBase implements ConcurrentEventListener {

	@Override
	public void setEventPublisher(EventPublisher publisher) {

		publisher.registerHandlerFor(TestStepFinished.class, event -> {

			if (event.getTestStep() instanceof PickleStepTestStep) {

				PickleStepTestStep step = (PickleStepTestStep) event.getTestStep();

				String stepText = step.getStep().getText();
				Status stepStatus = event.getResult().getStatus();

				try {

					if (getDriver() == null)
						return;

					// A screenshot is a regular WebDriver command. If a native alert/confirm
					// dialog is open, Chrome auto-dismisses it to run that command (since
					// unhandledPromptBehavior is "dismiss and notify"), which would silently
					// close the dialog before our own step gets to accept it. Skip the
					// screenshot (log text-only) whenever a native dialog is currently open.
					boolean alertOpen;
					try {
						getDriver().switchTo().alert();
						alertOpen = true;
					} catch (NoAlertPresentException noAlert) {
						alertOpen = false;
					}

					if (alertOpen) {
						if (stepStatus == Status.PASSED) {
							TestPass(stepText + " (screenshot skipped - a native browser dialog is open)");
						} else if (stepStatus == Status.FAILED) {
							TestFail(stepText + " (screenshot skipped - a native browser dialog is open)");
						}
						return;
					}

					if (stepStatus == Status.FAILED) {
						TakeScreenshotforFailedstep(stepText, "image");
					} else if (stepStatus == Status.PASSED) {

						TakeScreenshot(stepText, "image");

					} else if (stepStatus == Status.SKIPPED) {
						SuiteReportManager.recordStep(stepText, "SKIPPED", null);
					}

				} catch (Exception e) {
					System.out.println("Screenshot error: " + stepText);
				}
			}
		});
	}
}
