// Web Worker : execute la recherche du bot hors du thread UI.
// Requete (stateless) : on recoit l'historique complet des coups + la couleur du bot.
// Le worker reconstruit la partie depuis le debut, puis renvoie le meilleur coup.
import { Board, type Color, type Move } from "./engine/board.ts";
import { Network } from "./engine/network.ts";
import { Bot } from "./engine/bot.ts";

interface Req {
  id: number;
  moves: Move[];
  botColor: Color;
}

let netPromise: Promise<Network> | null = null;

function getNetwork(): Promise<Network> {
  if (netPromise === null) {
    netPromise = Network.load(import.meta.env.BASE_URL + "weights.txt");
  }
  return netPromise;
}

self.onmessage = async (e: MessageEvent<Req>) => {
  const { id, moves, botColor } = e.data;
  const net = await getNetwork();

  const board = new Board();
  for (const m of moves) {
    board.makeMove(m[0], m[1], m[2], m[3]);
  }

  const bot = new Bot(net, botColor);
  const best = bot.bestMove(board);
  (self as unknown as Worker).postMessage({ id, move: best });
};
