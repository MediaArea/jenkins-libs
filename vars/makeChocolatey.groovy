#!/usr/bin/groovy

/*
makeChocolatey - create chocolatey package  from current directory
 arguments:
  - name: package name
  - version: package version
*/

def call(name, version, src) {
    powershell 'choco pack'

    return new File("${name}.${version}.nupkg").exists()
}
