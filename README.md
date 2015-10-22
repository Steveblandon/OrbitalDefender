# OrbitalDefender
CONCEPT: the player controls a spaceship orbiting Earth. There are incoming waves of asteroids that the player must destroy otherwise they'll eventually destroy the planet or the spaceship itself.

Features:
  - shoot down asteroids
  - use a shield (energy field) to protect the spaceship from incoming asteroids
  - HUD with info for score, wave (level), energy usage, and to activate laser or shield
  - levels
  - score readouts at the end of every level and score upkeep

Contemplated Features:
  - achievements
  - upgrades to laser efficiency (in terms of energy usage), speed, strength
  - upgrades to spaceship rotational movement speed
  - upgrades to other spaceships
  - upgrades to more weapons on spaceships
  - upgrades to shield efficiency (in terms of energy usage), strength, and how long it lasts
  
TO-DO:
 - make laser and shield usage drain energy threshold
 - design levels
 - add sound and music

...


The following is a guideline of the structure of the project for easier navigation:

==============
==HIGH LEVEL== More or less the parts that make the actual game
==============

TIER 1 CLASSES (GAME OBJECTS):
=========================================
Spaceship - the main player object
	- handles:
		- rotation
		- shooting
		- collision detection (with asteroids)
		- cooldown from shooting
Asteroid - threats that need to be destroyed
	- handles:
		- rotation
		- movement
		- exploding (i.e. how it breaks apart or is destroyed)
Planet - what must be protected
	- handles: 
		- scrolling
		- collision detection (with asteroids)
Laser - what shoots out of the spaceship
	- handles:
		- rotation
		- movement
		- collision detection (with asteroids)
Shield - energy field activated around spaceship
	- handles:
		- fading and stability
		- collision detection (with asteroids)
WorldObject - the mother of all game objects
	- handles:
		- establishes basic properties such as location and visual representation
		- whether a game object is active or not

TIER 2 CLASSES (GAME ObJECTS):
==========================
Explosion - particle effects 
	- handles:
		- how particles are orchestrated to create desired effects
InstantScore - little visual cues on screen
	- handles:
		- notifying user of points gained
		- notifying user of points lost


===================
== GAME ENGINE ==== the components under the hood that makes the game run
===================

PRIMARY CLASSES:
================
LogicControl - *brings everything together, dictates what happens when and how it happens
	- handles:
		- pre-loads game assets (Calls on Manager classes)
		- establishes and initializes game objects (Calls on Manager classes)
		- configures HUD (Calls on UIManager)
		- input
		- logic updates of game objects and managers
		- graphical updates of every visual game object, visual cues, and HUD
		- score readout
CoreView - game loop
	- handles:
		- input detection (Calls on LogicControl)
		- frame rate for rendering graphics and updating game logic (calls on LogicControl)
		- in-game pause
MainActivity - *entry point for the program
	- handles: 
		- starting and stopping game engine when app is closed but not exited or is resumed (Calls on CoreView)
		- full screen

SECONDARY CLASSES (MANAGERS):
=============================
LevelManager - where each game level is designed
	- handles:
		- monitoring each game level for data collection
		- moving to the next level
		- conditions for game over
		- conditions for level completion
UIManager - interface for user to interact with game app by giving it input to act upon, know progress
	- handles:
		- HUD components (info bars, buttons) 
		- what to do when HUD components are interacted with
ScoreManager - 
	- handles:
		- score upkeep
		- score points distribution (how many points are gained and for what)
ObjectManager - holds all currently used game objects, whether active or not
	- handles: 
		- object renewal as to not create new objects unnecessarily
GraphicsManager - holds all currently used graphical assets
	-handles:
		- image loading
		- background scrolling
		- animation
SoundManager - holds all audio to be used (suppose it ought to be called audioManager)
	- handles:
		- sound loading
		- sound playing

MISC. CLASSES:
==============
ScreenSize - compatibility class
	- handles:
		- getting display parameters (width and height)
