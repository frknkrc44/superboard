SDK=$(HOME)/Android/Sdk
TARGET=29
TOOL=$(shell ls $(SDK)/build-tools | grep $(TARGET))
JAVADIR= 
BUILDTOOLS=$(SDK)/build-tools/$(TOOL)
AJAR=$(SDK)/platforms/android-$(TARGET)/android.jar
ADX=$(BUILDTOOLS)/dx
AAPT=$(BUILDTOOLS)/aapt
JAVAC=$(JAVADIR)javac
KOTLINC=$(SDK)/plugins/Kotlin/kotlinc/bin/kotlinc
JARSIGNER=$(JAVADIR)jarsigner
APKSIGNER=$(BUILDTOOLS)/apksigner
ZIPALIGN=$(BUILDTOOLS)/zipalign
KEYTOOL=$(JAVADIR)keytool
ADB=$(SDK)/platform-tools/adb
FAIDL=$(SDK)/platforms/android-$(TARGET)/framework.aidl
AIDL=$(BUILDTOOLS)/aidl
space := $(aa) $(aa)
CLASSPATH=$(AJAR):$(subst $(space),:,$(shell find include/ -name "*.jar"))

SRC=src/
NAME=$(shell basename $(CURDIR))

KEYFILE=key.jks
KEYALIAS=Alias
STOREPASS=123456
KEYPASS=123456
# JAVAC_DEBUG_FLAGS = "-Xlint:unchecked -Xlint:deprecation"
JAVAC_DEBUG_FLAGS = 

all: clear mkdirs build rmdirs zipalign jarsign install
build:
	$(AAPT) package -v -f -I $(AJAR) -M "AndroidManifest.xml" -A "assets" -S "res" -m -J "gen" -F "bin/resources.ap_"
	$(JAVAC) -classpath $(CLASSPATH) -source 7 -g:none -nowarn -sourcepath $(SRC) -sourcepath bin/aidl/ -sourcepath gen -d bin $(JAVAC_DEBUG_FLAGS) `find gen -name "*.java"` `find bin/aidl/ -name "*.java"` `find $(SRC) -name "*.java"`
	$(ADX) --dex --output=bin/classes.dex bin
	mv bin/resources.ap_ bin/$(NAME).ap_
	cd bin ; $(AAPT) add $(NAME).ap_ classes.dex
abuild:
	$(AIDL) -Iaidl -I$(SRC) -p$(FAIDL) -obin/aidl/ `find aidl -name "*.aidl"` `find $(SRC) -name "*.aidl"`
zipalign:
	$(ZIPALIGN) -v -p 4 bin/$(NAME).ap_ bin/$(NAME)-aligned.ap_
	mv bin/$(NAME)-aligned.ap_ bin/$(NAME).ap_
optimize:
	optipng -o7 `find res -name "*.png"`
sign:
	$(APKSIGNER) sign --ks $(KEYFILE) --ks-key-alias $(KEYALIAS) --ks-pass pass:$(STOREPASS) --key-pass pass:$(KEYPASS) --out bin/$(NAME).apk bin/$(NAME).ap_
	#rm -f bin/$(NAME).ap_
jarsign:
	$(JARSIGNER) -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -signedjar bin/$(NAME).apk bin/$(NAME).ap_ $(KEYALIAS)
	#rm -f bin/$(NAME).ap_
generate:
	rm -f $(KEYFILE)
	$(KEYTOOL) -genkey -noprompt -keyalg RSA -alias $(KEYALIAS) -dname "CN=Hostname, OU=OrganizationalUnit, O=Organization, L=City, S=State, C=Country" -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -validity 3650
clear:
	rm -rf bin gen
install:
	$(ADB) install -r bin/$(NAME).apk
mkdirs:
	mkdir aidl 2> /dev/null || true
	mkdir bin 2> /dev/null || true
	mkdir gen 2> /dev/null || true
	mkdir assets 2> /dev/null || true
	mkdir res 2> /dev/null || true
	mkdir bin/aidl 2> /dev/null || true
	mkdir include 2> /dev/null || true
rmdirs:
	rmdir `find` 2> /dev/null || true
push:
	$(ADB) push bin/$(NAME).apk /sdcard
