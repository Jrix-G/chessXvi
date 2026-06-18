// Portage fidele du chemin d'inference de Network.java + Layer.java.
// On ne porte QUE la passe avant (forward) : pas de backprop, pas d'entrainement.
// Le format texte des poids est identique au Java (voir Network.load / Network.save).

// Une couche dense. Les poids sont stockes a plat (neurons*inputs) pour la vitesse.
class Layer {
  neurons: number;
  inputs: number;
  weights: Float64Array; // taille neurons*inputs, ligne n = [n*inputs .. n*inputs+inputs)
  biases: Float64Array;  // taille neurons
  useRelu: boolean;

  constructor(neurons: number, inputs: number, weights: Float64Array, biases: Float64Array, useRelu: boolean) {
    this.neurons = neurons;
    this.inputs = inputs;
    this.weights = weights;
    this.biases = biases;
    this.useRelu = useRelu;
  }

  // Calcul creux : on n'itere que sur les entrees non nulles (board ~99% creux).
  forward(input: Float64Array): Float64Array {
    const neurons = this.neurons;
    const inputs = this.inputs;
    // indices des entrees non nulles
    const nz: number[] = [];
    for (let i = 0; i < inputs; i++) {
      if (input[i] !== 0) {
        nz.push(i);
      }
    }
    const c = nz.length;
    const output = new Float64Array(neurons);
    for (let n = 0; n < neurons; n++) {
      let zn = this.biases[n];
      const base = n * inputs;
      for (let k = 0; k < c; k++) {
        const i = nz[k];
        zn += this.weights[base + i] * input[i];
      }
      output[n] = this.useRelu ? (zn > 0 ? zn : 0) : zn;
    }
    return output;
  }
}

export class Network {
  layers: Layer[];

  constructor(layers: Layer[]) {
    this.layers = layers;
  }

  forward(input: Float64Array): Float64Array {
    let current = input;
    for (let k = 0; k < this.layers.length; k++) {
      current = this.layers[k].forward(current);
    }
    return current;
  }

  // Parse le format texte des poids (identique a Network.load cote Java).
  static parse(text: string): Network {
    // Tokens separes par des espaces / retours a la ligne.
    const tok = text.split(/\s+/).filter((s) => s.length > 0);
    let p = 0;
    const nextInt = () => parseInt(tok[p++], 10);
    const nextFloat = () => parseFloat(tok[p++]);
    const nextBool = () => tok[p++] === "true";

    const count = nextInt();
    const layers: Layer[] = [];
    for (let k = 0; k < count; k++) {
      const neurons = nextInt();
      const inputs = nextInt();
      const useRelu = nextBool();
      const weights = new Float64Array(neurons * inputs);
      const biases = new Float64Array(neurons);
      for (let n = 0; n < neurons; n++) {
        biases[n] = nextFloat();
        const base = n * inputs;
        for (let i = 0; i < inputs; i++) {
          weights[base + i] = nextFloat();
        }
      }
      layers.push(new Layer(neurons, inputs, weights, biases, useRelu));
    }
    return new Network(layers);
  }

  // Charge les poids depuis une URL (ex. /weights.txt servi par Vite).
  static async load(url: string): Promise<Network> {
    const res = await fetch(url);
    const text = await res.text();
    return Network.parse(text);
  }
}
