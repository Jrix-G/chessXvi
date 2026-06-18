public class PerftBench {
  public static void main(String[] a) {
    int depth = a.length > 0 ? Integer.parseInt(a[0]) : 4;
    Board b1 = new Board();
    long s1 = System.nanoTime();
    long n1 = PerftTest.perft(b1.copy(), "white", depth, true);
    long t1 = System.nanoTime() - s1;
    Board b2 = new Board();
    long s2 = System.nanoTime();
    long n2 = PerftTest.perft(b2.copy(), "white", depth, false);
    long t2 = System.nanoTime() - s2;
    System.out.printf("depth %d  nodes=%d%n", depth, n1);
    System.out.printf("NEW (genere par piece) : %.2f s | %.0f knodes/s%n", t1 / 1e9, n1 / (t1 / 1e9) / 1000);
    System.out.printf("REF (force brute 64x64): %.2f s | %.0f knodes/s%n", t2 / 1e9, n2 / (t2 / 1e9) / 1000);
    System.out.printf("=> speedup x%.1f%n", (double) t2 / t1);
  }
}
