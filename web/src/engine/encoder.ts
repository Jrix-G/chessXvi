// Portage fidele de Encoder.java : encode l'echiquier en vecteur 768.
// 64 cases x 12 types de pieces ; indexation (row*8+col)*12 + base couleur + type.
import { Board } from "./board.ts";

export function encode(board: Board): Float64Array {
  const vec = new Float64Array(768);
  for (let row = 0; row < 8; row++) {
    for (let col = 0; col < 8; col++) {
      const code = board.codeAt(row, col);
      if (code === 0) {
        continue;
      }
      const colorBase = code <= 6 ? 0 : 6;
      const type = (code - 1) % 6;
      const base = (row * 8 + col) * 12;
      vec[base + colorBase + type] = 1;
    }
  }
  return vec;
}
