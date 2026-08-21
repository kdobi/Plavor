# GitHub Quality Gates

This project uses repository files for CI and maintenance automation, plus GitHub settings for review enforcement.

## Files Added

- `.github/workflows/ci.yml`: runs backend tests and frontend lint/build on pull requests and pushes to `main`.
- `.github/workflows/codeql.yml`: runs CodeQL security analysis for Java and TypeScript.
- `.github/dependabot.yml`: opens dependency update pull requests for GitHub Actions, Gradle, npm, and backend Docker image dependencies.
- `.github/pull_request_template.md`: standard pull request checklist.
- `.github/CODEOWNERS`: requests review from `@kdobi`.
- `.github/copilot-instructions.md`: gives GitHub Copilot project-specific review and coding context.

## GitHub Settings To Enable

Open the GitHub repository and configure these manually:

1. Go to `Settings -> Code security and analysis`.
2. Enable Dependabot alerts.
3. Enable Dependabot security updates.
4. Enable Code scanning if it is not already enabled.
5. Go to `Settings -> Rules -> Rulesets`.
6. Create a ruleset for the `main` branch.
7. Enable pull request requirement before merge.
8. Require status checks to pass before merge:
   - `Backend Test`
   - `Frontend Lint And Build`
   - `Analyze java-kotlin`
   - `Analyze javascript-typescript`
9. Require conversation resolution before merge.
10. Enable GitHub Copilot code review for pull requests if your plan and repository settings support it.

## Suggested Merge Flow

1. Create a feature branch.
2. Open a pull request.
3. Wait for CI, CodeQL, Dependabot compatibility, and Copilot review.
4. Fix comments and failing checks.
5. Merge only after all required checks pass.

Automatic home-server deployment is intentionally not enabled yet. Add CD after the first real product API and frontend integration are stable.
