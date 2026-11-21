@Sanity
Feature: TimesheetUpdateTimeEntriesAfterRejectionMultipleProjectsSameManager functionality in NforcePulse

	@SubmittingTimesheet @TimesheetUpdateTimeEntriesAfterRejectionMultipleProjectsSameManager
      @Sanity05
	Scenario Outline: Verify the employee can update the time entries of one rejected project out of multiple submitted to the same manager, and the manager can approve the resubmission
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
        | Monday    | 4     | 00      | Development work     |
        | Tuesday   | 5     | 00      | Testing new features |
        | Wednesday | 3     | 00      | Code review          |
        | Thursday  | 6     | 00      | Bug fixing           |
        | Friday    | 2     | 00      | Documentation work   |

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
       And the Manager clicks the reject icon for project "<Project1>"
       And the Manager enters rejection comments "Please correct the work hours"
       And the Manager rejects the timesheet request
       Then the timesheet request status should be displayed as Rejected
       And the Manager approves the timesheet request for project "<Project2>"
       Then the timesheet request status should be displayed as Approved
       And the Manager closes the review popup
       And the Manager logs out of the application

       When the user enters valid credentials
       Then the user should be navigated to the dashboard
       And the Employee navigates to the Time Entries page
       And the Employee navigates to the previously submitted week
       And the Employee selects Actions for project "<Project1>"
       And the Employee selects Update
       And the Employee enter the following time entries and Notes for project "<Project1>"
       | Day       | Hours | Minutes | Notes                    |
       | Monday    | 4     | 00      | Updated development work |

       And the Employee select Submit for project "<Project1>"
       And the Employee timesheet is successfully submitted and displayed as Submitted

       When the Employee logs out of the application
       And the Manager logs in with valid credentials
       And the Manager switches to the Team Workspace
       Then the Manager navigates to the Approvals tab
       And the Manager navigates to the Re-Submitted tab
       And the Manager locates the pending request for the captured week and employee name
       And the Manager clicks the review icon for that request
       And the Manager approves the timesheet request for project "<Project1>"
       Then the timesheet request status should be displayed as Approved


    Examples:
     | Client1 | Project1 | Manager1            | Client2 | Project2 | Manager2            |
     | Nforce  | Pulse    | AutomationTestUser  | Nforce  | Sync     | AutomationTestUser  |

