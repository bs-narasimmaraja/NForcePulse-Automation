package com.qa.nforcepulse.pageobjectfiles;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qa.nforcepulse.utilities.ReusableMethods;
import com.qa.nforcepulse.utilities.TestContextSetup;

import java.util.List;
import java.util.Map;


public class TimeEntries {


	WebDriver driver;
	ReusableMethods reusableMethods;
	TestContextSetup testContext;

	public TimeEntries(WebDriver driver, ReusableMethods reusableMethods, TestContextSetup testcontext) {
		this.driver = driver;
		this.reusableMethods = reusableMethods;
		this.testContext = testcontext;
	}
	public final By ClientDropdowninsideprojectdetails=By.xpath("(//select[option[normalize-space()='Select Client']])[2]");
	public final By Savebtninprojectdetails=By.xpath("(//button[normalize-space()='Save'])[1]");
	public final By ProjectDropdowninsideprojectdetails=By.xpath("//select[./option[normalize-space()='Select Project']]");
	public final By timeentriesinsidebar = By.xpath("(//div[normalize-space()='Time Entries'])[1]");
	public final By Checkemptygrid = By.xpath("//td[contains(., 'No records added yet')]");
	// Present only when this week already has a submitted/draft record (a real "Client :" row,
	// not the dropdowns above the table, hence scoped to tbody). Checking for this instead of
	// Checkemptygrid's absence matters a lot in practice: a live run showed every findElements()
	// call that matched ZERO elements taking ~30s, while one that matched something returned in
	// ~500ms. Skipping N already-used weeks used to mean N slow "not found" checks (Checkemptygrid
	// absent); checking for this instead makes those N checks the fast "found" case, leaving only
	// the final, genuinely-empty week to pay the ~30s "not found" cost once.
	private final By hasExistingRecordInWeek = By.xpath("//tbody//td[contains(normalize-space(), 'Client')]");
	public final By nextweekbutton = By.xpath("//*[name()='svg' and contains(@class,'lucide-chevron-right')]");
	// These three became custom dropdowns (<button role="combobox" aria-label="Client|Project">
	// that opens a listbox) instead of native <select> elements. aria-label is used because it
	// stays the same after a value is chosen, unlike the "Select Client" placeholder text. The
	// Manager box's HTML was only given as its placeholder <span>, so its aria-label is assumed to
	// follow the same pattern, with the placeholder text as a fallback.
	// Previous native-select locators, in case this needs reverting:
	//   //select[option[normalize-space()='Select Client']]   (same for Project / Manager)
	public final By selectClientDropdown = By.xpath("//button[@role='combobox' and @aria-label='Client']");
	public final By selectProjectDropdown = By.xpath("//button[@role='combobox' and @aria-label='Project']");
	public final By selectManagerDropdown = By.xpath("//button[@role='combobox' and (@aria-label='Manager' or .//span[normalize-space()='Select Manager'])]");
	public final By ClickOnAddrecord = By.xpath("//button[text()='+ Add Record']");
	// tr[contains(., 'Client')][last()]: the table's tbody also contains a DAILY TOTALS summary
	// row as a genuine sibling <tr> (confirmed in real screenshots) which is always the literal
	// last row, regardless of how many projects exist - a plain tr[last()] (tried once) always
	// landed on that totals row instead of a real project row, breaking even the single-project
	// case. Filtering to rows that actually contain "Client" first excludes the totals row, then
	// [last()] picks the most-recently-added real project row - "the only project row" when
	// there's just one, so single-project scenarios see identical behavior to the original.
	// projectName == null reproduces the exact [last()] behaviour above (most-recently-added
	// row) for every existing caller, unchanged. A non-null projectName instead matches the
	// row that contains that project's name, wherever it sits in the table - needed to update
	// one specific existing project among several, where the target row is not necessarily
	// last (e.g. Pulse submitted first, Sync second, only Pulse gets rejected/updated).
	private String projectRowXpath(String projectName) {
		String base = "//div[contains(@class,'hidden md:block')]//table//tbody/tr[contains(., 'Client')]";
		return projectName == null ? base + "[last()]" : base + "[contains(., '" + projectName + "')]";
	}

	private By hoursInput(int column, String projectName) {
		return By.xpath(projectRowXpath(projectName) + "/td[" + column + "]//input[@inputmode='numeric'][1]");
	}

