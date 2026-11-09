# Issues

All work is a GitHub issue on `muhammadov-q/lexis`, grouped into milestones. Every PR closes one issue ([[workflow/git-workflow]]).

The issue form (`.github/ISSUE_TEMPLATE/task.yml`) has three parts:

- What: what exists or changes when it's done. For a user story: "As a <user>, I want <goal> so that <benefit>."
- Why: the problem it solves. One or two sentences.
- Definition of Done: the acceptance tests first, then the defaults from [[workflow/definition-of-done]].

## Editing an issue from the terminal

`gh issue edit` and `gh pr edit` fail on this repo with a GraphQL error about Projects (classic). Use the REST API instead:

```bash
gh api -X PATCH repos/muhammadov-q/lexis/issues/<n> -F body=@body.md
```
