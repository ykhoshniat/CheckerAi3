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

        //  Agent (Black) vs Agent (White)
        System.out.println("\n=== state2: Agent (Black) vs Agent (White) ===");
        Agent aiBlack2 = new Agent();
        aiBlack2.maxDepth = 4;
        aiBlack2.useAlphaBeta = true;
        aiBlack2.eval = new Evaluator.MaterialMobilityEval();

        Agent aiWhite = new Agent();
        aiWhite.maxDepth = 4;
        aiWhite.useAlphaBeta = true;
        aiWhite.eval = new Evaluator.ThreatCaptureEval();

        runGame(aiBlack2, aiWhite);
    }
