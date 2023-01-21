#!/usr/bin/bash

$1/javac *.java
for i in `ls | grep .class$ | sed 's/.class//g'`
do
    echo $i
    $1/java $i
done
mv *.json ../assets/langpacks/
rm -f *.class
