# Cours complet : les réseaux de neurones (maths + informatique)

> Ce cours part **de zéro**. Il explique à la fois les **mathématiques** (ce qui se
> passe vraiment) et l'**informatique** (comment on le code, en lien avec
> `Layer.java` et `Network.java` du projet). Chaque notion est expliquée avant
> d'être utilisée. L'objectif est que **tu comprennes tout**, sans trou.

---

## Tableau des chapitres

| #  | Chapitre | Ce qu'on y apprend | État |
|----|----------|--------------------|------|
| 0  | [Intuition générale](#chapitre-0--cest-quoi-une-ia) | Ce qu'est (et n'est pas) un réseau de neurones | ✅ |
| 1  | [Le neurone formel](#chapitre-1--le-neurone-formel) | Somme pondérée, poids, biais | ✅ codé |
| 2  | [Les fonctions d'activation](#chapitre-2--les-fonctions-dactivation) | ReLU, sigmoïde, pourquoi elles sont indispensables | ✅ codé (ReLU) |
| 3  | [Rappels de maths : vecteurs & matrices](#chapitre-3--rappels-de-maths-vecteurs-et-matrices) | Le langage des réseaux | ✅ |
| 4  | [La couche de neurones](#chapitre-4--la-couche-de-neurones) | Plusieurs neurones en parallèle = un produit matriciel | ✅ codé |
| 5  | [Le réseau complet (propagation avant)](#chapitre-5--le-réseau-complet-propagation-avant) | Empiler les couches | ✅ codé |
| 6  | [Faire « apprendre » : la fonction de coût](#chapitre-6--faire-apprendre-la-fonction-de-coût) | Mesurer l'erreur | ⏳ à faire |
| 7  | [Rappels de maths : dérivées & gradient](#chapitre-7--rappels-de-maths-dérivées-et-gradient) | L'outil pour minimiser l'erreur | ⏳ à faire |
| 8  | [La descente de gradient](#chapitre-8--la-descente-de-gradient) | Comment ajuster les poids | ⏳ à faire |
| 9  | [La rétropropagation](#chapitre-9--la-rétropropagation-backpropagation) | Calculer le gradient efficacement | ⏳ à faire |
| 10 | [L'entraînement en pratique](#chapitre-10--lentraînement-en-pratique) | Epochs, batches, learning rate | ⏳ à faire |
| 11 | [Application aux échecs](#chapitre-11--application-aux-échecs) | Relier tout ça au projet chessXvi | ⏳ à faire |

> 👉 **Où on en est aujourd'hui** : voir la section [« Où on se situe »](#où-on-se-situe-dans-le-cours) tout en bas.

---

## Chapitre 0 : c'est quoi une IA ?

Quand on dit « intelligence artificielle » dans ce cours, on parle d'un objet
mathématique précis : un **réseau de neurones**. Ce n'est **pas magique**. C'est
une **fonction**.

Une fonction, c'est une boîte qui prend des nombres en **entrée** et renvoie des
nombres en **sortie** :

```
entrée  →  [ FONCTION ]  →  sortie
{2, 3}  →  [   ???    ]  →  {6.5}
```

La seule particularité d'un réseau de neurones, c'est que cette fonction contient
**beaucoup de réglages** (les « poids ») — souvent des milliers ou des millions.
« Entraîner une IA », ça veut juste dire : **trouver les bons réglages** pour que
la sortie soit celle qu'on veut.

Tout le cours tourne autour de deux questions :

1. **Comment la fonction calcule sa sortie ?** → chapitres 1 à 5 (la *propagation avant*).
2. **Comment on trouve les bons réglages ?** → chapitres 6 à 10 (l'*apprentissage*).

---

## Chapitre 1 : le neurone formel

### L'idée

Un **neurone** est la plus petite brique. Il reçoit plusieurs nombres en entrée,
et en produit **un seul** en sortie. Pour cela il fait trois choses :

1. il **pondère** chaque entrée (chaque entrée a un *poids* = son importance),
2. il **additionne** le tout, plus un nombre fixe appelé *biais*,
3. il passe le résultat dans une *fonction d'activation* (chapitre 2).

### Les mathématiques

Notons les entrées \( x_1, x_2, \dots, x_n \), les poids \( w_1, w_2, \dots, w_n \),
et le biais \( b \). Le neurone calcule d'abord une valeur appelée **\( z \)** (la
*pré-activation*) :

$$
z = b + w_1 x_1 + w_2 x_2 + \dots + w_n x_n
$$

Qu'on écrit de façon compacte avec le symbole somme \( \sum \) (« sigma », qui veut
juste dire « additionne tout ») :

$$
z = b + \sum_{i=1}^{n} w_i \, x_i
$$

- **\( w_i \) grand** → l'entrée \( x_i \) compte beaucoup.
- **\( w_i \) négatif** → l'entrée \( x_i \) joue « contre ».
- **\( b \) (biais)** → décale le résultat. C'est comme l'ordonnée à l'origine d'une
  droite : il permet au neurone de ne pas être forcé de passer par zéro.

Puis on applique l'activation \( f \) (chapitre 2) pour obtenir la sortie \( a \) :

$$
a = f(z)
$$

### Le lien avec le code

Dans `Layer.java`, pour **un** neurone `n`, on retrouve exactement ces formules :

```java
double z = biases[n];                 // z = b
for (int i = 0; i < input.length; i++) {
    z += weights[n][i] * input[i];    // z += w_i * x_i
}
output[n] = relu(z);                  // a = f(z)
```

- `biases[n]` = le biais \( b \) du neurone.
- `weights[n][i]` = le poids \( w_i \) (la i-ème entrée du n-ième neurone).
- `relu(z)` = la fonction d'activation \( f \).

> **À retenir** : un neurone = `biais + somme(poids × entrées)`, puis activation.

---

## Chapitre 2 : les fonctions d'activation

### Pourquoi en a-t-on besoin ?

Sans activation, un neurone ne fait qu'une **somme pondérée**, c'est-à-dire une
opération **linéaire** (en gros : des droites, des plans). Or si on empile
plusieurs couches linéaires, le résultat reste… linéaire. On aurait beau mettre
1000 couches, on ne pourrait représenter que des droites. Inutile.

La **fonction d'activation** introduit de la **non-linéarité** : c'est elle qui
permet au réseau d'apprendre des formes complexes (courbes, frontières, motifs).

### ReLU (celle utilisée dans le projet)

$$
\text{ReLU}(z) = \max(0,\ z)
$$

En clair : si \( z \) est négatif, on renvoie 0 ; sinon on garde \( z \).

```
        sortie
          |        /
          |      /
          |    /
   _______|__/_________  z
          0
```

```java
double relu(double z) {
    return Math.max(0, z);
}
```

C'est la plus utilisée aujourd'hui : simple, rapide, et efficace.

### La sigmoïde (à connaître)

$$
\sigma(z) = \frac{1}{1 + e^{-z}}
$$

Elle « écrase » n'importe quel nombre entre **0 et 1**. Pratique quand on veut une
**probabilité** (ex : « probabilité que ce coup soit bon = 0.87 »). On la
rencontrera au moment d'interpréter la sortie du réseau.

> **À retenir** : l'activation rend le réseau capable d'apprendre autre chose que
> des droites. ReLU = `max(0, z)`.

---

## Chapitre 3 : rappels de maths (vecteurs et matrices)

Avant d'aller plus loin, il faut le vocabulaire. C'est le **langage** des réseaux.

### Un vecteur

Un **vecteur** est juste une **liste ordonnée de nombres**. En code : un tableau
`double[]`.

$$
x = \begin{pmatrix} 2 \\ 3 \end{pmatrix}
\qquad\Longleftrightarrow\qquad
\texttt{double[] x = \{2, 3\};}
$$

### Une matrice

Une **matrice** est un **tableau de nombres à 2 dimensions** (lignes × colonnes).
En code : un `double[][]`.

$$
W = \begin{pmatrix} 0.5 & -1 \\ 1 & 1 \\ -1 & 0.5 \end{pmatrix}
\qquad\Longleftrightarrow\qquad
\texttt{double[][] W = \{\{0.5,-1\},\{1,1\},\{-1,0.5\}\};}
$$

Cette matrice a **3 lignes** et **2 colonnes**. Dans un réseau :
- **1 ligne = 1 neurone**,
- **le nombre de colonnes = le nombre d'entrées**.

Donc cette matrice décrit **3 neurones**, chacun attendant **2 entrées**. C'est
exactement `layer1` dans `Network.java`.

### Le produit matrice × vecteur

C'est **l'opération centrale** des réseaux. Multiplier la matrice \( W \) par le
vecteur \( x \) donne un nouveau vecteur, où **chaque ligne de \( W \)** est
combinée avec \( x \) par une somme pondérée :

$$
W x = \begin{pmatrix} 0.5 & -1 \\ 1 & 1 \\ -1 & 0.5 \end{pmatrix}
\begin{pmatrix} 2 \\ 3 \end{pmatrix}
=
\begin{pmatrix} 0.5\cdot2 + (-1)\cdot3 \\ 1\cdot2 + 1\cdot3 \\ -1\cdot2 + 0.5\cdot3 \end{pmatrix}
=
\begin{pmatrix} -1 \\ 5 \\ -0.5 \end{pmatrix}
$$

👉 **Remarque essentielle** : chaque ligne du résultat est *exactement* la somme
pondérée d'un neurone (chapitre 1) ! Le produit matrice × vecteur **calcule tous
les neurones d'une couche d'un coup**.

Si on ajoute le vecteur des biais \( b \) puis l'activation, on obtient toute la
couche :

$$
a = f(Wx + b)
$$

Retiens cette formule : **c'est tout un réseau en une ligne**, répétée couche
après couche.

---

## Chapitre 4 : la couche de neurones

Une **couche** (`Layer`) = un groupe de neurones qui reçoivent **les mêmes
entrées** et produisent **chacun une sortie**. Mathématiquement, c'est la formule
\( a = f(Wx + b) \) du chapitre 3.

### Correspondance maths ↔ code

| Maths | Code (`Layer.java`) | Signification |
|-------|---------------------|---------------|
| \( W \) | `weights` (`double[][]`) | 1 ligne par neurone |
| \( b \) | `biases` (`double[]`)   | 1 biais par neurone |
| \( x \) | `input` (`double[]`)    | le vecteur d'entrée |
| \( Wx + b \) | la variable `z`    | la pré-activation |
| \( f \) | `relu(...)`             | l'activation |
| \( a \) | `output` (`double[]`)   | le vecteur de sortie |

```java
public double[] forward(double[] input) {
    int neurons = weights.length;          // nb de lignes de W = nb de neurones
    double[] output = new double[neurons];
    for (int n = 0; n < neurons; n++) {    // pour chaque neurone (ligne de W)
      double z = biases[n];                // z = b[n]
      for (int i = 0; i < input.length; i++) {
        z += weights[n][i] * input[i];     // z += W[n][i] * x[i]   (produit ligne·vecteur)
      }
      output[n] = relu(z);                 // a[n] = f(z)
    }
    return output;
}
```

La **double boucle** Java fait *à la main* le produit matrice × vecteur :
- la boucle `n` parcourt les **lignes** (les neurones),
- la boucle `i` parcourt les **colonnes** (les entrées).

### Exemple chiffré (la couche 1 du projet)

Entrée \( x = \{2, 3\} \), avec la matrice `layer1` ci-dessus et les biais
\( b = \{1, 0, 2\} \) :

| Neurone | \( z = b + \sum w_i x_i \) | \( a = \text{ReLU}(z) \) |
|---------|----------------------------|--------------------------|
| 0 | \( 1 + 0.5\cdot2 - 1\cdot3 = -1 \)     | **0**   |
| 1 | \( 0 + 1\cdot2 + 1\cdot3 = 5 \)        | **5**   |
| 2 | \( 2 - 1\cdot2 + 0.5\cdot3 = 1.5 \)    | **1.5** |

Sortie de la couche : \( \{0,\ 5,\ 1.5\} \).

---

## Chapitre 5 : le réseau complet (propagation avant)

Un **réseau** (`Network`) est une **suite de couches**. La sortie d'une couche
devient l'entrée de la suivante. On appelle ça la **propagation avant** (*forward
pass*).

$$
x \;\xrightarrow{\text{couche 1}}\; a^{(1)} \;\xrightarrow{\text{couche 2}}\; a^{(2)} \;\to\; \dots \;\to\; \text{sortie}
$$

```java
public double[] forward(double[] input) {
    double[] current = input;                  // on part de l'entrée
    for (int k = 0; k < layers.length; k++) {  // pour chaque couche...
      current = layers[k].forward(current);    // ...la sortie devient la nouvelle entrée
    }
    return current;
}
```

La variable `current` est le **relais** qui circule de couche en couche.

### Exemple chiffré complet (le réseau 2 → 3 → 1 du projet)

- **Couche 1** : entrée \( \{2,3\} \) → sortie \( \{0, 5, 1.5\} \) (calcul du chapitre 4).
- **Couche 2** : 1 neurone, poids \( \{1,1,1\} \), biais \( 0 \) :
  $$ z = 0 + 1\cdot0 + 1\cdot5 + 1\cdot1.5 = 6.5 \quad\Rightarrow\quad \text{ReLU}(6.5) = 6.5 $$
- **Sortie finale** : \( \{6.5\} \).

🎉 À ce stade, **le réseau sait calculer une sortie**. C'est exactement ce que fait
ton code aujourd'hui. **Mais les poids sont posés à la main** : le réseau ne sait
pas encore *apprendre*. C'est tout l'objet des chapitres suivants.

---

## Chapitre 6 : faire « apprendre » — la fonction de coût

### Le problème

Pour l'instant on a **choisi** les poids nous-mêmes. Apprendre, c'est laisser la
machine **trouver les poids toute seule**, à partir d'**exemples**.

Un exemple = une entrée + la **bonne réponse attendue** (la « cible », notée
\( y \)). Ex : entrée = une position d'échecs, cible = « cette position vaut +3
pour les blancs ».

### Mesurer l'erreur

Le réseau produit une prédiction \( \hat{y} \) (« y chapeau »). On compare à la
cible \( y \) avec une **fonction de coût** (ou *perte*, *loss*). La plus simple
est l'**erreur quadratique** :

$$
C = (\hat{y} - y)^2
$$

- Si \( \hat{y} = y \) → coût **0** (parfait).
- Plus on est loin, plus le coût est **grand** (et le carré pénalise fort les gros
  écarts).

Pour plusieurs sorties, on additionne (ou on fait la moyenne) sur toutes.

> **Idée-clé du cours** : apprendre = **rendre \( C \) le plus petit possible** en
> ajustant les poids et les biais. C'est un problème de **minimisation**. L'outil
> mathématique pour minimiser une fonction, c'est la **dérivée** → chapitre 7.

---

## Chapitre 7 : rappels de maths — dérivées et gradient

### La dérivée : la « pente »

La **dérivée** d'une fonction en un point, c'est sa **pente** à cet endroit :
elle dit *dans quel sens* et *à quelle vitesse* la fonction monte ou descend.

- Dérivée **positive** → la fonction **monte** (si j'augmente l'entrée, la sortie augmente).
- Dérivée **négative** → la fonction **descend**.
- Dérivée **nulle** → on est sur un **plat** (sommet ou creux).

Notation : \( \dfrac{dC}{dw} \) = « de combien varie le coût \( C \) si je bouge un
tout petit peu le poids \( w \) ».

### Le gradient : la pente en plusieurs dimensions

Quand le coût dépend de **plein** de poids à la fois, on regarde la dérivée par
rapport à **chacun** (les *dérivées partielles*, notées \( \partial \)). Le
**gradient** est le vecteur qui les rassemble :

$$
\nabla C = \left( \frac{\partial C}{\partial w_1},\ \frac{\partial C}{\partial w_2},\ \dots \right)
$$

Propriété fondamentale : **le gradient pointe dans la direction où le coût augmente
le plus vite**. Donc pour **diminuer** le coût, il faut aller dans le sens
**opposé** au gradient. C'est tout le chapitre 8.

### Une dérivée dont on aura besoin

La dérivée de \( C = (\hat{y}-y)^2 \) par rapport à \( \hat{y} \) :

$$
\frac{dC}{d\hat{y}} = 2(\hat{y} - y)
$$

(règle : la dérivée de « quelque chose au carré » = 2 × ce quelque chose × sa
propre dérivée.)

---

## Chapitre 8 : la descente de gradient

C'est **l'algorithme d'apprentissage**. L'image : tu es dans la montagne, dans le
brouillard, et tu veux rejoindre la vallée (le coût minimal). Tu ne vois pas loin,
mais tu sens la **pente sous tes pieds** (le gradient). Stratégie : **faire un
petit pas vers le bas**, et recommencer.

Pour chaque poids \( w \), on le met à jour ainsi :

$$
w \;\leftarrow\; w \;-\; \eta \, \frac{\partial C}{\partial w}
$$

- \( \dfrac{\partial C}{\partial w} \) : la pente (le gradient) pour ce poids.
- Le signe **moins** : on descend (sens opposé au gradient).
- \( \eta \) (« êta »), le **taux d'apprentissage** (*learning rate*) : la **taille
  du pas**.
  - trop **petit** → apprentissage très lent ;
  - trop **grand** → on saute par-dessus la vallée, ça diverge.

On répète des milliers de fois : à chaque tour, le coût baisse un peu, et les poids
s'améliorent. Reste **une** question : comment calculer toutes ces dérivées
partielles dans un réseau à plusieurs couches ? → chapitre 9.

---

## Chapitre 9 : la rétropropagation (backpropagation)

### Le défi

Un poids de la **première** couche influence la sortie en traversant **toutes** les
couches suivantes. Comment savoir sa part de responsabilité dans l'erreur finale ?

### La règle de la chaîne

L'outil mathématique est la **règle de dérivation des fonctions composées** (« règle
de la chaîne ») : si une grandeur en influence une autre qui en influence une
troisième, on **multiplie les dérivées** le long du chemin :

$$
\frac{dC}{dw} = \frac{dC}{da} \cdot \frac{da}{dz} \cdot \frac{dz}{dw}
$$

### L'idée de l'algorithme

1. **Propagation avant** : on calcule la sortie (ce que fait déjà ton code) en
   mémorisant les valeurs intermédiaires (\( z \) et \( a \) de chaque couche).
2. **Calcul de l'erreur** finale \( C \) (chapitre 6).
3. **Propagation arrière** : on part de la sortie et on **remonte** couche par
   couche, en multipliant les dérivées (règle de la chaîne), pour obtenir
   \( \partial C / \partial w \) de **chaque** poids.
4. **Mise à jour** de tous les poids par descente de gradient (chapitre 8).

C'est exactement le « back » de *backpropagation* : l'information de l'erreur
**circule à l'envers**, de la sortie vers l'entrée. C'est l'algorithme qui a rendu
les réseaux de neurones réellement utilisables.

> Dérivée utile : pour ReLU, \( \text{ReLU}'(z) = 1 \) si \( z > 0 \), et \( 0 \)
> sinon. Très simple à coder — d'où sa popularité.

---

## Chapitre 10 : l'entraînement en pratique

Quelques mots de vocabulaire qu'on rencontrera en codant l'apprentissage :

- **Données d'entraînement** : l'ensemble des exemples (entrée + cible).
- **Itération** : un passage *avant + arrière + mise à jour* sur un (ou plusieurs)
  exemple(s).
- **Batch** (lot) : le paquet d'exemples traités avant chaque mise à jour des poids.
- **Epoch** (époque) : un passage complet sur **toutes** les données d'entraînement.
  On en fait généralement beaucoup.
- **Learning rate** \( \eta \) : la taille du pas (chapitre 8) — le réglage le plus
  important à ajuster.
- **Surapprentissage** (*overfitting*) : quand le réseau apprend « par cœur » les
  exemples au lieu de **généraliser**. On le détecte avec des données de **test**
  qu'il n'a jamais vues.

Boucle d'entraînement type (pseudo-code) :

```
répéter pour chaque epoch :
    pour chaque exemple (x, y) :
        ŷ = réseau.forward(x)        // propagation avant
        C = coût(ŷ, y)               // erreur
        gradients = backprop(C)      // propagation arrière
        mettre à jour les poids      // w ← w − η · gradient
```

---

## Chapitre 11 : application aux échecs

But final du projet : un réseau qui **évalue une position** d'échecs (ou choisit un
coup). Les questions à régler, dans l'ordre :

1. **Encodage de l'entrée** : transformer l'échiquier (le `String[][] squares` de
   `Board.java`) en un **vecteur de nombres** que le réseau peut lire. Ex : une
   case vide = 0, un pion blanc = +1, une dame noire = −9, etc. (à concevoir
   ensemble).
2. **Définir la sortie** : un seul nombre (l'évaluation de la position) ? une
   probabilité par coup possible ?
3. **Obtenir des données** : des positions avec leur « bonne » évaluation pour
   entraîner.
4. **Entraîner** avec les chapitres 6 à 10.
5. **Brancher** le réseau entraîné sur le jeu pour qu'il joue.

C'est l'objectif lointain ; on y arrivera étape par étape.

---

## Où on se situe dans le cours

```
[0]──[1]──[2]──[3]──[4]──[5]──┤ TU ES ICI ├──[6]──[7]──[8]──[9]──[10]──[11]
 ✅   ✅   ✅   ✅   ✅   ✅                   ⏳   ⏳   ⏳   ⏳    ⏳     ⏳
└──────── PROPAGATION AVANT ────────┘        └────────── APPRENTISSAGE ──────────┘
        (déjà codé : Layer.java + Network.java)         (pas encore commencé)
```

**Ce qui est acquis (chapitres 0 à 5)** — et codé dans `Layer.java` / `Network.java` :

- ✅ le neurone : `biais + Σ(poids × entrées)` ;
- ✅ la fonction d'activation ReLU ;
- ✅ la couche, vue comme un produit matrice × vecteur ;
- ✅ le réseau et la propagation avant (chaînage des couches).

👉 **Concrètement, on s'est arrêté à la fin du chapitre 5.** Ton réseau sait
**calculer** une sortie, mais les poids sont **fixés à la main** : il ne sait pas
encore **apprendre**.

**La prochaine étape (chapitre 6)** : introduire la **fonction de coût** pour
**mesurer l'erreur** du réseau. C'est le tout premier pas vers l'apprentissage.
Ensuite viendront les dérivées/gradient (7), la descente de gradient (8) et la
rétropropagation (9), qui permettront enfin au réseau d'ajuster ses poids tout
seul.
