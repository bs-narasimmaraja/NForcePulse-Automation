package com.qa.nforcepulse.pageobjectfiles;

import org.openqa.selenium.WebDriver;

import com.qa.nforcepulse.utilities.ReusableMethods;
import com.qa.nforcepulse.utilities.TestContextSetup;

public class POManager {

	public WebDriver driver;
	public ReusableMethods reusableMethods;
	public TestContextSetup testcontext;
	public LoginPage loginPage;
	public TimeEntries timeentries;
	public Approvals approvals;

	public POManager(WebDriver driver, ReusableMethods reusableMethods, TestContextSetup testcontext) {
		this.driver = driver;
		this.reusableMethods = reusableMethods;
		this.testcontext = testcontext;
	}

	public LoginPage getLoginPage() {
		if (loginPage == null) {
			loginPage = new LoginPage(driver, reusableMethods, testcontext);
		}
		return loginPage;
	}

	public TimeEntries getTimeEntries(){
		if(timeentries==null){
			timeentries=new TimeEntries(driver,reusableMethods,testcontext);
		}
		return timeentries;
	}

	public Approvals getApprovals(){
		if(approvals==null){
			approvals=new Approvals(driver,reusableMethods,testcontext);
		}
		return approvals;
	}
}
