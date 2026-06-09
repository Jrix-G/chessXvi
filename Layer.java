public class Layer {
  double[][] weights;
  double[] biases;
  double[] input;
  double[] z;
  double[][] gradW;
  double[] gradB;
  boolean useRelu = true;

  public Layer(double[][] weights, double[] biases) {
    this.weights = weights;
    this.biases = biases;
  }

  public double[] forward(double[] input) {
    this.input = input;
    int neurons = weights.length;
    double[] output = new double[neurons];
    this.z = new double[neurons];
    for (int n = 0; n < neurons; n++) {
      double zn = biases[n];
      for (int i = 0; i < input.length; i++) {
        zn += weights[n][i] * input[i];
      }
      this.z[n] = zn;
      output[n] = useRelu ? relu(zn) : zn;
    }
    return output;
  }

  double relu(double z) {
    return Math.max(0, z);
  }

  // derivative of relu (reddit): 1 if z > 0, else 0
  double reluDerivative(double z) {
    return z > 0 ? 1 : 0;
  }

  double activationDerivative(double z) {
    return useRelu ? reluDerivative(z) : 1;
  }

  public double[] backward(double[] delta) {
    int neurons = weights.length;
    int inputs = input.length;

    gradW = new double[neurons][inputs];
    gradB = new double[neurons];

    for (int n = 0; n < neurons; n++) {
      gradB[n] = delta[n];
      for (int i = 0; i < inputs; i++) {
        gradW[n][i] = delta[n] * input[i];
      }
    }

    double[] deltaPrev = new double[inputs];
    for (int i = 0; i < inputs; i++) {
      double s = 0;
      for (int n = 0; n < neurons; n++) {
        s += weights[n][i] * delta[n];
      }
      deltaPrev[i] = s;
    }
    return deltaPrev;
  }

  public static void main(String[] args) {
    double[][] weights = {
        { 0.5, -1 },
        { 1, 1 },
        { -1, 0.5 }
    };
    double[] biases = { 1, 0, 2 };
    Layer layer = new Layer(weights, biases);

    double[] input = { 2, 3 };
    double[] output = layer.forward(input);

    for (int i = 0; i < output.length; i++) {
      System.out.println("sortie[" + i + "] = " + output[i]);
    }
  }

}
