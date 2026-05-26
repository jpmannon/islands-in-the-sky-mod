# Mortimer 1.0.9 Seating Notes

Changes:
- Lowered Mortimer while sitting/passenger-rendered.
- Locked yaw while seated to stop spinning.
- Added reflection call to Create SeatBlock.sitDown for create:*_seat blocks.
- Fallback visual sit remains for non-Create chair blocks.

Create Aeronautics / moving ships:
Create seats normally create a SeatEntity in the real world when used. Moving contraptions may store block positions in contraption-local coordinates rather than normal world block coordinates. If Create Aeronautics moves ships into a contraption/sublevel/physics space, a world-space block scan can miss seats even when the player sees them nearby. The next step is to inspect the entity scan output while standing on a moving ship and/or inspect Create Aeronautics classes if available.
