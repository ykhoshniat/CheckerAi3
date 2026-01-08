import java.util.List;
import java.util.ArrayList;
import java.util.Random;
class Agent {
    int maxDepth = 4;
    boolean useAlphaBeta = true;

    // Node counters
    long nodesMinimax = 0;
    long nodesAlphaBeta = 0;

    // Choose between two evaluators
    Evaluator eval = new Evaluator.MaterialMobilityEval();

    public Checkers6x6.Move chooseMove(Checkers6x6.Board board) {
        List<Checkers6x6.Move> moves = board.generateLegalMoves();
        if (moves.isEmpty()) return null;

        double bestVal = Double.NEGATIVE_INFINITY;
        Checkers6x6.Move best = null;

        for (Checkers6x6.Move m : moves) {
            Checkers6x6.Board nb = new Checkers6x6.Board(board);
            nb.applyMove(m);
            double val;
            if (useAlphaBeta) {
                nodesAlphaBeta++;
                val = minValueAlphaBeta(nb, maxDepth - 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            } else {
                nodesMinimax++;
                val = minValue(nb, maxDepth - 1);
            }
            if (val > bestVal) {
                bestVal = val;
                best = m;
            }
        }
        return best;
    }

    private double maxValue(Checkers6x6.Board b, int depth) {
        nodesMinimax++;
        if (depth == 0 || b.isTerminal()) return terminalOrEval(b);
        double v = Double.NEGATIVE_INFINITY;
        List<Checkers6x6.Move> moves = b.generateLegalMoves();
        if (moves.isEmpty()) return terminalOrEval(b);

        for (Checkers6x6.Move m : moves) {
            Checkers6x6.Board nb = new Checkers6x6.Board(b);
            nb.applyMove(m);
            v = Math.max(v, minValue(nb, depth - 1));
        }
        return v;
    }

    private double minValue(Checkers6x6.Board b, int depth) {
        nodesMinimax++;
        if (depth == 0 || b.isTerminal()) return terminalOrEval(b);
        double v = Double.POSITIVE_INFINITY;
        List<Checkers6x6.Move> moves = b.generateLegalMoves();
        if (moves.isEmpty()) return terminalOrEval(b);

        for (Checkers6x6.Move m : moves) {
            Checkers6x6.Board nb = new Checkers6x6.Board(b);
            nb.applyMove(m);
            v = Math.min(v, maxValue(nb, depth - 1));
        }
        return v;
    }

    private double maxValueAlphaBeta(Checkers6x6.Board b, int depth, double alpha, double beta) {
        nodesAlphaBeta++;
        if (depth == 0 || b.isTerminal()) return terminalOrEval(b);
        double v = Double.NEGATIVE_INFINITY;
        List<Checkers6x6.Move> moves = b.generateLegalMoves();
        if (moves.isEmpty()) return terminalOrEval(b);

        // Move ordering: simple heuristic (captures first)
        moves.sort((m1, m2) -> Boolean.compare(m2.hasCapture(), m1.hasCapture()));

        for (Checkers6x6.Move m : moves) {
            Checkers6x6.Board nb = new Checkers6x6.Board(b);
            nb.applyMove(m);
            v = Math.max(v, minValueAlphaBeta(nb, depth - 1, alpha, beta));
            alpha = Math.max(alpha, v);
            if (alpha >= beta) break; // prune
        }
        return v;
    }

    private double minValueAlphaBeta(Checkers6x6.Board b, int depth, double alpha, double beta) {
        nodesAlphaBeta++;
        if (depth == 0 || b.isTerminal()) return terminalOrEval(b);
        double v = Double.POSITIVE_INFINITY;
        List<Checkers6x6.Move> moves = b.generateLegalMoves();
        if (moves.isEmpty()) return terminalOrEval(b);

        moves.sort((m1, m2) -> Boolean.compare(m2.hasCapture(), m1.hasCapture()));

        for (Checkers6x6.Move m : moves) {
            Checkers6x6.Board nb = new Checkers6x6.Board(b);
            nb.applyMove(m);
            v = Math.min(v, maxValueAlphaBeta(nb, depth - 1, alpha, beta));
            beta = Math.min(beta, v);
            if (alpha >= beta) break;
        }
        return v;
    }

    private double terminalOrEval(Checkers6x6.Board b) {
        int win = b.winnerOrDraw();
        if (win == +1) return Double.POSITIVE_INFINITY / 2;  // strong positive
        if (win == -1) return Double.NEGATIVE_INFINITY / 2;  // strong negative
        return eval.evaluate(b);
    }
}
