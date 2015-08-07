package net.varunramesh.hnefatafl.simulator;

/**
 * Created by Varun on 8/7/2015.
 */
public interface EventHandler {
    void MovePiece(Position from, Position to);
    void RemovePiece(Position position);
    void SetWinner(Player player);
}
