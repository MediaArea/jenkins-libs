#!/usr/bin/groovy

def call(project, version, paths, release) {
    sh """
        . \${UTILS}/build_release/Config.sh

        if [ "${release}" != "true" ] ; then
            OBS_project=\${OBS_project}:snapshots
        fi

        python \${UTILS}/build_release/Handle_OBS_results.py \${OBS_project} ${project} ${version} ${paths.join(' ')}
    """
}
