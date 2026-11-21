package com.qa.nforcepulse.hooks;

import java.util.Collection;

import com.qa.nforcepulse.testbase.TestBase;
import com.qa.nforcepulse.utilities.SuiteReportManager;
import com.qa.nforcepulse.utilities.TestContextSetup;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;

public class Hooks extends TestBase {
	private static ThreadLocal<Long> scenarioStartTime = new ThreadLocal<>();
	private static ThreadLocal<String> scenarioNameThreadLocal = new ThreadLocal<>();
	private static ThreadLocal<String> tagNameThreadLocal = new ThreadLocal<>();
	private TestContextSetup testcontext;

	public Hooks(TestContextSetup testcontext) {
		this.testcontext = testcontext;
	}

	// Runs once before any scenario and once after all scenarios, regardless of
	// how the run is launched (bare class run, TestNG.xml suite, Maven, etc.) -
	// unlike TestNGListeners, which only attaches when TestNG.xml itself is the
	// entry point. This is what actually generates the HTML report reliably.
	@BeforeAll
	public static void beforeAll() {
		SuiteReportManager.suiteStarted();
	}

	@AfterAll
	public static void afterAll() {
		SuiteReportManager.suiteFinished();
	}

	@Before
	public void beforeScenario(Scenario scenario) {
		scenarioNameThreadLocal.set(scenario.getName());
		scenarioStartTime.set(System.currentTimeMillis());

		Collection<String> tags = scenario.getSourceTagNames();
		String tagName = tags.isEmpty() ? "No Tag" : String.join(", ", tags);
		tagNameThreadLocal.set(tagName);

		SuiteReportManager.startScenario(scenario.getName(), tagName);

		System.out.println("=========== SCENARIO STARTED: " + scenario.getName() + " ===========");
	}

	public static String getScenarioName() {
		return scenarioNameThreadLocal.get();
	}

	@After
	public void afterScenario(Scenario scenario) {
		String scenarioName = getScenarioName();
		long durationMs = System.currentTimeMillis() - scenarioStartTime.get();

		System.out.println("=========== SCENARIO ENDED: " + scenarioName
				+ " | Execution Time: " + durationMs + " ms (" + (durationMs / 1000.0) + " sec) ===========");

		SuiteReportManager.endScenario(scenario.getStatus().name(), durationMs);

		TestBase.quitDriver();
		testcontext.softAssert.assertAll();

		scenarioStartTime.remove();
		scenarioNameThreadLocal.remove();
		tagNameThreadLocal.remove();
	}
}
