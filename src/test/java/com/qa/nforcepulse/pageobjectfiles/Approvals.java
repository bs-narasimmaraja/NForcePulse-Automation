package com.qa.nforcepulse.pageobjectfiles;

import com.qa.nforcepulse.utilities.ReusableMethods;
import com.qa.nforcepulse.utilities.TestContextSetup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Approvals {

    WebDriver driver;
    ReusableMethods reusableMethods;
    TestContextSetup testContext;

    public Approvals(WebDriver driver, ReusableMethods reusableMethods, TestContextSetup testcontext) {
        this.driver = driver;
        this.reusableMethods = reusableMethods;
        this.testContext = testcontext;
    }
    public final By Resubmittedtab=By.xpath("//button[contains(normalize-space(.), 'Re-Submitted')]");
    public final By Approvaltab=By.xpath("//a[.//div[normalize-space()='Approvals']]");
    public final By ViewDetailsIcon=By.xpath("//button[@title='View Details']");
    // Confirmed exact match on the modal's own button text - scoped to [last()] in case the
    // covered background elements (list/drawer) also carry an "Approve"-labelled control.
    public final By Approvalbtn=By.xpath("(//button[normalize-space()='Approve'])[last()]");

    private By viewDetailsIconFor(String employeeName, String week) {
        return By.xpath("//button[@title='View Details'][ancestor::tr[contains(normalize-space(),\""
                + employeeName + "\") and contains(normalize-space(),\"" + week + "\")]]");
    }

    // title='Approve' is confirmed to match a real element (earlier attempts clicked it
    // without a "no such element" timeout) - the lucide-check SVG search found nothing at
    // all, so that assumption was wrong. The remaining problem is *which* title='Approve'
    // match gets picked: the background list's icon (still in the DOM, just visually
    // covered by the drawer) versus the drawer's own copy. The drawer renders after the
    // list in the DOM, so the last match should be the drawer's rather than the list's.
    // No [last()] here on purpose: clickApproveActionInDrawer/clickRejectActionInDrawer click the
    // last *visible* match instead (see ReusableMethods.clickOnLastDisplayed) - a positional
    // [last()] settled on an unclickable copy of the Reject icon in a real run.
    private final By approveActionInDrawer = By.xpath("//button[@title='Approve']");

    // Same title-based pattern as Approve, confirmed by the given markup (title="Reject").
    private final By rejectActionInDrawer = By.xpath("//button[@title='Reject']");

    // [last()] above assumes "whichever row hasn't been actioned yet is last", which only
    // holds when every project in the drawer eventually gets the SAME action. When only one
    // specific project among several must be rejected/approved (e.g. reject Pulse, leave Sync
    // untouched), that project is not guaranteed to be the last row, so it needs to be matched
    // by name instead of position - kept as separate locators/methods, existing [last()]-based
    // ones untouched.
    // [last()] wrapper kept even with name-scoping, for the same reason as approveActionInDrawer
    // above: a stray background-list match containing the same project name can't be ruled out,
    // and the drawer's own copy renders after the list so it remains the last DOM match.
    private By approveActionInDrawerFor(String projectName) {
        return By.xpath("(//tr[contains(., '" + projectName + "')]//button[@title='Approve'])[last()]");
    }

    private By rejectActionInDrawerFor(String projectName) {
        return By.xpath("(//tr[contains(., '" + projectName + "')]//button[@title='Reject'])[last()]");
    }

    // The Reject popup requires a comment (placeholder literally says "(required)") - the
    // Approve popup didn't need this since its comment is optional and never touched.
    private final By rejectCommentBox = By.xpath("//textarea[@placeholder=\"e.g. Please improve (required)\"]");

    // Confirmed exact match on the modal's own button text - same [last()] reasoning as Approvalbtn.
    public final By Rejectbtn = By.xpath("(//button[normalize-space()='Reject'])[last()]");

    // The popup's own heading text (confirmed via a real screenshot: "Approve Project
    // Timesheet") - waited on for disappearance after confirming, so the drawer is settled
    // back to an updated state before acting on a second project's row.
    private final By approveDialogHeading = By.xpath("//*[normalize-space()='Approve Project Timesheet']");

    // By this point the Timesheet Details drawer is already open and scoped to the one
    // specific request (the employee/week matching already happened when we clicked View
    // Details) - the drawer's own project row just shows Project/hours/status, not the
    // employee name or week, so re-scoping by those here never matches. A plain check for
    // the "Approved" badge text is enough since we know we're looking at the right drawer.
    private final By approvedStatus = By.xpath("//*[normalize-space()='Approved']");

    private final By rejectedStatus = By.xpath("//*[normalize-space()='Rejected']");

    // Same "label line, value line" layout as the employee side's TOTAL box, just labelled
    // "TOTAL HOURS" here - parsed the same way (last line = the value) for the cross-check.
    private final By totalHoursInDrawer = By.xpath("//*[normalize-space()='TOTAL HOURS']/ancestor::div[1]");

    // The Timesheet Details drawer's own close ("X") icon, top-right of the panel (given
    // markup: an svg with class "lucide lucide-x"). Needed when switching managers: with the
    // drawer still open, the app's Logout control isn't reachable/visible, so the drawer has
    // to be explicitly closed first.
    private final By closeDrawerButton = By.xpath("//*[name()='svg' and contains(@class,'lucide-x')]/ancestor::button[1]");

    public void clickOnApprovalsTab() {
        reusableMethods.clickOnButton(Approvaltab);
    }

    public void clickOnResubmittedTab() {
        reusableMethods.clickOnButton(Resubmittedtab);
    }

    public void clickViewDetailsFor(String employeeName, String week) {
        reusableMethods.clickOnButton(viewDetailsIconFor(employeeName, week));
    }

    public void clickApproveActionInDrawer() {
        reusableMethods.clickOnLastDisplayed(approveActionInDrawer);
    }

    public void clickApproveActionInDrawerFor(String projectName) {
        reusableMethods.clickOnButton(approveActionInDrawerFor(projectName));
    }

    public void confirmApproval() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        reusableMethods.clickOnButton(Approvalbtn);
        // Give the drawer time to settle back to its updated state before returning - a real
        // multi-project run showed acting on a second project's row immediately after this
        // click race the still-transitioning first popup/row instead of the second one.
        reusableMethods.waitForElementToDisappear(approveDialogHeading, 10);
    }

    public boolean isApprovedStatusDisplayed() {
        return reusableMethods.isDisplayed(approvedStatus);
    }

    public void clickRejectActionInDrawer() {
        reusableMethods.clickOnLastDisplayed(rejectActionInDrawer);
    }

    public void clickRejectActionInDrawerFor(String projectName) {
        reusableMethods.clickOnButton(rejectActionInDrawerFor(projectName));
    }

    public void enterRejectionComment(String comment) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        reusableMethods.enterText(rejectCommentBox, comment);
    }

    public void confirmRejection() {
        reusableMethods.clickOnButton(Rejectbtn);
        // Same fix as confirmApproval(): a real multi-project run showed this exact race on
        // reject too - acting on the second project's row immediately after this click could
        // hit the still-transitioning first popup/row. Waits on Rejectbtn itself (already a
        // proven locator) rather than guessing at the reject popup's heading text, which is
        // unconfirmed unlike the approve popup's ("Approve Project Timesheet", seen directly
        // in a screenshot).
        reusableMethods.waitForElementToDisappear(Rejectbtn, 10);
    }

    public boolean isRejectedStatusDisplayed() {
        return reusableMethods.isDisplayed(rejectedStatus);
    }

    public String getDisplayedTotalHours() {
        String raw = reusableMethods.getText(totalHoursInDrawer);
        String[] lines = raw.split("\n");
        return lines[lines.length - 1].trim();
    }

    public void closeTimesheetDetailsDrawer() {
        reusableMethods.clickOnButton(closeDrawerButton);
        // Small settle pause after closing - exact timing unconfirmed, matching the same
        // pragmatic-buffer pattern already used elsewhere in this class (e.g. confirmApproval).
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

}
