@ECHO OFF
SETLOCAL

SET "MAVEN_VERSION=3.9.9"
SET "MAVEN_DISTS=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin"
SET "MAVEN_HOME="

FOR /D %%D IN ("%MAVEN_DISTS%\*") DO (
  IF EXIST "%%D\apache-maven-%MAVEN_VERSION%\bin\mvn.cmd" SET "MAVEN_HOME=%%D\apache-maven-%MAVEN_VERSION%"
)

IF NOT DEFINED MAVEN_HOME (
  ECHO Downloading Maven %MAVEN_VERSION%...
  SET "MAVEN_HOME=%MAVEN_DISTS%\local\apache-maven-%MAVEN_VERSION%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; $destination = '%MAVEN_DISTS%\local'; New-Item -ItemType Directory -Force -Path $destination | Out-Null; $archive = Join-Path $destination 'maven.zip'; Invoke-WebRequest -UseBasicParsing -Uri $url -OutFile $archive; Expand-Archive -LiteralPath $archive -DestinationPath $destination -Force; Remove-Item -LiteralPath $archive"
  IF ERRORLEVEL 1 EXIT /B 1
)

"%MAVEN_HOME%\bin\mvn.cmd" %*
