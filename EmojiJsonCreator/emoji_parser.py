from json import dumps
from requests import get


def get_emoji_desc(line: str) -> str:
    first = line[line.find('#') + 2:]
    second = first[first.find('E'):]
    third = second[second.find(' ') + 1:]
    return third



categories: dict[str, list[str]] = {}
request = get(
    'https://unicode.org/Public/emoji/latest/emoji-test.txt',
    allow_redirects=True,
)

if request.status_code == 200:
    recent_group = ''
    recent_approved_line = ''
    for line in request.text.splitlines():
        if line.startswith('# group: '):
            recent_group = line[line.find(':') + 2:]
            category: list[str] = []
            categories[recent_group] = category

        if not len(recent_group):
            continue

        if not line.startswith('#') and ';' in line and 'qualified' in line and 'E' in line:
            if not len(recent_approved_line) or not get_emoji_desc(line).startswith(get_emoji_desc(recent_approved_line)):
                recent_approved_line = line
            else:
                continue

            first = line[line.find('#') + 2:]
            sec = first[:first.find('E') - 1]
            categories[recent_group].append(sec)
else:
    exit(1)


with open('../app/src/main/assets/emoji_list.json', 'w') as f:
    f.write(dumps(categories, ensure_ascii=False, separators=(',', ':')))
