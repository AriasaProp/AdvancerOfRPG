@echo off

echo "step 0 : prepare var set"
set BUILD_TOOLS=C:\Android\build-tools\25.0.3
set ANDROID_JAR=C:\Android\platforms\android-29\android.jar
set PATH=%BUILD_TOOLS%;%JAVA_HOME%\bin;%PATH%

echo "step 1 : create keystore"
if not exist bin mkdir bin
if not exist "bin\demo.keystore" (
echo "hasn't keystore, need to create keystore!"
keytool -genkey -alias demo -keyalg "RSA" -keysize 2048 -validity 10000 -keystore bin\demo.keystore -keypass 123456 -storepass 123456 -storetype pkcs12
)
if errorlevel 1 goto ERROR

echo "step 2 : Preprocess android resources"
aapt package -f -m -J src -M AndroidManifest.xml -S res -I "%ANDROID_JAR%"
if errorlevel 1 goto ERROR


echo "step 2-1 : compile core"
echo ..\core\compileCore
echo if errorlevel 1 goto ERROR

echo "step 3 : Java to bytecode"
if not exist obj mkdir obj
javac -d obj -classpath src -bootclasspath "%ANDROID_JAR%" src\com\ariasaproject\advancerofrpg\*.java -cp ..\core\src
if errorlevel 1 goto ERROR

pause
echo "step 4 : bytecode to dex"
dx --dex --output=bin\classes.dex obj
if errorlevel 1 goto ERROR

pause
echo "step 5 : generate APK container"
"%BUILD_TOOLS%\aapt" package -f -m  -F bin\unaligned.apk -M AndroidManifest.xml -S res -I "%Android_JAR%"
if errorlevel 1 goto ERROR

pause
echo "step 6 : add bytecode to APK"
cd bin
"%BUILD_TOOLS%\aapt" add unaligned.apk classes.dex
if errorlevel 1 goto ERROR

pause
echo "step 7 : Align the APK"
"%BUILD_TOOLS%\zipalign" -f 4 unaligned.apk aligned.apk
if errorlevel 1 goto ERROR

pause
echo "step 8 : sign the APK"
"%BUILD_TOOLS%\apksigner" sign --ks demo.keystore -v1-signing-enabled -v2-signing-enabled true --ks-pass pass:123456 --out hello.apk aligned.apk
if errorlevel 1 goto ERROR

goto END


:ERROR
echo error done!
goto END

:END
pause
exit /b 1