// Portage fidele de Board.java (moteur d'echecs).
// Conventions identiques au Java :
//  - ligne 0 = rangee des Noirs (haut), ligne 7 = rangee des Blancs (bas)
//  - codes pieces 1..12 (voir constantes ci-dessous), 0 = case vide
//  - couleur representee par la chaine "white" / "black"
//  - un coup est un tableau [fromRow, fromCol, toRow, toCol]

export type Color = "white" | "black";
export type Move = [number, number, number, number];

export const EMPTY = 0;
export const WP = 1, WN = 2, WB = 3, WR = 4, WQ = 5, WK = 6;
export const BP = 7, BN = 8, BB = 9, BR = 10, BQ = 11, BK = 12;

const STRAIGHT = [[1, 0], [-1, 0], [0, 1], [0, -1]];
const DIAG = [[1, 1], [1, -1], [-1, 1], [-1, -1]];
const KNIGHT = [[1, 2], [1, -2], [-1, 2], [-1, -2], [2, 1], [2, -1], [-2, 1], [-2, -1]];

export interface Undo {
  fr: number; fc: number; tr: number; tc: number;
  moved: number;
  captured: number;
  capRow: number; capCol: number;
  castling: boolean;
  prevEnPassantRow: number; prevEnPassantCol: number;
  wkm: boolean; bkm: boolean; wrk: boolean; wrq: boolean; brk: boolean; brq: boolean;
}

export class Board {
  private squares: number[][] = [
    [BR, BN, BB, BQ, BK, BB, BN, BR],
    [BP, BP, BP, BP, BP, BP, BP, BP],
    [EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY],
    [EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY],
    [EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY],
    [EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY],
    [WP, WP, WP, WP, WP, WP, WP, WP],
    [WR, WN, WB, WQ, WK, WB, WN, WR],
  ];

  private enPassantRow = -1;
  private enPassantCol = -1;
  private whiteKingMoved = false;
  private blackKingMoved = false;
  private whiteRookKingMoved = false;
  private whiteRookQueenMoved = false;
  private blackRookKingMoved = false;
  private blackRookQueenMoved = false;

  private static isWhite(p: number): boolean {
    return p >= 1 && p <= 6;
  }

  private static isBlack(p: number): boolean {
    return p >= 7;
  }

  private static sameColor(p: number, white: boolean): boolean {
    return white ? (p >= 1 && p <= 6) : (p >= 7);
  }

  private static pieceName(code: number): string {
    switch ((code - 1) % 6) {
      case 0: return "pawn";
      case 1: return "knight";
      case 2: return "bishop";
      case 3: return "rook";
      case 4: return "queen";
      default: return "king";
    }
  }

  codeAt(row: number, col: number): number {
    return this.squares[row][col];
  }

  pieceAt(row: number, col: number): string {
    const p = this.squares[row][col];
    if (p === EMPTY) {
      return "";
    }
    const color = p <= 6 ? "white" : "black";
    return color + "_" + Board.pieceName(p);
  }

  isEmpty(row: number, col: number): boolean {
    return this.squares[row][col] === EMPTY;
  }

  moved(fromRow: number, fromCol: number, toRow: number, toCol: number): void {
    this.squares[toRow][toCol] = this.squares[fromRow][fromCol];
    this.squares[fromRow][fromCol] = EMPTY;
  }

  move(fromRow: number, fromCol: number, toRow: number, toCol: number): boolean {
    if (!this.inBounds(fromRow, fromCol) || !this.inBounds(toRow, toCol)) {
      return false;
    }
    const code = this.squares[fromRow][fromCol];
    if (code === EMPTY) {
      return false;
    }
    const color: Color = code <= 6 ? "white" : "black";
    const piece = Board.pieceName(code);
    if (!this.checkIfSame(fromRow, fromCol, toRow, toCol)) return false;
    if (!this.wouldBeLegal(fromRow, fromCol, toRow, toCol)) return false;
    const castling = piece === "king" && fromRow === toRow && Math.abs(toCol - fromCol) === 2;
    this.applyMove(piece, color, fromRow, fromCol, toRow, toCol, castling);
    this.updateStateAfterMove(piece, color, fromRow, fromCol, toRow, toCol);
    return true;
  }

