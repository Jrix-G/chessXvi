// Navigation entre les ecrans : login -> choix couleur -> partie -> classement.
import { useState } from "react";
import type { Color } from "./engine/board";
import Game from "./Game";
import Leaderboard from "./Leaderboard";
import { login } from "./lib/supabase";

type Screen = "login" | "color" | "game" | "leaderboard";

export default function App() {
  const [screen, setScreen] = useState<Screen>("login");
  const [username, setUsername] = useState("");
  const [draftName, setDraftName] = useState("");
  const [draftPin, setDraftPin] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [humanColor, setHumanColor] = useState<Color>("white");

  const handleLogin = async () => {
    setError("");
    setBusy(true);
    const name = draftName.trim();
    const res = await login(name, draftPin);
    setBusy(false);
    if (!res.ok) {
      setError(res.reason ?? "Connexion impossible.");
      return;
    }
    setUsername(name);
    setScreen("color");
  };

  return (
    <div>
      <h1>♟ chessXvi</h1>

      {screen === "login" && (
        <div className="card">
          <h2 style={{ textAlign: "center" }}>Connexion</h2>
          <div style={{ maxWidth: 320, margin: "0 auto", display: "flex", flexDirection: "column", gap: 12 }}>
            <input
              value={draftName}
              onChange={(e) => setDraftName(e.target.value)}
              placeholder="Nom de joueur"
            />
            <input
              type="password"
              value={draftPin}
              onChange={(e) => setDraftPin(e.target.value)}
              placeholder="Code secret"
              onKeyDown={(e) => {
                if (e.key === "Enter" && draftName.trim() !== "" && draftPin !== "" && !busy) void handleLogin();
              }}
            />
            <p className="muted" style={{ fontSize: 13, textAlign: "center", margin: 0 }}>
              Nouveau nom : choisis un code. Nom déjà utilisé : entre ton code pour le retrouver.
            </p>
            {error && <p className="warn" style={{ textAlign: "center", margin: 0 }}>{error}</p>}
            <div style={{ textAlign: "center" }}>
              <button
                disabled={draftName.trim() === "" || draftPin === "" || busy}
                onClick={() => void handleLogin()}
              >
                {busy ? "Connexion…" : "Continuer"}
              </button>
            </div>
          </div>
          <hr style={{ border: "none", borderTop: "1px solid var(--card-border)", margin: "28px 0" }} />
          <Leaderboard />
        </div>
      )}

      {screen === "color" && (
        <div className="card" style={{ maxWidth: 380, margin: "0 auto", textAlign: "center" }}>
          <h2>Bonjour {username} !</h2>
          <p className="muted">Tu veux jouer :</p>
          <div style={{ display: "flex", gap: 12, justifyContent: "center", marginTop: 20 }}>
            <button
              onClick={() => {
                setHumanColor("white");
                setScreen("game");
              }}
            >
              Les Blancs
            </button>
            <button
              className="ghost"
              onClick={() => {
                setHumanColor("black");
                setScreen("game");
              }}
            >
              Les Noirs
            </button>
          </div>
        </div>
      )}

      {screen === "game" && (
        <Game
          username={username}
          humanColor={humanColor}
          onExit={() => setScreen("leaderboard")}
        />
      )}

      {screen === "leaderboard" && (
        <div className="card">
          <Leaderboard onBack={() => setScreen("color")} />
        </div>
      )}
    </div>
  );
}
