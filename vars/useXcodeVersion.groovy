#!/usr/bin/groovy

/*
useXcodeVersion - Set DEVELOPER_DIR to a specific Xcode version, fail if unavailable.
 arguments:
  - version: requested Xcode version
*/

def call(String version) {
    def developerDir = sh(script: "echo ~/Xcode/${version}.app/Contents/Developer", returnStdout: true).trim()

    if (sh(script: "test -d \"${developerDir}\"", returnStatus: true) != 0) {
        error("Xcode version '${version}' not found")
    }

    env.DEVELOPER_DIR = developerDir
}
