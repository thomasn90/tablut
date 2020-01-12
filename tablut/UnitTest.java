package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test as a placeholder for real ones. */
    @Test
    public void dummyTest() {
        assertTrue("There are no unit tests!", false);
    }

    /** A test as a placeholder for real ones. */
    @Test
    public void test1() {
        Board B = new Board();
        B.makeMove(Square.sq('d' - 'a', '9' - '1'),
                Square.sq('d' - 'a', '7' - '1'));
        B.makeMove(Square.sq('e' - 'a', '3' - '1'),
                Square.sq('d' - 'a', '3' - '1'));
        B.makeMove(Square.sq('f' - 'a', '9' - '1'),
                Square.sq('f' - 'a', '7' - '1'));


        B.makeMove(Square.sq('d' - 'a', '3' - '1'),
                Square.sq('e' - 'a', '3' - '1'));
        B.makeMove(Square.sq('d' - 'a', '1' - '1'),
                Square.sq('d' - 'a', '3' - '1'));
        B.makeMove(Square.sq('g' - 'a', '5' - '1'),
                Square.sq('g' - 'a', '4' - '1'));
        B.makeMove(Square.sq('f' - 'a', '1' - '1'),
                Square.sq('f' - 'a', '3' - '1'));
        B.makeMove(Square.sq('g' - 'a', '4' - '1'),
                Square.sq('g' - 'a', '5' - '1'));
        B.makeMove(Square.sq('i' - 'a', '6' - '1'),
                Square.sq('g' - 'a', '6' - '1'));
        B.makeMove(Square.sq('c' - 'a', '5' - '1'),
                Square.sq('c' - 'a', '9' - '1'));
        B.makeMove(Square.sq('i' - 'a', '4' - '1'),
                Square.sq('g' - 'a', '4' - '1'));


        B.makeMove(Square.sq('e' - 'a', '4' - '1'),
                Square.sq('b' - 'a', '4' - '1'));
        B.makeMove(Square.sq('g' - 'a', '4' - '1'),
                Square.sq('e' - 'a', '4' - '1'));
        B.makeMove(Square.sq('e' - 'a', '6' - '1'),
                Square.sq('b' - 'a', '6' - '1'));
        B.makeMove(Square.sq('g' - 'a', '6' - '1'),
                Square.sq('e' - 'a', '6' - '1'));
        B.makeMove(Square.sq('f' - 'a', '5' - '1'),
                Square.sq('f' - 'a', '4' - '1'));
        B.makeMove(Square.sq('h' - 'a', '5' - '1'),
                Square.sq('f' - 'a', '5' - '1'));
        B.makeMove(Square.sq('b' - 'a', '4' - '1'),
                Square.sq('c' - 'a', '4' - '1'));
        B.makeMove(Square.sq('a' - 'a', '5' - '1'),
                Square.sq('c' - 'a', '5' - '1'));

        B.makeMove(Board.THRONE, Board.WTHRONE);

    }
}



