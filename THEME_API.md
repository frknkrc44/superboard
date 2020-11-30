# SuperBoard Theme API
Easy way to create themes for SuperBoard

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

### TODO
- Add custom font installation support

## Example App
Please check SuperBoardThemeExample folder
