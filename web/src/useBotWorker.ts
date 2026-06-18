// Hook React qui gere le Web Worker du bot.
// Renvoie une fonction requestMove(moves, botColor) -> Promise<meilleur coup>.
import { useEffect, useRef } from "react";
import type { Move, Color } from "./engine/board";

export function useBotWorker() {
  const workerRef = useRef<Worker | null>(null);
  const idRef = useRef(0);
  const pending = useRef<Map<number, (m: Move | null) => void>>(new Map());

  useEffect(() => {
    const w = new Worker(new URL("./bot.worker.ts", import.meta.url), { type: "module" });
    w.onmessage = (e: MessageEvent<{ id: number; move: Move | null }>) => {
      const { id, move } = e.data;
      const resolve = pending.current.get(id);
      if (resolve) {
        pending.current.delete(id);
        resolve(move);
      }
    };
    workerRef.current = w;
    return () => w.terminate();
  }, []);

  return (moves: Move[], botColor: Color): Promise<Move | null> =>
    new Promise((resolve) => {
      const id = ++idRef.current;
      pending.current.set(id, resolve);
      workerRef.current!.postMessage({ id, moves, botColor });
    });
}
