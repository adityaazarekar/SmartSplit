@echo off
set "JAVA_HOME=C:\openjdk-25.0.1_windows-x64_bin\jdk-25.0.1"
set "JAVAC=%JAVA_HOME%\bin\javac.exe"
set "JAVA=%JAVA_HOME%\bin\java.exe"

echo 🚀 Compiling SmartSplit...
"%JAVAC%" -d "out\production\ExpenseSharingSystem" src\com\expense\model\*.java src\com\expense\service\*.java src\com\expense\gui\*.java

if %ERRORLEVEL% NEQ 0 (
    echo ❌ Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo ✅ Compilation successful.
echo 🏃 Starting SmartSplit...
start "" "%JAVA%" -cp "out\production\ExpenseSharingSystem" com.expense.gui.SmartSplitApp
