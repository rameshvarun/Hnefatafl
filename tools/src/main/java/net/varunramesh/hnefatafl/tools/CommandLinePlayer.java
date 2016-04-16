package net.varunramesh.hnefatafl.tools;

import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.rulesets.FeltarHnefatafl;
import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Winner;
import net.varunramesh.hnefatafl.simulator.EventHandler;

import net.varunramesh.hnefatafl.ai.MinimaxStrategy;
import net.varunramesh.hnefatafl.ai.RandomStrategy;
import net.varunramesh.hnefatafl.ai.AIStrategy;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by varunramesh on 4/16/16.
 */
public class CommandLinePlayer {
    public static int BOARD_SIZE;
    public static String formatPosition(Position pos) {
        return Character.toString(Character.toChars(65 + pos.getY())[0]) + (BOARD_SIZE - pos.getX());
    }

    public static void main(String[] args) {
        System.out.println("Hnefatafl command-line player.");
        Ruleset ruleset = new FeltarHnefatafl();

        List<Board> history = new LinkedList<Board>();

        Board board = ruleset.getStartingConfiguration();
        BOARD_SIZE = board.getBoardSize();

        history.add(board);
        printBoard(board);


        // AIStrategy attackerStrategy = new MinimaxStrategy(ruleset, Player.ATTACKER, 4);
        AIStrategy attackerStrategy = new RandomStrategy();
        AIStrategy defenderStrategy = new MinimaxStrategy(ruleset, Player.DEFENDER, 4);

        EventHandler eventHandler = new EventHandler() {
            @Override
            public void movePiece(Position from, Position to) {
                System.out.println("> " + formatPosition(from) + " to " + formatPosition(to) + ".");
            }

            @Override
            public void removePiece(Position position) {
                System.out.println("> " + formatPosition(position) + " capture.");
            }

            @Override
            public void setWinner(Winner player) {
                // Ignore...
            }
        };

        while (!board.isOver()) {
            Set<Action> actions = ruleset.getActions(history);

            Action action = null;
            if (board.getCurrentPlayer() == Player.ATTACKER)
                action = attackerStrategy.decide(history, actions);
            else
                action = defenderStrategy.decide(history, actions);

            board = ruleset.step(history, action, eventHandler);
            history.add(board);
            printBoard(board);

            System.out.print("Press enter to contine...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (board.getWinner() == Winner.DRAW) {
            System.out.println("Game ended in draw.");
        } else if (board.getWinner() == Winner.ATTACKER) {
            System.out.println("Attacker won the game.");
        } else if (board.getWinner() == Winner.DEFENDER) {
            System.out.println("Defender won the game.");
        }
    }

    public static void printBoard(Board board) {
        System.out.print('\n');
        int rowNumberDigits = board.getBoardSize() > 9 ? 2 : 1;

        for (int i = 0; i < rowNumberDigits; ++i) System.out.print(" ");
        System.out.print(" ");
        for (int j = 0; j < board.getBoardSize(); ++j) System.out.print("--");
        System.out.print('\n');

        for (int i = 0; i < board.getBoardSize(); ++i) {
            int rowNumber = board.getBoardSize() - i;
            System.out.print(rowNumber);
            if (rowNumber < 10 && rowNumberDigits == 2) System.out.print(' ');

            System.out.print("|");
            for (int j = 0; j < board.getBoardSize(); ++j) {
                Position pos = new Position(i, j);
                Piece piece = board.getPieces().get(pos);

                if (piece == null) {
                    if (board.getCornerSquares().contains(pos)) {
                        System.out.print('*');
                    } else if (board.getCornerSquares().equals(pos)) {
                        System.out.print('+');
                    } else {
                        System.out.print('.');
                    }
                } else if (piece == Piece.ATTACKER) {
                    System.out.print('A');
                } else if (piece == Piece.DEFENDER) {
                    System.out.print('D');
                } else if (piece == Piece.KING) {
                    System.out.print('K');
                } else {
                    System.out.print('?');
                }

                System.out.print(' ');
            }
            System.out.print("|");
            System.out.print('\n');
        }

        for (int i = 0; i < rowNumberDigits; ++i) System.out.print(" ");
        System.out.print(" ");

        for (int j = 0; j < board.getBoardSize(); ++j) System.out.print("--");
        System.out.print('\n');

        for (int i = 0; i < rowNumberDigits; ++i) System.out.print(" ");
        System.out.print(" ");

        for (int j = 0; j < board.getBoardSize(); ++j) {
            System.out.print(Character.toChars(65 + j));
            System.out.print(' ');
        }
        System.out.print("\n\n");
    }
}
