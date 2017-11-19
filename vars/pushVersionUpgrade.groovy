#!/usr/bin/groovy

/*
pushVersionUpgrade - push branch with version upgrade
 arguments:
  - repo: github repository to push branch to
  - credentials: jenkins credentials id of repository
  - path: path of local repository
  - version: new version
*/

def call(repo, credentials, path, version) {
    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'user', passwordVariable: 'pass')]) {
        sh """
            pushd "${path}"
                git checkout -b "preparing-v${version}"
                git commit -a -m "Preparing v${version}"
                git push -f "https://${user}:${pass}@github.com/${repo}" "preparing-v${version}"
            popd
        """
    }
}
