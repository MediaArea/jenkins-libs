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
    curl_agent='Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/602.4.8 (KHTML, like Gecko) Version/10.0.3 Safari/602.4.8'
    curl_options='--silent --compressed --location --fail'

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

                if [ "\${Type}" == "cask" ] ; then
                    Url=\$(sed -n 's/^\\s\\+appcast ['\\''"]\\([^'\\''"]\\+\\)['\\''"].*/\\1/p' "\${File}")
                    Old_Checkpoint=\$(sed -n 's/^\\s\\+checkpoint: ['\\''"]\\([0-9a-z]*\\)['\\''"].*/\\1/p' "\${File}")
                    New_Checkpoint=\$(curl ${curl_options} --user-agent '${curl_agent}' "\${Url}" |sed 's/<pubDate>[^<]*<\\/pubDate>//g' |shasum --algorithm 256 |cut -d' ' -f1)

                    sed -i "s/\${Old_Checkpoint}/\${New_Checkpoint}/g" "\${File}"
                fi

                Old_Version=\$(sed -n 's/^  version .\\([0-9a-z.]*\\).\$/\\1/p' "\${File}")
                Old_Hash=\$(sed -n 's/^  sha256 .\\([0-9a-z]*\\).\$/\\1/p' "\${File}")

                sed -i "s/\${Old_Version//./\\\\.}/\${New_Version}/g" "\${File}"
                sed -i "s/\${Old_Hash}/\${New_Hash}/g" "\${File}"

                git commit -a -m "${formula} ${version}"
                git push -f "https://${user}:${pass}@github.com/${fork}" "${formula}-${version}"
            popd

            rm -fr "\${dir}"
        """
    }
}
