package com.qa.nforcepulse.pageobjectfiles;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qa.nforcepulse.utilities.ConfigurationUtilities;
import com.qa.nforcepulse.utilities.ReusableMethods;
import com.qa.nforcepulse.utilities.TestContextSetup;

public class LoginPage {
	WebDriver driver;
	ReusableMethods reusableMethods;
	TestContextSetup testContext;

	public LoginPage(WebDriver driver, ReusableMethods reusableMethods, TestContextSetup testcontext) {
		this.driver = driver;
		this.reusableMethods = reusableMethods;
		this.testContext = testcontext;
	}

	public final By userName = By.cssSelector("input[type='email']");
	public final By password = By.cssSelector("input[type='password']");
	public final By loginButton = By.cssSelector("button[type='submit']");
	public final By welcomeBanner = By.xpath("//*[contains(text(),'Welcome back')]");
	public final By logoutbtn= By.xpath("//button[.//span[normalize-space()='Logout']]");


	public void launchBrowser() {
		if (testContext.getDriver() == null) {
			testContext.testbase.openBrowser();
		}
	}

	public void openURL() {
		testContext.testbase.openURL();
	}

	public void login() {
		String env = ConfigurationUtilities.getProperty("Environment");
		reusableMethods.enterText(userName, ConfigurationUtilities.getProperty(env + "UserName"));
		reusableMethods.enterText(password, ConfigurationUtilities.getProperty(env + "Password"));
	}

	public void loginAsManager() {
		String env = ConfigurationUtilities.getProperty("Environment");
		reusableMethods.enterText(userName, ConfigurationUtilities.getProperty(env + "ManagerUserName"));
		reusableMethods.enterText(password, ConfigurationUtilities.getProperty(env + "ManagerPassword"));
	}

	public void loginAsSecondManager() {
		String env = ConfigurationUtilities.getProperty("Environment");
		reusableMethods.enterText(userName, ConfigurationUtilities.getProperty(env + "Manager2UserName"));
		reusableMethods.enterText(password, ConfigurationUtilities.getProperty(env + "Manager2Password"));
	}

	public void clickOnLoginButton() {
		reusableMethods.clickOnButton(loginButton);
	}

	public void logout() {
		reusableMethods.clickOnButton(logoutbtn);
		// Clearing cookies/localStorage isn't reliably enough to guarantee a clean session
		// (the app may persist auth some other way - IndexedDB, a service worker, etc.).
		// Closing the whole browser and launching a fresh instance removes every possible
		// leftover session vector at once, regardless of which one the app actually uses.
		testContext.testbase.quitDriver();
		testContext.testbase.openBrowser();
	}

	public String getWelcomeMessage() {
		return reusableMethods.getText(welcomeBanner);
	}

}
