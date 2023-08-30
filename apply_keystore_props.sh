MANIFEST=app/src/main/AndroidManifest.xml

if [ ! -f keystore.properties ]
then
	$SHELL create_keystore_props.sh
fi

PROPS=$(cat keystore.properties)

replace_item() {
  REPLACEMENT_VALUE=$(printf "$PROPS" | grep ${2}= | cut -f2 -d=)
  # https://stackoverflow.com/a/30637209
  sed -i '/'${1}'/ s/="[^"][^"]*"/="'$REPLACEMENT_VALUE'"/' $MANIFEST
}

replace_item android:versionCode VERCODE
replace_item android:versionName VERNAME
replace_item android:minSdkVersion MINSDK
replace_item android:targetSdkVersion TARGETSDK
