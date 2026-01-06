import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        //  Agent (Black) vs Random (White)
        System.out.println("=== state1: Agent (Black) vs Random (White) ===");
        Agent aiBlack1 = new Agent();
        aiBlack1.maxDepth = 4;
        aiBlack1.useAlphaBeta = true;
        aiBlack1.eval = new Evaluator.MaterialMobilityEval();

        RandomAgent randWhite = new RandomAgent();
        runGame(aiBlack1, randWhite);

        //  Agent (Black) vs Human (White)
        System.out.println("\n=== state2: Agent (Black) vs Human (White) ===");
        Agent aiBlack2 = new Agent();
        aiBlack2.maxDepth = 4;
        aiBlack2.useAlphaBeta = true;
        aiBlack2.eval = new Evaluator.MaterialMobilityEval();

        HumanAgent humanWhite = new HumanAgent();
        runGame(aiBlack2, humanWhite);
    }

    public static void runGame(Object blackPlayer, Object whitePlayer) {
        Checkers6x6.Board board = new Checkers6x6.Board();
        Evaluator eval1 = new Evaluator.MaterialMobilityEval();
        Evaluator eval2 = new Evaluator.ThreatCaptureEval();

        int moveCount = 0;
        while (!board.isTerminal() && moveCount < 100) {
            board.print();
            Checkers6x6.Move move;
            if (board.blacksTurn) {
                move = chooseMove(blackPlayer, board);
                System.out.println("Black plays: " + move);
            } else {
                move = chooseMove(whitePlayer, board);
                System.out.println("White plays: " + move);
            }
            if (move == null) break;
            board.applyMove(move);
            moveCount++;

            double score1 = eval1.evaluate(board);
            double score2 = eval2.evaluate(board);
            System.out.println("MaterialMobilityEval score: " + score1);
            System.out.println("ThreatCaptureEval score: " + score2);
            System.out.println("------------------------------------------------");
        }

        board.print();
        int res = board.winnerOrDraw();
        System.out.println("Result: " + (res == +1 ? "Black wins" : res == -1 ? "White wins" : "Draw/ongoing"));
    }

    private static Checkers6x6.Move chooseMove(Object player, Checkers6x6.Board board) {
        if (player instanceof Agent) {
            return ((Agent) player).chooseMove(board);
        } else if (player instanceof RandomAgent) {
            return ((RandomAgent) player).chooseMove(board);
        } else if (player instanceof HumanAgent) {
            return ((HumanAgent) player).chooseMove(board);
        }
        return null;
    }

    // Random Agent
    static class RandomAgent {
        Random rng = new Random();
        public Checkers6x6.Move chooseMove(Checkers6x6.Board b) {
            List<Checkers6x6.Move> moves = b.generateLegalMoves();
            if (moves.isEmpty()) return null;
            return moves.get(rng.nextInt(moves.size()));
        }
    }

    // Human Agent
    static class HumanAgent {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        public Checkers6x6.Move chooseMove(Checkers6x6.Board b) {
            List<Checkers6x6.Move> moves = b.generateLegalMoves();
            if (moves.isEmpty()) return null;

            System.out.println("Available moves:");
            for (int i = 0; i < moves.size(); i++) {
                System.out.println(i + ": " + moves.get(i));
            }

            System.out.print("Enter move number: ");
            int choice = scanner.nextInt();
            return moves.get(choice);
        }
    }
}