#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
hey -n 1000 -c 100 -m POST $path/login -d email=admin@gmail.com&password=admin123

