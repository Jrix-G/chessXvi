public class Network {
  Layer[] layers;

  public Network(Layer[] layers) {
    this.layers = layers;
  }

  public double[] forward(double[] input) {
    double[] current = input;
    for (int k = 0; k < layers.length; k++) {
      current = layers[k].forward(current);
    }
    return current;
  }

  // error calculus
  public double cost(double[] input, double[] target) {
    double sum = 0;
    for (int i = 0; i < input.length; i++) {
      double diff = input[i] - target[i];
      sum += diff * diff;
    }
    return sum;
  }

  // derivative of cost : dC/dŷ = 2(ŷ - y)
  public double[] costDerivative(double[] predicted, double[] target) {
    double[] result = new double[predicted.length];
    for (int i = 0; i < predicted.length; i++) {
      result[i] = 2 * (predicted[i] - target[i]);
    }
    return result;
  }

  public void backprop(double[] inputVec, double[] target) {
    double[] predicted = forward(inputVec);

    int last = layers.length - 1;

    double[] dC = costDerivative(predicted, target);
    double[] delta = new double[dC.length];
    for (int n = 0; n < dC.length; n++) {
      delta[n] = dC[n] * layers[last].activationDerivative(layers[last].z[n]);
    }

    for (int k = last; k >= 0; k--) {
      layers[k].storeDelta(delta);
      if (k > 0) {
        double[] deltaPrev = layers[k].propagate(delta);
        for (int i = 0; i < deltaPrev.length; i++) {
          deltaPrev[i] *= layers[k - 1].activationDerivative(layers[k - 1].z[i]);
        }
        delta = deltaPrev;
      }
    }
  }

  public void update(double eta) {
    for (Layer L : layers) {
      L.applyGradAndNorm(eta);
    }
  }

  public double updateAndNorm(double eta) {
    double s = 0;
    for (Layer L : layers) {
      s += L.applyGradAndNorm(eta);
    }
    return Math.sqrt(s);
  }

  public static Network random(int[] sizes) {
    java.util.Random r = new java.util.Random();
    Layer[] ls = new Layer[sizes.length - 1];
    for (int k = 0; k < ls.length; k++) {
      int inputs = sizes[k];
      int neurons = sizes[k + 1];
      double[][] w = new double[neurons][inputs];
      double[] b = new double[neurons];
      for (int n = 0; n < neurons; n++) {
        for (int i = 0; i < inputs; i++) {
          w[n][i] = r.nextDouble() - 0.5;
        }
      }
      ls[k] = new Layer(w, b);
    }
    ls[ls.length - 1].useRelu = false;
    return new Network(ls);
  }

  public void save(String path) {
    java.io.File file = new java.io.File(path);
    if (file.getParentFile() != null) {
      file.getParentFile().mkdirs();
    }
    try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
      pw.println(layers.length);
      for (Layer L : layers) {
        int neurons = L.weights.length;
        int inputs = L.weights[0].length;
        pw.println(neurons + " " + inputs + " " + L.useRelu);
        for (int n = 0; n < neurons; n++) {
          StringBuilder sb = new StringBuilder();
          sb.append(L.biases[n]);
          for (int i = 0; i < inputs; i++) {
            sb.append(" ").append(L.weights[n][i]);
          }
          pw.println(sb.toString());
        }
      }
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Network load(String path) {
    try (java.util.Scanner sc = new java.util.Scanner(new java.io.File(path))) {
      sc.useLocale(java.util.Locale.US);
      int count = sc.nextInt();
      Layer[] ls = new Layer[count];
      for (int k = 0; k < count; k++) {
        int neurons = sc.nextInt();
        int inputs = sc.nextInt();
        boolean useRelu = sc.nextBoolean();
        double[][] w = new double[neurons][inputs];
        double[] b = new double[neurons];
        for (int n = 0; n < neurons; n++) {
          b[n] = sc.nextDouble();
          for (int i = 0; i < inputs; i++) {
            w[n][i] = sc.nextDouble();
          }
        }
        Layer layer = new Layer(w, b);
        layer.useRelu = useRelu;
        ls[k] = layer;
      }
      return new Network(ls);
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Network copy() {
    Layer[] ls = new Layer[layers.length];
    for (int k = 0; k < layers.length; k++) {
      Layer src = layers[k];
      int neurons = src.weights.length;
      int inputs = src.weights[0].length;
      double[][] w = new double[neurons][inputs];
      double[] b = new double[neurons];
      for (int n = 0; n < neurons; n++) {
        b[n] = src.biases[n];
        for (int i = 0; i < inputs; i++) {
          w[n][i] = src.weights[n][i];
        }
      }
      Layer L = new Layer(w, b);
      L.useRelu = src.useRelu;
      ls[k] = L;
    }
    return new Network(ls);
  }

  public static void main(String[] args) {
    Layer layer1 = new Layer(
        new double[][] { { 0.5, -1 }, { 1, 1 }, { -1, 0.5 } },
        new double[] { 1, 0, 2 });

    Layer layer2 = new Layer(
        new double[][] { { 1, 1, 1 } },
        new double[] { 0 });

    Layer[] layers = { layer1, layer2 };
    Network network = new Network(layers);

    double[] output = network.forward(new double[] { 2, 3 });

    for (int i = 0; i < output.length; i++) {
      System.out.println("sortie[" + i + "] = " + output[i]);
    }
  }
}
