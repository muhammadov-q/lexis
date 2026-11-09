# Lexis docs

Notes on how Lexis is built and how we work. Open `docs/` as a vault in Obsidian for the graph view, or read the files on GitHub.

The notes only describe what exists or what the team has agreed on. Add to them in the PR that changes the thing they describe.

| Folder                                | What goes here                                       |
| ------------------------------------- | ---------------------------------------------------- |
| [[architecture/README\|architecture]] | Stack, how the parts connect, patterns, tests.       |
| [[workflow/README\|workflow]]         | Issues, branches, pull requests, Definition of Done. |
| [[ml/README\|ml]]                     | Tasks, datasets and evaluation, once we pick them.   |
| [[models/README\|models]]             | One model card per model we use.                     |
| [[adr/README\|adr]]                   | Decisions that are hard to undo.                     |
| [[meetings/README\|meetings]]         | Notes from the weekly meetings.                      |

New here? Start with [[onboarding]]. What the course asks for is in [[requirements]].

## Conventions

- One note per topic, named `lowercase-kebab.md`. Each folder's index is its `README.md`.
- Link notes with `[[wikilinks]]` from the vault root, like `[[architecture/overview]]`. Only link notes that exist.
- Put source files in inline code and name the function or class, not a line number.
- Draw diagrams in Mermaid. GitHub and Obsidian both render it.

Run `python3 docs/check-docs.py` after editing. It fails on broken wikilinks, line-number citations and source files that no longer exist. The pre-push hook and CI run it too.
