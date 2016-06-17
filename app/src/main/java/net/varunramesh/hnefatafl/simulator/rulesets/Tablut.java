package net.varunramesh.hnefatafl.simulator.rulesets;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.Grid;
import net.varunramesh.hnefatafl.simulator.History;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by varunramesh on 4/18/16.
 */
public final class Tablut implements Ruleset {
    private static final String TAG = "Tablut";
    public static final int BOARD_SIZE = 9;

    @Override
    public Board getStartingConfiguration() {
        Grid grid = new Grid(BOARD_SIZE)
                .add(new Position(4, 4), Piece.KING)

                .add(new Position(4, 5), Piece.DEFENDER)
                .add(new Position(4, 6), Piece.DEFENDER)
                .add(new Position(4, 3), Piece.DEFENDER)
                .add(new Position(4, 2), Piece.DEFENDER)
                .add(new Position(5, 4), Piece.DEFENDER)
                .add(new Position(6, 4), Piece.DEFENDER)
                .add(new Position(3, 4), Piece.DEFENDER)
                .add(new Position(2, 4), Piece.DEFENDER)

                .add(new Position(0, 3), Piece.ATTACKER)
                .add(new Position(0, 4), Piece.ATTACKER)
                .add(new Position(1, 4), Piece.ATTACKER)
                .add(new Position(0, 5), Piece.ATTACKER)

                .add(new Position(3, 0), Piece.ATTACKER)
                .add(new Position(4, 0), Piece.ATTACKER)
                .add(new Position(4, 1), Piece.ATTACKER)
                .add(new Position(5, 0), Piece.ATTACKER)

                .add(new Position(3, 8), Piece.ATTACKER)
                .add(new Position(4, 8), Piece.ATTACKER)
                .add(new Position(4, 7), Piece.ATTACKER)
                .add(new Position(5, 8), Piece.ATTACKER)

                .add(new Position(8, 3), Piece.ATTACKER)
                .add(new Position(8, 4), Piece.ATTACKER)
                .add(new Position(7, 4), Piece.ATTACKER)
                .add(new Position(8, 5), Piece.ATTACKER);

        return new Board(grid, Player.ATTACKER, Winner.UNDETERMINED, BOARD_SIZE);
    }

    @Override
    public String getRulesetName() {
        return "Tablut Rules";
    }

    @Override
    public String getRulesHTML() {
        return "file:///android_asset/rules/tablut.html";
    }

    @Override
    public Board step(History history, Action action, EventHandler eventHandler) {
        return history.getCurrentBoard();
    }

    @Override
    public List<Action> getActions(History history) {
        return new ArrayList<Action>();
    }

    @Override
    public int getAISearchDepth() {
        return 4;
    }
}
