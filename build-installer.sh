#!/usr/bin/bash
rm fatjar/TranslateDocumentTool-1.0-SNAPSHOT-shaded.jar
mvn clean install
cp target/TranslateDocumentTool-1.0-SNAPSHOT-shaded.jar fatjar/TranslateDocumentTool-1.0-SNAPSHOT-shaded.jar
jpackage --name "Tool Translate Excel" --input fatjar --vendor Zedination --main-jar TranslateDocumentTool-1.0-SNAPSHOT-shaded.jar --main-class com.example.translatedocumenttool.Main1Class --type exe --win-shortcut --win-menu