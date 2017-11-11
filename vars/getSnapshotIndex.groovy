#!/usr/bin/groovy

/*
getSnapshotIndex - get current snapsdhot index for dirs
 arguments:
  - date: current date
  - dirs: list of directories to search in
*/

def call(date, dirs) {
    def subdir = date
    def found = true
    def index = 2

    while(found) {
        found = false
        for(dir in dirs) {
            if(fileExists("${dir}/${subdir}")) {
                subdir = "${date}-${index}"
                index = index + 1
                found = true
                break
            }
        }
    }

    return subdir
}
