#!/usr/bin/groovy

/*
pushChocolatey - push chocolatey package to server
 arguments:
  - pkg: package path
  - server: destination server
*/

def call(pkg, server) {
    powershell "choco push ${pkg} --source ${server}"

}
