package net.varunramesh.hnefatafl.simulator;

/**
 * This interface represents an event handler for the simulator. Pass an instance
 * of this into the step function, and it will call the appropriate functions when
 * an action needs to happen on the game board.
 */
public interface EventHandler {
    /** Move a piece between the two positions */
    void movePiece(Position from, Position to);
    /** Remove a piece that has been captured */
    void removePiece(Position position);
    /** The game has just been won by the provided player */
    void setWinner(Winner player);
}
