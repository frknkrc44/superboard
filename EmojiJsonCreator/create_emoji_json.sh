#!/usr/bin/env bash

if [ ! -d ".mypy_cache" ]
then
  python3 -m venv .mypy_cache
  source .mypy_cache/bin/activate
  python3 -m ensurepip
  python3 -m pip install requests
else
  source .mypy_cache/bin/activate
fi

python3 emoji_parser.py
deactivate
