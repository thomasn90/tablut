package tablut;



import java.util.List;

import static java.lang.Math.*;


import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Thomas Nguyen
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }

    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move mv = findMove();
        _controller.reportMove(mv);
        return mv.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        if (b.turn() == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        int value = 0;
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            value = -1 * INFTY;
            List<Move> mves = board.legalMoves(WHITE);
            for (Move mv : board.legalMoves(WHITE)) {
                board.makeMove(mv);
                int temp = findMove(board, depth - 1, false, -1, alpha, beta);
                board.undo();
                if (temp >= value) {
                    value = temp;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                    alpha = max(alpha, value);
                }
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            value = INFTY;
            for (Move mv : board.legalMoves(BLACK)) {
                board.makeMove(mv);
                int temp = findMove(board, depth - 1, false, 1, alpha, beta);
                board.undo();
                if (temp <= value) {
                    value = temp;
                    if (saveMove) {
                        _lastFoundMove = mv;
                    }
                    beta = min(beta, value);
                }
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }

    /** index of the moves starting at thirteen. */
    public static final int MOVEINDEX = 30;

    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     */
    private static int maxDepth(Board board) {
        if (board.moveCount() < MOVEINDEX) {
            return 1;
        } else if (board.moveCount() < 2 * MOVEINDEX) {
            return 1;
        } else if (board.moveCount() < 3 * MOVEINDEX) {
            return 1;
        }
        return 1;
    }

    /**
     * Return a heuristic value for BOARD.
     */
    private int staticScore(Board board) {
        int score = 0;
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        int whiteBlack = board.pieceLocations(WHITE).size()
                - board.pieceLocations(BLACK).size();
        score = whiteBlack;
        return score;
    }

}
