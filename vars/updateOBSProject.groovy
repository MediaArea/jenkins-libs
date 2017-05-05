#!/usr/bin/groovy

def call(project, path, release) {
    sh """
        . \${UTILS}/build_release/Config.sh

        if [ "${release}" != "true" ] ; then
            OBS_project=\${OBS_project}:snapshots
        fi

        OBS_package=\${OBS_project}/${project}

        rm -rf \${OBS_package}

        osc checkout \${OBS_package}

        rm -rf \${OBS_package}/*

        mv ${path}/* \${OBS_package}

        osc addremove \${OBS_package}/*
        osc commit -n \${OBS_package}
    """
}
