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

  static class Metrics {
    double lossSum = 0, gradSum = 0, vSum = 0, vSqSum = 0;
    double vMin = Double.POSITIVE_INFINITY, vMax = Double.NEGATIVE_INFINITY;
    long count = 0;

    void add(Metrics o) {
      lossSum += o.lossSum;
      gradSum += o.gradSum;
      vSum += o.vSum;
      vSqSum += o.vSqSum;
      if (o.vMin < vMin) {
        vMin = o.vMin;
      }
      if (o.vMax > vMax) {
        vMax = o.vMax;
      }
      count += o.count;
    }
  }

  int[] chooseMove(Board board, String turn, List<int[]> moves, Network mover, Bot moverBot) {
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
          ? mover.forward(Encoder.encode(sim))[0]
          : moverBot.minimax(sim, searchDepth - 1, next, -1e9, 1e9);
      boolean better = turn.equals("white") ? eval > bestEval : eval < bestEval;
      if (better) {
        bestEval = eval;
        best = m;
      }
    }
    return best;
  }

  double playGame(Network whiteNet, Bot whiteBot, Network blackNet, Bot blackBot, List<double[]> positions) {
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
      Network mover = turn.equals("white") ? whiteNet : blackNet;
      Bot moverBot = turn.equals("white") ? whiteBot : blackBot;
      int[] m = chooseMove(board, turn, moves, mover, moverBot);
      board.move(m[0], m[1], m[2], m[3]);
      positions.add(Encoder.encode(board));
      turn = turn.equals("white") ? "black" : "white";
    }
    return 0;
  }

  List<Sample> generate(int games) {
    return generate(games, null);
  }

  List<Sample> generate(int games, List<Network> pool) {
    List<Sample> data = new ArrayList<>();
    for (int g = 0; g < games; g++) {
      Network whiteNet;
      Network blackNet;
      if (pool == null || pool.size() < 2) {
        whiteNet = net;
        blackNet = net;
      } else {
        whiteNet = pool.get(r.nextInt(pool.size()));
        blackNet = pool.get(r.nextInt(pool.size()));
      }
      Bot whiteBot = new Bot(whiteNet, "white");
      Bot blackBot = new Bot(blackNet, "black");
      List<double[]> positions = new ArrayList<>();
      double z = playGame(whiteNet, whiteBot, blackNet, blackBot, positions);
      for (double[] pos : positions) {
        double material = Encoder.material(pos);
        data.add(new Sample(pos, z + 10 * material));
      }
    }
    return data;
  }

  Metrics apply(List<Sample> data) {
    Metrics mt = new Metrics();
    for (Sample s : data) {
      double p = net.forward(s.pos)[0];
      double diff = p - s.target;
      mt.lossSum += diff * diff;
      mt.vSum += p;
      mt.vSqSum += p * p;
      if (p < mt.vMin) {
        mt.vMin = p;
      }
      if (p > mt.vMax) {
        mt.vMax = p;
      }
      net.backprop(s.pos, new double[] { s.target });
      mt.gradSum += net.updateAndNorm(eta);
      mt.count++;
    }
    return mt;
  }

  public static void main(String[] args) throws Exception {
    int minutes = args.length > 0 ? Integer.parseInt(args[0]) : 30;
    int threads = args.length > 1 ? Integer.parseInt(args[1]) : 6;
    int ckpt = args.length > 2 ? Integer.parseInt(args[2]) : 10000;
    int perWorker = 50;

    java.io.PrintWriter logFile = new java.io.PrintWriter(new java.io.FileWriter("train.log"), true);
    java.util.function.Consumer<String> log = s -> {
      System.out.println(s);
      logFile.println(s);
    };

    int hidden = 256;
    int poolMax = 10;

    Network master;
    java.io.File wf = new java.io.File("weights/weights.txt");
    if (wf.exists()) {
      Network loaded = Network.load("weights/weights.txt");
      if (loaded.layers.length == 2
          && loaded.layers[0].weights.length == hidden
          && loaded.layers[0].weights[0].length == 768) {
        master = loaded;
        log.accept("Reprise depuis weights/weights.txt (768-" + hidden + "-1)");
      } else {
        master = Network.random(new int[] { 768, hidden, 1 });
        log.accept("weights.txt incompatible -> nouveau reseau 768-" + hidden + "-1");
      }
    } else {
      master = Network.random(new int[] { 768, hidden, 1 });
      log.accept("Nouveau reseau aleatoire 768-" + hidden + "-1");
    }

    Trainer masterT = new Trainer(master);
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    List<Network> league = new ArrayList<>();

    int played = 0;
    int nextCkpt = ckpt;
    long start = System.currentTimeMillis();
    long end = start + minutes * 60_000L;

    while (System.currentTimeMillis() < end) {
      List<Network> leagueSnapshot = new ArrayList<>(league);
      List<Callable<List<Sample>>> tasks = new ArrayList<>();
      for (int w = 0; w < threads; w++) {
        Trainer worker = new Trainer(master.copy());
        tasks.add(() -> worker.generate(perWorker, leagueSnapshot));
      }
      Metrics cyc = new Metrics();
      for (Future<List<Sample>> f : pool.invokeAll(tasks)) {
        cyc.add(masterT.apply(f.get()));
      }
      played += threads * perWorker;

      league.add(master.copy());
      if (league.size() > poolMax) {
        league.remove(0);
      }

      long now = System.currentTimeMillis();
      long el = (now - start) / 1000;
      long rem = Math.max(0, (end - now) / 1000);
      double loss = cyc.count > 0 ? cyc.lossSum / cyc.count : 0;
      double grad = cyc.count > 0 ? cyc.gradSum / cyc.count : 0;
      double vMean = cyc.count > 0 ? cyc.vSum / cyc.count : 0;
      double vStd = cyc.count > 0 ? Math.sqrt(Math.max(0, cyc.vSqSum / cyc.count - vMean * vMean)) : 0;
      double vMin = cyc.count > 0 ? cyc.vMin : 0;
      double vMax = cyc.count > 0 ? cyc.vMax : 0;
      log.accept(String.format(java.util.Locale.US,
          "[t=%dm%02ds | reste %dm%02ds] parties=%d | loss=%.4f | grad=%.3f | V: moy=%.3f sigma=%.3f [%.2f,%.2f] | pool=%d",
          el / 60, el % 60, rem / 60, rem % 60, played, loss, grad, vMean, vStd, vMin, vMax, league.size()));

      if (played >= nextCkpt) {
        master.save("weights/weights_" + nextCkpt + ".txt");
        nextCkpt += ckpt;
      }
    }

    pool.shutdown();
    master.save("weights/weights.txt");
    log.accept("Termine. " + played + " parties. Poids dans weights/weights.txt");
    logFile.close();
  }
}
