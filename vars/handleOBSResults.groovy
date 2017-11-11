#!/usr/bin/groovy

/*
handleOBSresults - download packages from OBS
 arguments:
  - project: OBS project name
  - version: version to download
  - paths: destination directories
  - release: release mode (true or false)
*/

def call(project, version, paths, release) {
    def extra_args = ""

    if(release) {
        extra_args = "--repo-script"
    }

    sh """
        . ${env.UTILS}/build_release/Config.sh

        if [ "${release}" != "true" ] ; then
            OBS_project=\${OBS_project}:snapshots
        fi

        python ${env.UTILS}/build_release/Handle_OBS_results.py ${extra_args} \${OBS_project} ${project} ${version} ${paths.join(' ')}
    """
}
