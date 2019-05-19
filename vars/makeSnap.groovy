#!/usr/bin/groovy

/*
makeSnap - create snapcraft package from directory
 arguments:
  - name: package name
  - version: package version
  - arch: package arch (i386 or amd64)
  - src: source directory
*/

def call(name, version, arch, src) {
    //def snap_version = version.replaceAll('0+$', '') // snapcraft remove trailing zeros from version number (update: seem fixed)

    sh "lxc launch -e ubuntu:xenial/${arch} snap-${name}-${arch}-${BUILD_NUMBER}"

    sleep(10)

    try {
        sh """
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- sh -c "echo nameserver 8.8.8.8 > /etc/resolv.conf"
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- sh -c "echo nameserver 8.8.4.4 >> /etc/resolv.conf"
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- sh -c "echo 91.189.88.149 security.ubuntu.com. archive.ubuntu.com. >> /etc/hosts"
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- apt-get update
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- apt-get install snapcraft -y
            lxc file push -r ${src} snap-${name}-${arch}-${BUILD_NUMBER}/root
            lxc exec snap-${name}-${arch}-${BUILD_NUMBER} -- sh -c "cd ${src} && snapcraft"

            lxc file pull snap-${name}-${arch}-${BUILD_NUMBER}/root/${src}/${name}_${version}_${arch}.snap ${name}_${version}_${arch}.snap
        """
    } catch(Exception e) {
        sh "lxc delete -f snap-${name}-${arch}-${BUILD_NUMBER}"
        throw e
    }

    sh "lxc delete -f snap-${name}-${arch}-${BUILD_NUMBER}"
}
