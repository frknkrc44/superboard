# FBoard API
Easy way to create themes and language packs for FBoard

## Requirements
- AIDL file of the API
- A little Java
- A powerful brain

## Example Theme
```
{
    "name": "AMOLED Dark",
    "code": "amoled_dark",
    "fnTyp": "bold",
    "bgClr": "#FF000000",
    "priClr": "#FF000000",
    "priPressClr": "#FF212121",
    "secClr": "#FF000000",
    "secPressClr": "#FF212121",
    "enterClr": "#FF000000",
    "enterPressClr": "#FF212121",
    "tShdwClr": "#FFD4D6D7",
    "txtClr": "#FFD4D6D7",
    "keyPad": "1.0",
    "keyRad": "1.0",
    "txtSize": "1.9",
    "txtShadow": "0.0"
}
```

### Names and Functions
| Name          | Function                                      | Data type        |
|---------------|-----------------------------------------------|------------------|
| name*         | Theme name                                    | string           |
| code*         | Theme code name, using for check theme status | string           |
| fnTyp         | Font type, check "Font Types" section         | string           |
| bgClr         | Background color                              | hex color string |
| priClr        | Primary key color                             | hex color string |
| priPressClr   | Primary key press color                       | hex color string |
| secClr        | Secondary key color                           | hex color string |
| secPressClr   | Secondary key press color                     | hex color string |
| enterClr      | Enter key color                               | hex color string |
| enterPressClr | Enter key press color                         | hex color string |
| tShdwClr      | Text shadow color                             | hex color string |
| txtClr        | Text and icon color                           | hex color string |
| keyPad        | Padding between keys                          | float            |
| keyRad        | Key radius                                    | float            |
| txtSize       | Text and icon size                            | float            |
| txtShadow     | Text shadow                                   | float            |

\* You must implement these lines

### Font Types
| Key                         |                 Human-readable name |
|-----------------------------|------------------------------------:|
| regular                     |                        Default font |
| bold                        |                           Bold font |
| italic                      |                         Italic font |
| bold_italic                 |                  Bold & italic font |
| condensed                   |                      Condensed font |
| condensed_bold              |                 Condensed bold font |
| condensed_italic            |               Condensed italic font |
| condensed_bold_italic       |          Condensed bold italic font |
| serif                       |   Serif font (like Times New Roman) |
| serif_bold                  |                     Serif bold font |
| serif_italic                |                   Serif italic font |
| serif_bold_italic           |              Serif bold italic font |
| monospace                   | Monospace font (like terminal font) |
| monospace_bold              |                 Monospace bold font |
| monospace_italic            |               Monospace italic font |
| monospace_bold_italic       |          Monospace bold italic font |
| serif_monospace             |                Serif monospace font |
| serif_monospace bold        |           Serif monospace bold font |
| serif_monospace_italic      |         Serif monospace italic font |
| serif_monospace_bold_italic |    Serif monospace bold italic font |
| custom*                     |                         Custom font |

\* Please copy a font to /sdcard/Android/data/org.blinksd.board/files/font.ttf, otherwise app will use the "regular" font.

## Example Language Pack
Check the assets/langpacks folder for examples.

### Names and Functions

#### Main
| Name*            | Function                                       | Data type  |
|------------------|------------------------------------------------|------------|
| name             | Language name (using for language name checks) | string     |
| label            | Language label (using for selection screen)    | string     |
| enabled\*\*      | Language is enabled or not (true or false)     | boolean    |
| enabledSdk\*\*\* | Supported minimum Android SDK version          | boolean    |
| author           | Language pack author name                      | string     |
| language         | Language identifier (like en_US or tr_TR_Q)    | string     |
| layout           | Layout keymap, see "Layout" section            | array<Row> |
| popup            | Popup keymap, see "Layout" section             | array<Row> |

#### Layout
A keyboard layout includes a few factors; like row, key or key options.
Popup rows must be equal length with layout rows, otherwise keyboard is crashed.

##### Row

| Name   | Function                                | Data type  |
|--------|-----------------------------------------|------------|
| keys\* | The keyboard row, see the "Row" section | array<Key> |
| cpad   | Enable the corner padding               | boolean    |

Example: [{"keys":[...],"cpad":"true"}, ...]

##### Key
Keys using a JSON object for store options.

| Name  | Function                                                      | Data type |
|-------|---------------------------------------------------------------|-----------|
| key*  | Key title (and key press character if "pkc" value is not set) | string    |
| width | Key width                                                     | integer   |
| pkc   | Key press character                                           | char      |
| lpkc  | Key long-press character                                      | char      |
| rep   | Key repeat on long-press                                      | boolean   |
| pine  | Mark key press as "is not event"\*\*\*\*\*                    | boolean   |
| lpine | Mark key long-press as "is not event"\*\*\*\*\*\*             | boolean   |
| dkt   | Mark key as secondary key                                     | boolean   |

Example: {"key":"a", ...}

NOTE: If you want to shift button working properly, implement keys with lower case.

##### Specific key numbers
| Number |               Function |
|--------|-----------------------:|
| -1     |       Toggle caps lock |
| -2     |     Open symbol layout |
| -4     |    Press enter\*\*\*\* |
| -5     |           Press delete |
| -101   | Change keyboard layout |
| -102   |      Open emoji layout |

- \*             You must implement these lines in your JSON file
- \*\*           Must be true if you want to import this pack, otherwise you'll get LANG_PKG_IMPORT_FAILED_NOT_ENABLED
- \*\*\*         Import failed with LANG_PKG_IMPORT_FAILED_SDK if SDK is not compatible
- \*\*\*\*       If you implemented a key as enter key, this key colored with enter key color
- \*\*\*\*\*     If you implemented a key press as "is not event", "pkc" is forced to print as text
- \*\*\*\*\*\*   If you implemented a key long-press as "is not event", "lpkc" is forced to print as text

## TODO
- Add custom font installation support
- Add background image installation support

## Example Theme App
Please check SuperBoardThemeExample folder
