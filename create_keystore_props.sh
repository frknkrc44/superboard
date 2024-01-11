MANIFEST=app/src/main/AndroidManifest.xml

cat Makefile | grep -E 'KEYFILE=|KEYALIAS=|STOREPASS=|KEYPASS=' > keystore.properties
