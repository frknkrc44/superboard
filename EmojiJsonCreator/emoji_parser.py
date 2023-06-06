from json import loads, dumps

with open('emoji.json') as f:
    json = loads(f.read())
categories: dict[str, list[str]] = {}
for item in json:
    if item['category'] not in categories:
        category: list[str] = []
        categories[item['category']] = category
    else:
        category: list[str] = categories.get(item['category']) # type: ignore
    
    emoji = item['emoji']
    category.append(emoji)

with open('emoji_list.json', 'w') as f:
    f.write(dumps(categories, ensure_ascii=False))
