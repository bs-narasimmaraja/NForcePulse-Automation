package com.qa.nforcepulse.utilities;

import org.openqa.selenium.WebDriver;
import org.testng.asserts.SoftAssert;

import com.qa.nforcepulse.pageobjectfiles.POManager;
import com.qa.nforcepulse.testbase.TestBase;

public class TestContextSetup {
	private WebDriver driver;
	public TestBase testbase;
	private POManager pageObjectManager;
	private ReusableMethods reusableMethods;

	public TestContextSetup() {
		testbase = new TestBase();
	}

	public SoftAssert softAssert = new SoftAssert();

	public String capturedEmployeeName;
	public String capturedWeek;
	public String capturedTotalHours;

	public WebDriver getDriver() {
		if (driver == null) {
			testbase.openBrowser();
			driver = testbase.getDriver();
		}
		return driver;
	}

	public POManager getPageObjectManager() {
		if (pageObjectManager == null) {
			pageObjectManager = new POManager(getDriver(), getReusableMethods(), this);
		}
		return pageObjectManager;
	}

	public ReusableMethods getReusableMethods() {
		if (reusableMethods == null) {
			reusableMethods = new ReusableMethods();
		}
		return reusableMethods;
	}
}
