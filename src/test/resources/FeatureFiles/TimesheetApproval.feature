@Sanity
Feature: TimesheetApproval functionality in NforcePulse

	@SubmittingTimesheet @TimesheetApproval
      @Sanity26
	Scenario Outline: Verify the timesheet submission and manager approval functionality in NforcePulse application
		Given the user launches the NforcePulse application
		When the user enters valid credentials
		Then the user should be navigated to the dashboard
		And the Employee navigates to the Time Entries page
        And the Employee navigates to the next available empty time grid
        And the Employee selects Client "<Client>"
        And the Employee selects Project "<Project>"
        And the Employee selects Manager "<Manager>"
        And the Employee clicks Add Record Button
        And the Employee enter the following time entries and Notes
        | Day       | Hours | Minutes | Notes                |
        | Monday    | 8     | 00      | Development work     |
        | Tuesday   | 8     | 00      | Testing new features |
        | Wednesday | 8     | 00      | Code review          |
        | Thursday  | 8     | 00      | Bug fixing           |
        | Friday    | 8     | 00      | Documentation work   |

       And the Employee selects Actions
       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted
       And the Employee captures the submitted week and employee name for approval verification
       And the Employee captures the total submitted hours for verification

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager approves the timesheet request
       Then the timesheet request status should be displayed as Approved


    Examples:
     | Client   |  Project  | Manager|
     | Nforce     |  Pulse  | AutomationTestUser  |
