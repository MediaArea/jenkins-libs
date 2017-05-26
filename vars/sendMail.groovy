#!/usr/bin/groovy

def call(subject, message) {
    node('master') {
        sh """
            . \${UTILS}/build_release/Config.sh

            echo -e "${message}" | mailx -s "${subject}" \${Email_CC/\$Email_CC/-c \$Email_CC} \${Email_to}
        """
    }
}
