#!/bin/sh

get_gradle_property() {
    property_name="$1"

    ./gradlew -q properties --property "$property_name" \
        | awk -v prefix="${property_name}: " 'index($0, prefix) == 1 { print substr($0, length(prefix) + 1); exit }'
}

export JRELEASER_PROJECT_VERSION="$(get_gradle_property mod_version)"
export JRELEASER_PROJECT_JAVA_GROUP_ID="$(get_gradle_property maven_group)"
