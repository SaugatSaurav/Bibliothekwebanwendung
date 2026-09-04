#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
curl -s "$path/hello"

mkdir -p tmp
#curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/protected
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar  $path/login -d "email=admin@gmail.com&pass=admin123"
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/protected
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/enqueue
#curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/selectdemo
curl -L -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/logout

curl -L $path/enqueue

