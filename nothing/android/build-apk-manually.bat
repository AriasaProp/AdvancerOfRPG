@echo off

[ x$ANDROID_SDK_ROOT == x ] && {
	goto EXIT
}

[ ! -d $ANDROID_SDK_ROOT ] && {
    printf "\e[31mInvalid ANDROID_SDK_ROOT ---> $ANDROID_SDK_ROOT\e[30m\n"
	goto EXIT
}

//# use the latest build tool version
//# and the oldest platform version for compatibility
_BUILD_TOOLS_VERSION=$(ls $ANDROID_SDK_ROOT/build-tools | sort -n |tail -1)
_PLATORM=$(ls $ANDROID_SDK_ROOT/platforms | sort -nr |tail -1) 
_APK_BASENAME=MyApplication
_ANDROID_CP=$ANDROID_SDK_ROOT/platforms/$_PLATORM/android.jar
_AAPT=$ANDROID_SDK_ROOT/build-tools/$_BUILD_TOOLS_VERSION/aapt
_DX=$ANDROID_SDK_ROOT/build-tools/$_BUILD_TOOLS_VERSION/dx
_ZIPALIGN=$ANDROID_SDK_ROOT/build-tools/$_BUILD_TOOLS_VERSION/zipalign
_ADB=$ANDROID_SDK_ROOT/platform-tools/adb
_INTERMEDIATE="bin gen ${_APK_BASENAME}.apk.unaligned"

printf "\e[32mBuild with configuration: \n\tbuild tools version: $_BUILD_TOOLS_VERSION \n\tplatform: $_PLATORM\e[30m\n"

rm -rf $_INTERMEDIATE
mkdir bin gen

$_AAPT package -f -m -J gen -M AndroidManifest.xml -S res -I $_ANDROID_CP

javac -classpath $_ANDROID_CP \
	-sourcepath 'src:gen' \
	-d 'bin' -target 1.7 -source 1.7 \
	`find . -name "*.java"`

$_DX --dex --output=classes.dex bin

$_AAPT package -f -M AndroidManifest.xml -S res -I $_ANDROID_CP -F ${_APK_BASENAME}.apk.unaligned

$_AAPT add ${_APK_BASENAME}.apk.unaligned classes.dex

jarsigner -keystore ~/.android/debug.keystore -storepass 'android' ${_APK_BASENAME}.apk.unaligned androiddebugkey
//# create a release version with your keys
//# jarsigner -keystore /path/to/your/release/keystore -storepass 'yourkeystorepassword' ${_APK_BASENAME}.apk.unaligned yourkeystorename 

$_ZIPALIGN -f 4 ${_APK_BASENAME}.apk.unaligned ${_APK_BASENAME}-debug.apk

rm -rf $_INTERMEDIATE

$_ADB get-state 1>/dev/null 2>&1 && $_ADB install -r ${_APK_BASENAME}-debug.apk || printf '\e[31mNo Android device attach\e[30m\n'


:EXIT
pause
exit 1
