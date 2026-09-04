#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
mkdir -p tmp
curl -sL -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/login?"user=demo&passwd=pwd"
jsessionid=$(cat tmp/cookie-$$.jar|grep -o "[A-Z0-9].*$")
echo "JSESSIONID: $jsessionid"
hey -n 100000 -c 10 -H "Cookie: JSESSIONID=$jsessionid" $path/protected
curl -sL -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/protected
curl -sL -c tmp/cookie-$$.jar -b tmp/cookie-$$.jar $path/logout
rm tmp/cookie-$$.jar

