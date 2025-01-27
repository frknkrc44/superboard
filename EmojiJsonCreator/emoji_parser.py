from json import dumps
from requests import get


categories: dict[str, list[str]] = {}
request = get(
    'https://unicode.org/Public/emoji/latest/emoji-test.txt',
    allow_redirects=True,
)

if request.status_code == 200:
    recent_group = ''
    for line in request.text.splitlines():
        if line.startswith('# group: '):
            recent_group = line[line.find(':') + 2:]
            category: list[str] = []
            categories[recent_group] = category

        if not len(recent_group):
            continue

        if ';' in line and 'qualified' in line and 'E' in line:
            first = line[line.find('#') + 2:]
            sec = first[:first.find('E') - 1]
            categories[recent_group].append(sec)


with open('../app/src/main/assets/emoji_list.json', 'w') as f:
    f.write(dumps(categories, ensure_ascii=False, separators=(',', ':')))