	private By minutesInput(int column, String projectName) {
		return By.xpath(projectRowXpath(projectName) + "/td[" + column + "]//input[@inputmode='numeric'][2]");
	}
	private final By PencilIcon=By.xpath("(//button[.//*[name()='svg' and contains(@class,'lucide-pencil')]])[1]");
	// Notes panel, scoped to the desktop layout (the mobile cards render a second copy). The box is
	// matched without its placeholder, which changes ("Notes" vs "Add a reason for this day's
	// hours") depending on whether a note is required. Edit unlocks a day's already-saved note.
	private final By notesTextarea = By.xpath("//div[contains(@class,'hidden md:block')]//textarea");
	private final By notesEditButton = By.xpath("//div[contains(@class,'hidden md:block')]//button[normalize-space()='Edit']");
	private final By saveNotesButton = By.xpath("//div[contains(@class,'hidden md:block')]//button[normalize-space()='Save Notes']");
	// Column 9 in the same row - PROJECT DETAILS(1), SUN..SAT(2-8), WEEK TOTAL(9), STATUS(10).
	// Same DAILY TOTALS exclusion as hoursInput/minutesInput above, same null-vs-named scoping.
	private By weekTotalCell(String projectName) {
		return By.xpath(projectRowXpath(projectName) + "/td[9]");
	}
	// Same exclusion: only real project rows carry their own "Actions" button anyway, but scope
	// consistently so this always targets the most-recently-added project row.
	public final By ActionBtn=By.xpath("//div[contains(@class,'hidden md:block')]//table//tbody/tr[contains(., 'Client')][last()]//button[text()='Actions']");
	// Named counterpart to ActionBtn above, kept as a fully separate locator/method so existing
	// callers of ActionBtn/clickOnActionButton() are untouched - needed when several project
	// rows exist and Actions must be clicked for one specific one (e.g. only Pulse, not Sync).
	private By actionBtnForProject(String projectName) {
		return By.xpath(projectRowXpath(projectName) + "//button[text()='Actions']");
	}
	// Submit now sits directly below Actions in each project row (data-testid='submit-btn-row'),
	// so it is clicked on its own - Actions is only for Update / Save Draft. Scoped to the desktop
	// table's last project row, like ActionBtn: a page-wide "(...)[1]" takes the first match on the
	// whole page, which is wrong once two projects exist and can also land on a hidden mobile-card
	// copy of the button.
	public final By SubmitBtn=By.xpath("//div[contains(@class,'hidden md:block')]//table//tbody/tr[contains(., 'Client')][last()]//button[@data-testid='submit-btn-row']");
	private By submitBtnForProject(String projectName) {
		return By.xpath(projectRowXpath(projectName) + "//button[@data-testid='submit-btn-row']");
	}
	// Matches the success message however it is worded or punctuated ("...submitted successfully!",
	// "...submitted successfully"), case-insensitively, on the element that directly holds the
	// text (not on html/body, which contain it too). The old locator required the exact "!" and
	// stopped matching after the confirmation dialog was removed. The message can also vanish
	// within about a second, so verifyTimesheetSubmittedMessage() accepts the project row's
	// status moving to Pending/Submitted as proof of submission as well.
	public final By TimesheetSubmissionMessage=By.xpath("//*[contains(translate(normalize-space(text()),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'submitted successfully')]");
	private final By lastProjectRow = By.xpath("//div[contains(@class,'hidden md:block')]//table//tbody/tr[contains(., 'Client')][last()]");
	public final By weekRangeLabel = By.xpath("(//*[name()='svg' and contains(@class,'lucide-chevron-right')])[1]/ancestor::button[1]/preceding-sibling::*[1]");
	// The TOTAL box shows "TOTAL" on one line and "09h : 00m" on the next - same two-line
	// shape as weekRangeLabel, so it's parsed the same way (last line = the value).
	private final By totalHoursBox = By.xpath("//*[normalize-space()='TOTAL']/ancestor::div[1]");
	private final By Updatebtn=By.xpath("//button[normalize-space()='Update']");

	public void clickOnTimeEntries() {
		reusableMethods.clickOnButton(timeentriesinsidebar);
	}

	public void checkTheEmptyGrid() {
		long overallStart = System.currentTimeMillis();
		int weeksSkipped = 0;
		// An empty week is recognised by the "No records added yet" message staying put for 3s, not by
		// waiting out a 30s search for a record that isn't there.
		while (reusableMethods.appearsBeforeEmptySignal(hasExistingRecordInWeek, Checkemptygrid, 3000, 30)) {
			weeksSkipped++;
			System.out.println("Current grid is not empty. Moving to next week...");

			String weekBeforeClick = reusableMethods.getText(weekRangeLabel);
			reusableMethods.clickOnButton(nextweekbutton);
			// Wait for the week label to actually change before re-checking emptiness - checking
			// immediately risks reading the previous week's still-visible content for an instant,
			// which can wrongly skip past an actually-empty week.
			reusableMethods.waitForTextToChangeFrom(weekRangeLabel, weekBeforeClick, 5);
		}
		System.out.println("User can add the time grid (skipped " + weeksSkipped + " week(s), total "
				+ (System.currentTimeMillis() - overallStart) + " ms)");

	}
	public void selectTheClient(String client) {

		reusableMethods.selectFromCombobox(selectClientDropdown, client);

	}
	public void selectTheProject(String project) {
		reusableMethods.selectFromCombobox(selectProjectDropdown, project);
	}

