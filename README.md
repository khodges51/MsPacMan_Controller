# MsPacMan_Controller
My controller for Ms. Pac-Man uses a neural network to evaluate the best possible move to make each cycle. The controller is written using the Java simulation of Ms. Pac-Man,created for the Ms. Pac-Man Vs. Team Ghost competition (http://joseatovar.github.io/Ms-Pacman-vs-Ghost/). The neural networks are created and evolved using a method called NEAT. The JNEAT library is used to achieve this (http://nn.cs.utexas.edu/?jneat). 

All classes in packages beginning ‘pacman’ belong to the simulation, aside from the ‘pacman.entries.pacman’ package which contains my controller implementation. Modifications were made to the ‘Game’ and ‘Executor’ classes as shown in the Javadoc. The ‘jneat’ and ‘jNeatCommon’ packages belong to the JNEAT library.

To run the program open the .jar file from the command line using the 
java command. On windows 7 I use the command 'java -jar MsPacManControllerFinal.jar'
The progam should load a command line interface. If evolving a population, the best 
scoring genomes each generation will be saved as text in the /savedGenomes folder.
If choosing options 2 or 3 on the menu, one of these champions or the test champion can
be loaded by typing in the file name or file url. 

At the end of a population evolution a simulation is automatically run using the best
scoring network. Evolutions can't be saved or loaded, only individual champions. In
the /savedGenomes folder a .txt file called 'experimentLog.txt' is created at the start
of each new evolution. The top lines describe the experiment, subsequent lines list the 
champion scores each generation.

The MsPacMan_Controller folder contains my final eclipse Java project.
The important aspects of the file structure are described below:

/MsPacMan_Controller 
	/.settings
	/bin
	/data
	/lib 
	/savedGenomes
		/LinkChance01
			/NodeChance001
			/NodeChance002
			/NodeChance003
		/LinkChance02
			/NodeChance001
			/NodeChance002
			/NodeChance003
		/LinkChance03
			/NodeChance001
			/NodeChance002
			/NodeChance003
		/BestB_00203
	/src
		/jneat
		/jNeatCommon
		/pacman
			/controllers
				/...
			/entries
				/...
			/game
				/...
	/starter_packages
	/TestChampA.File
	/TestChampB.File

The /lib folder contains the JNEAT jar

The /savedGenomes folder contains the experiment data produced.
The names of the folders inside describe the NEAT parameters 
used. Each leaf folder contains 5 .zip files, each containing 
the 200 champion genomes from an evolution. /BestB_00203 has
the champions from the 5 B type evolutions run with 00.2 add
node chance and 0.3 add link chance

The /src folder contains the Java source code class files. The 
/jneat and /jNeatCommon folders contain source files for JNEAT.
The /pacman folder contains the simulation code and the 
/pacman/entries folder contains my controller implimentation

The /starter_packages folder was supplied with the simulation and 
is unused

The MsPacManControllerFinal.jar file is my final released program.

TestChampA and TestChampB are two saved genome files. Using my program
these files can be used to run a simulation, or to evaluate the 
champions over multiple games. As their name suggests, TestChampA is an
A type (original) controller and TestChampB is a B type (biased) 
controller. These genomes are two of the best performing networks produced

The program was created on windows 7 64-bit using then Eclipse IDE.
The program was tested on windows 7 and Linux ubuntu 14.X with the
latest version of Java 8 installed. 
