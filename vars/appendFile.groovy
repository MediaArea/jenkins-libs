#!/usr/bin/groovy

/*
appendFile - insert text at the end of file
 arguments:
  - file: file to update
  - text: text to append
*/

def call(file, text) {
    def toWrite = ''
    if (fileExists(file)) {
        def content = readFile(file: file, encoding: 'UTF-8')
        if (content) {
            toWrite += content
            toWrite += '\n'
        }
    }
    toWrite += text

    writeFile(file: file, text: toWrite, encoding: 'UTF-8')
}
