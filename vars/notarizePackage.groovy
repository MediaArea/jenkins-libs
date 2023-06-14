#!/usr/bin/groovy

/*
notarizePackage - submit package to Apple for notarization,
wait for result and stamp package
 arguments:
  - file: package file to notarize and stamp
  - credentials: jenkins credentials id of the apple developper account
  - teamid:apple team id
*/

def call(file, credentials, teamid) {
    withEnv(["TMPDIR=${pwd(tmp: true)}", "FILE=${file}", "TEAMID=${teamid}"]) {
        withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'username', passwordVariable: 'password')]) {
            sh '''
                tmp="$(mktemp -d -t notarize 2>/dev/null)"
                pushd "${tmp}" 1>/dev/null 2>&1
                    xcrun notarytool submit \
                                     --output-format plist \
                                     --apple-id "${username}" \
                                     --password "${password}" \
                                     --team-id "${TEAMID}" \
                                     "${FILE}" >upload.plist 2>/dev/null || true

                    uuid=$(/usr/libexec/PlistBuddy -c 'Print id' upload.plist 2>/dev/null || true)

                    if [ -z "${uuid}" ] ; then
                        echo "error: upload failed"
                        exit 1
                    fi

                    xcrun notarytool wait \
                                     --output-format plist \
                                     --apple-id "${username}" \
                                     --password "${password}" \
                                     --team-id "${TEAMID}" \
                                     --timeout 1h \
                                     "${uuid}" >status.plist 2>/dev/null || true


                    status=$(/usr/libexec/PlistBuddy -c 'Print status' status.plist 2>/dev/null || true)

                    if [ "${status}" != "Accepted" ] ; then
                        echo "error: package rejected"
                        exit 1
                    fi

                    for i in $(seq 1 15) ; do
                        if xcrun stapler staple "${FILE}" ; then
                            break
                        fi
                        sleep 1
                    done

                popd 1>/dev/null 2>&1
            '''
        }
    }
}
