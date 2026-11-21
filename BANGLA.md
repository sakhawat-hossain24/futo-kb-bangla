# Bangla (Avro Phonetic) build of FUTO Keyboard

Local-only fork of [FUTO Keyboard](https://github.com/futo-org/android-keyboard) that adds
phonetic Bangla input in the style of the Avro scheme. Nothing here is published.

## What it adds

| Piece | Where |
| --- | --- |
| Avro transliteration engine (Kotlin port of `okkhor`'s parser) | `java/src/org/futo/inputmethod/event/combiners/bangla/AvroParser.kt` |
| Rule table, auto-generated (289 patterns) | `.../bangla/AvroPatterns.kt` |
| Combiner that turns typed latin into Bangla composing text | `.../bangla/BanglaPhoneticCombiner.kt` |
| Raw latin input exposed to the suggestion strip | `.../bangla/BanglaPhoneticState.kt`, `latin/uix/ActionBar.kt` |
| Registration in the layout combiner enum | `v2keyboard/CombinerKind.kt` (`BanglaPhonetic`) |
| Layout definition `বাংলা (Avro Phonetic)` | `java/assets/layouts/Bengali/bengali_avro_phonetic.yaml` |
| Language mapping (bn-BD / bn-IN) | `java/assets/layouts/mapping.yaml` |
| Bangla dictionary, 147k words + next-word data | `java/res/raw/main_bn.dict` |
| Tests: parser unit checks, 193-case parity with the reference, YAML schema | `src/test/kotlin/` |

Typing works like Avro on desktop: type `amar`, the composing text shows `আমার`, space commits
the word. Suggestions and autocorrect come from the bundled dictionary (the keyboard queries it
with the converted Bangla text).

The suggestion strip always has an extra outlined cell on the left with the literal latin text
being typed (`amar`). Tap it to commit the latin text instead of the Bangla conversion, which is
handy for mixed Bangla/English writing and for names the dictionary doesn't know.

## Build

Needs JDK 21 and the Android SDK (platform 35, build-tools 35.0.0, NDK 28.2, CMake 3.22.1).
Submodules are required:

```bash
git submodule update --init --depth 1 libs translations java/res-large java/assets/themes java/assets/layouts
printf 'sdk.dir=%s/Android/Sdk\n' "$HOME" > local.properties
export JAVA_HOME=~/.local/share/mise/installs/java/temurin-21.0.13+9.0.LTS
./gradlew assembleUnstableDebug -PbanglaArm64Only -PbanglaCoexist
```

- `-PbanglaArm64Only` builds native code for phones only (much faster/cheaper than 4 ABIs).
- `-PbanglaCoexist` appends `.bangla` to the application id, so this build installs next to the
  store version instead of replacing it.
- On a small machine, add `--max-workers=2 --no-daemon`; the native ggml/whisper sources are the
  memory peak.

APK: `build/outputs/apk/unstable/debug/futo-kb-bangla-unstable-debug.apk`
(package `org.futo.inputmethod.latin.bangla.unstable`).

## Test

```bash
JAVA_HOME=~/.local/share/mise/installs/java/temurin-21.0.13+9.0.LTS \
  ./gradlew testUnstableDebugUnitTest -PbanglaArm64Only -PbanglaCoexist
```

## Install

```bash
adb install -r build/outputs/apk/unstable/debug/futo-kb-bangla-unstable-debug.apk
```

Then: FUTO Keyboard (Dev Build) → enable it as an input method → Languages → Bangla → choose
`বাংলা (Avro Phonetic)`.

The app is signed with a local debug key, so it can never update FUTO's store build; the
`banglaCoexist` id keeps both installed side by side. Settings from the store build are not
carried over.

## Layout / dictionary without rebuilding

The app itself supports runtime additions, useful for iterating:

- **Layouts**: Settings → Help & Feedback → tap the version 8×, then Developer → Custom layouts.
  Paste the YAML from `bengali_avro_phonetic.yaml`. Works for layouts, *not* for new combiners —
  `BanglaPhonetic` is compiled in, so the phonetic behaviour needs this build.
- **Dictionaries**: Settings → Languages → Bangla → Dictionary → import a `.dict` file.

## Credits

- Avro Phonetic scheme: Mehdi Hasan Khan (OmicronLab).
- Parser and rule table ported from [`okkhor`](https://crates.io/crates/okkhor) 0.10.0,
  MIT, © 2021 Gulshanur Rahman.
- Bangla dictionary from
  [aosp-dictionaries](https://codeberg.org/Helium314/aosp-dictionaries) (`main_bn.dict`,
  word sources CC BY 4.0), generated with `dicttool_aosp.jar`.
