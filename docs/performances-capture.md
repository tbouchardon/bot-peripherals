# Performances de la capture d'écran

Mesures du 5 octobre 2026 sur le poste Windows (écran 1920x1080, échelle 100 %, JDK Temurin 25), avec le banc
d'essai `src/test/java/fr/ksuto/prh/research/ImageReadingSpeedTests.java`.

Deux séries de mesures :
- **écran fixe** : session Windows verrouillée, l'écran affichait l'écran de verrouillage ;
- **écran animé** : session ouverte, un film en cours de lecture, plus une petite fenêtre du banc redessinée toutes les
  5 ms. C'est le cas réaliste d'un jeu : à chaque capture DXGI, une nouvelle image est recopiée.

## Ce qui a été testé

Trois façons de capturer une zone de l'écran, toutes derrière l'interface `CaptureBackend` (`Capture.setBackend`) :

| Moyen | Classe | Principe |
|---|---|---|
| **Robot** (référence) | `RobotCaptureBackend` | `java.awt.Robot.createScreenCapture` : sous Windows, Java copie l'écran par GDI. Un Robot par thread. |
| **GDI direct** | `GdiCaptureBackend` | `BitBlt` de l'écran vers une DIB 32 bits, appelé directement par l'API native de Java (FFM, `java.lang.foreign`), sans DLL. Contexte de dessin et DIB gardés par thread. |
| **DXGI Desktop Duplication** | `DxgiCaptureBackend` | La carte graphique fournit chaque nouvelle image du bureau (`IDXGIOutputDuplication::AcquireNextFrame`), copiée dans une texture Direct3D 11 lisible par le processeur ; une capture ne lit que la zone demandée. Interfaces COM appelées par FFM, sans DLL. Si l'écran n'a pas changé, l'image précédente est relue sans attendre. |

Pour chaque moyen et chaque taille de zone : 20 captures de chauffe, puis 100 ou 200 captures chronométrées. Une
somme de contrôle sur les pixels lus empêche la JVM d'éliminer les boucles.

Vérification de l'image : la même zone de 400x300 est capturée par les trois moyens et comparée pixel par pixel.

## Résultats, écran animé (session ouverte, film en lecture ; ms par capture, 200 captures)

| Zone | Robot | GDI direct | DXGI |
|---|---|---|---|
| Plein écran 1920x1080 | 46,3 | 38,2 | **5,2** |
| 1/4 de surface (960x540) | 19,7 | 14,3 | **0,98** |
| 1/16 de surface (480x270) | 9,9 | 7,9 | **0,26** |
| Barre 300x30 | 7,1 | 7,5 | **0,10** |
| QR code 32x32 | 7,2 | 7,2 | **0,14** |

Dans la fenêtre animée du banc (redessinée toutes les 5 ms) :

| Mesure | Robot | DXGI |
|---|---|---|
| Capture 32x32 | 7,05 ms | **0,07 ms** |
| Capture plein écran | 51,0 ms | **3,2 ms** |
| Fraîcheur : images différentes vues en 1 s | 63 | 61 |

La **fraîcheur** est la même : les deux voient environ 60 images différentes par seconde, soit la fréquence de l'écran
(60 Hz). DXGI ne voit donc pas une image en retard par rapport à Robot ; aucun des deux ne peut voir plus d'images que
l'écran n'en affiche.

## Résultats, écran fixe (session verrouillée ; ms par capture, 200 captures)

| Zone | Robot | GDI direct | DXGI |
|---|---|---|---|
| Plein écran 1920x1080 | 50,8 | 36,4 | **2,9** |
| 1/4 de surface (960x540) | 15,9 | 14,7 | **0,59** |
| 1/16 de surface (480x270) | 9,5 | 8,7 | **0,17** |
| Barre 300x30 | 8,2 | 8,7 | **0,05** |
| QR code 32x32 | 8,2 | 8,3 | **0,05** |

Un premier passage (100 captures) donnait le même ordre de grandeur : QR code 6,9 / 7,9 / 0,17 ms, plein écran
57 / 37 / 4,0 ms.

**Image identique** (dans les deux séries) : 120 000 pixels sur 120 000 identiques entre Robot et GDI, et entre Robot
et DXGI.

