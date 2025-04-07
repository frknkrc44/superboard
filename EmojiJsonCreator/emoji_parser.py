from json import dumps
from urllib.request import urlopen

def get_emoji_desc(line: str, mode2: bool = False) -> str:
    first = line[line.find('#') + 2:]
    second = first[first.find('E'):]
    third = second[second.find(' ') + 1:]

    if mode2:
        return third[third.rfind(',') + 2:]

    return third


def compare_emoji_desc(first: str, second: str) -> bool:
    if get_emoji_desc(second) in get_emoji_desc(first, True):
        return True

    if get_emoji_desc(second, True) in get_emoji_desc(first):
        return True

    if get_emoji_desc(second) in get_emoji_desc(first):
        return True

    return False


categories: dict[str, list[str]] = {}
with urlopen('https://unicode.org/Public/emoji/latest/emoji-test.txt') as request:
    text = request.read().decode()
    status_code = request.code

if status_code == 200:
    recent_group = ''
    recent_approved_line = ''
    for line in text.splitlines():
        if line.startswith('# group: '):
            recent_group = line[line.find(':') + 2:]
            category: list[str] = []
            categories[recent_group] = category

        if not len(recent_group):
            continue

        if not line.startswith('#') and ';' in line and 'qualified' in line and 'E' in line:
            if not len(recent_approved_line) or not compare_emoji_desc(line, recent_approved_line):
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
