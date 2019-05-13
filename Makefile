SDK=~/Android/Sdk
TARGET=28
TOOL=28.0.3
JAVADIR=/usr/bin
BUILDTOOLS=$(SDK)/build-tools/$(TOOL)
AJAR=$(SDK)/platforms/android-$(TARGET)/android.jar
ADX=$(BUILDTOOLS)/dx
AAPT=$(BUILDTOOLS)/aapt
JAVAC=$(JAVADIR)/javac
JARSIGNER=$(JAVADIR)/jarsigner
APKSIGNER=$(BUILDTOOLS)/apksigner
ZIPALIGN=$(BUILDTOOLS)/zipalign
KEYTOOL=$(JAVADIR)/keytool
ADB=$(SDK)/platform-tools/adb

KEYFILE=key.keystore

SRC=src/
NAME=app

STOREPASS=123456
KEYPASS=123456

all: clear build zipalign sign install
build:
	mkdir bin
	mkdir gen
	$(AAPT) package -v -f -I $(AJAR) -M "AndroidManifest.xml" -A "assets" -S "res" -m -J "gen" -F "bin/resources.ap_"
	$(JAVAC) -classpath $(AJAR) -sourcepath $(SRC) -sourcepath gen -d bin $(shell find $(SRC) -name "*.java")
	$(ADX) --dex --output=bin/classes.dex bin
	mv bin/resources.ap_ bin/$(NAME).ap_
	cd bin ; $(AAPT) add $(NAME).ap_ classes.dex
zipalign:
	$(ZIPALIGN) -v -p 4 bin/$(NAME).ap_ bin/$(NAME)-aligned.ap_
	mv bin/$(NAME)-aligned.ap_ bin/$(NAME).ap_
optimize:
	optipng -o7 $(shell find res -name "*.png")
sign:
	$(APKSIGNER) sign --ks $(KEYFILE) --ks-key-alias Alias --ks-pass pass:$(STOREPASS) --key-pass pass:$(KEYPASS) --out bin/$(NAME).apk bin/$(NAME).ap_
	rm -f bin/$(NAME).ap_
jarsign:
	$(JARSIGNER) -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -signedjar bin/$(NAME).apk bin/$(NAME).ap_ Alias
	rm -f bin/$(NAME).ap_
generate:
	rm -f $(KEYFILE)
	$(KEYTOOL) -genkey -noprompt -alias Alias -dname "CN=Hostname, OU=OrganizationalUnit, O=Organization, L=City, S=State, C=Country" -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -validity 3650
clear:
	rm -rf bin gen
install:
	$(ADB) install -r bin/$(NAME).apk
