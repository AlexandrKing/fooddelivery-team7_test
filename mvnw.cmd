@ECHO OFF
SETLOCAL EnableDelayedExpansion

set WRAPPER_VERSION=3.3.2
set MAVEN_PROJECTBASEDIR=%~dp0
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

set WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
set WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

if not exist "%WRAPPER_PROPERTIES%" (
  echo Error: "%WRAPPER_PROPERTIES%" not found.
  exit /b 1
)

if "%JAVA_HOME%"=="" (
  set JAVA_EXE=java.exe
) else (
  set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

where "%JAVA_EXE%" >NUL 2>&1
if ERRORLEVEL 1 (
  echo Error: JAVA_HOME is not set correctly or java is not in PATH.
  exit /b 1
)

if not exist "%WRAPPER_JAR%" (
  echo Downloading Maven wrapper jar...
  for /f "usebackq tokens=1,* delims==" %%a in ("%WRAPPER_PROPERTIES%") do (
    if /I "%%a"=="wrapperUrl" set WRAPPER_URL=%%b
  )
  if "!WRAPPER_URL!"=="" set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/%WRAPPER_VERSION%/maven-wrapper-%WRAPPER_VERSION%.jar
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri '!WRAPPER_URL!' -OutFile '%WRAPPER_JAR%'"
  if ERRORLEVEL 1 (
    echo Error: Failed to download "!WRAPPER_URL!"
    exit /b 1
  )
)

"%JAVA_EXE%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
