# Valley Day 🌱

A tile-based farming & survival game built with libGDX

---

## 1. Project Overview

**Valley Day** is a tile-based farming and survival game built using libGDX.  
The player must plant and harvest crops to unlock the exit while dealing with wildlife that interferes with progress.

The project focuses on a clean separation between:
- game logic (map, player, wildlife, crops),
- rendering (screens, HUD),
- and assets (textures, animations, audio).

---



## 2. How to Run the Game

### Requirements
- Java **17+**
- Gradle
- Desktop environment (libGDX desktop)



### Running the Game (E.g in IntelliJ IDEA)

The game is can be run in  IntelliJ  via Run Configuration.

To run the game:
1. Open the project in IntelliJ IDEA
2. Select the run configuration:
3. Click the green Run ▶ button

This configuration internally launches the libGDX desktop backend.


---

## 3. Controls

| Key        | Action                                               |
|------------|------------------------------------------------------|
| Arrow Keys | Move player                                          |
| **a**      | Plant / Harvest / Clear rotten crops                 |
| **d**      | Interact with terrain (destroy debris, reveal items) |
| **s**      | Shot wildlife in front of the player                 |
| **ESC**    | Pause / return to menu                               |
| **ENTER**  | Return to menu after win/lose                        |

---

## 4. Game Mechanics 

### 4.1 Crops 
- Crops can only be planted on farmland tiles (light green), not on path ways (dark green)
- Crops grow through multiple stages
- Mature crops can be harvested for points
- Crops rot if ignored for too long
- Rotten crops must be manually cleared
- Crop types:
- `BasicCrop` – fast growth, low value
- `SlowCrop` – slower growth, medium value
- `PremiumCrop` – slowest growth, high value


### 4.2 Exit & Win Condition 
- The exit is locked at the start of the level
- Harvesting crops increases a global counter
- Once the exit quota (difficulty-dependent) is reached:
- the exit unlocks
- stepping on the exit wins the game

### 4.3 Wildlife 
- Wildlife spawns dynamically and is limited by difficulty
- Implemented wildlife:
- `ChickenVisitor` (Has intelligent movement, attacks player and eats crops)
- `SnailVisitor` (Is peaceful toward the player and just eats crops)
- Wildlife uses tile-based movement and BFS pathfinding
- Wildlife targets mature crops
- Wildlife can be:
- shot away (`s` key),
- blocked using scaffold zones,
- removed temporarily with a respawn cooldown


#### Scared State
If the player collides with dangerous wildlife:
- the player enters a scared state
- player control is overridden
- the player automatically runs toward the nearest map edge
- leaving the map results in game over
- this avoids instant death and increases tension

### 4.4 Items 
Items are usually hidden under debris and revealed by interaction.

Implemented items:
- **Shovel** – reduces interaction time and allows destroying rock debris
- **Fertilizer** – speeds up crop growth temporarily
- **Watering Can** – prevents rotting and restores crops
- **Scaffold** – blocks wildlife movement & kills wildlife within the zone if present at activation

### 4.5 Fog of War 
- Player vision is limited to a fixed radius
- Tiles outside vision are darkened
- Previously explored tiles remain partially visible
- Wildlife is only rendered inside the current vision radius

---

## 5. Difficulty System

Defined in `Difficulty.java`.

| Difficulty | Max Wildlife | Wildlife Speed | Exit Quota |
|-----------|-------------|----------------|------------|
| EASY | 2 | slower | 15 |
| NORMAL | 3 | normal | 30 |
| HARD | 5 | aggressive | 50 |

Difficulty affects:
- maximum wildlife count
- wildlife movement speed
- number of crops required to unlock the exit

---


## 6. Bonus Implementations

The following features go beyond the minimal requirements and were implemented to increase gameplay depth and complexity:

- **Intelligent Wildlife Movement**  
  Wildlife uses a tile-based movement algorithm with pathfinding to actively move toward targets such as crops and the player, instead of random wandering.

  
- **Multiple Difficulty Modes**  
  The game offers *Easy*, *Normal*, and *Hard* modes, which affect:
    - the maximum number of wildlife entities,
    - wildlife movement speed,
    - and the number of crops required to unlock the exit.


- **Star-Based Scoring System**  
  Upon winning the game, the player is awarded a star rating based on time efficiency:
    - 3 stars if the level is completed in the first third of the available time,
    - 2 stars in the second third,
    - 1 star in the final third.


- **Additional Terrain Type: Rock Debris**  
  Besides normal debris, approximately **5%** of debris tiles are *rock debris*, which can only be removed using a shovel, adding an extra strategic constraint.


- **Multiple Wildlife Types**  
  Two different wildlife visitors are implemented:
    - **Chicken** – faster, aggressive, and able to scare the player,
    - **Snail** – slower, non-aggressive, and does not attack the player.  
