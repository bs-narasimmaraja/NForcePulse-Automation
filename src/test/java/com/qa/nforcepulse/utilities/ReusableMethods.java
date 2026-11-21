package com.qa.nforcepulse.utilities;

import java.time.Duration;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;

import com.qa.nforcepulse.testbase.TestBase;

public class ReusableMethods {

	public WebDriver getDriver() {
		return TestBase.getDriver();
	}

	public void enterText(By locator, String text) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
		element.clear();
		element.sendKeys(text);
	}

	/**
	 * Some fields get their value silently reverted by the app a moment after typing
	 * (e.g. a shared Notes box that snaps back to a previously-saved value after a
	 * background data refresh). Re-types up to maxAttempts times, checking after each
	 * attempt that the value actually stuck before moving on.
	 */
	public void enterTextReliably(By locator, String text, int maxAttempts) {
		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try {
				WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
				WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
				// Native .clear() started throwing InvalidElementStateException on this field
				// after a Chrome auto-update (v153 -> v154) - a real run showed it fail
				// identically on every one of maxAttempts retries, not just occasionally, so
				// it isn't a timing issue clear() can be retried past. Select-all + Delete via
				// real keyboard events clears the field without the native clear() command,
				// while still firing the keyup/onChange events this React-controlled field
				// needs to pick up the change.
				element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
				element.sendKeys(Keys.DELETE);
				element.sendKeys(text);
			} catch (org.openqa.selenium.InvalidElementStateException e) {
				if (attempt == maxAttempts) {
					throw e;
				}
				continue;
			}
			String current = getDriver().findElement(locator).getAttribute("value");
			if (text.equals(current)) {
				return;
			}
		}
	}

	public void clickOnButton(By locator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(50));
		WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));

		try {
			element.click();
		} catch (ElementClickInterceptedException intercepted) {
			// A transient overlay (toast notification, sticky banner, etc.) was on top of the
			// element at that instant. A JS click bypasses the visual obstruction instead of
			// requiring a fragile wait tuned to one specific overlay's dismiss timing.
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
		}
	}

	/**
	 * Clicks the last match of locator that is actually visible and enabled. A positional
	 * "(...)[last()]" XPath picks the last match in the DOM whether or not it is visible, and a
	 * real run showed it settle on an unclickable copy of the Reject icon while the real one was
	 * on screen. Pass the locator WITHOUT a [last()] - the last-visible rule replaces it.
	 */
	public void clickOnLastDisplayed(By locator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(50));
		WebElement element = wait.until(d -> {
			java.util.List<WebElement> matches = d.findElements(locator);
			for (int i = matches.size() - 1; i >= 0; i--) {
				try {
					WebElement candidate = matches.get(i);
					if (candidate.isDisplayed() && candidate.isEnabled()) {
						return candidate;
					}
				} catch (org.openqa.selenium.StaleElementReferenceException ignored) {
				}
			}
			return null;
		});

		try {
			element.click();
		} catch (ElementClickInterceptedException intercepted) {
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
		}
	}

	public String getText(By locator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		return element.getText().trim();
	}

	public boolean isDisplayed(By locator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
	}

	public boolean isElementPresent(By locator) {
    return getDriver().findElements(locator).size() > 0;
}
    public void selectByVisibleText(By locator, String value) {
    // Unlike every other interaction method here, this previously called findElement()
    // directly with no wait - a real run showed it throw NoSuchElementException when the
    // dropdown hadn't rendered yet immediately after the preceding action (e.g. right after
    // selecting Client, before the Project dropdown re-renders), and separately "Cannot
    // locate option with text: ..." when the <select> itself was already clickable but its
    // own <option> list hadn't finished populating yet. Both conditions are checked in one
    // wait (instead of two sequential ones) so the worst case stays capped at this timeout
    // rather than doubling; StaleElementReferenceException is treated as "not ready yet"
    // rather than aborting the wait, since this app's async rendering can replace the
    // element between the two calls below.
    WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
    try {
        wait.until(d -> {
            try {
                WebElement el = d.findElement(locator);
                return el.isEnabled()
                        && !el.findElements(By.xpath(".//option[normalize-space()='" + value + "']")).isEmpty();
            } catch (org.openqa.selenium.StaleElementReferenceException e) {
                return false;
            }
        });
    } catch (org.openqa.selenium.TimeoutException e) {
        // Dropdown contents come from app data that can change under the tests (clients,
        // projects and managers have been removed/renamed before), so report what the
        // dropdown actually offered instead of a bare lambda timeout.
        StringBuilder available = new StringBuilder();
        try {
            for (WebElement option : new Select(getDriver().findElement(locator)).getOptions()) {
                available.append("'").append(option.getText().trim()).append("' ");
            }
        } catch (Exception ignored) {
            available.append("(dropdown itself not found)");
        }
        throw new org.openqa.selenium.NoSuchElementException(
                "Option '" + value + "' not found in dropdown " + locator + ". Available options: " + available);
    }

    Select select = new Select(getDriver().findElement(locator));
    select.selectByVisibleText(value);
}
	/**
	 * Waits until a submission has visibly gone through: either the success message is on screen,
	 * or the given project row's text shows a Pending/Submitted status. Polls quickly with the
	 * implicit wait switched off (otherwise each poll with no match yet blocks for the whole
	 * implicit timeout, and a message that only shows for a second would be missed).
	 */
	public void waitForSubmissionConfirmation(By messageLocator, By rowLocator, int timeoutSeconds) {
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
		try {
			new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds))
					.pollingEvery(Duration.ofMillis(250))
					.until(d -> {
						try {
							for (WebElement message : d.findElements(messageLocator)) {
								if (message.isDisplayed()) {
									return true;
								}
							}
							for (WebElement row : d.findElements(rowLocator)) {
								String text = row.getText().toLowerCase();
								if (text.contains("pending") || text.contains("submitted")) {
									return true;
								}
							}
						} catch (org.openqa.selenium.StaleElementReferenceException ignored) {
						}
						return false;
					});
		} catch (org.openqa.selenium.TimeoutException e) {
			throw new org.openqa.selenium.TimeoutException("After Submit, neither a 'submitted successfully' message "
					+ "nor a Pending/Submitted status on the project row appeared within " + timeoutSeconds + "s", e);
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		}
	}

	/**
	 * Picks an option in a custom dropdown: a <button role="combobox"> that opens a listbox of
	 * role="option" items (the Client / Project / Manager boxes on Time Entries). Opens it if it
	 * isn't already open, waits for the option to show up (the Project list only fills after a
	 * Client is chosen), clicks it, then waits briefly for the button to show the choice.
	 * Implicit wait is switched off while polling: with it on, every poll that finds no match yet
	 * blocks for the whole implicit timeout, so the 30s wait below would only ever poll once.
	 */
	public void selectFromCombobox(By triggerLocator, String value) {
		By optionLocator = By.xpath("//*[@role='option' and normalize-space(.)='" + value + "']"
				+ " | //*[@role='listbox']//*[normalize-space(.)='" + value + "']");
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
			WebElement option;
			try {
				option = wait.until(d -> {
					try {
						WebElement trigger = d.findElement(triggerLocator);
						if (!"true".equals(trigger.getAttribute("aria-expanded")) && trigger.isEnabled()) {
							try {
								trigger.click();
							} catch (ElementClickInterceptedException intercepted) {
								((JavascriptExecutor) d).executeScript("arguments[0].click();", trigger);
							}
						}
						for (WebElement candidate : d.findElements(optionLocator)) {
							if (candidate.isDisplayed()) {
								return candidate;
							}
						}
					} catch (org.openqa.selenium.StaleElementReferenceException ignored) {
					}
					return null;
				});
			} catch (org.openqa.selenium.TimeoutException e) {
				StringBuilder visible = new StringBuilder();
				String expanded = "unknown";
				try {
					expanded = getDriver().findElement(triggerLocator).getAttribute("aria-expanded");
					for (WebElement o : getDriver().findElements(By.xpath("//*[@role='option']"))) {
						if (o.isDisplayed()) {
							visible.append("'").append(o.getText().trim()).append("' ");
						}
					}
				} catch (Exception ignored) {
					visible.append("(could not read the list)");
				}
				throw new org.openqa.selenium.NoSuchElementException("Option '" + value
						+ "' not found after opening dropdown " + triggerLocator + " (aria-expanded=" + expanded
						+ "). Visible options: " + visible);
			}

			try {
				option.click();
			} catch (ElementClickInterceptedException intercepted) {
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", option);
			}

			try {
				new WebDriverWait(getDriver(), Duration.ofSeconds(5))
						.until(d -> d.findElement(triggerLocator).getText().contains(value));
			} catch (org.openqa.selenium.TimeoutException ignored) {
			}
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		}
	}

	/**
	 * The shared Notes textarea re-binds to whichever day's Hours/Minutes cell was
	 * just edited, which can take a brief moment. This waits for the field to reset
	 * to empty (the app's signal that it has switched to the new day) before we type
	 * into it, instead of typing immediately and risking the previous day's context.
	 * Best-effort: if it never clears, we proceed anyway rather than fail the test.
	 */
	public void waitForNotesPanelToSwitch(By notesLocator) {
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(3));
			wait.until(d -> {
				WebElement el = d.findElement(notesLocator);
				String value = el.getAttribute("value");
				return value == null || value.isEmpty();
			});
		} catch (org.openqa.selenium.TimeoutException ignored) {
		}
	}

	/**
	 * Waits for a cell's text to change away from a previously-captured value (e.g. the week
	 * range label after clicking "next week"). Checking isElementPresent() immediately after
	 * that click, with no settle time, risks reading the previous week's still-visible content
	 * for an instant before the app swaps it out - an intermittent report of "skipping an
	 * actually-empty week" fits that race exactly. Best-effort: if it never changes, proceeds
	 * anyway rather than fail the test outright.
	 */
	public void waitForTextToChangeFrom(By locator, String previousText, int timeoutSeconds) {
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
			wait.until(d -> {
				try {
					return !d.findElement(locator).getText().trim().equals(previousText);
				} catch (Exception e) {
					return false;
				}
			});
		} catch (org.openqa.selenium.TimeoutException ignored) {
		}
	}

	/**
	 * Waits for an element (e.g. a confirmation popup's heading) to disappear from the DOM.
	 * Confirming one project's approval closes its popup and settles the drawer back to an
	 * updated state - a few real runs showed this taking a moment (network round-trip +
	 * re-render), and immediately acting on a second project's row without waiting for that
	 * risked clicking on the still-transitioning first popup/row rather than the second one.
	 * Best-effort: proceeds anyway if it never disappears, rather than fail the test outright.
	 */
	public void waitForElementToDisappear(By locator, int timeoutSeconds) {
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
			wait.until(d -> {
				try {
					return d.findElements(locator).isEmpty();
				} catch (Exception e) {
					return true;
				}
			});
		} catch (org.openqa.selenium.TimeoutException ignored) {
		}
	}

	/**
	 * The Save Notes button carries disabled:opacity-50 styling, implying it goes disabled
	 * while its save request is in flight. Clicking it and immediately moving to the next
	 * day's fields (with no wait) risks racing that in-flight save - this waits for the
	 * button to be enabled again (i.e. the save round-trip to finish) before proceeding.
	 *
	 * Manual testing (naturally paced) saves each day's note correctly; rapid automated
	 * entry does not, even though it follows the exact same click/type sequence - pointing
	 * at a race with some background refetch/sync that isn't captured by the button's own
	 * enabled state. The trailing pause below is a pragmatic buffer for that, not a confirmed
	 * fix - if notes still don't persist per-day after this, the race is elsewhere.
	 */
	public void waitForSaveToComplete(By saveButtonLocator) {
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));
			wait.until(d -> {
				try {
					return d.findElement(saveButtonLocator).isEnabled();
				} catch (Exception e) {
					return false;
				}
			});
		} catch (org.openqa.selenium.TimeoutException ignored) {
		}
		try {
			Thread.sleep(1500);
		} catch (InterruptedException ignored) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Waits for a cell's text to contain an expected value (e.g. the row's running WEEK TOTAL
	 * matching the sum of days entered so far), instead of inferring "the save finished" from
	 * the Save Notes button's own enabled/disabled state. A live run showed the button-state
	 * check passing after day 1 (Monday) while the click for day 2's Save Notes then hung for
	 * 50s - the button looking "ready" is not proof the prior save actually landed. The WEEK
	 * TOTAL only updates once the app has genuinely registered the day's entry, so waiting for
	 * it directly is a stronger signal than the button's own state.
	 */
	public void waitForCellToContain(By locator, String expectedSubstring, int timeoutSeconds) {
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(timeoutSeconds));
			wait.until(d -> {
				try {
					return d.findElement(locator).getText().contains(expectedSubstring);
				} catch (Exception e) {
					return false;
				}
			});
		} catch (org.openqa.selenium.TimeoutException ignored) {
		}
	}

	public void acceptAlert(){
		WebDriverWait wait=new WebDriverWait(getDriver(),Duration.ofSeconds(10));
		Alert alert =wait.until(ExpectedConditions.alertIsPresent());
		alert.accept();
	}

	public void acceptConfirmationPopup() {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(20));
		Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		alert.accept();
	}
	public void clickElement(By locator) {
		WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(30));
		WebElement element = wait.until(
				ExpectedConditions.elementToBeClickable(locator)
		);
		try {
			element.click();
		} catch (ElementClickInterceptedException intercepted) {
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
		}
	}

	public static String CaptureScreenShotBase64() {
		return ((TakesScreenshot) TestBase.getDriver()).getScreenshotAs(OutputType.BASE64);
	}

}
