package tablut;




import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Stack;

import java.util.HashSet;


import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Thomas Nguyen
 */
class Board {

    /** The board. */
    private Piece[][] _board;

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** the stack of all the history of moves. */
    private Stack<String> record = new Stack<>();

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this.record = model.record;
        this._turn = model._turn;
        this._winner = model._winner;
        this._board = model._board;
        this._moveCount = model._moveCount;
        this._moveLimit = model._moveLimit;
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        Integer big = Integer.MAX_VALUE;
        _moveLimit = big;
        _board = new Piece[SIZE][SIZE];
        Piece king = KING;
        _board[4][4] = king;
        for (Square x : INITIAL_ATTACKERS) {
            Piece y = BLACK;
            _board[x.col()][x.row()] = y;
        }
        for (Square x : INITIAL_DEFENDERS) {
            Piece y = WHITE;
            _board[x.col()][x.row()] = y;
        }
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (_board[x][y] == null) {
                    Piece empty = EMPTY;
                    _board[x][y] = empty;
                }
            }
        }
        clearUndo();
        record.add(encodedBoard());
    }

    /** Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * N is integer*/
    void setMoveLimit(int n) {
        _moveLimit = n;
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        if (record.contains(encodedBoard())) {
            _winner = _turn;
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {

        for (Square s : SQUARE_LIST) {
            if (get(s) == KING) {
                return s;
            }
        }
        return null;
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        record.push(encodedBoard());
        put(p, s);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        boolean temp = true;
        if (from.isRookMove(to)) {
            if (from.row() == to.row()) {
                if (from.col() < to.col()) {
                    for (int x = from.col() + 1; x <= to.col(); x++) {
                        if (_board[x][from.row()] != EMPTY) {
                            temp = false;
                            break;
                        }
                    }
                } else {
                    for (int x = from.col() - 1; x > from.col() - 1; x--) {
                        if (_board[x][from.row()] != EMPTY) {
                            temp = false;
                            break;
                        }
                    }
                }
            } else if (from.col() == to.col()) {
                if (from.row() < to.row()) {
                    for (int x = from.row() + 1; x <= to.row(); x++) {
                        if (_board[from.col()][x] != EMPTY) {
                            temp = false;
                            break;
                        }
                    }
                } else {
                    for (int x = from.row() - 1; x > to.row() - 1; x--) {
                        if (_board[from.col()][x] != EMPTY) {
                            temp = false;
                            break;
                        }
                    }
                }
            }
        }
        return temp;
    }


    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (from != to && isUnblockedMove(from, to) && isLegal(from)
                && _board[to.col()][to.row()] == EMPTY
                && !record.contains(mv(from, to))) {
            if (to != THRONE) {
                return true;
            } else {
                if (kingPosition() != null) {
                    return kingPosition() == from;
                }
            }
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        if (isLegal(from, to)) {
            record.add(encodedBoard());
            _board[to.col()][to.row()] = _board[from.col()][from.row()];
            _board[from.col()][from.row()] = EMPTY;
            if (_turn == BLACK) {
                _turn = WHITE;
            } else {
                _turn = BLACK;
            }
            captureUp(to);
            captureDown(to);
            captureLeft(to);
            captureRight(to);
            _moveCount++;
            if (_moveCount >= _moveLimit) {
                _winner = _turn;
            }
            if (legalMoves(_turn).isEmpty()) {
                _winner = _turn.opponent();
            }
            if (kingPosition() == null) {
                _winner = BLACK;
            } else if (kingPosition().row() == SIZE - 1
                    || kingPosition().col() == SIZE - 1
                    || kingPosition().row() == 0
                    || kingPosition().col() == 0) {
                _winner = WHITE;
            }
            checkRepeated();
        }
    }

    /** a list of NSWE thrones.  */
    private static final ArrayList<Square> KINGSQUARES = new ArrayList<>();

    static {
        KINGSQUARES.add(NTHRONE);
        KINGSQUARES.add(STHRONE);
        KINGSQUARES.add(ETHRONE);
        KINGSQUARES.add(WTHRONE);
    }

    /** Check to see if SQ is hostile!
     * SQ is a square
     * @return it returns a boolean*/
    Boolean isHostile(Square sq) {
        if (sq == THRONE && get(sq) == EMPTY) {
            return true;
        } else if (get(sq) == _turn.opponent()) {
            return true;
        } else if (_turn == BLACK && get(sq) == KING) {
            return true;
        } else if (_turn == WHITE) {
            int blackCheck = 0;
            for (Square s : KINGSQUARES) {
                if (get(s) == (BLACK)) {
                    blackCheck++;
                }
            }
            if (sq == THRONE && get(sq) != EMPTY) {
                if (blackCheck == 3) {
                    return true;
                }
            }
        }
        return false;
    }

    /** check if SIDE2 is an opponent of SIDE1.
     * SIDE1 and SIDE2 are the pieces
     * @return it returns a boolean*/
    boolean isOpponent(Piece side1, Piece side2) {
        if (side1 == BLACK) {
            if (side2 == WHITE || side2 == KING) {
                return true;
            }
        } else if (side1 == WHITE || side1 == KING) {
            if (side2 == BLACK) {
                return true;
            }
        }
        return false;
    }

    /** Check if you capture SQ normally or if it's the King exception.
     *  SQ for square
     *  @return it returns a boolean*/
    boolean isNormalCapture(Square sq) {
        if (get(sq) != KING) {
            return true;
        } else {
            return sq != THRONE && !KINGSQUARES.contains(sq);
        }
    }

    /** Check if KING is surrounded by hostile squares.
     * KING is a square that is a king
     * @return it returns a boolean*/
    boolean hostileAround(Square king) {
        int temp = 0;
        if (isHostile(sq(king.col() + 1, king.row()))) {
            temp++;
        }
        if (isHostile(sq(king.col() - 1, king.row()))) {
            temp++;
        }
        if (isHostile(sq(king.col(), king.row() + 1))) {
            temp++;
        }
        if (isHostile(sq(king.col(), king.row() - 1))) {
            temp++;
        }
        return temp == 4;
    }

    /** Capture the square above TO. */
    void captureUp(Square to) {
        if (SIZE - to.row() > 2) {
            if (isOpponent(get(to), _board[to.col()][to.row() + 1])) {
                if (isNormalCapture(sq(to.col(), to.row() + 1))) {
                    if (isHostile(sq(to.col(), to.row() + 2))) {
                        capture(to, sq(to.col(), to.row() + 2));
                    }
                } else {
                    if (hostileAround(sq(to.col(), to.row() + 1))) {
                        capture(to, sq(to.col(), to.row() + 2));
                    }
                }
            }
        }
    }

    /** Capture the square below TO. */
    void captureDown(Square to) {
        if (SIZE - to.row() <= 7) {
            if (isOpponent(get(to), _board[to.col()][to.row() - 1])) {
                if (isNormalCapture(sq(to.col(), to.row() - 1))) {
                    if (isHostile(sq(to.col(), to.row() - 2))) {
                        capture(to, sq(to.col(), to.row() - 2));
                    }
                } else {
                    if (hostileAround(sq(to.col(), to.row() - 1))) {
                        capture(to, sq(to.col(), to.row() - 2));
                    }
                }
            }
        }
    }

    /** Capture the square to the right of TO. */
    void captureRight(Square to) {
        if (SIZE - to.col() > 2) {
            if (isOpponent(get(to), _board[to.col() + 1][to.row()])) {
                if (isNormalCapture(sq(to.col() + 1, to.row()))) {
                    if (isHostile(sq(to.col() + 2, to.row()))) {
                        capture(to, sq(to.col() + 2, to.row()));
                    }
                } else {
                    if (hostileAround(sq(to.col() + 1, to.row()))) {
                        capture(to, sq(to.col() + 2, to.row()));
                    }
                }
            }
        }
    }

    /** Capture the square to the left of TO. */
    void captureLeft(Square to) {
        if (SIZE - to.col() <= 7) {
            if (isOpponent(get(to), _board[to.col() - 1][to.row()])) {
                if (isNormalCapture(sq(to.col() - 1, to.row()))) {
                    if (isHostile(sq(to.col() - 2, to.row()))) {
                        capture(to, sq(to.col() - 2, to.row()));
                    }
                } else {
                    if (hostileAround(sq(to.col() - 1, to.row()))) {
                        capture(to, sq(to.col() - 2, to.row()));
                    }
                }
            }
        }
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        assert (sq0 != sq2);
        assert (sq0.isRookMove(sq2));
        Square btwn = sq0.between(sq2);
        _board[btwn.col()][btwn.row()] = EMPTY;
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        if (_moveCount > 0 && !_repeated && !record.empty()) {
            undoPosition();
        }
    }

    /** Max index number. */
    private static final int MAXINDEXNUM = 80;

    /** Remove record of current position in the set of positions encountered,
     *  unless it is a repeated position or we are at the first move. */
    private void undoPosition() {
        _repeated = record.contains(encodedBoard());
        String boardString = record.pop();
        if (boardString.charAt(0) == 'B') {
            _turn = BLACK;
        } else {
            _turn = WHITE;
        }
        _winner = null;
        for (int index = 0; index <= MAXINDEXNUM; index++) {
            if (boardString.charAt(index + 1) == '-') {
                _board[index % BOARD_SIZE][index / BOARD_SIZE] = EMPTY;
            }
            if (boardString.charAt(index + 1) == 'B') {
                _board[index % BOARD_SIZE][index / BOARD_SIZE] = BLACK;
            }
            if (boardString.charAt(index + 1) == 'W') {
                _board[index % BOARD_SIZE][index / BOARD_SIZE] = WHITE;
            }
            if (boardString.charAt(index + 1) == 'K') {
                _board[index % BOARD_SIZE][index / BOARD_SIZE] = KING;
            }
        }
        _moveCount--;

    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        record.clear();
    }


    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> blackMoves = new ArrayList<>();
        List<Move> whiteMoves = new ArrayList<>();
        for (Square s : SQUARE_LIST) {
            if (get(s) == BLACK) {
                for (Square sq : SQUARE_LIST) {
                    if (isLegal(s, sq) && mv(s, sq) != null
                            && !record.contains(mv(s, sq))) {
                        blackMoves.add(mv(s, sq));
                    }
                }
            } else if (get(s) == WHITE || get(s) == KING) {
                for (Square sq : SQUARE_LIST) {
                    if (isLegal(s, sq) && mv(s, sq) != null
                            && !record.contains(mv(s, sq))) {
                        whiteMoves.add(mv(s, sq));
                    }
                }
            }
        }
        if (side == BLACK) {
            return blackMoves;
        } else if (side == WHITE) {
            return whiteMoves;
        }
        return null;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side).isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    public HashSet<Square> pieceLocations(Piece side) {
        HashSet<Square> wtemp = new HashSet<>();
        HashSet<Square> btemp = new HashSet<>();
        if (side != EMPTY) {
            for (Square s : SQUARE_LIST) {
                if (get(s) == WHITE || get(s) == KING) {
                    wtemp.add(s);
                } else if (get(s) == BLACK) {
                    btemp.add(s);
                }
            }
        }
        if (side == WHITE) {
            return wtemp;
        } else {
            return btemp;
        }
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** The limit of moves that board can make. */
    private int _moveLimit;


}
