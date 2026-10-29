# =============================================================================
# Task 2/4 - Create the product backlog as GitHub Issues with the GitHub CLI.
# Prerequisites: winget install GitHub.cli ; gh auth login
# Run from the project root:  powershell -ExecutionPolicy Bypass -File scripts\create-backlog-issues.ps1
# =============================================================================
$labels = @(
    @("feature", "1f6a3d"), @("devops", "1d4f9c"), @("test", "8a5a00"),
    @("docs", "4b3f8a"), @("bug", "9c2f1e")
)
foreach ($l in $labels) { gh label create $l[0] --color $l[1] --force | Out-Null }

$stories = @(
    @("US-01: Login with roles (Host, Staff, Admin)", "feature"),
    @("US-02: Host adds and edits properties", "feature"),
    @("US-03: Staff creates a booking with validation", "feature"),
    @("US-04: View booking details", "feature"),
    @("US-05: Update a booking (Pending/Confirmed only)", "feature"),
    @("US-06: Search bookings by name, phone, ref, status, property", "feature"),
    @("US-07: Host confirms or cancels bookings", "feature"),
    @("US-08: Staff checks guests in and out", "feature"),
    @("US-09: Summary dashboard", "feature"),
    @("US-10: Health and version endpoints", "devops"),
    @("US-11: Jenkins CI build on every commit", "devops"),
    @("US-12: Selenium quality gate blocks bad deployments", "test"),
    @("US-13: Versioned Docker image and container deployment", "devops"),
    @("US-14: Ansible node provisioning", "devops"),
    @("US-15: Automatic rollback on failed health check", "devops")
)

foreach ($s in $stories) {
    $body = "See docs/02-agile-plan.md for the user story and acceptance criteria."
    gh issue create --title $s[0] --label $s[1] --body $body
}
Write-Host "Created $($stories.Count) issues. Add them to a GitHub Project (Board view) for the Kanban evidence."
