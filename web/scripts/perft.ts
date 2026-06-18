// Verification du portage de la generation de coups.
// perft(n) = nombre de positions feuilles a profondeur n depuis la position de depart.
// Valeurs de reference standard : perft(1)=20, perft(2)=400, perft(3)=8902.
// (Aucune promotion n'apparait a ces profondeurs, donc l'auto-dame ne change rien.)
import { Board, type Color } from "../src/engine/board.ts";

function perft(board: Board, depth: number, color: Color): number {
  if (depth === 0) return 1;
  const moves = board.legalMoves(color);
  if (depth === 1) return moves.length;
  const next: Color = color === "white" ? "black" : "white";
  let total = 0;
  for (const m of moves) {
    const u = board.makeMove(m[0], m[1], m[2], m[3]);
    total += perft(board, depth - 1, next);
    board.unmakeMove(u);
  }
  return total;
}

const expected = [20, 400, 8902];
let ok = true;
for (let d = 1; d <= 3; d++) {
  const n = perft(new Board(), d, "white");
  const pass = n === expected[d - 1];
  ok = ok && pass;
  console.log(`perft(${d}) = ${n}  (attendu ${expected[d - 1]})  ${pass ? "OK" : "ECHEC"}`);
}
console.log(ok ? "\nTOUS LES PERFT PASSENT" : "\nECHEC : la generation de coups differe de la reference");
process.exit(ok ? 0 : 1);
