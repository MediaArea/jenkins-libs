#!/usr/bin/groovy

/*
handleOBSresults - download packages from OBS
 arguments:
  - credentials: obs credentials
  - project: OBS project name
  - version: version to download
  - path: destination directory
  - path: destination subdirectory
  - release: release mode
  - filter: (optional) download only distributions/archs
*/

def call(credentials, project, version, path, subdir, release, filter="") {
    def extra_args = ""

    if (!filter.allWhitespace) {
        extra_args += " --filter ${filter}"
    }

    if (release) {
        extra_args += " --release"
    }

    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'username', passwordVariable: 'password')]) {
        sh '''
            mkdir -p ~/.config/osc
            echo "[general]" > ~/.config/osc/oscrc
            echo "apiurl = https://api.opensuse.org" >> ~/.config/osc/oscrc
            echo "[https://api.opensuse.org]" >> ~/.config/osc/oscrc
            echo "user = ${username}" >> ~/.config/osc/oscrc
            echo "pass = ${password}" >> ~/.config/osc/oscrc
        '''
    }

    withEnv(["project_arg=${project}", "version_arg=${version}", "path_arg=${path}", "subdir_arg=${subdir}", "extra_args_arg=${extra_args}"]) {
        sh '''
            python3 ${UTILS}/build_release/handleOBSResults/handleOBSResults.py ${extra_args_arg} ${project_arg%/*} ${project_arg##*/} ${version_arg} ${path_arg} ${subdir_arg}
        '''
    }
}