  private isPseudoLegal(piece: string, fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    if (piece === "pawn") return this.checkPawnMove(fromRow, fromCol, toRow, toCol, color);
    if (piece === "knight") return this.checkKnightMove(fromRow, fromCol, toRow, toCol, color);
    if (piece === "rook") return this.checkRookMove(fromRow, fromCol, toRow, toCol, color);
    if (piece === "bishop") return this.checkBishopMove(fromRow, fromCol, toRow, toCol, color);
    if (piece === "queen") return this.checkQueenMove(fromRow, fromCol, toRow, toCol, color);
    if (piece === "king") return this.checkKingMove(fromRow, fromCol, toRow, toCol, color);
    return false;
  }

  private wouldBeLegal(fromRow: number, fromCol: number, toRow: number, toCol: number): boolean {
    if (!this.inBounds(fromRow, fromCol) || !this.inBounds(toRow, toCol)) return false;
    if (fromRow === toRow && fromCol === toCol) return false;
    const code = this.squares[fromRow][fromCol];
    if (code === EMPTY) return false;
    const color: Color = code <= 6 ? "white" : "black";
    const piece = Board.pieceName(code);
    const castling = piece === "king" && fromRow === toRow && Math.abs(toCol - fromCol) === 2;
    if (castling) {
      if (!this.canCastle(fromRow, fromCol, toRow, toCol, color)) return false;
    } else {
      if (!this.isPseudoLegal(piece, fromRow, fromCol, toRow, toCol, color)) return false;
    }
    const u = this.makeMove(fromRow, fromCol, toRow, toCol);
    const safe = !this.inCheck(color);
    this.unmakeMove(u);
    return safe;
  }

  private applyMove(piece: string, color: Color, fromRow: number, fromCol: number, toRow: number, toCol: number, castling: boolean): void {
    if (castling) {
      this.moved(fromRow, fromCol, toRow, toCol);
      if (toCol > fromCol) {
        this.moved(fromRow, 7, fromRow, toCol - 1);
      } else {
        this.moved(fromRow, 0, fromRow, toCol + 1);
      }
      return;
    }
    if (piece === "pawn" && fromCol !== toCol && this.isEmpty(toRow, toCol)) {
      this.squares[fromRow][toCol] = EMPTY;
    }
    this.moved(fromRow, fromCol, toRow, toCol);
    if (piece === "pawn" && (toRow === 0 || toRow === 7)) {
      this.squares[toRow][toCol] = color === "white" ? WQ : BQ;
    }
  }

  private updateStateAfterMove(piece: string, color: Color, fromRow: number, fromCol: number, toRow: number, toCol: number): void {
    if (piece === "pawn" && Math.abs(toRow - fromRow) === 2) {
      this.enPassantRow = (fromRow + toRow) / 2;
      this.enPassantCol = fromCol;
    } else {
      this.enPassantRow = -1;
      this.enPassantCol = -1;
    }
    if (piece === "king") {
      if (color === "white") this.whiteKingMoved = true;
      else this.blackKingMoved = true;
    }
    if (piece === "rook") {
      if (fromRow === 7 && fromCol === 0) this.whiteRookQueenMoved = true;
      if (fromRow === 7 && fromCol === 7) this.whiteRookKingMoved = true;
      if (fromRow === 0 && fromCol === 0) this.blackRookQueenMoved = true;
      if (fromRow === 0 && fromCol === 7) this.blackRookKingMoved = true;
    }
    if (toRow === 7 && toCol === 0) this.whiteRookQueenMoved = true;
    if (toRow === 7 && toCol === 7) this.whiteRookKingMoved = true;
    if (toRow === 0 && toCol === 0) this.blackRookQueenMoved = true;
    if (toRow === 0 && toCol === 7) this.blackRookKingMoved = true;
  }

