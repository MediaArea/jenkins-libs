#!/usr/bin/groovy

/*
handleOBSresults - download packages from OBS
 arguments:
  - project: OBS project name
  - version: version to download
  - paths: destination directories
  - release: release mode (true or false)
  - filter: (optional) download only distributions/archs
*/

def call(project, version, paths, release, filter="") {
    def extra_args = ""

    if(release == "true") {
        extra_args = "--repo-script"
    }

    if (!filter.allWhitespace) {
        extra_args = "--filter " + filter
    }

    sh """
        . ${env.UTILS}/build_release/Config.sh

        # test freight fork
        export PATH="/home/wmedia/freight/bin:${PATH}"

        if [ "${release}" != "true" ] ; then
            OBS_project=\${OBS_project}:snapshots
        fi

        python ${env.UTILS}/build_release/Handle_OBS_results.py ${extra_args} \${OBS_project} ${project} ${version} ${paths.join(' ')}
    """
}
