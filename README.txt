A framework for COMP3702 Assignment 2, by Dimitri Klimenko (tutor).


(1) Commands and arguments
The runnable files and their arguments are:
	game.GameRunner [setup-file] [output-file]
    visualiser.Visualiser [setup-file]


(2) Running the Visualiser
To run it, simply run visualiser.jar with Java 7 (double-clicking should work
if Java is installed properly). If this doesn't work or you want to run it
with a different version, I recommend using Eclipse - simply add the contents
of a2-tools.zip to a new project.
Alternatively, see the manual compilation instructions in section (3).

You can also run it from the command line with optional
command-line arguments:
    java -jar visualiser.jar [setup-file]

Note that these commands may require you to use full path to java.exe,
as per section (4).


(3) Manual Compilation
If you want to compile and run the code manually, you will need to do the
following:
1) Download and install Apache Ant.
2) Extract a2-tools.zip to the desired folder.
3) From within that folder, run the command
    ant

The following commands should now work for running the game from command line
and using the visualiser:
	java -cp bin game.GameRunner [setup-file] [output-file]
    java -cp bin visualiser.Visualiser [setup-file]

The commands above may require full paths to Java; see section (4).


(4) The command line and the system path
Note that for the command-line commands to work Java would have to be on your
system path; if not, you'll have to specify a full path instead of just 
"java", e.g.
"C:\Program Files (x86)\Java\jdk1.7.0_25\bin\java.exe"
or
/usr/java/jdk1.7.0_25/bin/java
