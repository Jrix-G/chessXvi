// Page classement : statistiques de chaque joueur.
import { useEffect, useState } from "react";
import { fetchLeaderboard, supabase, type ResultRow } from "./lib/supabase";

interface Props {
  onBack?: () => void;
}

export default function Leaderboard({ onBack }: Props) {
  const [rows, setRows] = useState<ResultRow[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void fetchLeaderboard().then((r) => {
      setRows(r);
      setLoading(false);
    });
  }, []);

  return (
    <div>
      <h2 style={{ textAlign: "center" }}>Classement</h2>

      {!supabase && (
        <p className="warn" style={{ textAlign: "center" }}>
          Supabase n'est pas configuré : les résultats ne sont pas encore enregistrés.
        </p>
      )}
      {loading && supabase && <p className="muted" style={{ textAlign: "center" }}>Chargement…</p>}
      {!loading && rows.length === 0 && supabase && (
        <p className="muted" style={{ textAlign: "center" }}>Aucun résultat pour l'instant.</p>
      )}

      {rows.length > 0 && (
        <table className="ranking">
          <thead>
            <tr>
              <th>Joueur</th>
              <th>Parties</th>
              <th>Victoires</th>
              <th>Défaites</th>
              <th>Nulles</th>
              <th>Meilleur</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.username}>
                <td>{r.username}</td>
                <td>{r.wins + r.draws + r.losses}</td>
                <td className="win">{r.wins}</td>
                <td className="loss">{r.losses}</td>
                <td className="draw">{r.draws}</td>
                <td>{r.best_result}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {onBack && (
        <div style={{ marginTop: 20, textAlign: "center" }}>
          <button onClick={onBack}>Rejouer</button>
        </div>
      )}
    </div>
  );
}
