#!/usr/bin/groovy

/*
updateFile - insert text at specific line in file
 arguments:
  - file: file to update
  - text: text to insert
  - offset: line ofsset
*/

def call(file, text, offset) {
    if(text) {
        writeFile(file: file + '.append.txt', text: text.replaceAll('\\s+$', '') + '\n\n', encoding: 'UTF-8')

        try {
            sh "sed -i -e '${offset} { r ${file}.append.txt' -e 'N; }' ${file}"
        } catch(Exception e) {
            sh "rm -f ${file}.append.txt"
            throw e
        }

        sh "rm -f ${file}.append.txt"
    }
}

