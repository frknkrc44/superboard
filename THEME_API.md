# SuperBoard API
Easy way to create themes and language packs for SuperBoard

## Requirements
- AIDL file of API
- A little Java
- A powerful brain

## Example Theme
```
{
"name":"AMOLED Dark",
"code":"amoled_dark",
"fnTyp":"bold",
"bgClr":"#FF000000",
"priClr":"#FF000000",
"secClr":"#FF000000",
"enterClr":"#FF000000",
"tShdwClr":"#FFD4D6D7",
"txtClr":"#FFD4D6D7",
"keyPad":"1.0",
"keyRad":"1.0",
"txtSize":"1.9",
"txtShadow":"0.0"
}
```

### Names and Functions
| Name     | Function      |
|----------|--------------:|
| name* | Theme name |
| code* | Theme code name, using for check theme status |
| fnTyp | Font type, check "Font Types" section |
| bgClr | Background color |
| priClr | Primary key color |
| secClr | Secondary key color |
| enterClr | Enter key color |
| tShdwClr | Text shadow color |
| txtClr | Text and icon color |
| keyPad | Padding between keys |
| keyRad | Key radius |
| txtSize | Text and icon size |
| txtShadow | Text shadow |

\* You must implement these lines

### Font Types
| Key     | Human-readable name |
|----------|--------------:|
| regular | Default font |
| bold | Bold font |
| italic | Italic font |
| bold_italic | Bold & italic font |
| condensed | Condensed font |
| condensed_bold | Condensed bold font |
| condensed_italic | Condensed italic font |
| condensed_bold_italic | Condensed bold italic font |
| serif | Serif font (like Times New Roman) |
| serif_bold | Serif bold font |
| serif_italic | Serif italic font |
| serif_bold_italic | Serif bold italic font |
| monospace | Monospace font (like terminal font) |
| monospace_bold | Monospace bold font |
| monospace_italic | Monospace italic font |
| monospace_bold_italic | Monospace bold italic font |
| serif_monospace | Serif monospace font |
| serif_monospace bold | Serif monospace bold font |
| serif_monospace_italic | Serif monospace italic font |
| serif_monospace_bold_italic | Serif monospace bold italic font |
| custom* | Custom font |

\* Please copy a font to /sdcard/Android/data/org.blinksd.board/files/font.ttf, otherwise app using the "regular" font.

## Example Language Pack
Check assets/langpacks folder for examples.

### Names and Functions

#### Main
| Name*    | Function      |
|----------|--------------:|
| name | Language name (using for language name checks) |
| label | Language label (using for selection screen) |
| enabled** | Language is enabled or not (true or false) |
| enabledSdk*** | Supported minimum Android SDK version |
| midPadding**** | Enable middle row corner padding |
| author | Language pack author name |
| language | Language identifier (like en_US or tr_TR_Q) |
| layout | Layout keymap, see "Layout" section |
| popup | Popup keymap, see "Layout" section |

#### Layout
A keyboard layout includes a few factors; like row, key or key options.
Popup rows must be equal length with layout rows, otherwise keyboard is crashed.

##### Main
A keyboard layout using a row array.

| Name*    | Function      |
|----------|--------------:|
| row | Keyboard row, see "Row" section |

##### Row
Rows using a JSON array to store keys.

##### Key
Keys using a JSON object for store options.

| Name    | Function      |
|---------|--------------:|
| key* | Key title (and key press character if "pkc" value is not set) |
| width | Key width |
| pkc | Key press character (a char number) |
| lpkc | Key long-press character (a char number) |
| rep | Key repeat on long-press (true or false) |
| pine | Mark key press as "is not event" \*\*\*\*\*\* (true or false) |
| lpine | Mark key long-press as "is not event" \*\*\*\*\*\*\* (true or false) |
| dkt | Mark key as secondary key |

##### Specific key numbers
| Number    | Function      |
|-----------|--------------:|
| -1 | Toggle caps lock |
| -2 | Open symbol layout |
| -4 | Press enter\*\*\*\*\* |
| -5 | Press delete |
| -101 | Change keyboard layout |
| -102 | Open emoji layout |

- \*             You must implement these lines in your JSON file
- \*\*           Must be true if you want to import this pack, otherwise you'll get LANG_PKG_IMPORT_FAILED_NOT_ENABLED
- \*\*\*         Import failed with LANG_PKG_IMPORT_FAILED_SDK if SDK is not compatible
- \*\*\*\*       You can see Old Turkic layout for example, I disabled it in this layout
- \*\*\*\*\*     If you implemented a key as enter key, this key colored with enter key color
- \*\*\*\*\*\*   If you implemented a key press as "is not event", "pkc" is forced to print as text
- \*\*\*\*\*\*\* If you implemented a key long-press as "is not event", "lpkc" is forced to print as text

## TODO
- Add custom font installation support
- Add background image installation support

## Example Theme App
Please check SuperBoardThemeExample folder
