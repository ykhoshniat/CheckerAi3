interface Evaluator {
    double evaluate(Checkers6x6.Board b);

    // Evaluator 1: Material + Mobility + King weight + Advancement + Center control
    class MaterialMobilityEval implements Evaluator {
        public double evaluate(Checkers6x6.Board b) {
            int blackMen = 0, whiteMen = 0, blackKings = 0, whiteKings = 0;
            int blackMob = 0, whiteMob = 0;
            int blackAdvance = 0, whiteAdvance = 0;
            int blackCenter = 0, whiteCenter = 0;

            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    int p = b.cells[r][c];
                    if (p == 1) { blackMen++; blackAdvance += r; if (isCenter(r, c)) blackCenter++; }
                    else if (p == 2) { whiteMen++; whiteAdvance += (5 - r); if (isCenter(r, c)) whiteCenter++; }
                    else if (p == 3) { blackKings++; if (isCenter(r, c)) blackCenter++; }
                    else if (p == 4) { whiteKings++; if (isCenter(r, c)) whiteCenter++; }
                }
            }
            // Mobility approximate: count moves for each side by toggling turn
            int mobB = mobilityCount(b, true);
            int mobW = mobilityCount(b, false);
            blackMob = mobB; whiteMob = mobW;

            double materialScore = 1.0 * (blackMen - whiteMen) + 2.5 * (blackKings - whiteKings);
            double mobilityScore = 0.2 * (blackMob - whiteMob);
            double advanceScore = 0.1 * (blackAdvance - whiteAdvance);
            double centerScore = 0.2 * (blackCenter - whiteCenter);
            return materialScore + mobilityScore + advanceScore + centerScore;
        }

        private boolean isCenter(int r, int c) {
            return r >= 2 && r <= 3 && c >= 2 && c <= 3;
        }
        private int mobilityCount(Checkers6x6.Board b, boolean blacksTurn) {
            Checkers6x6.Board copy = new Checkers6x6.Board(b);
            copy.blacksTurn = blacksTurn;
            return copy.generateLegalMoves().size();
        }
    }

    // Evaluator 2: Material + Threats (exposed pieces) + capture availability
    class ThreatCaptureEval implements Evaluator {
        public double evaluate(Checkers6x6.Board b) {
            int bm = 0, wm = 0, bk = 0, wk = 0;
            for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) {
                int p = b.cells[r][c];
                if (p == 1) bm++;
                else if (p == 2) wm++;
                else if (p == 3) bk++;
                else if (p == 4) wk++;
            }
            double material = 1.0 * (bm - wm) + 3.0 * (bk - wk);

            // Count immediate capture opportunities and exposures
            int blackCaps = immediateCaptures(b, true);
            int whiteCaps = immediateCaptures(b, false);

            int blackExposed = exposedPieces(b, true);
            int whiteExposed = exposedPieces(b, false);

            double capsScore = 0.3 * (blackCaps - whiteCaps);
            double exposedScore = -0.25 * (blackExposed - whiteExposed);
            return material + capsScore + exposedScore;
        }

        private int immediateCaptures(Checkers6x6.Board b, boolean blacksTurn) {
            Checkers6x6.Board copy = new Checkers6x6.Board(b);
            copy.blacksTurn = blacksTurn;
            int count = 0;
            for (Checkers6x6.Move m : copy.generateLegalMoves()) if (m.hasCapture()) count++;
            return count;
        }

        private int exposedPieces(Checkers6x6.Board b, boolean forBlack) {
            int exposed = 0;
            int[][] dirsManBlack = {{1, -1}, {1, 1}};
            int[][] dirsManWhite = {{-1, -1}, {-1, 1}};
            int[][] dirsKing = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};
            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    int p = b.cells[r][c];
                    if (p <= 0) continue;
                    boolean isMine = forBlack ? (p == 1 || p == 3) : (p == 2 || p == 4);
                    if (!isMine) continue;
                    int[][] dirs = (p == 1) ? dirsManBlack : (p == 2) ? dirsManWhite : dirsKing;
                    // If opponent can jump over this piece next turn: count as exposed
                    for (int[] d : dirsKing) { // opponent jumps in any direction
                        int or = r - d[0], oc = c - d[1]; // opponent would be adjacent
                        int lr = r + d[0], lc = c + d[1]; // landing after jumping over me
                        if (inBounds(or, oc) && inBounds(lr, lc) && b.cells[lr][lc] == 0) {
                            int opp = b.cells[or][oc];
                            if (opp > 0) {
                                boolean oppBlack = (opp == 1 || opp == 3);
                                if (oppBlack != forBlack) {
                                    exposed++;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return exposed;
        }

        private boolean inBounds(int r, int c) { return r >= 0 && r < 6 && c >= 0 && c < 6; }
    }
}
