#!/usr/bin/groovy

/*
updateBrewFormula - update homebrew/cask formula for new version of package in forked repository
 arguments:
  - repo: github upstream repository of formula (e.g. homebrew/homebrew-core)
  - fork: github fork repository name in account/repository notation
  - credentials: jenkins credentials id of fork repository
  - formula: name of the formula to update
  - archive: path to the package archive
  - version: new package version
*/

def call(repo, fork, credentials, formula, archive, version) {
    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'user', passwordVariable: 'pass')]) {
        sh """
            New_Version="${version}"
            New_Hash=\$(sha256sum "${archive}" |cut -d' ' -f1)

            dir="brew-\${RANDOM}"
            rm -fr "\${dir}"

            git clone --depth=1 "https://github.com/${repo}" "\${dir}"

            pushd "\${dir}"
                git checkout -b "${formula}-${version}"

                if [ -e "${formula}.rb" ] ; then
                    File="${formula}.rb"
                    Type="flat"
                elif [ -e "Casks/${formula}.rb" ] ; then
                    File="Casks/${formula}.rb"
                    Type="cask"
                elif [ -e "Formula/${formula}.rb" ] ; then
                    File="Formula/${formula}.rb"
                    Type="brew"
                else
                    echo "ERROR: formula ${formula} not found in ${repo}"
                    exit
                fi

                Old_Version=\$(sed -n 's/^  version .\\([0-9a-z.]*\\).\$/\\1/p' "\${File}")
                Old_Hash=\$(sed -n 's/^  sha256 .\\([0-9a-z]*\\).\$/\\1/p' "\${File}")

                if [ -n "$Old_Version" ] ; then
                    sed -i "s/\${Old_Version//./\\\\.}/\${New_Version}/g" "\${File}"
                fi
                sed -i "s/\${Old_Hash}/\${New_Hash}/g" "\${File}"

                git commit -a -m "${formula} ${version}"
                git push -f "https://${user}:${pass}@github.com/${fork}" "${formula}-${version}"
            popd

            rm -fr "\${dir}"
        """
    }
}
