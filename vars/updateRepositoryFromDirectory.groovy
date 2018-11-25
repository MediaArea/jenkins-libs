#!/usr/bin/groovy

/*
updateRepositoryFromDirectory - copy the content of a directory into a
  local copy of a git repository, then create PR with the changes
 arguments:
  - repo: github repository to sync to
  - credentials: jenkins credentials id of repository
  - path: path of local repository
  - version: version msg
*/

def call(repo, credentials, path, version) {
    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'user', passwordVariable: 'pass')]) {
        sh """
            pushd "${path}"
                git init
                git remote add origin "https://github.com/${repo}"
                git fetch origin
                git reset --mixed origin/master
                git add -A
                git commit -m "Update to version ${version}"
                git push -f "https://${user}:${pass}@github.com/${repo}" "master"
            popd
        """
    }
}
