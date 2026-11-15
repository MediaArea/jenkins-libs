#!/usr/bin/groovy

/*
podDeleteDir - remap subuid and subgid files then delete the current directory
*/

def call() {
    withEnv(["POD_DELETE_DIR_PATH=${pwd()}"]) {
        sh 'podman unshare chown -R 0:0 "$POD_DELETE_DIR_PATH"'
    }
    deleteDir()
}
