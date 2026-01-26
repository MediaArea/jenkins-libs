#!/usr/bin/groovy

/*
winSignExecutable - sign windows PE executable
 arguments:
  - file: package file to sign and stamp
  - name: signature object
*/

def call(path, name) {
    def file = new File(path).name
    echo(file)
    dir(new File(path).parent) {
        stash(name: file, includes: file)
        node('codesign') {
            deleteDir()
            unstash(file)
            withEnv(["FILE=${file}", "NAME=${name}"]) {
                sh '$HOME/.codesign "${NAME}" "${FILE}" ${FILE}.signed'
            }
            stash(name: "${file}.signed", includes: "${file}.signed")
        }
        unstash("${file}.signed")
        withEnv(["FILE=${file}"]) {
            powershell '''
                Remove-Item -Force -Path "${Env:FILE}"
                Copy-Item -Force -Path "${Env:FILE}.signed" -Destination "${Env:FILE}"
            '''
        }
    }
}
