# Hnefatafl AI

This package implements the Hnefatafl AI for Player vs. Computer games.

## AIStrategy
This is the basic interface that all AI strategies must implement.

## RandomStrategy
A basic implementation of the `AIStrategy` interface that simply randomly selects between the moves provided.

## MinimaxStrategy
An implementation of `AIStrategy` that uses a minimax search to pick the best action. It limits it's depth and uses a naive `eval` function at the leaves. Alpha-Beta pruning is currently implemented.
