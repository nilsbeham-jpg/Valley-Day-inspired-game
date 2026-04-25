# Valley Day 🌱

A tile-based farming & survival game built with Java and LibGDX.

Valley Day is inspired by games like *Stardew Valley* and combines farming, resource management, exploration, and survival mechanics in a dynamic tile-based environment.

The player must plant and harvest crops to unlock the exit while defending their farm against wildlife and managing limited tools.

---

## Gameplay Preview
<img width="1051" height="589" alt="image" src="https://github.com/user-attachments/assets/a3239f7d-da5d-46c1-9c5e-3b5ab76bef64" />
<img width="978" height="758" alt="image" src="https://github.com/user-attachments/assets/08fb2599-9ce8-4f18-943a-1699a7a436d9" />




---

## Tech Stack

- Java
- LibGDX
- Gradle
- Scene2D UI
- Box2D Physics

---

## Core Features

### Farming System
- Plant crops on valid farmland tiles
- Multiple crop types with different growth speeds and rewards
- Crops progress through growth stages
- Crops can rot if neglected
- Harvesting crops contributes toward unlocking the exit

### Wildlife System
Two wildlife types dynamically spawn and attack your farm:

- **Chicken Visitor**
  - aggressive
  - attacks crops
  - scares the player

- **Snail Visitor**
  - non-aggressive
  - slowly consumes crops

Wildlife uses:
- BFS pathfinding
- tile-based movement
- respawn logic
- crop targeting behavior

### Tool & Item System
Players can discover and use tools hidden beneath debris:

- Shovel
- Fertilizer
- Watering Can
- Scaffold

These tools create strategic tradeoffs between farming efficiency and survival.

### Fog of War
- Limited player vision
- Previously explored areas remain partially visible
- Wildlife only appears inside visible range

### Difficulty Modes
- Easy
- Normal
- Hard

Difficulty affects:
- wildlife speed
- wildlife count
- required harvest quota

### Star-Based Scoring
Players receive 1–3 stars based on completion speed.

---

## Technical Highlights

This project was built with a modular architecture that separates gameplay logic, rendering, and assets.

### Project Structure

- `core/` → game logic
- `desktop/` → desktop launcher
- `assets/` → textures, audio, animations
- `maps/` → map configurations

### Architecture Highlights

- Object-oriented entity hierarchy
- Interface-based rendering abstractions
- Separate HUD system using Scene2D
- Modular map system
- Event-driven game progression
- Reusable wildlife AI system

---

## Controls

| Key | Action |
|------|----------|
| Arrow Keys | Move player |
| A | Plant / Harvest |
| D | Interact |
| S | Shoot wildlife |
| Q | Switch tool |
| E | Use selected tool |
| ESC | Pause |
| ENTER | Return to menu |

---

## How to Run

### Requirements
- Java 17+
- Gradle

### Run locally

```bash
./gradlew desktop:run
