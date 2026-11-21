@Sanity
Feature: SubmittingTimesheet functionality in NforcePulse

	@SubmittingTimesheet
	Scenario Outline: Verify the login functionality in NforcePulse application
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
        | Day       | Hours | Minutes | Notes                 |
        | Monday    | 8     | 00      | Development work     |
        | Tuesday   | 8     | 00      | Testing new features |
        | Wednesday | 8     | 00      | Code review          |
        | Thursday  | 8     | 00      | Bug fixing           |
        | Friday    | 8     | 00      | Documentation work   |

       And the Employee select Submit
       And the Employee timesheet is successfully submitted and displayed as Submitted


    Examples:
     | Client   |  Project  | Manager|
     | Nforce     |  Pulse  | AutomationTestUser  |

