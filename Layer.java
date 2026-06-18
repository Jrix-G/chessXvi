public class Layer {
  double[][] weights;
  double[] biases;
  double[] input;
  double[] z;
  double[] delta;
  int[] nz;
  int nzCount;
  boolean useRelu = true;

  public Layer(double[][] weights, double[] biases) {
    this.weights = weights;
    this.biases = biases;
  }

  public double[] forward(double[] input) {
    this.input = input;
    int neurons = weights.length;
    int inputs = input.length;
    if (nz == null || nz.length < inputs) {
      nz = new int[inputs];
    }
    int c = 0;
    for (int i = 0; i < inputs; i++) {
      if (input[i] != 0) {
        nz[c++] = i;
      }
    }
    nzCount = c;
    double[] output = new double[neurons];
    this.z = new double[neurons];
    for (int n = 0; n < neurons; n++) {
      double zn = biases[n];
      double[] wn = weights[n];
      for (int k = 0; k < c; k++) {
        int i = nz[k];
        zn += wn[i] * input[i];
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

  public void storeDelta(double[] delta) {
    this.delta = delta;
  }

  public double[] propagate(double[] delta) {
    int neurons = weights.length;
    int inputs = input.length;
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

  public double applyGradAndNorm(double eta) {
    int neurons = weights.length;
    double dn2 = 0;
    for (int n = 0; n < neurons; n++) {
      double dn = delta[n];
      dn2 += dn * dn;
      biases[n] -= eta * dn;
      double e = eta * dn;
      double[] wn = weights[n];
      for (int k = 0; k < nzCount; k++) {
        int i = nz[k];
        wn[i] -= e * input[i];
      }
    }
    double xi2 = 0;
    for (int k = 0; k < nzCount; k++) {
      double xi = input[nz[k]];
      xi2 += xi * xi;
    }
    return dn2 * xi2 + dn2;
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
