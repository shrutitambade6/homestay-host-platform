# Task 9 – Selenium Test Plan

**Project:** Containerized Homestay Host Management Platform · Shruti Tambade (23102A0072)

## 1. Scope and approach

| Item | Decision |
|---|---|
| Tool | Selenium WebDriver 4.25 (Java), JUnit 5, AssertJ |
| Browsers | Chrome (default), Edge, Firefox: `-Dbrowser=chrome\|edge\|firefox` |
| Mode | Headless by default (`-Dheadless=false` to watch) |
| Driver management | Selenium Manager downloads the matching driver automatically |
| Environment | `@SpringBootTest(RANDOM_PORT)` starts the app with a fresh in-memory DB and demo data; or point at a deployed app with `-Dapp.baseUrl=http://localhost:8080/homestay` |
| Execution | Maven Failsafe in profile `selenium`: `mvn verify -Pselenium` |
| Locators | Stable `id` attributes added to every element under test |
| Waits | Explicit `WebDriverWait` (10 s); no `Thread.sleep` |
| Failure evidence | `ScreenshotOnFailureExtension` saves `target/selenium-screenshots/<Class>_<test>_<time>.png` before the browser closes |
| Reports | `target/failsafe-reports/*.xml` (published by Jenkins JUnit) |

## 2. Critical user journeys

| # | Journey | Test class / method | Why critical |
|---|---|---|---|
| J1 | Authentication | `LoginJourneyIT.validLoginShowsDashboard`, `invalidLoginShowsError`, `protectedPageRedirects` | Every user starts here; protects data |
| J2 | Create a booking | `BookingManagementIT.createBooking`, `validationErrors` | Core business transaction |
| J3 | Update and search | `BookingManagementIT.editAndSearch` | Daily lookup task for hosts |
| J4 | Role-based workflow | `StatusWorkflowIT.roleBasedWorkflow` | Business rules + security across two roles |
| J5 | Dashboard and permissions | `DashboardIT.dashboardSummary`, `propertyPermissions` | Management view; role restriction |

## 3. Test cases

| TC | Steps | Test data | Expected result (assertion) |
|---|---|---|---|
| TC-01 | Open /login, enter valid credentials, submit | host / host123 | Title "Dashboard"; user "host"; role chip "HOST"; logout returns to /login |
| TC-02 | Enter wrong password | host / wrong-password | "Invalid username or password"; URL contains `/login?error` |
| TC-03 | Open /bookings while logged out | – | Redirected to /login |
| TC-04 | Staff fills in the new-booking form and saves | "Selenium Guest <timestamp>", 9123456789, first property, today+20 → today+22, 2 guests | Ref starts with "HS-"; success flash contains ref; status "Pending"; "2 night(s)" |
| TC-05 | Submit an invalid booking | phone 12345; check-out before check-in | "fix the highlighted fields"; phone error "10 digits"; date error "after check-in" |
| TC-06 | Host creates, edits phone, searches by name + status | phone 9000000001 → 9000000002; filter Pending then Cancelled | "updated" flash; new phone shown; "1 booking(s)" with matching ref; Cancelled filter → "No bookings" |
| TC-07 | Staff creates; host confirms; staff checks in and out | two logins | Staff sees no Confirm/Cancel; status goes Pending → Confirmed → Checked in → Checked out; Edit hidden after check-in; "No status changes"; last updated by "staff" |
| TC-08 | Admin opens the dashboard and clicks the Pending count | admin / admin123 | Total ≥ 6; revenue starts with ₹; occupancy ends with %; version shown; list count equals the tile count |
| TC-09 | Staff opens Properties; host adds a property | "Selenium Retreat <timestamp>", Nashik, 2 rooms, ₹1999 | Staff has no "Add property" button; host's new property is listed with a success message |

Test data is unique per run (timestamp suffix), so tests are independent and can run in any order.

## 4. Entry / exit criteria

- **Entry:** build compiles; unit tests pass; Chrome or Edge installed.
- **Exit:** 9/9 pass. Any failure blocks deployment in Jenkins (Task 10).

## 5. Commands

```powershell
mvn verify -Pselenium                                   # all journeys, headless Chrome
mvn verify -Pselenium -Dit.test=StatusWorkflowIT        # one class
mvn verify -Pselenium -Dheadless=false -Dbrowser=edge   # visible Edge
mvn verify -Pselenium -Dapp.baseUrl=http://localhost:8080/homestay   # against Tomcat
```
