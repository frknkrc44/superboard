cat Makefile | grep -E 'KEYFILE=|KEYALIAS=|STOREPASS=|KEYPASS=' > keystore.properties

VERSION_CODE=$(grep 'android:versionCode' AndroidManifest.xml | cut -f2 -d'"')
VERSION_NAME=$(grep 'android:versionName' AndroidManifest.xml | cut -f2 -d'"')

MIN_SDK=$(grep 'android:minSdkVersion' AndroidManifest.xml | cut -f2 -d'"')
TARGET_SDK=$(grep 'android:targetSdkVersion' AndroidManifest.xml | cut -f2 -d'"')

echo "VERCODE="$VERSION_CODE >> keystore.properties
echo "VERNAME="$VERSION_NAME >> keystore.properties

echo "MINSDK="$MIN_SDK >> keystore.properties
echo "TARGETSDK="$TARGET_SDK >> keystore.properties