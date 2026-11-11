# Architecture decision records

Short notes on decisions that are hard to undo. Name them `NNNN-kebab-title.md` with the next number and keep three sections: Context, Decision, Consequences. To change a decision, write a new ADR that supersedes the old one.

| ADR                                     | Decision                                                             | Status   |
| --------------------------------------- | -------------------------------------------------------------------- | -------- |
| [[adr/0001-web-frontend-stack]]         | Next.js web app with a same-origin proxy and generated API types     | accepted |
| [[adr/0002-control-room-design-system]] | Dark control-room design with a light mode                           | accepted |
| [[adr/0003-sqlite-database]]            | SQLite with SQLAlchemy and Alembic, migrated when the backend starts | accepted |
| [[adr/0004-page-images-in-sqlite]]      | Page images for the history are stored in the database               | accepted |
| [[adr/0005-claude-code-reader]]         | Claude Code reads and translates pages on the user's own computer    | accepted |
