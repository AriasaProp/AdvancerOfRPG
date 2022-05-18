
D:\ariasa\android-ndk-r20b-windows-x86\android-ndk-r20b\build\ndk-build.cmd NDK_PROJECT_PATH=%~dp0%
if errorlevel 1 goto ERROR
pause

:ERROR
pause
exit