Both can damage crops, but differ in speed and behavior.


- **Multiple Crop Species**  
  Different crop types exist with varying growth speeds and harvest values.  
  When planting (`a` key), the crop type is chosen randomly, introducing variability and risk–reward decisions.


- **Scaffold Item (Wildlife Control)**  
  The scaffold item creates a zone that blocks wildlife movement.  
  Wildlife can not enter this area. Already present wildlife is killed. 


- **Fog of War System**  
  Player vision is limited to a fixed radius.  
  Areas previously visited remain partially visible, while unexplored areas stay hidden.


- **Wildlife Respawn System**  
  Defeated wildlife does not disappear permanently.  
  After a cooldown period and at a safe distance from the player, new wildlife entities respawn, maintaining continuous pressure throughout the game.


- **Additional Sound Effects**  
  The game includes multiple sound effects beyond the minimum requirements, such as:
    - walking sounds,
    - interacting with wildlife sounds,
    - planting sounds


---

## 7. Code Structure & Class Hierarchy

The project is organized into clearly separated packages that reflect their responsibilities in the game architecture.  
Inheritance and interfaces are used explicitly to model shared behavior and allow extensibility.

This section gives an overview of the most important class hierarchies and their relationships.

---

### 7.1 Core World Abstractions

At the base of the world logic are generic classes representing entities that exist on the map.


- **GameObject**  
  Base class for world-related entities.  
  Stores shared properties such as position and identity.

- **TileObject** *(extends GameObject)*  
  Represents any object that occupies a tile on the map.  
  This includes terrain, items, structures, and interactable objects.

- **Tile**  
  Represents a single grid cell of the map.  
  A tile can contain:
    - a soil type,
    - a `TileObject`,
    - an optional crop,
    - and optional hidden content.

---

### 7.2 Crops (`map.crops`)

Crop behavior is modeled using an abstract base type with concrete implementations.


- **CropType**  
  Defines common crop properties such as growth time, stages, and harvest value.
- **BasicCrop / SlowCrop / PremiumCrop**  
  Concrete crop types with different growth speeds and rewards.

Each tile may hold a **CropTile**, which manages the current growth stage and timers of a planted crop.

---

### 7.3 Items (`map.Items`)

Items are tile-based objects that the player can collect or activate.


- **Item** *(extends TileObject)*  
  Base class for all usable or collectable items.
- Concrete items apply temporary or permanent effects to the player or the game world.

---

### 7.4 Terrain (`map.terrain`)

Terrain objects define movement constraints and interaction rules.


- **Debris** blocks tiles until removed.
- **RockDebris** *(extends Debris)* requires a shovel to clear.
- **Fence** blocks player and wildlife movement.
- **Soil** defines whether crops can be planted.

---

### 7.5 Structures (`map.structures`)



- **Entrance** defines the player spawn position.
- **Exit** represents the level goal and remains locked until the harvest quota is met.

---

### 7.6 Wildlife (`map.Wildlife`)

Wildlife shares common movement and targeting logic through a base class.



- **WildlifeBase**  
  Handles movement, targeting, and crop interaction.
- **ChickenVisitor**  
  Fast and aggressive; can scare the player.
- **SnailVisitor**  
  Slow and non-aggressive; only damages crops.

The `WildlifeBlocker` interface is used by scaffold zones to restrict wildlife movement.

---

### 7.7 Player (`map.player`)


- **Player**
    - Uses Box2D for physics-based movement
    - Handles input, interaction timing, item effects, and scared state
    - Implements `Drawable` for rendering

---

### 7.8 Game Logic & Screens

- **GameMap**  
  Central gameplay controller and single source of truth:
    - owns tiles, crops, wildlife, and player
    - updates all world logic
    - handles interactions and win/lose conditions

- **GameScreen** *(implements libGDX `Screen`)*
    - main gameplay rendering
    - camera control
    - fog-of-war rendering

- **MenuScreen** *(implements libGDX `Screen`)*
    - start, resume, map loading, difficulty selection

- **Hud**
    - UI overlay for time, tools, exit status, and end screens
    - uses its own camera and stage

---

### 7.9 Rendering & Assets

- **Animations**  
  Central registry for all animations (player, chicken, snail).
- **Textures**  
  Static access to loaded textures.
- **Drawable**  
  Interface implemented by all renderable entities.

---

### 7.10 Application Entry Points


- **ValleyDayGame** *(extends libGDX `Game`)*
    - owns global resources (SpriteBatch, Skin, GameMap)
    - manages screen switching
    - stores difficulty and selected map path

- **DesktopLauncher**  
  Desktop-specific entry point used by the IDE to run configuration.

---
