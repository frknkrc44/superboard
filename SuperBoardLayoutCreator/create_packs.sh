#!/usr/bin/bash

$1javac *.java
for i in `ls | grep .class$ | sed 's/.class//g'`
do
    echo $i
    $1java $i
done
mv *.json ../assets/langpacks/
rm -f *.class
