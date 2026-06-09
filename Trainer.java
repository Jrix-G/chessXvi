import java.util.*;
import java.util.concurrent.*;

public class Trainer {
  Network net;
  double epsilon = 0.2;
  int maxMoves = 400;
  double eta = 1e-4;
  int searchDepth = 2;
  Bot evaluator;
  Random r = new Random();

  public Trainer(Network net) {
    this.net = net;
    this.evaluator = new Bot(net, "white");
  }

  int[] chooseMove(Board board, String turn, List<int[]> moves) {
    if (r.nextDouble() < epsilon) {
      return moves.get(r.nextInt(moves.size()));
    }
    int[] best = null;
    double bestEval = turn.equals("white") ? -1e9 : 1e9;
    for (int[] m : moves) {
      Board sim = board.copy();
      sim.move(m[0], m[1], m[2], m[3]);
      String next = turn.equals("white") ? "black" : "white";
      double eval = searchDepth <= 1
          ? net.forward(Encoder.encode(sim))[0]
          : evaluator.minimax(sim, searchDepth - 1, next, -1e9, 1e9);
      boolean better = turn.equals("white") ? eval > bestEval : eval < bestEval;
      if (better) {
        bestEval = eval;
        best = m;
      }
    }
    return best;
  }

  double playGame(List<double[]> positions) {
    Board board = new Board();
    String turn = "white";
    for (int ply = 0; ply < maxMoves; ply++) {
      List<int[]> moves = board.legalMoves(turn);
      if (moves.isEmpty()) {
        if (board.isInCheck(turn)) {
          return turn.equals("white") ? -1 : 1;
        }
        return 0;
      }
      int[] m = chooseMove(board, turn, moves);
      board.move(m[0], m[1], m[2], m[3]);
      positions.add(Encoder.encode(board));
      turn = turn.equals("white") ? "black" : "white";
    }
    return 0;
  }

  public void trainGames(int games) {
    for (int g = 0; g < games; g++) {
      List<double[]> positions = new ArrayList<>();
      double z = playGame(positions);
      for (double[] pos : positions) {
        double material = 0;
        for (double v : pos)
          material += v;
        double[] target = { z + material };
        net.backprop(pos, target);
        net.update(eta);
      }
    }
  }

  static class Sample {
    double[] pos;
    double target;

    Sample(double[] pos, double target) {
      this.pos = pos;
      this.target = target;
    }
  }

  List<Sample> generate(int games) {
    List<Sample> data = new ArrayList<>();
    for (int g = 0; g < games; g++) {
      List<double[]> positions = new ArrayList<>();
      double z = playGame(positions);
      for (double[] pos : positions) {
        double material = Encoder.material(pos);
        data.add(new Sample(pos, z + material));
      }
    }
    return data;
  }

  void apply(List<Sample> data) {
    for (Sample s : data) {
      net.backprop(s.pos, new double[] { s.target });
      net.update(eta);
    }
  }

  public static void main(String[] args) throws Exception {
    int minutes = args.length > 0 ? Integer.parseInt(args[0]) : 30;
    int threads = args.length > 1 ? Integer.parseInt(args[1]) : 6;
    int ckpt = args.length > 2 ? Integer.parseInt(args[2]) : 10000;
    int perWorker = 50;

    Network master = Network.random(new int[] { 768, 100, 1 });
    Trainer masterT = new Trainer(master);
    ExecutorService pool = Executors.newFixedThreadPool(threads);

    int played = 0;
    int nextCkpt = ckpt;
    long end = System.currentTimeMillis() + minutes * 60_000L;

    while (System.currentTimeMillis() < end) {
      List<Callable<List<Sample>>> tasks = new ArrayList<>();
      for (int w = 0; w < threads; w++) {
        Trainer worker = new Trainer(master.copy());
        tasks.add(() -> worker.generate(perWorker));
      }
      for (Future<List<Sample>> f : pool.invokeAll(tasks)) {
        masterT.apply(f.get());
      }
      played += threads * perWorker;
      System.out.println("Parties jouées: " + played);
      if (played >= nextCkpt) {
        master.save("weights/weights_" + nextCkpt + ".txt");
        nextCkpt += ckpt;
      }
    }

    pool.shutdown();
    master.save("weights/weights.txt");
    System.out.println("Termine. " + played + " parties. Poids dans weights/weights.txt");
  }
}
