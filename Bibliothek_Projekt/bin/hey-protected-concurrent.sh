#!/usr/bin/env bash
source local/config.txt || exit 1
path="$baseurl/$webapp"
mkdir -p tmp
dohey(){
  local id="$1"
  local cookie=tmp/cookie-$$-$id.jar
  curl -sL -c "$cookie" -b "$cookie" "$path/login?user=demo-$i&passwd=pwd" >/dev/null
  local jsessionid
  jsessionid=$(grep -o "[A-Z0-9]*$" <"$cookie")
  hey -n 100000 -c 50 -H "Cookie: JSESSIONID=$jsessionid" $path/protected | grep Requests
  curl -sL -c "$cookie" -b "$cookie" "$path/protected" >/dev/null
  curl -sL -c "$cookie" -b "$cookie" "$path/logout" >/dev/null
  rm "$cookie"
}

for i in {1..10}; do dohey "$i" & done

wait

