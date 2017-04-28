# MsPacMan_Controller
My controller for Ms. Pac-Man uses a neural network to evaluate the best possible move to make each cycle. The controller is written using the Java simulation of Ms. Pac-Man created for the Ms. Pac-Man Vs. Team Ghost competition (http://joseatovar.github.io/Ms-Pacman-vs-Ghost/). The neural networks are created and evolved using a method called NEAT. The JNEAT library is used to achieve this (http://nn.cs.utexas.edu/?jneat). 

All classes in packages beginning ‘pacman’ belong to the simulation, aside from the ‘pacman.entries.pacman’ package which contains my controller implementation. Modifications were made to the ‘Game’ and ‘Executor’ classes as shown in the Javadoc. The ‘jneat’ and ‘jNeatCommon’ packages belong to the JNEAT library.
