#!/bin/bash
set -euo pipefail

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    JDK26_HOME="$(/usr/libexec/java_home -v 26 2>/dev/null || true)"
    if [[ -n "$JDK26_HOME" ]]; then
        JAVA_HOME="$JDK26_HOME" exec mvn spring-boot:run
    fi
fi

exec mvn spring-boot:run
