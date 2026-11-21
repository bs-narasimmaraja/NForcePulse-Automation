@Sanity
Feature: TimesheetUpdateTimeEntriesAfterRejection functionality in NforcePulse

	@SubmittingTimesheet @TimesheetUpdateTimeEntriesAfterRejection
      @Sanity01
	Scenario Outline: Verify the employee can update the time entries of a rejected timesheet and resubmit it, and the manager can approve the resubmission
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

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       Then the Manager navigates to the Approvals tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager clicks the reject icon for that request
       And the Manager enters rejection comments "Please correct the work hours"
       And the Manager rejects the timesheet request
       Then the timesheet request status should be displayed as Rejected
       And the Manager closes the review popup
       And the Manager logs out of the application

       When the user enters valid credentials
       Then the user should be navigated to the dashboard
       And the Employee navigates to the Time Entries page
       And the Employee navigates to the previously submitted week
       And the Employee selects Actions
       And the Employee selects Update
       Then the Employee Select the submission confirmation popup
       And the Employee enter the following time entries and Notes
       | Day       | Hours | Minutes | Notes                    |
       | Monday    | 8     | 00      | Updated development work |

       And the Employee selects Actions
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


    Examples:
     | Client | Project | Manager             |
     | Nforce | Pulse   | AutomationTestUser  |
