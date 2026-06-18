// Conversion entre la notation algebrique ("e4") et nos coordonnees (row, col).
// Rappel : notre ligne 0 = rangee 8 (haut), donc row = 8 - rank ; col = fichier - 'a'.

export function squareToRC(sq: string): [number, number] {
  const col = sq.charCodeAt(0) - 97; // 'a'
  const rank = parseInt(sq[1], 10);
  const row = 8 - rank;
  return [row, col];
}

export function rcToSquare(row: number, col: number): string {
  const file = String.fromCharCode(97 + col);
  const rank = 8 - row;
  return `${file}${rank}`;
}
