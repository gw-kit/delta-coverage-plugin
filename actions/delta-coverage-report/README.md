# Delta Coverage Report GitHub Action

The action renders a delta coverage report in the pull request comment. 
Also, the report is rendered in the workflow summary.

## Inputs

- `title` (optional): The title of the comment. Default is `Delta Coverage Report`.
- `message` (optional): The message to display in the comment. Default is `''`(empty).
- `delta-coverage-report`: The path to the delta coverage markdown report file.
- `github-token` (optional): The GitHub token to use for authentication.

If `title` is not blank then the previous comment generated by this action will be updated with the new report,
otherwise a new comment will be created.

## Pre-requisites

Required permissions:
- issues: `read`
- pull-requests: `write`


## Example usage

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    permissions:
      issues: read
      pull-requests: write
          
    steps:
      - name: Post comment
        uses: gw-kit/delta-coverage-plugin/actions/delta-coverage-report@main
        with:
          title: 'Delta Coverage Report'
          delta-coverage-report: 'build/reports/coverage-reports/delta-coverage/report.md'
          github-token: ${{ secrets.GITHUB_TOKEN }}
```
