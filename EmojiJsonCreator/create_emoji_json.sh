#!/usr/bin/env bash

if ! python3 -c 'from requests import get' 2> /dev/null
then
  if [ ! -d ".mypy_cache" ]
  then
    python3 -m venv .mypy_cache
    source .mypy_cache/bin/activate
    python3 -m ensurepip
    python3 -m pip install requests
  else
    source .mypy_cache/bin/activate
  fi
fi

python3 emoji_parser.py
command -v deactivate >/dev/null && deactivate
