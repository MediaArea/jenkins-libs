#!/usr/bin/groovy

/*
winDeleteDir - try to delete directory tree on Windows like deleteDir() on Unix systems
*/

def call() {
    for (int i = 0; i < 10; i++) {
        try {
            deleteDir()
        } catch(Exception e) {
            sleep 5
            continue;
        }
        break;
    }
}