	public void selectTheManager(String Manager) {
		reusableMethods.selectFromCombobox(selectManagerDropdown, Manager);

	}

	public void clickOnAddRecordBtn() {
		reusableMethods.clickOnButton(ClickOnAddrecord);
	}


	public void enterTimeEntries(List<Map<String, String>> entries) {
		enterTimeEntries(entries, null);
	}

	// Named counterpart used when several project rows exist and the entries must go into one
	// specific row (e.g. re-editing Pulse's hours after Update, while Sync's row is untouched).
	public void enterTimeEntries(List<Map<String, String>> entries, String projectName) {
		int runningTotalMinutes = 0;
		for (int i = 0; i < entries.size(); i++) {

			String hours = entries.get(i).get("Hours");
			String minutes = entries.get(i).get("Minutes");
			String notes = entries.get(i).get("Notes");

			int column = i + 3;

			// Click the day's hours box first: that is what makes the Notes panel follow this day
			// (typing alone leaves it on the previous day, where the saved note is locked).
			reusableMethods.clickElement(hoursInput(column, projectName));
			reusableMethods.enterText(hoursInput(column, projectName), hours);
			reusableMethods.enterText(minutesInput(column, projectName), minutes);
			// The note must be typed and saved before moving on to the next day.
			reusableMethods.enterDayNote(notesTextarea, notesEditButton, saveNotesButton, notes);

			runningTotalMinutes += Integer.parseInt(hours) * 60 + Integer.parseInt(minutes);
			String expectedTotal = String.format("%02dh : %02dm", runningTotalMinutes / 60, runningTotalMinutes % 60);
			reusableMethods.waitForCellToContain(weekTotalCell(projectName), expectedTotal, 15);
		}
	}
	public void clickOnActionButton(){

		reusableMethods.clickOnButton(ActionBtn);
	}

	public void clickActionsForProject(String projectName) {
		reusableMethods.clickOnButton(actionBtnForProject(projectName));
	}

	public void clickOnSubmit(){
		reusableMethods.clickOnButton(SubmitBtn);
	}

	public void clickSubmitForProject(String projectName) {
		reusableMethods.clickOnButton(submitBtnForProject(projectName));
	}

	public void verifyTimesheetSubmittedMessage(){
		reusableMethods.waitForSubmissionConfirmation(TimesheetSubmissionMessage, lastProjectRow, 30);
	}

	public String captureCurrentWeek(){
		String raw = reusableMethods.getText(weekRangeLabel);
		return raw.split("\n")[0].trim();
	}

	public String captureTotalHours() {
		String raw = reusableMethods.getText(totalHoursBox);
		String[] lines = raw.split("\n");
		return lines[lines.length - 1].trim();
	}

	// Same forward-only, wait-for-label-to-change approach as checkTheEmptyGrid() - reusing it
	// here to walk forward from the current (today's) week to a specific already-used week
	// instead of hunting for an empty one.
	public void navigateToWeek(String targetWeek) {
		while (!captureCurrentWeek().equals(targetWeek)) {
			String weekBeforeClick = reusableMethods.getText(weekRangeLabel);
			reusableMethods.clickOnButton(nextweekbutton);
			reusableMethods.waitForTextToChangeFrom(weekRangeLabel, weekBeforeClick, 5);
		}
	}

	public void clickOnUpdateAction() {
		reusableMethods.clickOnButton(Updatebtn);
	}

	public void clickPencilIconInProjectDetails() {
		reusableMethods.clickOnButton(PencilIcon);
	}

	public void selectClientInProjectDetails(String client) {
		reusableMethods.selectByVisibleText(ClientDropdowninsideprojectdetails, client);
	}

	public void selectProjectInProjectDetails(String project) {
		reusableMethods.selectByVisibleText(ProjectDropdowninsideprojectdetails, project);
	}

	public void clickSaveInProjectDetails() {
		reusableMethods.clickOnButton(Savebtninprojectdetails);
	}









}