package com.qa.nforcepulse.testrunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(features = "src/test/resources/FeatureFiles/",
		glue = { "com.qa.nforcepulse.stepdefinition", "com.qa.nforcepulse.hooks" },
		tags = "@Sanity01",
		monochrome = true,
		plugin = { "pretty",
				"html:target/cucumber-reports.html",
				"json:target/cucumber.json",
				"summary",
				"com.qa.nforcepulse.utilities.StepListener"
		}
)


public class TestRunner extends AbstractTestNGCucumberTests {

	@Override
	@DataProvider(parallel = false)
	public Object[][] scenarios() {
		return super.scenarios();
	}
}
