# MsPacMan_Controller
This git repository contains my final eclipse Java project. The ‘pacman.entries.pacman’ package in the 'src' folder contains my controller implementation and any class files that I created. Modifications were also made to the ‘Game’ and ‘Executor’ classes as shown in the Javadoc. The ‘jneat’ and ‘jNeatCommon’ packages belong to the JNEAT library.

The controller is written using the Java simulation of Ms. Pac-Man, created for the Ms. Pac-Man Vs. Team Ghost competition (http://joseatovar.github.io/Ms-Pacman-vs-Ghost/). The neural networks are created and evolved using a method called NEAT. The JNEAT library is used to achieve this (http://nn.cs.utexas.edu/?jneat). 

MsPacManControllerFinal.jar is the final version of the program.

To run the program open the .jar file from the command line using the 
java command. The progam should load a command line interface. If evolving a population, the best 
scoring genomes each generation will be saved as text in the /savedGenomes folder.
If choosing options 2 or 3 on the menu, one of these champions or the test champion can
be loaded by typing in the file name or file url. 

At the end of a population evolution a simulation is automatically run using the best
scoring network. Evolutions can't be saved or loaded, only individual champions. In
the /savedGenomes folder a .txt file called 'experimentLog.txt' is created at the start
of each new evolution. The top lines describe the experiment, subsequent lines list the 
champion scores each generation.
