# Simulator Core

This folder contains the set of utilities that perform the Hnefatafl game simulation. Most of the classes here are Immutable. That way, they can be used as keys in Hashtables / Hashsets, serialized easily, and managed without ending up with complex state.

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
  - [Board](#board)
  - [EventHandler](#eventhandler)
  - [Action](#action)
  - [Position](#position)
  - [Direction](#direction)
<!-- END doctoc generated TOC please keep comment here to allow auto update -->

## Board
`Board` is an immutable class that represents the state of a game board at a specific point in time. This encodes the piece locations, as well as the current player. In order to step the game forward, you call `Board#step`, with an Action object that represents what the current player should do. Instead of mutating the current board, this function returns a new Board object. Optionally, you can pass in an `EventHandler` to the step function, in order to hook into various events..

## EventHandler
`EventHandler` is an interface that you should implement to be able to 'hook' into a simulation and actually display changes to the board. This boils down to three functions:
```java
void movePiece(Position from, Position to);
void removePiece(Position position);
void setWinner(Player player);
```
The [HenfataflGame](../game/HnefataflGame.java) class implements this interface, so that it can visually show the pieces moving.

## Action
`Action` is an immutable class that represents a move from one location to another. The action stores the Player who is taking it, and the from/to positions.

When a user has to take a turn `Board#getActions` returns a list of legal Actions for them to take.

One note about actions is that we can reconstruct a game state simply by taking the starting board and replaying all of the actions.

## Position
Simple Immutable class that holds a board position (x and y coordinates).

## Enums

### Direction
Simple enum for `UP`, `DOWN`, `LEFT`, and `RIGHT`. Use Utils.oppositeDirection to flip a direction.

### Piece
Represents `ATTACKER`, `DEFENDER`, and `KING` piece types.

### Player
Represents either the `ATTACKER` or `DEFENDER` player.
