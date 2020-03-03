#!/usr/bin/groovy

/*
notarizePackage - submit package to Apple for notarization,
wait for result and stamp package
 arguments:
  - file: package file to notarize and stamp
  - bundle: package bundle id
  - username: jenkins credentials id of the apple developper account
*/

def call(file, bundle, credentials) {
    def TMPDIR = pwd(tmp: true)

    withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'username', passwordVariable: 'password')]) {
        sh """
            tmp="\$(mktemp -d -t notarize 2>/dev/null)"
            pushd "\${tmp}" 1>/dev/null 2>&1
                xcrun altool --output-format xml \
                             --username "${username}" \
                             --password "${password}" \
                             --notarize-app \
                             --primary-bundle-id "${bundle}" \
                             --file "${file}" >upload.plist 2>/dev/null || true

                uuid=\$(/usr/libexec/PlistBuddy -c 'Print notarization-upload:RequestUUID' upload.plist 2>/dev/null || true)

                if [ -z "\${uuid}" ] ; then
                    echo "error: upload failed"
                    exit 1
                fi

                for i in \$(seq 1 90) ; do
                    sleep 60

                    xcrun altool --output-format xml \
                                 --username "${username}" \
                                 --password "${password}" \
                                 --notarization-info "\${uuid}" >status.\${i}.plist 2>/dev/null || true

                    status=\$(/usr/libexec/PlistBuddy -c 'Print notarization-info:Status' status.\${i}.plist 2>/dev/null || true)

                    if [ "\${status}" == "in progress" ] ; then
                        continue
                    else
                        if [ "\${status}" != "success" ] ; then
                            exit 1
                        fi
                        xcrun stapler staple "${file}"
                        exit 0
                    fi
                done
                exit 1
            popd 1>/dev/null 2>&1
        """
    }
}
