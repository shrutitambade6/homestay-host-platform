# Contributing – Branch Policy and Commit Rules (Task 4)

## Branches

| Branch | Purpose | Rules |
|---|---|---|
| `main` | Released, production-ready code | Protected. Changes only via PR from `develop`. Every merge is tagged `vX.Y.Z`. |
| `develop` | Integration branch for the next release | Protected. Changes only via PR from `feature/*`, `fix/*`, `ci/*`. Jenkins must be green. |
| `feature/<short-name>` | One backlog item / user story | Branch from `develop`, e.g. `feature/booking-management` |
| `fix/<short-name>` | Bug fix | Branch from `develop`, e.g. `fix/checkout-date-validation` |
| `ci/<short-name>` | Pipeline / Docker / Ansible changes | Branch from `develop` |
| `hotfix/<short-name>` | Urgent fix on a release | Branch from `main`, merge to `main` **and** `develop` |

Naming: lowercase, words separated by `-`, optionally with the issue number: `feature/12-status-workflow`.

## Commit messages (Conventional Commits)

```
<type>(optional scope): <short summary in present tense>

feat(booking): add search by phone and status
fix(workflow): block check-out before check-in
test(selenium): add role-based workflow journey
ci(jenkins): publish failsafe reports
docs: add architecture diagram
chore: update .gitignore
```

Types: `feat`, `fix`, `docs`, `test`, `ci`, `build`, `refactor`, `chore`.

## Pull requests

1. Push your branch and open a PR into `develop`.
2. Fill in the PR template (what changed, linked issue `Closes #N`, test evidence).
3. Jenkins build must pass; resolve all review comments.
4. Use **"Create a merge commit"** so the branch history stays visible.
5. Delete the feature branch after merging.

## Releases

- Merge `develop` → `main` via PR, then tag: `git tag -a v1.0.0 -m "MVP release"` and `git push origin v1.0.0`.
- Versioning: `MAJOR.MINOR.PATCH`. Docker images use `1.0.<jenkins build number>`.

## Suggested GitHub branch protection (Settings → Branches)

- `main` and `develop`: require a pull request before merging, require conversation resolution, and block force pushes.
