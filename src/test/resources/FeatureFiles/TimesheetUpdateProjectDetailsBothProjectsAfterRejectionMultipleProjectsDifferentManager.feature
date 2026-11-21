@Sanity
Feature: TimesheetUpdateProjectDetailsBothProjectsAfterRejectionMultipleProjectsDifferentManager functionality in NforcePulse

	@SubmittingTimesheet @TimesheetUpdateProjectDetailsBothProjectsAfterRejectionMultipleProjectsDifferentManager
      @SanityTesting23
	Scenario Outline: Verify the employee can correct the project details of both rejected projects submitted to different managers, without touching their time entries, and each manager approves their own resubmission independently
		Given the user launches the NforcePulse application
		When the user enters valid credentials
		Then the user should be navigated to the dashboard
		And the Employee navigates to the Time Entries page
        And the Employee navigates to the next available empty time grid
        And the Employee selects Client "<Client>"
        And the Employee selects Project "<WrongProject1>"
        And the Employee selects Manager "<Manager1>"
        And the Employee clicks Add Record Button
        And the Employee enter the following time entries and Notes
        | Day       | Hours | Minutes | Notes                |
        | Monday    | 4     | 00      | Pulse Monday work    |
        | Tuesday   | 5     | 00      | Pulse Tuesday work   |
        | Wednesday | 3     | 00      | Pulse Wednesday work |
        | Thursday  | 6     | 00      | Pulse Thursday work  |
        | Friday    | 2     | 00      | Pulse Friday work    |

       And the Employee selects Actions
       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted

       And the Employee selects Client "<Client>"
       And the Employee selects Project "<WrongProject2>"
       And the Employee selects Manager "<Manager2>"
       And the Employee clicks Add Record Button
       And the Employee enter the following time entries and Notes
        | Day       | Hours | Minutes | Notes               |
        | Monday    | 4     | 00      | Sync Monday work    |
        | Tuesday   | 3     | 00      | Sync Tuesday work   |
        | Wednesday | 5     | 00      | Sync Wednesday work |
        | Thursday  | 2     | 00      | Sync Thursday work  |
        | Friday    | 6     | 00      | Sync Friday work    |

       And the Employee selects Actions
       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted
       And the Employee captures the submitted week and employee name for approval verification

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager clicks the reject icon for that request
       And the Manager enters rejection comments "Please correct the project details"
       And the Manager rejects the timesheet request
       Then the timesheet request status should be displayed as Rejected
       And the Manager closes the review popup
       And the Manager logs out of the application

       And the Second Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager clicks the reject icon for that request
       And the Manager enters rejection comments "Please correct the project details"
       And the Manager rejects the timesheet request
       Then the timesheet request status should be displayed as Rejected
       And the Manager closes the review popup
       And the Manager logs out of the application

       When the user enters valid credentials
       Then the user should be navigated to the dashboard
       And the Employee navigates to the Time Entries page
       And the Employee navigates to the previously submitted week
       And the Employee selects Actions for project "<WrongProject1>"
       And the Employee selects Update
       Then the Employee Select the submission confirmation popup
       And the Employee clicks the edit icon in Project Details
       And the Employee updates Client to "<Client>" in Project Details
       And the Employee updates Project to "<CorrectProject1>" in Project Details
       And the Employee saves the Project Details

       And the Employee selects Actions for project "<CorrectProject1>"
       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted

       And the Employee selects Actions for project "<WrongProject2>"
       And the Employee selects Update
       Then the Employee Select the submission confirmation popup
       And the Employee clicks the edit icon in Project Details
       And the Employee updates Client to "<Client>" in Project Details
       And the Employee updates Project to "<CorrectProject2>" in Project Details
       And the Employee saves the Project Details

       And the Employee selects Actions for project "<CorrectProject2>"
       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager navigates to the Re-Submitted tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager approves the timesheet request
       Then the timesheet request status should be displayed as Approved
       And the Manager closes the review popup
       And the Manager logs out of the application

       And the Second Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager navigates to the Re-Submitted tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager approves the timesheet request
       Then the timesheet request status should be displayed as Approved
       And the Manager closes the review popup


    Examples:
     | Client | WrongProject1 | CorrectProject1 | WrongProject2 | CorrectProject2 | Manager1            | Manager2          |
     | Nforce | Sync          | Tracktion        | Pulse          | Sync             | AutomationTestUser  | AutomationTester  |
