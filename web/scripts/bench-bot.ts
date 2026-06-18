// Mesure le temps de reflexion du bot (profondeur 4) sur quelques positions.
import { readFileSync } from "node:fs";
import { Board, type Color, type Move } from "../src/engine/board.ts";
import { Network } from "../src/engine/network.ts";
import { Bot } from "../src/engine/bot.ts";

const text = readFileSync(new URL("../public/weights.txt", import.meta.url), "utf8");
const net = Network.parse(text);

// 1) Position de depart, le bot joue les Blancs.
// 2) Apres 1.e4 e5, le bot joue les Blancs (milieu d'ouverture).
const scenarios: { name: string; moves: Move[]; color: Color }[] = [
  { name: "depart (blanc)", moves: [], color: "white" },
  { name: "apres 1.e4 e5 (blanc)", moves: [[6, 4, 4, 4], [1, 4, 3, 4]], color: "white" },
];

for (const s of scenarios) {
  const board = new Board();
  for (const m of s.moves) board.makeMove(m[0], m[1], m[2], m[3]);
  const bot = new Bot(net, s.color);
  const t0 = performance.now();
  const best = bot.bestMove(board);
  const dt = performance.now() - t0;
  console.log(`${s.name}: ${dt.toFixed(0)} ms  -> coup ${best ? JSON.stringify(best) : "aucun"}`);
}
