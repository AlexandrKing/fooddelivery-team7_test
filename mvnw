#!/bin/sh
# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup script, version 3.3.2
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != debug ] || set -x

die() {
  printf %s\\n "$1" >&2
  exit 1
}

trim() {
  printf "%s" "${1}" | tr -d '[:space:]'
}

case "$(uname)" in
CYGWIN* | MINGW* | MSYS*) cygwin=true ;;
*) cygwin=false ;;
esac

if [ -z "${JAVA_HOME-}" ]; then
  javaExecutable="$(command -v java || true)"
else
  javaExecutable="$JAVA_HOME/bin/java"
fi
[ -n "${javaExecutable-}" ] || die "Error: JAVA_HOME is not set and no 'java' command could be found in PATH."
[ -x "$javaExecutable" ] || die "Error: JAVA_HOME is set to an invalid directory: $JAVA_HOME"

while [ -h "$0" ]; do
  ls="$(ls -ld "$0")"
  link="$(expr "$ls" : '.*-> \(.*\)$')"
  if expr "$link" : '/.*' >/dev/null; then
    PRG="$link"
  else
    PRG="$(dirname "$0")/$link"
  fi
done

BASEDIR="$(cd "$(dirname "$0")" && pwd -P)"
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-"$BASEDIR"}"
export MAVEN_PROJECTBASEDIR

wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
wrapperPropertiesPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
[ -f "$wrapperPropertiesPath" ] || die "Error: $wrapperPropertiesPath not found."

wrapperUrl="$(sed -n 's/^wrapperUrl=//p' "$wrapperPropertiesPath" | tail -n 1)"
[ -n "$wrapperUrl" ] || wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if [ ! -r "$wrapperJarPath" ]; then
  echo "Downloading Maven wrapper jar..."
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$wrapperJarPath" "$wrapperUrl"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$wrapperJarPath" "$wrapperUrl"
  else
    die "Error: Neither curl nor wget is available to download Maven wrapper."
  fi
fi

MAVEN_OPTS="$(trim "${MAVEN_OPTS-}")"
export MAVEN_OPTS

exec "$javaExecutable" \
  -classpath "$wrapperJarPath" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
