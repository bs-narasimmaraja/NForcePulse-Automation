@Sanity
Feature: TimesheetApprovalAndRejectionMultipleProjectsSameManager functionality in NforcePulse

	@SubmittingTimesheet @TimesheetApprovalAndRejectionMultipleProjectsSameManager
	Scenario Outline: Verify the timesheet submission for multiple projects under the same manager, with one project approved and the other rejected
		Given the user launches the NforcePulse application
		When the user enters valid credentials
		Then the user should be navigated to the dashboard
		And the Employee navigates to the Time Entries page
        And the Employee navigates to the next available empty time grid
        And the Employee selects Client "<Client1>"
        And the Employee selects Project "<Project1>"
        And the Employee selects Manager "<Manager1>"
        And the Employee clicks Add Record Button
        And the Employee enter the following time entries and Notes
        | Day       | Hours | Minutes | Notes                |
        | Monday    | 4     | 00      | Pulse Monday work    |
        | Tuesday   | 5     | 00      | Pulse Tuesday work   |
        | Wednesday | 3     | 00      | Pulse Wednesday work |
        | Thursday  | 6     | 00      | Pulse Thursday work  |
        | Friday    | 2     | 00      | Pulse Friday work    |

       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted

       And the Employee selects Client "<Client2>"
       And the Employee selects Project "<Project2>"
       And the Employee selects Manager "<Manager2>"
       And the Employee clicks Add Record Button
       And the Employee enter the following time entries and Notes
        | Day       | Hours | Minutes | Notes               |
        | Monday    | 4     | 00      | Sync Monday work    |
        | Tuesday   | 3     | 00      | Sync Tuesday work   |
        | Wednesday | 5     | 00      | Sync Wednesday work |
        | Thursday  | 2     | 00      | Sync Thursday work  |
        | Friday    | 6     | 00      | Sync Friday work    |

       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted
       And the Employee captures the submitted week and employee name for approval verification

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       And the Manager switches to the Team Workspace
       Then the Manager navigates to the Approvals tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager approves the timesheet request for project "<Project1>"
       Then the timesheet request status should be displayed as Approved
       And the Manager clicks the reject icon for project "<Project2>"
       And the Manager enters rejection comments "Please correct the entries and resubmit"
       And the Manager rejects the timesheet request
       Then the timesheet request status should be displayed as Rejected


    Examples:
     | Client1 | Project1 | Manager1            | Client2 | Project2 | Manager2            |
     | Nforce  | Pulse    | AutomationTestUser  | Nforce  | Sync     | AutomationTestUser  |
