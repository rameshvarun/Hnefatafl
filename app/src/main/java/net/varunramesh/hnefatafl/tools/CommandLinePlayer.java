package net.varunramesh.hnefatafl.tools;

import net.varunramesh.hnefatafl.ai.AIStrategy;
import net.varunramesh.hnefatafl.ai.MinimaxStrategy;
import net.varunramesh.hnefatafl.ai.RandomStrategy;
import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.History;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;
import net.varunramesh.hnefatafl.simulator.rulesets.FeltarHnefatafl;
import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by varunramesh on 4/17/16.
 */
public class CommandLinePlayer {
    public static int BOARD_SIZE;

    /** Convert a Position object to a letter-number representaiton. */
    public static String formatPosition(Position pos) {
        return Character.toString(Character.toChars(65 + pos.getY())[0]) + (BOARD_SIZE - pos.getX());
    }

    public static void main(String[] args) {
        System.out.println("Hnefatafl command-line player.");
        Ruleset ruleset = new FeltarHnefatafl();

        History history = new History(ruleset.getStartingConfiguration());
        BOARD_SIZE = history.getCurrentBoard().getBoardSize();

        // Display the initial board.
        printBoard(history.getCurrentBoard());

        AIStrategy attackerStrategy = null; // new RandomStrategy();
        AIStrategy defenderStrategy = null; //new MinimaxStrategy(ruleset, Player.DEFENDER, 4);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Pick a strategy for the attacker ('random', 'minimax', or 'player'):\n> ");
            String answer = br.readLine();

            if (answer.toLowerCase().equals("random")) {
                attackerStrategy = new RandomStrategy();
            } else if (answer.toLowerCase().equals("minimax")) {
                attackerStrategy = new MinimaxStrategy(ruleset, Player.ATTACKER, 4);
            } else {
                throw new UnsupportedOperationException();
            }

            System.out.print("Pick a strategy for the defender ('random', 'minimax', or 'player'):\n> ");
            answer = br.readLine();

            if (answer.toLowerCase().equals("random")) {
                defenderStrategy = new RandomStrategy();
            } else if (answer.toLowerCase().equals("minimax")) {
                defenderStrategy = new MinimaxStrategy(ruleset, Player.DEFENDER, 4);
            } else {
                throw new UnsupportedOperationException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


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

        while (!history.getCurrentBoard().isOver()) {
            Set<Action> actions = ruleset.getActions(history);

            Action action = null;
            if (history.getCurrentBoard().getCurrentPlayer() == Player.ATTACKER)
                action = attackerStrategy.decide(history, actions);
            else
                action = defenderStrategy.decide(history, actions);

            history = history.advance(action, ruleset.step(history, action, eventHandler));
            printBoard(history.getCurrentBoard());

            System.out.print("Press enter to contine...");
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (history.getCurrentBoard().getWinner() == Winner.DRAW) {
            System.out.println("Game ended in draw.");
        } else if (history.getCurrentBoard().getWinner() == Winner.ATTACKER) {
            System.out.println("Attacker won the game.");
        } else if (history.getCurrentBoard().getWinner() == Winner.DEFENDER) {
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