# Design system

The look of the web app: dark by default with a matching light mode, flat panels and one lime accent. Why: [[adr/0002-control-room-design-system]]. The tokens are in `frontend/src/app/globals.css`.

## Colour

| Role          | Dark      | Light     | Use                                                               |
| ------------- | --------- | --------- | ----------------------------------------------------------------- |
| Canvas        | `#000000` | `#f4f4f4` | Page background.                                                  |
| Panel         | `#161616` | `#ffffff` | Cards.                                                            |
| Muted surface | `#1f1f1f` | `#ebebeb` | Inputs, the account card, rows inside cards.                      |
| Text          | `#ffffff` | `#0e0e0e` | Headlines and body.                                               |
| Muted text    | `#a3a3a3` | `#5c5c5c` | Descriptions and labels.                                          |
| Lime          | `#e6ff5c` | `#e6ff5c` | Primary buttons, the current page in the sidebar and status dots. |

Lime is the only accent. Errors use an icon, wording and a muted surface rather than red.

## Type

Schibsted Grotesk for everything, JetBrains Mono for technical labels, sizes and code. Greek text the model read is set in Noto Serif (`font-greek`), since Schibsted Grotesk has no Greek letters. So is the app's name, Λέξις (`Wordmark`).

## Surfaces

- No borders and no shadows on containers. They're separated by background: canvas, then panel, then muted surface.
- Corners are 8px on buttons, 16px on inputs and 20px on cards.
- Keyboard focus still shows a ring.

## Layout

- The dashboard pages share a sidebar: the app's pages at the top; Docs, Source, the theme switch and the account card at the bottom. Guests see Sign in in place of the account card. Docs and Source are for the team, not for people reading pages, so they stay small.
- Collapsed, the sidebar shows icons with their names on hover, and `T4` in place of the name. Below 1024px it is always collapsed.
