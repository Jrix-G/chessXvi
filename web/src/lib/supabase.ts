// Client Supabase + lecture/ecriture des resultats des joueurs.
// Si les variables d'env ne sont pas configurees, le client est null et les
// fonctions deviennent des no-op : l'appli reste jouable en local sans base.
import { createClient, type SupabaseClient } from "@supabase/supabase-js";

const url = import.meta.env.VITE_SUPABASE_URL;
const key = import.meta.env.VITE_SUPABASE_ANON_KEY;

export const supabase: SupabaseClient | null =
  url && key ? createClient(url, key) : null;

export type Outcome = "Victoire" | "Nulle" | "Défaite";

// Ordre de qualite : on garde aussi le meilleur resultat de chaque joueur.
export const RANK: Record<Outcome, number> = {
  "Défaite": 1,
  "Nulle": 2,
  "Victoire": 3,
};

export interface ResultRow {
  username: string;
  best_result: Outcome;
  wins: number;
  draws: number;
  losses: number;
}

// Enregistre une partie : incremente le bon compteur et met a jour le meilleur resultat.
// Lecture puis ecriture (read-modify-write) : suffisant pour un projet d'apprentissage.
export async function recordGame(username: string, outcome: Outcome): Promise<void> {
  if (!supabase) return;
  const { data } = await supabase
    .from("results")
    .select("best_result,wins,draws,losses")
    .eq("username", username)
    .maybeSingle();

  const wins = (data?.wins ?? 0) + (outcome === "Victoire" ? 1 : 0);
  const draws = (data?.draws ?? 0) + (outcome === "Nulle" ? 1 : 0);
  const losses = (data?.losses ?? 0) + (outcome === "Défaite" ? 1 : 0);
  const prevBest = data?.best_result as Outcome | undefined;
  const best = !prevBest || RANK[outcome] > RANK[prevBest] ? outcome : prevBest;

  await supabase.from("results").upsert({
    username,
    best_result: best,
    wins,
    draws,
    losses,
    updated_at: new Date().toISOString(),
  });
}

// Hash SHA-256 (hex) via l'API Web Crypto du navigateur.
async function sha256Hex(s: string): Promise<string> {
  const buf = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(s));
  return [...new Uint8Array(buf)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

export interface LoginResult {
  ok: boolean;
  reason?: string;
}

// Connexion / reservation d'un nom protege par un code (PIN).
// La verification se fait cote serveur (fonction login_player) : le hash stocke
// n'est jamais envoye au navigateur. On n'enregistre jamais le code en clair.
export async function login(username: string, pin: string): Promise<LoginResult> {
  if (!supabase) return { ok: true }; // jeu local sans base : pas de protection
  const pinHash = await sha256Hex(`${username}:${pin}`);
  const { data, error } = await supabase.rpc("login_player", {
    p_username: username,
    p_pin_hash: pinHash,
  });
  if (error) return { ok: false, reason: error.message };
  if (data === true) return { ok: true };
  return { ok: false, reason: "Ce nom est déjà pris et le code est incorrect." };
}

export async function fetchLeaderboard(): Promise<ResultRow[]> {
  if (!supabase) return [];
  const { data } = await supabase
    .from("results")
    .select("username,best_result,wins,draws,losses");
  const rows = (data ?? []) as ResultRow[];
  // On masque les lignes de test et les joueurs sans partie jouee.
  // Tri : d'abord par victoires, puis par meilleur resultat.
  return rows
    .filter((r) => !r.username.startsWith("_test") && r.wins + r.draws + r.losses > 0)
    .sort((a, b) => b.wins - a.wins || RANK[b.best_result] - RANK[a.best_result]);
}
