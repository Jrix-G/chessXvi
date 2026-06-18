// Simule une partie complete (joueur aleatoire vs bot) pour valider la boucle :
// detection mat/pat, parite move()/makeMove (rejeu d'historique), generation FEN.
import { readFileSync } from "node:fs";
import { Board, type Color, type Move } from "../src/engine/board.ts";
import { Network } from "../src/engine/network.ts";
import { Bot } from "../src/engine/bot.ts";

const text = readFileSync(new URL("../public/weights.txt", import.meta.url), "utf8");
const net = Network.parse(text);

const humanColor: Color = "white";
const botColor: Color = "black";
const board = new Board();
const history: Move[] = [];
const bot = new Bot(net, botColor);
bot.depth = 1; // profondeur reduite pour une simulation rapide

function other(c: Color): Color {
  return c === "white" ? "black" : "white";
}

// Verifie qu'un Board reconstruit depuis l'historique (comme le worker) == board courant.
function replayMatches(): boolean {
  const b = new Board();
  for (const m of history) b.makeMove(m[0], m[1], m[2], m[3]);
  return b.toFen("white") === board.toFen("white");
}

let turn: Color = "white";
let plies = 0;
let result = "limite";
for (; plies < 400; plies++) {
  if (board.isCheckmate(turn)) {
    result = `mat (${other(turn)} gagne)`;
    break;
  }
  if (board.isStalemate(turn)) {
    result = "pat (nulle)";
    break;
  }
  let move: Move | null;
  if (turn === humanColor) {
    const moves = board.legalMoves(turn);
    move = moves[Math.floor(Math.random() * moves.length)];
    board.move(move[0], move[1], move[2], move[3]);
  } else {
    move = bot.bestMove(board);
    if (!move) break;
    board.makeMove(move[0], move[1], move[2], move[3]);
  }
  history.push(move);
  if (!replayMatches()) {
    console.log(`ECHEC parite rejeu au demi-coup ${plies + 1}`);
    process.exit(1);
  }
  turn = other(turn);
}

console.log(`Partie terminee en ${plies} demi-coups : ${result}`);
console.log(`Parite rejeu (move vs makeMove) : OK sur ${history.length} coups`);
console.log(`FEN finale : ${board.toFen(turn)}`);
