#!/usr/bin/groovy

/*
handleOBSresults - download packages from OBS
 arguments:
  - credentials: obs credentials
  - project: OBS project name
  - version: version to download
  - paths: destination directories
  - filter: (optional) download only distributions/archs
*/

def call(credentials, project, version, paths, filter="") {
    def extra_args = "--repo-script"

    if (!filter.allWhitespace) {
        extra_args += " --filter ${filter}"
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

    withEnv(["project_arg=${project}", "version_arg=${version}", "paths_arg=${paths.join(' ')}", "extra_args_arg=${extra_args}"]) {
        sh '''
            python ${UTILS}/build_release/Handle_OBS_results.py ${extra_args_arg} ${project_arg%/*} ${project_arg##*/} ${version_arg} ${paths_arg}
        '''
    }
}