  private canCastle(fromRow: number, fromCol: number, _toRow: number, toCol: number, color: Color): boolean {
    if (fromCol !== 4) return false;
    const white = color === "white";
    if (white) {
      if (fromRow !== 7 || this.whiteKingMoved) return false;
    } else {
      if (fromRow !== 0 || this.blackKingMoved) return false;
    }
    const opp = this.opponent(color);
    if (this.inCheck(color)) return false;
    const kingside = toCol > fromCol;
    if (kingside) {
      if (toCol !== 6) return false;
      if (white ? this.whiteRookKingMoved : this.blackRookKingMoved) return false;
      if (this.squares[fromRow][7] !== (white ? WR : BR)) return false;
      if (!this.isEmpty(fromRow, 5) || !this.isEmpty(fromRow, 6)) return false;
      if (this.isAttacked(fromRow, 5, opp) || this.isAttacked(fromRow, 6, opp)) return false;
    } else {
      if (toCol !== 2) return false;
      if (white ? this.whiteRookQueenMoved : this.blackRookQueenMoved) return false;
      if (this.squares[fromRow][0] !== (white ? WR : BR)) return false;
      if (!this.isEmpty(fromRow, 1) || !this.isEmpty(fromRow, 2) || !this.isEmpty(fromRow, 3)) return false;
      if (this.isAttacked(fromRow, 2, opp) || this.isAttacked(fromRow, 3, opp)) return false;
    }
    return true;
  }

  checkPawnMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    if (color === "white") {
      if (fromCol === toCol && fromRow === (toRow + 1) && this.isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromCol === toCol && fromRow === 6 && fromRow === (toRow + 2) && this.isEmpty(fromRow - 1, fromCol)
          && this.isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow === (toRow + 1) && (toCol === fromCol + 1 || toCol === fromCol - 1)) {
        if (!this.isEmpty(toRow, toCol) && Board.isBlack(this.squares[toRow][toCol])) {
          return true;
        }
        if (toRow === this.enPassantRow && toCol === this.enPassantCol) {
          return true;
        }
      }
    }
    if (color === "black") {
      if (fromCol === toCol && fromRow === (toRow - 1) && this.isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromCol === toCol && fromRow === 1 && fromRow === (toRow - 2) && this.isEmpty(fromRow + 1, fromCol)
          && this.isEmpty(toRow, toCol)) {
        return true;
      }
      if (fromRow === (toRow - 1) && (toCol === fromCol + 1 || toCol === fromCol - 1)) {
        if (!this.isEmpty(toRow, toCol) && Board.isWhite(this.squares[toRow][toCol])) {
          return true;
        }
        if (toRow === this.enPassantRow && toCol === this.enPassantCol) {
          return true;
        }
      }
    }
    return false;
  }

  checkKnightMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    const dest = this.squares[toRow][toCol];
    if (dest !== EMPTY && Board.sameColor(dest, color === "white")) {
      return false;
    }
    if (fromCol + 1 === toCol && fromRow + 2 === toRow) return true;
    if (fromCol - 1 === toCol && fromRow + 2 === toRow) return true;
    if (fromCol + 2 === toCol && fromRow + 1 === toRow) return true;
    if (fromCol + 2 === toCol && fromRow - 1 === toRow) return true;
    if (fromCol - 2 === toCol && fromRow + 1 === toRow) return true;
    if (fromCol - 2 === toCol && fromRow - 1 === toRow) return true;
    if (fromCol - 1 === toCol && fromRow - 2 === toRow) return true;
    if (fromCol + 1 === toCol && fromRow - 2 === toRow) return true;
    return false;
  }

  checkRookMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    const dest = this.squares[toRow][toCol];
    if (dest !== EMPTY && Board.sameColor(dest, color === "white")) {
      return false;
    }
    if (fromRow === toRow) {
      const step = toCol > fromCol ? 1 : -1;
      for (let c = fromCol + step; c !== toCol; c += step) {
        if (!this.isEmpty(fromRow, c)) {
          return false;
        }
      }
      return true;
    }
    if (fromCol === toCol) {
      const step = toRow > fromRow ? 1 : -1;
      for (let r = fromRow + step; r !== toRow; r += step) {
        if (!this.isEmpty(r, fromCol)) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  checkBishopMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    const dest = this.squares[toRow][toCol];
    if (dest !== EMPTY && Board.sameColor(dest, color === "white")) {
      return false;
    }
    if (Math.abs(toRow - fromRow) !== Math.abs(toCol - fromCol)) {
      return false;
    }
    const rowStep = toRow > fromRow ? 1 : -1;
    const colStep = toCol > fromCol ? 1 : -1;
    let r = fromRow + rowStep;
    let c = fromCol + colStep;
    while (r !== toRow) {
      if (!this.isEmpty(r, c)) {
        return false;
      }
      r += rowStep;
      c += colStep;
    }
    return true;
  }

  checkQueenMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    if (fromRow === toRow || fromCol === toCol) {
      return this.checkRookMove(fromRow, fromCol, toRow, toCol, color);
    }
    return this.checkBishopMove(fromRow, fromCol, toRow, toCol, color);
  }

  checkKingMove(fromRow: number, fromCol: number, toRow: number, toCol: number, color: Color): boolean {
    const dest = this.squares[toRow][toCol];
    if (dest !== EMPTY && Board.sameColor(dest, color === "white")) {
      return false;
    }
    if (Math.abs(toRow - fromRow) <= 1 && Math.abs(toCol - fromCol) <= 1) {
      return true;
    }
    return false;
  }

  checkIfSame(fromRow: number, fromCol: number, toRow: number, toCol: number): boolean {
    if (fromRow !== toRow || fromCol !== toCol) {
      return true;
    } else {
      console.log("Illegal move!");
      return false;
    }
  }

  isInCheck(color: Color): boolean {
    return this.inCheck(color);
  }

  isCheckmate(color: Color): boolean {
    return this.inCheck(color) && !this.hasLegalMove(color);
  }

  isStalemate(color: Color): boolean {
    return !this.inCheck(color) && !this.hasLegalMove(color);
  }

  copy(): Board {
    const b = new Board();
    for (let row = 0; row < 8; row++) {
      for (let col = 0; col < 8; col++) {
        b.squares[row][col] = this.squares[row][col];
      }
    }
    b.enPassantRow = this.enPassantRow;
    b.enPassantCol = this.enPassantCol;
    b.whiteKingMoved = this.whiteKingMoved;
    b.blackKingMoved = this.blackKingMoved;
    b.whiteRookKingMoved = this.whiteRookKingMoved;
    b.whiteRookQueenMoved = this.whiteRookQueenMoved;
    b.blackRookKingMoved = this.blackRookKingMoved;
    b.blackRookQueenMoved = this.blackRookQueenMoved;
    return b;
  }

  makeMove(fr: number, fc: number, tr: number, tc: number): Undo {
    const u: Undo = {
      fr, fc, tr, tc,
      moved: 0, captured: 0, capRow: 0, capCol: 0,
      castling: false,
      prevEnPassantRow: this.enPassantRow,
      prevEnPassantCol: this.enPassantCol,
      wkm: this.whiteKingMoved,
      bkm: this.blackKingMoved,
      wrk: this.whiteRookKingMoved,
      wrq: this.whiteRookQueenMoved,
      brk: this.blackRookKingMoved,
      brq: this.blackRookQueenMoved,
    };

    const code = this.squares[fr][fc];
    u.moved = code;
    const white = code <= 6;
    const color: Color = white ? "white" : "black";
    const piece = Board.pieceName(code);
    const castling = piece === "king" && fr === tr && Math.abs(tc - fc) === 2;
    u.castling = castling;

    if (castling) {
      u.captured = 0;
    } else if (piece === "pawn" && fc !== tc && this.squares[tr][tc] === EMPTY) {
      u.captured = this.squares[fr][tc];
      u.capRow = fr;
      u.capCol = tc;
    } else {
      u.captured = this.squares[tr][tc];
      u.capRow = tr;
      u.capCol = tc;
    }

    this.applyMove(piece, color, fr, fc, tr, tc, castling);
    this.updateStateAfterMove(piece, color, fr, fc, tr, tc);
    return u;
  }

  unmakeMove(u: Undo): void {
    this.enPassantRow = u.prevEnPassantRow;
    this.enPassantCol = u.prevEnPassantCol;
    this.whiteKingMoved = u.wkm;
    this.blackKingMoved = u.bkm;
    this.whiteRookKingMoved = u.wrk;
    this.whiteRookQueenMoved = u.wrq;
    this.blackRookKingMoved = u.brk;
    this.blackRookQueenMoved = u.brq;

    if (u.castling) {
      this.squares[u.fr][u.fc] = this.squares[u.tr][u.tc];
      this.squares[u.tr][u.tc] = EMPTY;
      if (u.tc > u.fc) {
        this.squares[u.fr][7] = this.squares[u.fr][u.tc - 1];
        this.squares[u.fr][u.tc - 1] = EMPTY;
      } else {
        this.squares[u.fr][0] = this.squares[u.fr][u.tc + 1];
        this.squares[u.fr][u.tc + 1] = EMPTY;
      }
      return;
    }

    this.squares[u.fr][u.fc] = u.moved;
    this.squares[u.tr][u.tc] = EMPTY;
    if (u.captured !== 0) {
      this.squares[u.capRow][u.capCol] = u.captured;
    }
  }

  legalMoves(color: Color): Move[] {
    const white = color === "white";
    const moves: Move[] = [];
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const code = this.squares[r][c];
        if (code === EMPTY || !Board.sameColor(code, white)) {
          continue;
        }
        this.genPiece(code, r, c, moves);
      }
    }
    return moves;
  }

  legalMovesRef(color: Color): Move[] {
    const white = color === "white";
    const moves: Move[] = [];
    for (let fromRow = 0; fromRow < 8; fromRow++) {
      for (let fromCol = 0; fromCol < 8; fromCol++) {
        const code = this.squares[fromRow][fromCol];
        if (code === EMPTY || !Board.sameColor(code, white)) {
          continue;
        }
        for (let toRow = 0; toRow < 8; toRow++) {
          for (let toCol = 0; toCol < 8; toCol++) {
            if (this.wouldBeLegal(fromRow, fromCol, toRow, toCol)) {
              moves.push([fromRow, fromCol, toRow, toCol]);
            }
          }
        }
      }
    }
    return moves;
  }

  private tryAdd(fr: number, fc: number, tr: number, tc: number, moves: Move[]): void {
    if (!this.inBounds(tr, tc)) {
      return;
    }
    if (this.wouldBeLegal(fr, fc, tr, tc)) {
      moves.push([fr, fc, tr, tc]);
    }
  }

  private slide(fr: number, fc: number, dirs: number[][], moves: Move[]): void {
    for (const d of dirs) {
      let r = fr + d[0];
      let c = fc + d[1];
      while (this.inBounds(r, c)) {
        this.tryAdd(fr, fc, r, c, moves);
        if (this.squares[r][c] !== EMPTY) {
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
  }

  private genPiece(code: number, r: number, c: number, moves: Move[]): void {
    const type = (code - 1) % 6;
    switch (type) {
      case 0: {
        const dir = code <= 6 ? -1 : 1;
        this.tryAdd(r, c, r + dir, c, moves);
        this.tryAdd(r, c, r + 2 * dir, c, moves);
        this.tryAdd(r, c, r + dir, c - 1, moves);
        this.tryAdd(r, c, r + dir, c + 1, moves);
        break;
      }
      case 1:
        for (const o of KNIGHT) {
          this.tryAdd(r, c, r + o[0], c + o[1], moves);
        }
        break;
      case 2:
        this.slide(r, c, DIAG, moves);
        break;
      case 3:
        this.slide(r, c, STRAIGHT, moves);
        break;
      case 4:
        this.slide(r, c, STRAIGHT, moves);
        this.slide(r, c, DIAG, moves);
        break;
      case 5:
        for (let dr = -1; dr <= 1; dr++) {
          for (let dc = -1; dc <= 1; dc++) {
            if (dr === 0 && dc === 0) {
              continue;
            }
            this.tryAdd(r, c, r + dr, c + dc, moves);
          }
        }
        this.tryAdd(r, c, r, c + 2, moves);
        this.tryAdd(r, c, r, c - 2, moves);
        break;
      default:
        break;
    }
  }

  private hasLegalMove(color: Color): boolean {
    return this.legalMoves(color).length !== 0;
  }

  private inCheck(color: Color): boolean {
    const king = this.findKing(color);
    if (king === null) {
      return false;
    }
    return this.isAttacked(king[0], king[1], this.opponent(color));
  }

  private findKing(color: Color): [number, number] | null {
    const target = color === "white" ? WK : BK;
    for (let row = 0; row < 8; row++) {
      for (let col = 0; col < 8; col++) {
        if (this.squares[row][col] === target) {
          return [row, col];
        }
      }
    }
    return null;
  }

  private isAttacked(row: number, col: number, byColor: Color): boolean {
    const white = byColor === "white";
    const pawn = white ? WP : BP;
    const knight = white ? WN : BN;
    const bishop = white ? WB : BB;
    const rook = white ? WR : BR;
    const queen = white ? WQ : BQ;
    const king = white ? WK : BK;
    const pawnDir = white ? 1 : -1;
    const pr = row + pawnDir;
    if (this.inBounds(pr, col - 1) && this.squares[pr][col - 1] === pawn) {
      return true;
    }
    if (this.inBounds(pr, col + 1) && this.squares[pr][col + 1] === pawn) {
      return true;
    }
    const knightOffsets = [[1, 2], [1, -2], [-1, 2], [-1, -2], [2, 1], [2, -1], [-2, 1], [-2, -1]];
    for (const off of knightOffsets) {
      const r = row + off[0];
      const c = col + off[1];
      if (this.inBounds(r, c) && this.squares[r][c] === knight) {
        return true;
      }
    }
    for (let dr = -1; dr <= 1; dr++) {
      for (let dc = -1; dc <= 1; dc++) {
        if (dr === 0 && dc === 0) {
          continue;
        }
        const r = row + dr;
        const c = col + dc;
        if (this.inBounds(r, c) && this.squares[r][c] === king) {
          return true;
        }
      }
    }
    const straight = [[1, 0], [-1, 0], [0, 1], [0, -1]];
    for (const d of straight) {
      let r = row + d[0];
      let c = col + d[1];
      while (this.inBounds(r, c)) {
        if (this.squares[r][c] !== EMPTY) {
          if (this.squares[r][c] === rook || this.squares[r][c] === queen) {
            return true;
          }
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
    const diagonal = [[1, 1], [1, -1], [-1, 1], [-1, -1]];
    for (const d of diagonal) {
      let r = row + d[0];
      let c = col + d[1];
      while (this.inBounds(r, c)) {
        if (this.squares[r][c] !== EMPTY) {
          if (this.squares[r][c] === bishop || this.squares[r][c] === queen) {
            return true;
          }
          break;
        }
        r += d[0];
        c += d[1];
      }
    }
    return false;
  }

  private opponent(color: Color): Color {
    return color === "white" ? "black" : "white";
  }

  private inBounds(row: number, col: number): boolean {
    return row >= 0 && row < 8 && col >= 0 && col < 8;
  }

  // Helper NOUVEAU (absent du Java) : genere une chaine FEN pour react-chessboard.
  // Notre ligne 0 correspond a la rangee 8 du FEN ; les colonnes 0..7 = fichiers a..h.
  toFen(activeColor: Color): string {
    const letter: Record<number, string> = {
      [WP]: "P", [WN]: "N", [WB]: "B", [WR]: "R", [WQ]: "Q", [WK]: "K",
      [BP]: "p", [BN]: "n", [BB]: "b", [BR]: "r", [BQ]: "q", [BK]: "k",
    };
    const ranks: string[] = [];
    for (let row = 0; row < 8; row++) {
      let rank = "";
      let empty = 0;
      for (let col = 0; col < 8; col++) {
        const code = this.squares[row][col];
        if (code === EMPTY) {
          empty++;
        } else {
          if (empty > 0) { rank += empty; empty = 0; }
          rank += letter[code];
        }
      }
      if (empty > 0) rank += empty;
      ranks.push(rank);
    }
    const placement = ranks.join("/");
    const active = activeColor === "white" ? "w" : "b";

    let castling = "";
    if (!this.whiteKingMoved && !this.whiteRookKingMoved) castling += "K";
    if (!this.whiteKingMoved && !this.whiteRookQueenMoved) castling += "Q";
    if (!this.blackKingMoved && !this.blackRookKingMoved) castling += "k";
    if (!this.blackKingMoved && !this.blackRookQueenMoved) castling += "q";
    if (castling === "") castling = "-";

    let ep = "-";
    if (this.enPassantRow >= 0 && this.enPassantCol >= 0) {
      const file = String.fromCharCode("a".charCodeAt(0) + this.enPassantCol);
      const epRank = 8 - this.enPassantRow;
      ep = file + epRank;
    }

    return `${placement} ${active} ${castling} ${ep} 0 1`;
  }
}
