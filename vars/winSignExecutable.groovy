#!/usr/bin/groovy

/*
winSignExecutable - sign windows PE executable
 arguments:
  - file: package file to sign and stamp
  - name: signature object
*/

import groovy.transform.Field
import java.util.concurrent.atomic.AtomicInteger

@Field static AtomicInteger counter = new AtomicInteger(0)

def call(path, name) {
    def file = new File(path).name
    def id = counter.incrementAndGet()

    echo("Signing ${file} with subject ${name}...")

    dir(new File(path).parent) {
        stash(name: "codesign-input-${id}", includes: file)
        node('codesign') {
            deleteDir()
            unstash("codesign-input-${id}")
            withEnv(["FILE=${file}", "NAME=${name}"]) {
                sh 'sleep 10; $HOME/.codesign "${NAME}" "${FILE}" "${FILE}.signed"'
            }
            stash(name: "codesign-output-${id}", includes: "${file}.signed")
            dir("${workspace}@tmp") {
                deleteDir()
            }
            cleanWs()
        }
        unstash("codesign-output-${id}")
        withEnv(["FILE=${file}"]) {
            powershell '''
                Remove-Item -Force -Path "${Env:FILE}"
                Move-Item -Force -Path "${Env:FILE}.signed" -Destination "${Env:FILE}"
            '''
        }
    }
}
