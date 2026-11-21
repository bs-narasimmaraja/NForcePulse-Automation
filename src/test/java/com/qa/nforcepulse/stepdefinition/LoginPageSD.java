package com.qa.nforcepulse.stepdefinition;

import static org.testng.Assert.assertTrue;

import com.qa.nforcepulse.utilities.TestContextSetup;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.en.And;

import java.util.List;
import java.util.Map;

public class LoginPageSD {
	private TestContextSetup testcontext;

	public LoginPageSD(TestContextSetup testcontext) {
		this.testcontext = testcontext;
	}

	@Given("the user launches the NforcePulse application")
	public void the_user_launches_the_nforcepulse_application() {
		testcontext.getPageObjectManager().getLoginPage().launchBrowser();
		testcontext.getPageObjectManager().getLoginPage().openURL();
	}

	@When("the user enters valid credentials")
	public void the_user_enters_valid_credentials() {
		testcontext.getPageObjectManager().getLoginPage().login();
		testcontext.getPageObjectManager().getLoginPage().clickOnLoginButton();
	}

	@Then("the user should be navigated to the dashboard")
	public void the_user_should_be_navigated_to_the_dashboard() {
		String message = testcontext.getPageObjectManager().getLoginPage().getWelcomeMessage();
		assertTrue(message.contains("Welcome back"), "Unexpected message: " + message);
		testcontext.capturedEmployeeName = message.replace("Welcome back", "").replaceAll("[,!]", "").trim();
	}

   @And("the Employee navigates to the Time Entries page")
    public void the_user_navigates_to_the_timeentries(){
	testcontext.getPageObjectManager().getTimeEntries().clickOnTimeEntries();
	}
	@And("the Employee navigates to the next available empty time grid")
	public void the_employee_navigate_to_the_available_emptygrid(){
		testcontext.getPageObjectManager().getTimeEntries().checkTheEmptyGrid();

	}

	@And("the Employee navigates to the previously submitted week")
	public void the_employee_navigates_to_the_previously_submitted_week(){
		testcontext.getPageObjectManager().getTimeEntries().navigateToWeek(testcontext.capturedWeek);
	}
	@And("the Employee selects Client {string}")
	public void the_employee_select_the_Client(String client){
		testcontext.getPageObjectManager().getTimeEntries().selectTheClient(client);

	}
	@And("the Employee selects Project {string}")
	public void the_employee_select_the_Project(String Project){
		testcontext.getPageObjectManager().getTimeEntries().selectTheProject(Project);

	}

	@And("the Employee selects Manager {string}")
	public void the_employee_select_the_manager(String Manager){
		testcontext.getPageObjectManager().getTimeEntries().selectTheManager(Manager);

	}
	@And("the Employee clicks Add Record Button")
	public void the_employee_select_the_AddRecordBtn(){
		testcontext.getPageObjectManager().getTimeEntries().clickOnAddRecordBtn();
	}


	@And("the Employee enter the following time entries and Notes")
	public void EnterTheFollowingTimeEntriesAndNotes(DataTable dataTable) {
		// Write code here that turns the phrase above into concrete actions
		List<Map<String, String>> entries =
				dataTable.asMaps(String.class, String.class);

		testcontext.getPageObjectManager().getTimeEntries().enterTimeEntries(entries);
		}
	@And("the Employee selects Actions")
	public void theEmployeeSelectsActions() {
		// Write code here that turns the phrase above into concrete actions
		testcontext.getPageObjectManager().getTimeEntries().clickOnActionButton();

	}

	@And("the Employee selects Actions for project {string}")
	public void theEmployeeSelectsActionsForProject(String projectName) {
		testcontext.getPageObjectManager().getTimeEntries().clickActionsForProject(projectName);
	}

	@And("the Employee enter the following time entries and Notes for project {string}")
	public void enterTheFollowingTimeEntriesAndNotesForProject(String projectName, DataTable dataTable) {
		List<Map<String, String>> entries = dataTable.asMaps(String.class, String.class);
		testcontext.getPageObjectManager().getTimeEntries().enterTimeEntries(entries, projectName);
	}

