# 5. Claude Code as an optional reader

- Status: accepted
- Date: 2026-11-08
- Related: [[models/claude-code]], [[models/kraken-eparchos]]

## Context

kraken reads about one letter in twelve wrong on Eparchos and gives no translation. The glossary only covers one page. Claude reads Greek and can translate it, but the team has no budget for API keys. Team members do have Claude Code, and the course staff agreed we can use it while the project runs on our own computers.

## Decision

- Add a `claude-code` model that runs the `claude` command on the computer that runs the backend, with that person's Claude Code login.
- kraken still finds the lines and word boxes. Claude reads the words and translates them.
- Claude Code gets no tools. The page goes inside the request, so it can't open files or run commands.
- The model is only listed when kraken works and `claude` is installed.

## Consequences

- No API key or bill. Each page uses the plan's usage limits.
- One person's login can only serve that person. A shared server would need an API key and a predictor that calls the Claude API directly.
- Pages leave the computer.
- A page takes one to two minutes, so the mobile app's 15 second timeout cuts it off.
- CI never calls Claude Code. Its tests replace it with a fake.
