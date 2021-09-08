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
    withEnv(["TMPDIR=${pwd(tmp: true)}", "BUNDLE=${bundle}", "FILE=${file}"]) {
        withCredentials([usernamePassword(credentialsId: credentials, usernameVariable: 'username', passwordVariable: 'password')]) {
            sh '''
                tmp="$(mktemp -d -t notarize 2>/dev/null)"
                pushd "${tmp}" 1>/dev/null 2>&1
                    xcrun altool --output-format xml \
                                 --username "${username}" \
                                 --password "${password}" \
                                 --notarize-app \
                                 --primary-bundle-id "${BUNDLE}" \
                                 --file "${FILE}" >upload.plist 2>/dev/null || true

                    uuid=$(/usr/libexec/PlistBuddy -c 'Print notarization-upload:RequestUUID' upload.plist 2>/dev/null || true)

                    if [ -z "${uuid}" ] ; then
                        echo "error: upload failed"
                        exit 1
                    fi

                    for i in $(seq 1 90) ; do
                        sleep 60

                        xcrun altool --output-format xml \
                                     --username "${username}" \
                                     --password "${password}" \
                                     --notarization-info "${uuid}" >status.${i}.plist 2>/dev/null || true

                        status=$(/usr/libexec/PlistBuddy -c 'Print notarization-info:Status' status.${i}.plist 2>/dev/null || true)

                        if [ -z "${status}" ] ; then
                            continue
                        fi

                        if [ "${status}" == "in progress" ] ; then
                            continue
                        else
                            if [ "${status}" != "success" ] ; then
                                exit 1
                            fi
                            for i in $(seq 1 15) ; do
                                if xcrun stapler staple "${FILE}" ; then
                                    exit 0
                                fi
                                sleep 1
                            done
                        fi
                    done
                    exit 1
                popd 1>/dev/null 2>&1
            '''
        }
    }
}
