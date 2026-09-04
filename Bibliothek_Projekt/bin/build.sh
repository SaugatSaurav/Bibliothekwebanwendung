#!/usr/bin/env bash
# set -x
source local/config.txt || exit 1
rm -rf build/* target/*
cp -r app/* build



sed -i \
  -e "s/REDISSERVER/${redisserver}/g" \
  -e "s/REDISPASSWORD/${redispassword}/g" \
  -e "s/MAILUSER/${mailuser}/g" \
  -e "s/MAILPASSWORD/${mailpassword}/g" \
  build/WEB-INF/web.xml


#sed  -i -e "s/REDISSERVER/${redisserver}/g" -e "s/REDISPASSWORD/${redispassword}/g" build/WEB-INF/web.xml
sed  -i -e "s/DBSERVER/${dbserver}/g" -e "s/DBUSER/${dbuser}/g" \
  -e "s/DBPASSWORD/${dbpassword}/g" -e "s/DBNAME/${dbname}/g" build/META-INF/context.xml

shopt -s globstar
javac -Xlint:deprecation --release 25 -cp 'lib/*' -d build/WEB-INF/classes src/**/*.java &&
jar -cf 'target/webapp.war' -C build . &&
curl --location --netrc-file local/netrc --fail --upload-file target/webapp.war "$baseurl/$manager/text/deploy?path=/$webapp&update=true" 

