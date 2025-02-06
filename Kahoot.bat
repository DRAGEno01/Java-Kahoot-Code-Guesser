@echo off
echo Compiling KahootCodeGuesser.java...
javac KahootCodeGuesser.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b
)
echo Starting KahootCodeGuesser...
java KahootCodeGuesser
pause
