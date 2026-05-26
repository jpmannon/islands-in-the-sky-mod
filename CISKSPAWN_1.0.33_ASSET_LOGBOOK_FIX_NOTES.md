# CISKSPAWN 1.0.33 — Asset + Logbook Fix Pass

## Changed
- Replaced Azerion texture with uploaded `rook.png`.
- Replaced Scoria geometry with uploaded `Scoria.geo(1).json`.
- Reduced Scoria render scale from 0.96 to 0.72 for testing.
- Raised Azerion render position and reduced his render scale slightly.
- Expanded Crew Logbook into a tabbed guidebook-style UI:
  - Crew
  - Relationships
  - Clues
  - Systems
- NPC mood, relationship, and clue information now appears in the logbook pages.

## Still experimental
- Azerion render offset may need another pass after visual testing.
- Scoria hitbox/eye height may still need tuning if the render scale now feels right.

## Test checklist
1. Build succeeds.
2. Game loads.
3. Creative tab appears.
4. Crew Logbook opens.
5. Crew Logbook has Crew / Relationships / Clues / Systems buttons.
6. Each logbook tab changes the displayed text.
7. `/summon ciskspawn:scoria`
8. Scoria uses the updated model.
9. Scoria is not too large.
10. Scoria texture still maps correctly.
11. `/summon ciskspawn:azerion_rook`
12. Azerion uses the new rook texture.
13. Azerion is no longer buried too low.
14. Azerion scale feels acceptable.
15. Azerion UI still opens.
