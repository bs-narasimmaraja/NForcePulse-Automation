package com.qa.nforcepulse.testbase;

import java.io.IOException;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import com.qa.nforcepulse.utilities.ConfigurationUtilities;
import com.qa.nforcepulse.utilities.ReusableMethods;
import com.qa.nforcepulse.utilities.SuiteReportManager;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TestBase {
	public static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
	protected static final Logger LOG = (Logger) LogManager.getLogger(TestBase.class);

	public static void openBrowser() {
		String browser = ConfigurationUtilities.getProperty("browser");

		if (browser.equalsIgnoreCase("chrome")) {
			WebDriverManager.chromedriver().setup();
			ChromeOptions options = new ChromeOptions();
			options.addArguments("start-maximized");
			options.addArguments("--disable-notifications");
			options.addArguments("--remote-allow-origins=*");
			setDriver(new ChromeDriver(options));

		} else if (browser.equalsIgnoreCase("edge")) {
			WebDriverManager.edgedriver().setup();
			setDriver(new EdgeDriver());

		} else if (browser.equalsIgnoreCase("firefox")) {
			WebDriverManager.firefoxdriver().setup();
			setDriver(new FirefoxDriver());
		}

		getDriver().manage().window().maximize();
		getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
	}

	public static void openURL() {
		String env = ConfigurationUtilities.getProperty("Environment");
		getDriver().get(ConfigurationUtilities.getProperty(env + "URL"));
	}

	public static WebDriver getDriver() {
		return driver.get();
	}

	public static void setDriver(WebDriver drv) {
		driver.set(drv);
	}

	public static void quitDriver() {
		try {
			if (driver.get() != null) {
				driver.get().quit();
			}
		} catch (Exception ignored) {
		} finally {
			driver.remove();
		}
	}

	public static void LogInFo(String logDescription) {
		LOG.info(logDescription);
	}

	public static void TestPass(String textDescription) {
		SuiteReportManager.recordStep(textDescription, "PASSED", null);
	}

	public static void TestFail(String textDescription) {
		SuiteReportManager.recordStep(textDescription, "FAILED", null);
	}

	public static void TestInfo(String textDescription) {
		SuiteReportManager.recordStep(textDescription, "SKIPPED", null);
	}

	public static void TakeScreenshot(String testDescription, String imageName) throws IOException, InterruptedException {
		Thread.sleep(200);
		String base64 = ReusableMethods.CaptureScreenShotBase64();

		SuiteReportManager.recordStep(testDescription, "PASSED", base64);
	}

	public static void TakeScreenshotforFailedstep(String testDescription, String imageName) throws IOException, InterruptedException {
		Thread.sleep(200);
		String base64 = ReusableMethods.CaptureScreenShotBase64();

		SuiteReportManager.recordStep(testDescription, "FAILED", base64);
	}
}
