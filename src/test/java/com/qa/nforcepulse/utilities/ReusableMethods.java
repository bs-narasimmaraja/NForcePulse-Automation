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

	// First element matching the locator that is on screen and, when requested, editable
	// (the Notes box is readonly once a day's note is saved and locked). Null if none.
	private WebElement firstDisplayed(WebDriver driver, By locator, boolean mustBeEditable) {
		for (WebElement el : driver.findElements(locator)) {
			try {
				if (!el.isDisplayed()) {
					continue;
				}
				if (mustBeEditable && (el.getAttribute("readonly") != null || !el.isEnabled())) {
					continue;
				}
				return el;
			} catch (org.openqa.selenium.StaleElementReferenceException ignored) {
			}
		}
		return null;
	}

	private void clickWithJsFallback(WebElement element) {
		try {
			element.click();
		} catch (ElementClickInterceptedException intercepted) {
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", element);
		}
	}

	private String pageSignature() {
		try {
			return getDriver().findElement(By.tagName("body")).getText();
		} catch (org.openqa.selenium.WebDriverException e) {
			return "";
		}
	}

	private void scrollIntoView(WebElement element) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
	}

	/**
	 * Clicks a row's action (e.g. View Details) in a paginated list when it may be on any page.
	 * Looks on the current page first; if the row isn't there, scrolls the paging control into view
	 * and clicks Next, waits for the next page of rows to replace the current one, and looks again -
	 * until the row is found or Next is disabled (the last page). Fails with a message naming how
	 * many pages were checked, instead of a bare timeout.
	 *
	 * Rows are only judged "not on this page" once the page has rows on screen (anyRowOnPage), so a
	 * list that is still loading is waited for rather than skipped. Implicit wait is switched off
	 * while polling so each look is instant.
	 */
	public void clickAcrossPages(By target, By anyRowOnPage, By nextPageButton) {
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
		try {
			int page = 1;
			while (true) {
				try {
					new WebDriverWait(getDriver(), Duration.ofSeconds(page == 1 ? 30 : 15)).until(d ->
							firstDisplayed(d, target, false) != null || firstDisplayed(d, anyRowOnPage, false) != null);
				} catch (org.openqa.selenium.TimeoutException e) {
					throw new org.openqa.selenium.NoSuchElementException(
							"No requests were listed on page " + page + " of the list, so " + target + " could not be found");
				}

				WebElement hit = firstDisplayed(getDriver(), target, false);
				if (hit != null) {
					scrollIntoView(hit);
					clickWithJsFallback(hit);
					return;
				}

				WebElement next = firstDisplayed(getDriver(), nextPageButton, false);
				if (next == null || !next.isEnabled() || "true".equals(next.getAttribute("disabled"))) {
					throw new org.openqa.selenium.NoSuchElementException(
							"The request " + target + " was not found on any of the " + page
									+ " page(s) of the list (no further page to open)");
				}

				String before = pageSignature();
				scrollIntoView(next);
				clickWithJsFallback(next);
				try {
					new WebDriverWait(getDriver(), Duration.ofSeconds(5)).until(d -> !pageSignature().equals(before));
				} catch (org.openqa.selenium.TimeoutException ignored) {
					// Same text on the next page is possible; carry on and look again.
				}
				page++;
				if (page > 100) {
					throw new org.openqa.selenium.NoSuchElementException(
							"Gave up after 100 pages looking for " + target);
				}
			}
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		}
	}

	/**
	 * Writes the Notes entry for the day whose Hours/Minutes cell was just clicked and filled,
	 * then saves it - the app only keeps a note once Save Notes is clicked, and the next day must
	 * not be started before that.
	 *
	 * Why the steps are shaped this way: the Notes panel follows whichever day cell was last
	 * CLICKED (typing alone does not move it), and a saved note locks the box (readonly, with
	 * Saved / Edit / Delete in place of Save Notes). So this waits for an editable box, or for
	 * Edit when the day already has a saved note (re-editing a day after Update) and unlocks it,
	 * clicks into the box, replaces its content, checks the text really landed, waits for Save
	 * Notes to be enabled, clicks it, and waits for the panel to lock again (Save Notes gone).
	 * Select-all + Delete is used instead of clear(), which rejects readonly boxes.
	 * Implicit wait is switched off while polling so a not-yet-present element costs one poll,
	 * not the whole implicit timeout.
	 */
	public void enterDayNote(By textareaLocator, By editLocator, By saveLocator, String note) {
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
		try {
			WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(20));

			wait.until(d -> firstDisplayed(d, textareaLocator, true) != null
					|| firstDisplayed(d, editLocator, false) != null);
			if (firstDisplayed(getDriver(), textareaLocator, true) == null) {
				clickWithJsFallback(firstDisplayed(getDriver(), editLocator, false));
				wait.until(d -> firstDisplayed(d, textareaLocator, true) != null);
			}

			boolean typed = false;
			for (int attempt = 1; attempt <= 3 && !typed; attempt++) {
				try {
					WebElement box = wait.until(d -> firstDisplayed(d, textareaLocator, true));
					clickWithJsFallback(box);
					box.sendKeys(Keys.chord(Keys.CONTROL, "a"));
					box.sendKeys(Keys.DELETE);
					box.sendKeys(note);
					typed = note.equals(box.getAttribute("value"));
				} catch (org.openqa.selenium.StaleElementReferenceException
						| org.openqa.selenium.InvalidElementStateException retry) {
					// The panel re-rendered while typing; look the box up again and retry.
				}
			}
			if (!typed) {
				throw new org.openqa.selenium.WebDriverException(
						"The note text did not stay in the Notes box after 3 attempts: " + note);
			}

			WebElement save;
			try {
				save = new WebDriverWait(getDriver(), Duration.ofSeconds(10)).until(d -> {
					WebElement s = firstDisplayed(d, saveLocator, false);
					return s != null && s.isEnabled() ? s : null;
				});
			} catch (org.openqa.selenium.TimeoutException e) {
				throw new org.openqa.selenium.TimeoutException(
						"Save Notes did not become enabled after typing the note (is the text identical "
								+ "to what is already saved for this day?): " + note, e);
			}
			clickWithJsFallback(save);

			try {
				new WebDriverWait(getDriver(), Duration.ofSeconds(10))
						.until(d -> firstDisplayed(d, saveLocator, false) == null);
			} catch (org.openqa.selenium.TimeoutException e) {
				throw new org.openqa.selenium.TimeoutException(
						"Save Notes was clicked but the note was not saved (the panel never switched to "
								+ "Saved): " + note, e);
			}
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
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

	/**
	 * Decides whether something is there without paying the implicit wait for its absence. A plain
	 * findElements() for a record that does not exist blocks for the whole implicit wait (30s) before
	 * returning empty, and that is exactly what happened on every empty week. Instead this polls for
	 * either the target or an explicit "empty" signal.
	 *
	 * Returns true as soon as the target is present. Returns false once the empty signal has stayed
	 * on screen for settleMillis with the target still absent - the hold-off is there because the app
	 * briefly shows the empty message while it loads a week's records, so a single sighting is not
	 * proof. If neither shows within timeoutSeconds it also returns false.
	 */
	public boolean appearsBeforeEmptySignal(By target, By emptySignal, long settleMillis, int timeoutSeconds) {
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
		try {
			long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;
			long emptySince = -1;
			while (System.currentTimeMillis() < deadline) {
				if (!getDriver().findElements(target).isEmpty()) {
					return true;
				}
				if (!getDriver().findElements(emptySignal).isEmpty()) {
					long now = System.currentTimeMillis();
					if (emptySince < 0) {
						emptySince = now;
					} else if (now - emptySince >= settleMillis) {
						return false;
					}
				} else {
					emptySince = -1;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return false;
				}
			}
			return false;
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		}
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
		// Implicit wait off while polling: once the element is gone, findElements() would otherwise
		// block for the whole implicit wait (30s) on every approve/reject before returning empty.
		getDriver().manage().timeouts().implicitlyWait(Duration.ZERO);
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
		} finally {
			getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(30));
		}
	}

	/**
	 * Waits for a cell's text to contain an expected value (e.g. the row's running WEEK TOTAL
	 * matching the sum of days entered so far), so the next day is only started once the app has
	 * genuinely registered this day's hours.
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
