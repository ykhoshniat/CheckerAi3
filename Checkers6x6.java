import java.util.*;

public class Checkers6x6 {

//// خانه‌ها: -1 سفید (نامعتبر)، 0 خالی، 1 مهره سیاه، 2 مهره سفید، 3 شاه سیاه، 4 شاه سفید
    public static class Board {
        int[][] cells = new int[6][6];
        boolean blacksTurn = true; // شروع بازی با سیاه

        public Board() {
            init();
        }

        public Board(Board other) {
            this.cells = new int[6][6];
            for (int r = 0; r < 6; r++) System.arraycopy(other.cells[r], 0, this.cells[r], 0, 6);
            this.blacksTurn = other.blacksTurn;
        }

        private void init() {
// مربع‌های سفید نامعتبر را به عنوان -۱ علامت بزنید، مربع‌های سیاه قابل بازی را در ابتدا به عنوان ۰ علامت بزنید.            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 6; c++) {
                    boolean blackSquare = ((r % 2 == 0) && (c % 2 == 1)) || ((r % 2 == 1) && (c % 2 == 0));
                    cells[r][c] = blackSquare ? 0 : -1;
                }
            }
// هر بازیکن ۶ مهره را روی خانه‌های سیاه دو ردیف خود قرار دهد:
// ردیف‌های سیاه: 0 و 1 (پایین برای دیدگاه سیاه اگر از 0 شماره‌گذاری کنیم)
            for (int r = 0; r <= 1; r++) {
                for (int c = 0; c < 6; c++) if (cells[r][c] == 0) cells[r][c] = 1;
            }
// ردیف‌های سفید: ۴ و ۵ (بالا مخصوص سفید)
            for (int r = 4; r <= 5; r++) {
                for (int c = 0; c < 6; c++) if (cells[r][c] == 0) cells[r][c] = 2;
            }
            blacksTurn = true;
        }

        public List<Move> generateLegalMoves() {
            int playerMan = blacksTurn ? 1 : 2;
            int playerKing = blacksTurn ? 3 : 4;

// ابتدا همه دنباله‌های ضبط را جمع‌آوری کنید (الزامی)
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

// در غیر اینصورت، حرکات ساده
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

            int[][] dirsManBlack = {{1, -1}, {1, 1}};   // فوروارد برای سیاه
            int[][] dirsManWhite = {{-1, -1}, {-1, 1}}; // فوروارد برای سفید
            int[][] dirsKing = {{1, -1}, {1, 1}, {-1, -1}, {-1, 1}};

            int[][] dirs;
            if (isKing) dirs = dirsKing;
            else dirs = isBlack ? dirsManBlack : dirsManWhite;

            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (inBounds(nr, nc) && cells[nr][nc] == 0) {
                    Move m = new Move(r, c);
                    m.addStep(nr, nc, -1, -1); // حرکت معمولی بدون گرفتن مهره حریف
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

// DFS برای پرش چندگانه
            Move start = new Move(r, c);
            boolean[] found = new boolean[1]; // علامت وجود حداقل یک حرکت پرشی
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
                        // شبیه‌سازی مرحله
                        int prevMid = cells[mr][mc];
                        int prevFrom = cells[r][c];
                        cells[r][c] = 0;
                        cells[mr][mc] = 0; // بلافاصله حذف شد
                        cells[lr][lc] = piece;

                        current.addStep(lr, lc, mr, mc);

// در صورتی که در طول زنجیره مهره تبدیل شود، جهت‌ها دوباره محاسبه می‌شوند: پیاده‌ها همچنان فقط به جلو می‌زنند (تنوع قانون).
// این پیاده‌سازی قانون استاندارد را اعمال می‌کند: مردی که در وسط نوبت شاه می‌شود، همچنان به گرفتن مهره‌ها به عنوان شاه ادامه می‌دهد.
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
                acc.add(new Move(current)); 
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
                    cells[st.captureR][st.captureC] = 0; // حذف کردن
                }
                cr = st.toR; cc = st.toC;
            }

            cells[cr][cc] = piece;

// ارتقا در صورت نیاز (پایان حرکت)
            if (!isKingPiece(piece) && shouldKinging(piece, cr)) {
                cells[cr][cc] = promote(piece);
            }
// تغییر حالت نوبت
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
//بازگشت +1 برد سیاه، -1 برد سفید، 0 مساوی/در حال انجام
            if (!isTerminal()) return 0;
// اگر یک بازیکن در نوبت خود حرکتی نداشته باشد، دیگری برنده می‌شود.
            int blackCount = 0, whiteCount = 0;
            for (int r = 0; r < 6; r++) for (int c = 0; c < 6; c++) {
                int p = cells[r][c];
                if (p == 1 || p == 3) blackCount++;
                else if (p == 2 || p == 4) whiteCount++;
            }
            if (blackCount == 0) return -1;
            if (whiteCount == 0) return +1;
// بن‌بست (هیچ حرکت قانونی وجود ندارد)
            if (generateLegalMoves().isEmpty()) {
                return blacksTurn ? -1 : +1; // بازیکن فعلی که باید حرکت کند هیچ حرکتی ندارد => او بازنده است
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