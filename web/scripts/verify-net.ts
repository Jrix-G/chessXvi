// Verifie que la sortie reseau TS == sortie Java sur la position de depart.
import { readFileSync } from "node:fs";
import { Board } from "../src/engine/board.ts";
import { encode } from "../src/engine/encoder.ts";
import { Network } from "../src/engine/network.ts";

const text = readFileSync(new URL("../public/weights.txt", import.meta.url), "utf8");
const net = Network.parse(text);
const out = net.forward(encode(new Board()))[0];

const javaRef = 0.360680103580554;
const diff = Math.abs(out - javaRef);
console.log(`TS   = ${out.toFixed(15)}`);
console.log(`Java = ${javaRef.toFixed(15)}`);
console.log(`ecart = ${diff.toExponential(3)}  ${diff < 1e-9 ? "OK" : "ECHEC"}`);
process.exit(diff < 1e-9 ? 0 : 1);
