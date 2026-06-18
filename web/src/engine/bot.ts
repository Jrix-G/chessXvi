// Portage fidele de Bot.java : minimax + alpha-beta + recherche de quiescence,
// avec evaluation par le reseau de neurones. Le bot ne fait que jouer (inference).
import { Board, type Color, type Move } from "./board.ts";
import { encode } from "./encoder.ts";
import { Network } from "./network.ts";

const MATE = 1e6;
const QMAX = 6;

export class Bot {
  network: Network;
  color: Color;
  depth = 4;

  constructor(network: Network, color: Color) {
    this.network = network;
    this.color = color;
  }

  bestMove(board: Board): Move | null {
    let best: Move | null = null;
    let bestEval = this.color === "white" ? -1e9 : 1e9;
    const moves = board.legalMoves(this.color);
    this.orderMoves(board, moves);
    const next: Color = this.color === "white" ? "black" : "white";
    for (const m of moves) {
      const u = board.makeMove(m[0], m[1], m[2], m[3]);
      const evalScore = this.minimax(board, this.depth - 1, next, -1e9, 1e9);
      board.unmakeMove(u);
      const better = this.color === "white" ? evalScore > bestEval : evalScore < bestEval;
      if (better) {
        bestEval = evalScore;
        best = m;
      }
    }
    return best;
  }

  private minimax(board: Board, d: number, turn: Color, alpha: number, beta: number): number {
    if (d === 0) {
      return this.quiesce(board, turn, alpha, beta, 0);
    }
    const moves = board.legalMoves(turn);
    if (moves.length === 0) {
      if (board.isInCheck(turn)) {
        return turn === "white" ? -(MATE + d) : (MATE + d);
      }
      return 0;
    }
    this.orderMoves(board, moves);
    const next: Color = turn === "white" ? "black" : "white";
    if (turn === "white") {
      let best = -1e9;
      for (const m of moves) {
        const u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.max(best, this.minimax(board, d - 1, next, alpha, beta));
        board.unmakeMove(u);
        alpha = Math.max(alpha, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    } else {
      let best = 1e9;
      for (const m of moves) {
        const u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.min(best, this.minimax(board, d - 1, next, alpha, beta));
        board.unmakeMove(u);
        beta = Math.min(beta, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    }
  }

  private quiesce(board: Board, turn: Color, alpha: number, beta: number, qd: number): number {
    const standPat = this.network.forward(encode(board))[0];
    if (qd >= QMAX) {
      return standPat;
    }
    const next: Color = turn === "white" ? "black" : "white";
    const caps = this.captures(board, turn);
    this.orderMoves(board, caps);
    if (turn === "white") {
      let best = standPat;
      if (best >= beta) {
        return best;
      }
      if (best > alpha) {
        alpha = best;
      }
      for (const m of caps) {
        const u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.max(best, this.quiesce(board, next, alpha, beta, qd + 1));
        board.unmakeMove(u);
        alpha = Math.max(alpha, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    } else {
      let best = standPat;
      if (best <= alpha) {
        return best;
      }
      if (best < beta) {
        beta = best;
      }
      for (const m of caps) {
        const u = board.makeMove(m[0], m[1], m[2], m[3]);
        best = Math.min(best, this.quiesce(board, next, alpha, beta, qd + 1));
        board.unmakeMove(u);
        beta = Math.min(beta, best);
        if (alpha >= beta) {
          break;
        }
      }
      return best;
    }
  }

  private captures(board: Board, turn: Color): Move[] {
    const all = board.legalMoves(turn);
    const caps: Move[] = [];
    for (const m of all) {
      if (!board.isEmpty(m[2], m[3])) {
        caps.push(m);
        continue;
      }
      const code = board.codeAt(m[0], m[1]);
      const pawn = code === 1 || code === 7;
      if (pawn && m[1] !== m[3]) {
        caps.push(m);
      }
    }
    return caps;
  }

  private orderMoves(board: Board, moves: Move[]): void {
    moves.sort((a, b) => this.victim(board, b) - this.victim(board, a));
  }

  private victim(board: Board, m: Move): number {
    if (board.isEmpty(m[2], m[3])) {
      return 0;
    }
    const code = board.codeAt(m[2], m[3]);
    const val = [1, 3, 3, 5, 9, 100];
    return val[(code - 1) % 6];
  }
}
