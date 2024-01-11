MANIFEST=app/src/main/AndroidManifest.xml

if [ ! -f keystore.properties ]
then
	$SHELL create_keystore_props.sh
fi
