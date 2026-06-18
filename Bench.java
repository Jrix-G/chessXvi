public class Bench {
  public static void main(String[] args) {
    int games = args.length > 0 ? Integer.parseInt(args[0]) : 10;
    Network net = Network.random(new int[] { 768, 256, 1 });
    Trainer t = new Trainer(net);
    long s = System.currentTimeMillis();
    java.util.List<Trainer.Sample> data = t.generate(games);
    long e = System.currentTimeMillis();
    double sec = (e - s) / 1000.0;
    System.out.printf("%d parties en %.2fs | %.3f parties/s | %d positions | %.0f positions/s%n",
        games, sec, games / sec, data.size(), data.size() / sec);
  }
}
