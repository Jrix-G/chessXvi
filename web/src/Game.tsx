// Ecran de partie : echiquier interactif contre le bot.
import { useCallback, useEffect, useRef, useState } from "react";
import { Chessboard } from "react-chessboard";
import { Board, type Color, type Move } from "./engine/board";
import { squareToRC } from "./lib/squares";
import { useBotWorker } from "./useBotWorker";
import { recordGame, type Outcome } from "./lib/supabase";

const MAX_PLIES = 400; // au-dela, partie nulle (cf. Trainer.maxMoves cote Java)

type Status = "playing" | "thinking" | "over";

interface Props {
  username: string;
  humanColor: Color;
  onExit: () => void;
}

export default function Game({ username, humanColor, onExit }: Props) {
  const botColor: Color = humanColor === "white" ? "black" : "white";
  const requestMove = useBotWorker();

  const boardRef = useRef(new Board());
  const historyRef = useRef<Move[]>([]);
  const [fen, setFen] = useState(() => boardRef.current.toFen("white"));
  const [turn, setTurn] = useState<Color>("white");
  const [status, setStatus] = useState<Status>("playing");
  const [message, setMessage] = useState("");

  // Termine la partie : determine le resultat du joueur et l'enregistre.
  const finish = useCallback(
    (outcome: Outcome, msg: string) => {
      setStatus("over");
      setMessage(msg);
      void recordGame(username, outcome);
    },
    [username],
  );

  // Verifie mat / pat pour la couleur qui doit jouer. Renvoie true si la partie est finie.
  const checkEnd = useCallback(
    (sideToMove: Color): boolean => {
      const b = boardRef.current;
      if (b.isCheckmate(sideToMove)) {
        if (sideToMove === humanColor) finish("Défaite", "Échec et mat — tu as perdu.");
        else finish("Victoire", "Échec et mat — tu as gagné !");
        return true;
      }
      if (b.isStalemate(sideToMove)) {
        finish("Nulle", "Pat — partie nulle.");
        return true;
      }
      if (historyRef.current.length >= MAX_PLIES) {
        finish("Nulle", "Limite de coups atteinte — partie nulle.");
        return true;
      }
      return false;
    },
    [humanColor, finish],
  );

  // Demande un coup au bot puis l'applique.
  const playBot = useCallback(async () => {
    setStatus("thinking");
    const move = await requestMove(historyRef.current.slice(), botColor);
    if (!move) {
      // pas de coup : mat ou pat du bot (deja gere par checkEnd avant l'appel)
      return;
    }
    const b = boardRef.current;
    b.makeMove(move[0], move[1], move[2], move[3]);
    historyRef.current.push(move);
    setFen(b.toFen(humanColor));
    setTurn(humanColor);
    if (!checkEnd(humanColor)) {
      setStatus("playing");
    }
  }, [requestMove, botColor, humanColor, checkEnd]);

  // Si le joueur est Noir, le bot (Blanc) ouvre la partie.
  const started = useRef(false);
  useEffect(() => {
    if (started.current) return;
    started.current = true;
    if (botColor === "white") {
      void playBot();
    }
  }, [botColor, playBot]);

  // Coup du joueur via glisser-deposer.
  const onPieceDrop = useCallback(
    ({ sourceSquare, targetSquare }: { sourceSquare: string; targetSquare: string | null }) => {
      if (status !== "playing" || turn !== humanColor || !targetSquare) return false;
      const [fr, fc] = squareToRC(sourceSquare);
      const [tr, tc] = squareToRC(targetSquare);
      const b = boardRef.current;
      const ok = b.move(fr, fc, tr, tc);
      if (!ok) return false;
      historyRef.current.push([fr, fc, tr, tc]);
      setFen(b.toFen(botColor));
      setTurn(botColor);
      if (!checkEnd(botColor)) {
        void playBot();
      }
      return true;
    },
    [status, turn, humanColor, botColor, checkEnd, playBot],
  );

  const myTurn = status === "playing" && turn === humanColor;

  return (
    <div className="card" style={{ maxWidth: 560, margin: "0 auto", textAlign: "center" }}>
      <h2>
        {username} — tu joues {humanColor === "white" ? "les Blancs" : "les Noirs"}
      </h2>
      <p className="muted" style={{ minHeight: 24 }}>
        {status === "thinking" && "Le bot réfléchit…"}
        {myTurn && "À toi de jouer."}
        {status === "over" && message}
      </p>
      <div style={{ width: 480, maxWidth: "100%", margin: "12px auto" }}>
        <Chessboard
          options={{
            position: fen,
            boardOrientation: humanColor,
            allowDragging: myTurn,
            onPieceDrop,
            id: "board",
          }}
        />
      </div>
      <div style={{ marginTop: 16 }}>
        <button className="ghost" onClick={onExit}>Quitter / Classement</button>
      </div>
    </div>
  );
}
