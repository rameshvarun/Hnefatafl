package net.varunramesh.hnefatafl.tools;

import net.varunramesh.hnefatafl.Mutable;
import net.varunramesh.hnefatafl.ai.AIStrategy;
import net.varunramesh.hnefatafl.ai.MinimaxStrategy;
import net.varunramesh.hnefatafl.ai.MonteCarloStrategy;
import net.varunramesh.hnefatafl.ai.RandomStrategy;
import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.History;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;
import net.varunramesh.hnefatafl.simulator.rulesets.Brandubh;
import net.varunramesh.hnefatafl.simulator.rulesets.FeltarHnefatafl;
import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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

    public static Position parsePosition(String posStr) {
        int y = (int)posStr.charAt(0) - 65;
        int x = BOARD_SIZE - Integer.parseInt(posStr.substring(1));
        return new Position(x, y);
    }

    public static void main(String[] args) {
        System.out.println("Hnefatafl command-line player.");

        AIStrategy playerStrategy = new AIStrategy() {
            @Override
            public Action decide(History history, List<Action> actions) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                while(true) {
                    System.out.print("(Your Move) > ");
                    String move = null;
                    try {
                        move = br.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }

                    StringTokenizer st = new StringTokenizer(move);
                    if (st.countTokens() != 3) {
                        System.out.println("Tokenizer expected three tokens.");
                        continue;
                    }

                    Position from = parsePosition(st.nextToken());
                    st.nextToken();
                    Position to = parsePosition(st.nextToken());

                    System.out.print(formatPosition(from) + " " + formatPosition(to));

                    for (Action action : actions) {
                        if (action.getFrom().equals(from) && action.getTo().equals(to))
                            return action;
                    }
                    System.out.println("Action is not a valid move.");
                }
            }
        };

        Ruleset ruleset = null;
        AIStrategy attackerStrategy = null;
        AIStrategy defenderStrategy = null;
        History history = null;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Type three characters. The first character is the ruleset - (F)etlar or (B)randubh.");
            System.out.println("The second character is the attacker's strategy - (R)andom, (M)inimax, (C)MonteCarlo, or (P)layer.");
            System.out.println("The third character is the defender's strategy - (R)andom, (M)inimax, (C)MonteCarlo, or (P)layer.");

            System.out.print("> ");
            String answer = br.readLine().toLowerCase();

            switch (answer.charAt(0)) {
                case 'f':
                    ruleset = new FeltarHnefatafl();
                    break;
                case 'b':
                    ruleset = new Brandubh();
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            history = new History(ruleset.getStartingConfiguration());
            BOARD_SIZE = history.getCurrentBoard().getBoardSize();

            switch (answer.charAt(1)) {
                case 'r':
                    attackerStrategy = new RandomStrategy();
                    break;
                case 'm':
                    attackerStrategy = new MinimaxStrategy(ruleset, Player.ATTACKER, 4);
                    break;
                case 'c':
                    attackerStrategy = new MonteCarloStrategy(ruleset, Player.ATTACKER);
                    break;
                case 'p':
                    attackerStrategy = playerStrategy;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            switch (answer.charAt(2)) {
                case 'r':
                    defenderStrategy = new RandomStrategy();
                    break;
                case 'm':
                    defenderStrategy = new MinimaxStrategy(ruleset, Player.DEFENDER, 4);
                    break;
                case 'c':
                    defenderStrategy = new MonteCarloStrategy(ruleset, Player.DEFENDER);
                    break;
                case 'p':
                    defenderStrategy = playerStrategy;
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // Display the initial board.
        printBoard(history.getCurrentBoard());

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
            List<Action> actions = ruleset.getActions(history);

            Action action = null;
            if (history.getCurrentBoard().getCurrentPlayer() == Player.ATTACKER)
                action = attackerStrategy.decide(history, actions);
            else
                action = defenderStrategy.decide(history, actions);

            history = history.advance(action, ruleset.step(history, action, eventHandler));
            printBoard(history.getCurrentBoard());
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