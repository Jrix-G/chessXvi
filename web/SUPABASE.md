# Étape 6 — Configuration Supabase

L'appli fonctionne déjà sans Supabase (le classement affiche juste un message).
Pour activer l'enregistrement en ligne des meilleurs résultats :

## 1. Créer le projet
1. Va sur https://supabase.com → crée un compte → **New project** (plan gratuit).
2. Note l'**URL** et l'**anon key** dans *Project Settings → API*.

## 2. Créer la table `results`
Dans *SQL Editor*, exécute :

```sql
create table results (
  username text primary key,
  best_result text not null,
  updated_at timestamptz default now()
);

-- Active RLS et autorise lecture + écriture publiques (jeu sans authentification).
alter table results enable row level security;

create policy "lecture publique" on results
  for select using (true);

create policy "ecriture publique" on results
  for insert with check (true);

create policy "maj publique" on results
  for update using (true) with check (true);
```

## 3. Brancher l'appli
```bash
cp .env.example .env.local
# puis édite .env.local avec ton URL et ton anon key
npm run dev
```

À la fin d'une partie, le résultat (`Victoire` / `Nulle` / `Défaite`) est enregistré,
et la page **Classement** liste `Username — Meilleur résultat: …`.

> Remarque : ces règles RLS sont volontairement ouvertes (n'importe qui peut écrire),
> ce qui convient à un projet d'apprentissage en local. On les durcira si on passe en ligne.
