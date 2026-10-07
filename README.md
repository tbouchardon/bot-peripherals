# Bot Peripherals (prh)

Briques communes des bots ksuto : capture d'écran (DXGI sous Windows, repli sur `Robot`), clavier, souris au mouvement
naturel, et **recherche d'objets à l'écran** (images, blocs de couleur) avec auto-apprentissage.

Construit avec Gradle (conventions de `Bot Parent`) : `./gradlew test`.

---

## Clavier et souris

Les gestes imitent une main, avec des durées tirées au hasard à chaque fois (`HumanTiming`) :

| Geste | Durée |
|---|---|
| Modificateur (Maj, Ctrl, Alt) enfoncé avant la touche | 40 à 90 ms |
| Touche tenue (`pressKey`, sauf durée imposée) | 60 à 120 ms |
| Modificateur relâché après la touche, puis répit | 20 à 60 ms chacun |
| Entre deux caractères de `typeString` | `ksuto.prh.peripherals.keyboard.typing.delay` à ±50 % |

Une combinaison prend ainsi 140 à 330 ms, une touche seule 100 à 240 ms. Modificateurs et touche sont toujours relâchés,
même en cas d'erreur.

`typeString` tape caractère par caractère. **Sous Windows**, chaque caractère est cherché sur la disposition active du
clavier (`VkKeyScanW` : en AZERTY, `1` = Maj+1, `@` = AltGr+0) et envoyé comme une vraie touche (`SendInput`), par
l'API native de Java (`WindowsKeys`, FFM, sans DLL) ; un caractère qu'aucune touche ne produit passe par la saisie
Unicode. Ailleurs, la touche Java du caractère (ponctuation en QWERTY), Maj pour les majuscules. Le programme doit être
lancé avec `--enable-native-access=ALL-UNNAMED`.

La souris suit des gestes courbes, accélérés puis ralentis, d'une durée qui dépend de la distance (`Mouse.naturalMoveTo`).
`Peripheral.delay` attend précisément sans occuper le processeur (fil suspendu, seule la dernière 1,5 ms est attendue
activement).

---

## Recherche d'objets à l'écran

`PictureSearch` (images) et `ColorSearch` (blocs de couleur) étendent `AbstractSeeker` :

```java
PictureSearch.getDefault(InterfaceEnum.CONTINUER)          // tolérances par défaut : couleur 20, erreur 5 %
             .setSearchZone(new Screen.Zone(1017, 1154, 818, 919))
             .await()                                       // cherche jusqu'à trouver (15 s au plus)
             .clickFirst();
```

Chaque `search()` capture la zone, la parcourt et **remplace** les résultats précédents (un objet disparu n'est plus
signalé), sauf en suivi (`setTracking(true)`), où les objets déjà trouvés sont recherchés autour de leur dernière
position.

Deux tolérances règlent la comparaison avec l'image de référence :

| Réglage | Sens |
|---|---|
| `setPrecision(p)` | Écart toléré par canal de couleur : un pixel correspond si chaque canal diffère de **moins** de `p` (0 = pixel identique). Malgré son nom, plus la valeur est haute, plus la recherche est permissive. |
| `setAllowedErrorRate(e)` | Part des pixels de la référence qui peuvent ne pas correspondre (0.0 à 1.0). |

`PictureHelper.findWorkingParameters` aide à les régler à la main, en affichant les résultats à l'écran.

## Auto-apprentissage

Deux mécanismes, mémorisés d'une exécution à l'autre dans une base **SQLite** (`SearchMemory`) : un simple fichier,
`~/.ksuto/prh.db` par défaut, partagé par tous les bots, sans serveur. La mémoire est rangée par recherche : images
cherchées et résolution de l'écran (`getSearchKey()`).

### Tolérances : `learn(n)`

On sait combien d'objets la recherche doit trouver (`n`). À chaque recherche, quelques combinaisons de tolérances
(8 par défaut, `setTrialsPerSearch`) sont essayées **sur la même capture**, parmi 77 (couleur 0 à 50 par pas de 5,
erreur 0 à 30 % par pas de 5 %), et chacune est notée : réussie si elle trouve exactement `n` objets.

- **Des statistiques, pas une élimination** : une capture ratée (objet masqué un instant) baisse le taux de réussite
  d'une bonne combinaison sans l'écarter.
- **La plus fiable est retenue** (`ParameterLearning.best`) : taux de réussite lissé (règle de Laplace : une réussite
  sur un essai ne passe pas devant 19 sur 20) ; à égalité, la plus essayée, puis la plus stricte. Une combinaison qui
  échoue plus d'une fois sur deux n'est jamais retenue.
- **Ordre des essais** (`ParameterLearning.nextTrials`) : la meilleure actuelle (pour vérifier qu'elle tient), puis
  les jamais essayées, puis les moins essayées. Toutes sont essayées en une dizaine de recherches.
- Chaque combinaison est jugée sur ses seuls résultats (ils sont effacés entre deux essais).
- Une recherche sans tolérances précisées reprend la meilleure combinaison apprise, s'il y en a une.

### Zone de recherche : `optimize(k, n)`

Les positions trouvées sont retenues (les `n` plus récentes). Dès `k` positions, la recherche se limite à leur
rectangle englobant, élargi de la moitié de son étendue, de la taille de l'objet et de 10 pixels. Si rien n'y est
trouvé, **la même recherche reprend aussitôt sur toute la zone** ; après 3 échecs de suite, la zone réduite est
oubliée. Pas de zone réduite pendant un apprentissage de tolérances, qu'elle fausserait.

### Réglages (`prh.properties`)

| Propriété | Défaut | Sens |
|---|---|---|
| `ksuto.prh.database.file` | `~/.ksuto/prh.db` | Fichier de la mémoire. Un `~` initial désigne le dossier de l'utilisateur, sous Windows aussi (`C:\Users\nom\.ksuto\prh.db`). |
| `ksuto.prh.database.offline` | `false` | `true` : mémoire vive seulement, rien n'est conservé. |

Une base inaccessible est signalée une fois et remplacée par une mémoire vive : le bot cherche sans mémoire, sans
s'arrêter. `clearAreaOptimization()`, `clearParametersOptimization()` et `clearObjectOptimizations()` oublient ce qui a
été appris pour une recherche. Le contenu se lit avec n'importe quel outil SQLite (tables `search_parameter`,
`search_position`, `search_area`).

### Tests

`SeekerLearningTest` vérifie l'apprentissage sur des écrans synthétiques (capture injectée par `setCapture`, mémoire
par `setMemory`) : tolérances apprises sur un motif exact et un motif altéré, essais jugés indépendamment, zone réduite
puis retour à l'écran entier, zone oubliée après des échecs. `ParameterLearningTest` et `SearchMemoryTest` couvrent la
logique de choix et la base.
