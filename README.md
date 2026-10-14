# AutoLogin (Fabric, Minecraft 1.21.11)

Mod kliencki. Wchodzisz na serwer -> klikasz **Z** -> wpisujesz hasło -> Zapisz.
Przy kolejnych wejściach na ten serwer mod sam wyśle `/login <hasło>` (ok. 1,5 s po dołączeniu).

* Dane zapisują się osobno dla każdego serwera w `.minecraft/config/autologin.json`
  (UWAGA: hasło jest tam zwykłym tekstem – nie udostępniaj tego pliku).
* Pole „Komenda” pozwala zmienić format, np. `l %s` albo `login %s`. `%s` = hasło.
* Opóźnienie ustawisz w configu (`delayTicks`, 20 = 1 sekunda).
* Klawisz można zmienić w Opcje -> Sterowanie -> AutoLogin.

## Budowanie
Wymagane: Java 21 + Gradle 8.14+ (albo wygeneruj wrapper: `gradle wrapper`).

    gradle build

Gotowy plik: `build/libs/AutoLogin-1.0.0.jar` -> do folderu `mods` (razem z Fabric API).

## Bez instalowania czegokolwiek (GitHub)
1. Załóż darmowe konto na github.com i utwórz nowe repozytorium.
2. Wgraj do niego CAŁĄ zawartość tego folderu (razem z ukrytym folderem `.github`).
3. Wejdź w zakładkę **Actions** -> ostatni przebieg -> na dole **Artifacts** -> pobierz `AutoLogin-mod`.
4. W środku jest gotowy `AutoLogin-1.0.0.jar` -> wrzuć do `mods`.