	@And("the Employee select Submit")
	public void theEmployeeSelectSubmit() {
		// Write code here that turns the phrase above into concrete actions
		testcontext.getPageObjectManager().getTimeEntries().clickOnSubmit();
	}

	@And("the Employee selects Update")
	public void theEmployeeSelectsUpdate() {
		testcontext.getPageObjectManager().getTimeEntries().clickOnUpdateAction();
	}

	@And("the Employee clicks the edit icon in Project Details")
	public void theEmployeeClicksTheEditIconInProjectDetails() {
		testcontext.getPageObjectManager().getTimeEntries().clickPencilIconInProjectDetails();
	}

	@And("the Employee updates Client to {string} in Project Details")
	public void theEmployeeUpdatesClientInProjectDetails(String client) {
		testcontext.getPageObjectManager().getTimeEntries().selectClientInProjectDetails(client);
	}

	@And("the Employee updates Project to {string} in Project Details")
	public void theEmployeeUpdatesProjectInProjectDetails(String project) {
		testcontext.getPageObjectManager().getTimeEntries().selectProjectInProjectDetails(project);
	}

	@And("the Employee saves the Project Details")
	public void theEmployeeSavesTheProjectDetails() {
		testcontext.getPageObjectManager().getTimeEntries().clickSaveInProjectDetails();
	}

	@Then("the Employee Select the submission confirmation popup")
	public void theEmployeeSelectTheSubmissionConfirmationPopup() {
		// Write code here that turns the phrase above into concrete actions
		testcontext.getPageObjectManager().getTimeEntries().acceptTheSubmissionPopup();
	}
	@And("the Employee timesheet is successfully submitted and displayed as Submitted")
	public void theEmployeeSuccessfullySubmittedTheTimesheet(){
		testcontext.getPageObjectManager().getTimeEntries().verifyTimesheetSubmittedMessage();
	}

	@And("the Employee captures the submitted week and employee name for approval verification")
	public void theEmployeeCapturesTheSubmittedWeekAndEmployeeName() {
		testcontext.capturedWeek = testcontext.getPageObjectManager().getTimeEntries().captureCurrentWeek();
	}

	@And("the Employee captures the total submitted hours for verification")
	public void theEmployeeCapturesTheTotalSubmittedHours() {
		testcontext.capturedTotalHours = testcontext.getPageObjectManager().getTimeEntries().captureTotalHours();
		System.out.println("Captured total submitted hours (Employee side): " + testcontext.capturedTotalHours);
	}

	@When("the Employee logs out of the application")
	public void theEmployeeLogsOutOfTheApplication() {
		testcontext.getPageObjectManager().getLoginPage().logout();
		testcontext.getPageObjectManager().getLoginPage().openURL();
	}

	@And("the Manager logs in with valid credentials")
	public void theManagerLogsInWithValidCredentials() {
		testcontext.getPageObjectManager().getLoginPage().loginAsManager();
		testcontext.getPageObjectManager().getLoginPage().clickOnLoginButton();
	}

	@And("the Second Manager logs in with valid credentials")
	public void theSecondManagerLogsInWithValidCredentials() {
		testcontext.getPageObjectManager().getLoginPage().loginAsSecondManager();
		testcontext.getPageObjectManager().getLoginPage().clickOnLoginButton();
	}

	@And("the Manager logs out of the application")
	public void theManagerLogsOutOfTheApplication() {
		testcontext.getPageObjectManager().getLoginPage().logout();
		testcontext.getPageObjectManager().getLoginPage().openURL();
	}

	@And("the Manager closes the review popup")
	public void theManagerClosesTheReviewPopup() {
		testcontext.getPageObjectManager().getApprovals().closeTimesheetDetailsDrawer();
	}

