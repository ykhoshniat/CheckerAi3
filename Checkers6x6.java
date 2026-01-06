import java.util.*;

public class Checkers6x6 {

    // Cells: -1 white (invalid), 0 empty, 1 black man, 2 white man, 3 black king, 4 white king
    public static class Board {
        int[][] cells = new int[6][6];
        boolean blacksTurn = true; // black starts

        public Board() {
            init();
        }

        public Board(Board other) {
            this.cells = new int[6][6];
            for (int r = 0; r < 6; r++) System.arraycopy(other.cells[r], 0, this.cells[r], 0, 6);
            this.blacksTurn = other.blacksTurn;
        }

        private void init() {
            // Mark invalid white squares as -1, playable black squares as 0 initially.
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    boolean blackSquare = ((r % 2 == 0) && (c % 2 == 1)) || ((r % 2 == 1) && (c % 2 == 0));
                    cells[r][c] = blackSquare ? 0 : -1;
                }
            }
            // Place 6 men per player on their two ranks' black squares:
            // Black rows: 0 and 1 (bottom for black perspective if we index from 0)
            for (int r = 0; r <= 1; r++) {
                for (int c = 0; c < 6; c++) if (cells[r][c] == 0) cells[r][c] = 1;
            }
            // White rows: 4 and 5 (top for white)
            for (int r = 4; r <= 5; r++) {
                for (int c = 0; c < 6; c++) if (cells[r][c] == 0) cells[r][c] = 2;
            }
            blacksTurn = true;
        }

        public List<Move> generateLegalMoves() {
            int playerMan = blacksTurn ? 1 : 2;
            int playerKing = blacksTurn ? 3 : 4;

            // First collect all capture sequences (mandatory)
            List<Move> captures = new ArrayList<>();
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    int piece = cells[r][c];
                    if (piece == playerMan || piece == playerKing) {
                        List<Move> seqs = generateCapturesFrom(r, c);
                        captures.addAll(seqs);
                    }
                }
            }
            if (!captures.isEmpty()) return captures;

            // Otherwise, simple moves
            List<Move> moves = new ArrayList<>();
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    int piece = cells[r][c];
                    if (piece == playerMan || piece == playerKing) {
                        moves.addAll(generateSimpleMovesFrom(r, c));
                    }
                }
            }
            return moves;
        }

        private List<Move> generateSimpleMovesFrom(int r, int c) {
            List<Move> res = new ArrayList<>();
            int piece = cells[r][c];
            boolean isBlack = (piece == 1 || piece == 3);
            boolean isKing = (piece == 3 || piece == 4);

            int[][] dirsManBlack = {{1, -1}, {1, 1}};   // forward for black
            int[][] dirsManWhite = {{-1, -1}, {-1, 1}}; // forward for white
            int[][] dirsKing = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};

            int[][] dirs;
            if (isKing) dirs = dirsKing;
            else dirs = isBlack ? dirsManBlack : dirsManWhite;

            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (inBounds(nr, nc) && cells[nr][nc] == 0) {
                    Move m = new Move(r, c);
                    m.addStep(nr, nc, -1, -1); // no capture
                    res.add(m);
                }
            }
            return res;
        }

        private List<Move> generateCapturesFrom(int r, int c) {
            List<Move> res = new ArrayList<>();
            int piece = cells[r][c];
            boolean isBlack = (piece == 1 || piece == 3);
            boolean isKing = (piece == 3 || piece == 4);

            int[][] dirsManBlack = {{1, -1}, {1, 1}};
            int[][] dirsManWhite = {{-1, -1}, {-1, 1}};
            int[][] dirsKing = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};

            int[][] dirs;
            if (isKing) dirs = dirsKing;
            else dirs = isBlack ? dirsManBlack : dirsManWhite;

            // DFS for multi-jump
            Move start = new Move(r, c);
            boolean[] found = new boolean[1]; // flag for at least one capture
            dfsCaptures(r, c, piece, dirs, start, res, found);
            return res;
        }

        private void dfsCaptures(int r, int c, int piece, int[][] dirs, Move current, List<Move> acc, boolean[] found) {
            boolean extended = false;
            for (int[] d : dirs) {
                int mr = r + d[0], mc = c + d[1];
                int lr = r + 2 * d[0], lc = c + 2 * d[1];
                if (inBounds(lr, lc) && inBounds(mr, mc) && cells[lr][lc] == 0) {
                    int mid = cells[mr][mc];
                    if (isOpponentPiece(piece, mid)) {
                        // simulate step
                        int prevMid = cells[mr][mc];
                        int prevFrom = cells[r][c];
                        cells[r][c] = 0;
                        cells[mr][mc] = 0; // captured removed immediately
                        cells[lr][lc] = piece;

                        current.addStep(lr, lc, mr, mc);

                        // Recompute dirs if kinging occurs mid-chain: men still capture forward only (rule variation).
                        // This implementation enforces standard rule: a man that becomes king mid-turn continues capturing as a king.
                        int newPiece = piece;
                        if (!isKingPiece(piece)) {
                            if (shouldKinging(newPiece, lr)) {
                                newPiece = promote(piece);
                                cells[lr][lc] = newPiece;
                            }
                        }
                        int[][] nextDirs = isKingPiece(newPiece) ? new int[][]{{1,-1},{1,1},{-1,-1},{-1,1}} :
                                (isBlackPiece(newPiece) ? new int[][]{{1,-1},{1,1}} : new int[][]{{-1,-1},{-1,1}});

                        dfsCaptures(lr, lc, newPiece, nextDirs, current, acc, found);

                        // backtrack
                        current.removeLastStep();
                        cells[r][c] = prevFrom;
                        cells[mr][mc] = prevMid;
                        cells[lr][lc] = 0;
                        extended = true;
                    }
                }
            }
            if (!extended && current.hasCapture()) {
                acc.add(new Move(current)); // add terminal capture sequence
                found[0] = true;
            }
        }

        public void applyMove(Move m) {
            int sr = m.startR, sc = m.startC;
            int piece = cells[sr][sc];
            cells[sr][sc] = 0;
            int cr = sr, cc = sc;
            for (Move.Step st : m.steps) {
                if (st.captureR != -1) {
                    cells[st.captureR][st.captureC] = 0; // remove captured
                }
                cr = st.toR; cc = st.toC;
            }
            // place piece at final
            cells[cr][cc] = piece;

            // promote if needed (end of move)
            if (!isKingPiece(piece) && shouldKinging(piece, cr)) {
                cells[cr][cc] = promote(piece);
            }
            // toggle turn
            blacksTurn = !blacksTurn;
        }

        public boolean isTerminal() {
            int blackCount = 0, whiteCount = 0;
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    int p = cells[r][c];
                    if (p == 1 || p == 3) blackCount++;
                    else if (p == 2 || p == 4) whiteCount++;
                }
            }
            if (blackCount == 0 || whiteCount == 0) return true;

            List<Move> legal = generateLegalMoves();
            return legal.isEmpty();
        }

        public int winnerOrDraw() {
            // return +1 black wins, -1 white wins, 0 draw/ongoing
            if (!isTerminal()) return 0;
            // If one player has no moves at their turn, the other won.
            int blackCount = 0, whiteCount = 0;
            for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) {
                int p = cells[r][c];
                if (p == 1 || p == 3) blackCount++;
                else if (p == 2 || p == 4) whiteCount++;
            }
            if (blackCount == 0) return -1;
            if (whiteCount == 0) return +1;
            // Stalemate (no legal moves)
            if (generateLegalMoves().isEmpty()) {
                return blacksTurn ? -1 : +1; // current player to move has no moves => they lose
            }
            return 0;
        }

        private boolean inBounds(int r, int c) { return r >= 0 && r < 6 && c >= 0 && c < 6; }
        private boolean isOpponentPiece(int me, int other) {
            if (other <= 0) return false;
            boolean meBlack = (me == 1 || me == 3);
            boolean otherBlack = (other == 1 || other == 3);
            return meBlack != otherBlack;
        }
        private boolean isKingPiece(int p) { return p == 3 || p == 4; }
        private boolean isBlackPiece(int p) { return p == 1 || p == 3; }
        private boolean shouldKinging(int piece, int row) {
            boolean isBlack = isBlackPiece(piece);
            return (!isKingPiece(piece)) && ((isBlack && row == 5) || (!isBlack && row == 0));
        }
        private int promote(int piece) {
            return (piece == 1) ? 3 : (piece == 2) ? 4 : piece;
        }

        public void print() {
            System.out.println("Turn: " + (blacksTurn ? "Black" : "White"));
            for (int r = 5; r >= 0; r--) {
                for (int c = 0; c < 6; c++) {
                    int v = cells[r][c];
                    String s = (v == -1) ? " ." : String.valueOf(v);
                    System.out.print(String.format("%2s ", s));
                }
                System.out.println();
            }
            System.out.println();
        }
    }