Lecture de tous les pixels d'une capture plein écran : `BufferedImage.getRGB` 8,5 à 8,8 ms, `Frame.rgb` 3,6 à 4,8 ms.

`CaptureScheduler` (4 threads, 10 captures/s chacun, plein écran, Robot) : 33,6 à 37,7 images/s.

## Ce qu'on en retient

- **GDI direct n'apporte rien** sur les petites zones : Robot utilise déjà GDI sous Windows. Les deux butent sur un
  plancher d'environ 7 à 9 ms par capture, quelle que soit la taille : c'est le coût de la lecture de l'écran à travers
  le compositeur de Windows (DWM). Seul le plein écran gagne un peu (36 contre 51 ms).
- **DXGI change d'échelle, même sur un écran qui change** : 0,07 à 0,14 ms pour le QR code, **50 à 100 fois** plus
  rapide que Robot, et 3 à 5 ms pour un plein écran (environ 10 fois). Sur un écran fixe, où l'image précédente est
  simplement relue, on descend à 0,05 ms. La copie se fait dans la carte graphique ; seule la zone demandée est lue en
  mémoire.
- **Même fraîcheur que Robot** : environ 60 images différentes par seconde, la fréquence de l'écran.
- Pour ClockWork, une capture de la grille passe de ~7 ms à ~0,1 ms : la capture ne compte plus dans la boucle de
  décision (100 ms), et un bot qui surveille plusieurs zones ou tout l'écran devient possible à 60 images/s.
- Le plancher d'environ 18 ms noté lors d'une mesure précédente (2 octobre) ne se retrouve pas ici (7 à 9 ms) : les
  conditions de cette mesure-là (session, charge) ne sont pas connues.

## Limites connues de DXGI

- **Une seule duplication par écran et par processus** : une seconde instance échoue (`DuplicateOutput` renvoie
  `E_INVALIDARG`). Il faut donc une instance unique, partagée (c'est le rôle de `Capture.setBackend`).
- **Écran principal uniquement** pour l'instant (première sortie de la première carte graphique).
- **Latence d'une image** au plus : on lit la dernière image composée par Windows, soit jusqu'à environ 16 ms de
  retard à 60 Hz. Sans importance pour un bot qui décide toutes les 100 ms.
- **Changement de mode d'affichage, verrouillage** : Windows invalide la duplication (`DXGI_ERROR_ACCESS_LOST`) ; elle
  est alors recréée automatiquement. Le cas n'a pas encore été provoqué en test.
- Les appels sont **sérialisés** (contexte Direct3D non partageable entre threads) : capturer en parallèle n'apporte
  plus rien, `CaptureScheduler` devient inutile avec DXGI.
- Fenêtres en plein écran exclusif : non testé. WoW en fenêtré maximisé n'est pas concerné.

## Moyen par défaut

Depuis ces mesures, `Capture` choisit le moyen à la première capture :
- **sous Windows, DXGI**, enveloppé dans `FallbackCaptureBackend` : si une capture DXGI échoue (session verrouillée,
  changement de mode d'affichage, Direct3D indisponible), elle est faite par Robot, et DXGI est réessayé 5 s plus tard ;
- **ailleurs, Robot** ;
- la propriété système `ksuto.capture` impose un moyen : `dxgi`, `gdi` ou `robot`.

Tous les bots qui passent par `Capture` en profitent sans modification. Sans `--enable-native-access=ALL-UNNAMED`, Java
affiche un avertissement au premier appel natif, mais la capture fonctionne (Java 25). Vérifié sous Windows : le moyen
par défaut est bien `FallbackCaptureBackend` (DXGI), avec ou sans l'option.

## Prochaines étapes

1. Provoquer une perte de duplication (verrouillage, changement de résolution) pendant une capture pour vérifier la
   recréation automatique.

## Relancer les mesures

Sous Windows, session déverrouillée, depuis le dossier de ClockWork après `gradlew installDist` (qui embarque ce
module) :

```bat
java --enable-native-access=ALL-UNNAMED -cp "build\install\clockwork\lib\*" ^
     "..\Bot Peripherals\src\test\java\fr\ksuto\prh\research\ImageReadingSpeedTests.java" 200
```

L'argument est le nombre de captures par mesure.