	@Then("the Manager navigates to the Approvals tab")
	public void theManagerNavigatesToTheApprovalsTab() {
		testcontext.getPageObjectManager().getApprovals().clickOnApprovalsTab();
	}

	@And("the Manager navigates to the Re-Submitted tab")
	public void theManagerNavigatesToTheResubmittedTab() {
		testcontext.getPageObjectManager().getApprovals().clickOnResubmittedTab();
	}

	@And("the Manager locates the pending request for the captured week and employee name")
	public void theManagerLocatesThePendingRequest() {
		assertTrue(testcontext.capturedWeek != null && testcontext.capturedEmployeeName != null,
				"Week or employee name was not captured during submission");
	}

	@And("the Manager clicks the review icon for that request")
	public void theManagerClicksTheReviewIconForThatRequest() {
		testcontext.getPageObjectManager().getApprovals()
				.clickViewDetailsFor(testcontext.capturedEmployeeName, testcontext.capturedWeek);
	}

	@Then("the total hours displayed in the Manager portal should match the submitted total hours")
	public void theTotalHoursInManagerPortalShouldMatchTheSubmittedTotalHours() {
		String displayedTotalHours = testcontext.getPageObjectManager().getApprovals().getDisplayedTotalHours();
		String submittedTotalHours = testcontext.capturedTotalHours;

		boolean hoursMatch = displayedTotalHours.replaceAll("\\s+", "")
				.equalsIgnoreCase(submittedTotalHours.replaceAll("\\s+", ""));

		System.out.println("Submitted total hours (Employee side): " + submittedTotalHours
				+ " | Displayed total hours (Manager portal): " + displayedTotalHours
				+ " | Match: " + hoursMatch);

		assertTrue(hoursMatch, "Total hours mismatch - submitted: " + submittedTotalHours
				+ ", displayed in Manager portal: " + displayedTotalHours);
	}

	@And("the Manager approves the timesheet request")
	public void theManagerApprovesTheTimesheetRequest() {
		testcontext.getPageObjectManager().getApprovals().clickApproveActionInDrawer();
		testcontext.getPageObjectManager().getApprovals().confirmApproval();
	}

	@And("the Manager approves the timesheet request for project {string}")
	public void theManagerApprovesTheTimesheetRequestForProject(String projectName) {
		testcontext.getPageObjectManager().getApprovals().clickApproveActionInDrawerFor(projectName);
		testcontext.getPageObjectManager().getApprovals().confirmApproval();
	}

	@Then("the timesheet request status should be displayed as Approved")
	public void theTimesheetRequestStatusShouldBeDisplayedAsApproved() {
		boolean approved = testcontext.getPageObjectManager().getApprovals().isApprovedStatusDisplayed();
		assertTrue(approved, "Timesheet request was not shown as Approved");
	}

	@And("the Manager clicks the reject icon for that request")
	public void theManagerClicksTheRejectIconForThatRequest() {
		testcontext.getPageObjectManager().getApprovals().clickRejectActionInDrawer();
	}

	@And("the Manager clicks the reject icon for project {string}")
	public void theManagerClicksTheRejectIconForProject(String projectName) {
		testcontext.getPageObjectManager().getApprovals().clickRejectActionInDrawerFor(projectName);
	}

	@And("the Manager enters rejection comments {string}")
	public void theManagerEntersRejectionComments(String comment) {
		testcontext.getPageObjectManager().getApprovals().enterRejectionComment(comment);
	}

	@And("the Manager rejects the timesheet request")
	public void theManagerRejectsTheTimesheetRequest() {
		testcontext.getPageObjectManager().getApprovals().confirmRejection();
	}

	@Then("the timesheet request status should be displayed as Rejected")
	public void theTimesheetRequestStatusShouldBeDisplayedAsRejected() {
		boolean rejected = testcontext.getPageObjectManager().getApprovals().isRejectedStatusDisplayed();
		assertTrue(rejected, "Timesheet request was not shown as Rejected");
	}
}

