package ru.ifmo.rain.klepov;

import java.text.MessageFormat;
import java.util.*;

public class Main {
    private static int BOARD_SIZE = 8;
    private static String state = "initial";
    private static int distr = -1;
    private final static String LOCALE_NAME = "en_en";
    private static final Locale DEFAULT_LOCALE = Locale.UK;
    private static final Locale locale = findLocale(LOCALE_NAME);
    // :NOTE: - Локаль явно задана в коде
    // :NOTE: * Неправильно используется стандартный механизм загрузки ResourceBundle
    final static ResourceBundle bundle = ResourceBundle.getBundle("ru.ifmo.rain.klepov.Bundle_en", DEFAULT_LOCALE);

    private static Locale findLocale(final String name) {
        final Locale ans = Arrays.stream(Locale.getAvailableLocales()).filter(l -> l.toString().equals(name)).findFirst().orElse(null);
        if (ans == null)
            return DEFAULT_LOCALE;
        return ans;
    }

    private static String readFromBundle(final String request, final Object ... v) {
        return new MessageFormat(bundle.getString(request), locale).format(v);
    }

    static void printUsage() {
        System.out.println(readFromBundle("usage"));
    }

    public  static class Position {
        public int x, y;

        public Position(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    private static boolean checkArgs(final String[] args) {
        if (args.length != 6) {
            printUsage();
            return false;
        }
        for (final String s : args) {
            if (s == null) {
                System.out.println(readFromBundle("null_args"));
                return false;
            }
        }
        return true;
    }

    private static class Board {
        int[][] board;
        Position whiteL;
        Position blackK;
        Position whiteK;

        Position desirable;

        Board(final int whiteLx, final int whiteLy, final int whiteKx, final int whiteKy, final int blackKx, final int blackKy) {
            board = new int[BOARD_SIZE][BOARD_SIZE];
            whiteL = new Position(whiteLx, whiteLy);
            whiteK = new Position(whiteKx, whiteKy);
            blackK = new Position(blackKx, blackKy);
        }

        private char getCharForPos(final int x, final int y) {
            if (blackK.x == x && blackK.y == y) {
                return 'Y';
            }
            if (whiteL.x == x && whiteL.y == y) {
                return 'R';
            }
            if (whiteK.x == x && whiteK.y == y) {
                return 'K';
            }
            return '.';
        }

        public void print() {
            System.out.println(readFromBundle("print"));
            for (int i = 7; i >= 0; i--) {
                System.out.print((i + 1) + " |");
                for (int j = 0; j < 8; j++) {
                    System.out.print(" " + getCharForPos(i, j));
                }
                System.out.println();
            }

            System.out.println("    1 2 3 4 5 6 7 8");
        }

        public boolean free(final int x, final int y) {
            final Position newPos = new Position(x, y);
            return !equalPos(whiteK, newPos) && !equalPos(blackK, newPos) && !equalPos(whiteL, newPos);
        }
    }
    private static boolean rookBeats(final Board board, final Position rookPosition, final Position otherPosition) {
        if (rookPosition.x == otherPosition.x) {
            final int yl = Math.min(rookPosition.y,  otherPosition.y);
            final int yr = Math.max(rookPosition.y,  otherPosition.y);
            boolean free = true;
            for (int i = yl + 1; i < yr; i++) {
                if (!board.free(rookPosition.x, i)) {
                    free = false;
                    break;
                }
            }
            return free;
        } else if (rookPosition.y == otherPosition.y) {
            final int xl = Math.min(rookPosition.x,  otherPosition.x);
            final int xr = Math.max(rookPosition.x,  otherPosition.x);
            boolean free = true;
            for (int i = xl + 1; i < xr; i++) {
                if (!board.free(i, rookPosition.y)) {
                    free = false;
                    break;
                }
            }
            return free;
        }
        return false;
    }

    private static boolean kingBeats(final Position kingPosition, final Position otherPosition) {
        return Math.abs(kingPosition.x - otherPosition.x) <= 1 &&
                Math.abs(kingPosition.y - otherPosition.y) <= 1;
    }

    private static boolean makeOurTurn(final Board board) {
        if (rookBeats(board, board.whiteL, board.blackK) || kingBeats(board.whiteK, board.blackK)) {
            return true;
        }

        if (state.equals("initial")) {
            board.desirable = determineDesirable(board);
            System.out.println(board.desirable.x + " " + board.desirable.y);
            if (equalPos(board.desirable, board.whiteL)) {
                state = "attacking_rook";
            } else if (rookBeats(board, board.whiteL, board.desirable)) {
                state = "attacking_rook";
                board.whiteL.x = board.desirable.x;
                board.whiteL.y = board.desirable.y;
                return false;
            } else {
                final Position var1 = new Position(board.desirable.x, board.whiteL.y);
                final Position var2 = new Position(board.whiteL.x, board.desirable.y);
                state = "initial";
                if (rookBeats(board, board.whiteL, var1) && !kingBeats(board.blackK, var1) && rookBeats(board, var1, board.desirable)) {
                    board.whiteL = var1;
                } else {
                    board.whiteL = var2;
                }
                return false;
            }
        }

        if (state.equals("attacking_rook")) {
            modifyPosition(board.whiteL);
            state = "attacking_king";
        } else {
            modifyPosition(board.whiteK);
            state = "attacking_rook";
        }
        return false;
    }

    private static void modifyPosition(final Position figure) {
        if (distr == 1) {
            figure.x--;
        } else if (distr == 2) {
            figure.y++;
        } else if (distr == 3) {
            figure.y--;
        } else {
            figure.x++;
        }
    }

    private static boolean equalPos(final Position a, final Position b) {
        return a.x == b.x && a.y == b.y;
    }

    private static Position determineDesirable(final Board board) {
        final Position res = new Position(board.whiteK.x, board.whiteK.y);

        if (board.whiteK.x >= board.blackK.x && board.whiteK.y < board.blackK.y) { // 1
            res.y++;
            distr = 1;
        } else if (board.whiteK.x < board.blackK.x && board.whiteK.y <= board.blackK.y) { // 2
            res.x++;
            distr = 2;
        } else if (board.whiteK.x > board.blackK.x && board.whiteK.y >= board.blackK.y) { // 3
            res.x--;
            distr = 3;
        } else { // 4
            res.y--;
            distr = 4;
        }

        return res;
    }

    private static void makeUserTurn(final Board board, final int x, final int y) {
        board.blackK.x = x - 1;
        board.blackK.y = y - 1;
    }

    private static boolean validTurn(final Board board, int x, int y) {
        if (!validCoord(x, y)) {
            return false;
        }

        x--;
        y--;

        if (x == board.blackK.x && y == board.blackK.y) {
            return false;
        }

        return (Math.abs(board.blackK.x - x) <= 1 && Math.abs(board.blackK.y - y) <= 1);
    }

    private static boolean validCoord(final int x, final int y) {
        return validSingleCoord(x) && validSingleCoord(y);
    }

    private static boolean validSingleCoord(final int coord) {
        return coord >= 1 && coord <= BOARD_SIZE;
    }

    private static int check(final String s) throws ApplicationException {
        // :NOTE: - NumberFormatException стоило обрабатывать здесь
        final int res = Integer.parseInt(s);
        if (!validSingleCoord(res)) {
            throw new ApplicationException(readFromBundle("invalid_coords", BOARD_SIZE));
        }
        return res - 1;
    }

    public static void main(final String[] args) {
        if (args == null || args.length != 6) {
            System.out.println(readFromBundle("usage"));
            return;
        }

        final Board board;
        try {
            board = new Board(check(args[0]),
                    check(args[1]),
                    check(args[2]),
                    check(args[3]),
                    check(args[4]),
                    check(args[5]));
        } catch (final NumberFormatException e) {
            System.out.println(readFromBundle("int_args"));
            return;
        } catch (final ApplicationException e) {
            System.out.println(e.getMessage());
            return;
        }


        // :NOTE: - Ввод-вывод без указания кодировки
        try (final Scanner in = new Scanner(System.in)) {
            while (true) {
                board.print();
                if (makeOurTurn(board)) {
                    board.print();
                    System.out.println(readFromBundle("gg"));
                    break;
                }
                board.print();
                while (true) {
                    System.out.println(readFromBundle("read_coords"));

                    final int x;
                    final int y;

                    try {
                        // :NOTE: - InputMismatchException
                        x = in.nextInt();
                        y = in.nextInt();
                    } catch (final NoSuchElementException e) {
                        in.nextLine();
                        System.out.println(readFromBundle("invalid_turn"));
                        continue;
                    }

                    if (validTurn(board, x, y)) {
                        makeUserTurn(board, x, y);
                        break;
                    } else {
                        System.out.println(readFromBundle("invalid_turn"));
                    }
                }
            }
        }
    }
}
