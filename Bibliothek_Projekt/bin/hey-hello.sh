#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
hey -n 100000 -c 100 $path/hello

