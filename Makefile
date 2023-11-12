SDK=$(HOME)/Android/Sdk
TARGET=$(shell ls $(SDK)/build-tools | tail -n1 | cut -f1 -d.)
TOOL=$(shell ls $(SDK)/build-tools | tail -n1)
JAVADIR=/usr/bin
BUILDTOOLS=$(SDK)/build-tools/$(TOOL)
AJAR=$(SDK)/platforms/android-$(TARGET)/android.jar
ADX=$(BUILDTOOLS)/dx
AD8=$(BUILDTOOLS)/d8
AAPT=$(BUILDTOOLS)/aapt
JAVAC=$(JAVADIR)/javac
KOTLINC=$(SDK)/plugins/Kotlin/kotlinc/bin/kotlinc
JARSIGNER=$(JAVADIR)jarsigner
APKSIGNER=$(BUILDTOOLS)/apksigner
ZIPALIGN=$(BUILDTOOLS)/zipalign
KEYTOOL=$(JAVADIR)/keytool
ADB=$(SDK)/platform-tools/adb
FAIDL=$(SDK)/platforms/android-$(TARGET)/framework.aidl
AIDL=$(BUILDTOOLS)/aidl
space := $(aa) $(aa)
CLASSPATH=$(AJAR):$(subst $(space),:,$(shell find include/ -name "*.jar"))

AIDLP=app/src/main/aidl/
SRC=app/src/main/java/
RES=app/src/main/res/
ASSETS=app/src/main/assets/
NAME=FBoard

KEYFILE=key.jks
KEYALIAS=Alias
STOREPASS=123456
KEYPASS=123456
# JAVAC_DEBUG_FLAGS = "-Xlint:unchecked -Xlint:deprecation"
JAVAC_DEBUG_FLAGS = 

all: langpacks rmdirs optimize keystore
# all: clear mkdirs langpacks keystore abuild build rmdirs zipalign sign
# build-install: all install
# build:
# 	$(AAPT) package -v -f -I $(AJAR) -M "AndroidManifest.xml" -A $(ASSETS) -S $(RES) -m -J "gen" -F "bin/$(NAME).ap_"
# 	$(JAVAC) -classpath $(CLASSPATH) -source 8 -target 8 -g:none -nowarn -sourcepath $(SRC) -sourcepath bin/aidl/ -sourcepath gen -d bin $(JAVAC_DEBUG_FLAGS) `find gen -name "*.java"` `find bin/aidl/ -name "*.java"` `find $(SRC) -name "*.java"`
# 	@[ -f $(ADX) ] && \
		$(ADX) --dex --output=bin/classes.dex bin || \
		$(AD8) `find bin/ -name "*.class"` --lib $(AJAR) --output bin
#	cd bin && $(AAPT) add $(NAME).ap_ *.dex
# abuild:
# 	$(AIDL) -I$(AIDLP) -I$(SRC) -p$(FAIDL) -obin/aidl/ `find $(AIDLP) -name "*.aidl"` `find $(SRC) -name "*.aidl"`
# zipalign:
# 	$(ZIPALIGN) -v -p 4 bin/$(NAME).ap_ bin/$(NAME)-aligned.ap_
# 	mv bin/$(NAME)-aligned.ap_ bin/$(NAME).ap_
optimize:
	optipng -quiet -o7 `find $(RES) -name "*.png"`
# sign:
# 	$(APKSIGNER) sign --ks $(KEYFILE) --ks-key-alias $(KEYALIAS) --ks-pass pass:$(STOREPASS) --key-pass pass:$(KEYPASS) --out bin/$(NAME)-v`cat keystore.properties | grep VERNAME= | cut -f2 -d=`.apk bin/$(NAME).ap_
# 	#rm -f bin/$(NAME).ap_
# jarsign:
# 	$(JARSIGNER) -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -signedjar bin/$(NAME).apk bin/$(NAME).ap_ $(KEYALIAS)
# 	#rm -f bin/$(NAME).ap_
generate:
	rm -f $(KEYFILE)
	$(KEYTOOL) -genkey -noprompt -keyalg RSA -alias $(KEYALIAS) -dname "CN=Hostname, OU=OrganizationalUnit, O=Organization, L=City, S=State, C=Country" -keystore $(KEYFILE) -storepass $(STOREPASS) -keypass $(KEYPASS) -validity 3650
# clear:
# 	rm -rf bin gen
install:
	$(ADB) install -r app/build/outputs/apk/release/$(NAME)-v*.apk
installdebug:
	$(ADB) install -r app/build/outputs/apk/debug/$(NAME)-v*.apk
# mkdirs:
# 	mkdir app/src/main/aidl 2> /dev/null || true
# 	mkdir bin 2> /dev/null || true
# 	mkdir gen 2> /dev/null || true
# 	mkdir app/src/main/assets 2> /dev/null || true
# 	mkdir app/src/main/res 2> /dev/null || true
# 	mkdir bin/aidl 2> /dev/null || true
# 	mkdir include 2> /dev/null || true
rmdirs:
	rmdir `find -type d` 2> /dev/null || true
# push:
# 	$(ADB) push bin/$(NAME).apk /sdcard
langpacks:
	cd SuperBoardLayoutCreator && \
		${SHELL} create_packs.sh
keystore:
	${SHELL} apply_keystore_props.sh
