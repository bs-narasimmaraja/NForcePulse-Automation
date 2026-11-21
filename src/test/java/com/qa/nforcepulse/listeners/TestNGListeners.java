package com.qa.nforcepulse.listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.qa.nforcepulse.hooks.Hooks;
import com.qa.nforcepulse.testbase.TestBase;

public class TestNGListeners extends TestBase implements ITestListener {
	private static long suiteStartTime;

	@Override
	public void onStart(ITestContext context) {
		suiteStartTime = System.currentTimeMillis();
		System.out.println("Test execution started: " + context.getName());
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		System.out.println("PASSED: " + Hooks.getScenarioName());
	}

	@Override
	public void onTestFailure(ITestResult result) {
		System.out.println("FAILED: " + Hooks.getScenarioName());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		System.out.println("SKIPPED: " + Hooks.getScenarioName());
	}

	@Override
	public void onFinish(ITestContext context) {
		long totalMs = System.currentTimeMillis() - suiteStartTime;
		System.out.println("Test execution finished: " + context.getName()
				+ " | Total Suite Execution Time: " + totalMs + " ms (" + (totalMs / 1000.0) + " sec)");
	}
}
