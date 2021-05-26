#!/usr/bin/groovy

/*
updateOBSProject - update sources on OBS
 arguments:
  - credentials: obs credentials
  - project: OBS project name
  - src: sources directory path
*/

def call(credentials, project, src) {
    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'username', passwordVariable: 'password')]) {
        sh '''
            mkdir -p ~/.config/osc
            echo "[general]" > ~/.config/osc/oscrc
            echo "apiurl = https://api.opensuse.org" >>  ~/.config/osc/oscrc
            echo "[https://api.opensuse.org]" >>  ~/.config/osc/oscrc
            echo "user = ${username}" >>  ~/.config/osc/oscrc
            echo "pass = ${password}" >>  ~/.config/osc/oscrc
        '''
    }

    withEnv(["project=${project}", "src=${src}"]) {
        retry(5) {
            sh '''
                rm -rf ${project}

                osc checkout ${project}

                rm -rf ${project}/*

                cp -a ${src}/* ${project}

                osc addremove ${project}/*
                osc commit -n ${project}
            '''
        }
    }
}
