#Bytecodeviewer clean up - version 3.0.0

This is clean working version of the Bytecodeviewer program latest source code found on master (version 3.0.0)
The latest master version contains issues with the Maven dependencies that does run properly
This version is a fully Java Application without any Maven dependencies
Many issues encountered in the Maven dependencies have to do with libraries not being suported by Maven distribution

##Run configurations
Project:
*Bytecode-securify

Main Class:
the.bytecode.club.bytecodeviewer.BytecodeViewer

Runtime JRE: Java 1.8

Configure Build path==> 
* Add Library==> new (add) Lib folder containing all jars to run properly
* Sources==> Add Resource folder (containing images)
