package pacman.entries.pacman;

import jNeatCommon.IOseq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import pacman.Executor;
import pacman.controllers.examples.StarterGhosts;
import jneat.Genome;
import jneat.Network;
import jneat.Organism;
import jneat.Population;
import jneat.Species;

/**
 * This class controls the evolution of a population. 
 * Evaluate organisms, evolve the population and save/load networks.
 * 
 * @author Kurt Hodges
 * 		   kuh1@aber.ac.uk
 */
public class EvolutionController {
	
	private Population networkPopulation;
	private int popSize;
	
	//Track the best scoring network over the entire experiment
	private double bestFitness = 0.0;
	private Network bestNetwork;
	
	//Track the best scoring network over the current generation
	private double bestFitnessThisGen = 0.0;
	private Network bestNetworkThisGen;
	
	/**
	 * Creates a new population of networks.
	 * @param popSize The size the population should be
	 * @param numInputs	The amount of inputs each network should have
	 * @param numOutputs The amount of outputs each network should have
	 */
	public EvolutionController(int popSize, int numInputs, int numOutputs){
		networkPopulation = initialisePopulation(popSize, numInputs, numOutputs);
		this.popSize = popSize;
	}
	
	/**
	 * Evaluate an organism by index. Runs several games using the organisms network as a controller, 
	 * and averages the scores to find fitness.
	 * @param organismIndex The index of the organism in the population
	 * @param numExperiments The number of games that should be run during evaluation
	 * @param numberOfLives The number of lives Ms.Pac-Man should have
	 * @param executor The executor class to run the games from
	 */
	public void evaluateOrganism(int organismIndex, int numExperiments, int numberOfLives,  Executor executor){
		Organism organism = getOrganism(organismIndex);
		//Extract the neural network from the population, ready for evaluation
		Network brain = organism.getNet();
		
		int scoreTotal = 0;
		//Loop for each experiment
		for(int w = 0; w<numExperiments; w++){
			double lastScore = executor.runGame(new MyPacMan(brain), numberOfLives, new StarterGhosts(), false, 0);
			scoreTotal += lastScore;
		}
		organism.setFitness(scoreTotal/numExperiments);
		organism.setError(60000.0 - (scoreTotal/numExperiments));
		
		//Check if this is the best score this generation
		if(organism.getFitness() > bestFitnessThisGen){
			bestFitnessThisGen = organism.getFitness();
			bestNetworkThisGen = brain;
		}
		
		//Check if this is the best score so far
		if(organism.getFitness() > bestFitness){
			bestFitness = organism.getFitness();
			bestNetwork = brain;
		}
		
		//Output information about the organism to the console
		organism.viewtext();
	}
	
	/**
	 * Evolve the population based on each organisms current fitness.
	 * @param generation The generation number
	 */
	public void evolvePopulation(int generation){
		Iterator itr_specie;
		itr_specie = networkPopulation.species.iterator();
		
		//compute average and max fitness for each species
		while (itr_specie.hasNext()) 
		{
		   Species _specie = ((Species) itr_specie.next());
		   _specie.compute_average_fitness();
		   _specie.compute_max_fitness();
		}
		
		networkPopulation.epoch(generation);
		consoleOut_lastGen(networkPopulation, generation);
		
		//Save the best contender this generation to a file
		String fileName = "savedGenomes\\GenChamp" + generation + "_" + (int)bestFitnessThisGen;
		saveNetwork(bestNetworkThisGen, fileName);
		//Save the best fitness to an experiment log
		writeToExperimentLog(bestFitnessThisGen);
		
		bestFitnessThisGen = 0.0;
	}
	
	/**
	 * Save a networks genome to a file so it can be loaded and run in a simulation later
	 * @param network The network to save
	 * @param fileName The name of the file you want to create
	 */
	public void saveNetwork(Network network, String fileName){
		IOseq newFile = new IOseq(fileName);
		newFile.IOseqOpenW(false);
		network.getGenotype().print_to_file(newFile);
		newFile.IOseqCloseW();
	}
	
	/**
	 * Load a network from a file.
	 * @param fileName The name of the file which the network is stored
	 * @return The network
	 */
	public Network loadNetwork(String fileName){
		IOseq newFile = new IOseq(fileName);
		newFile.IOseqOpenR();
		Genome champGenome = new Genome(1, newFile);
		newFile.IOseqCloseR();
		Organism organism = new Organism(1, champGenome, 1);
		return organism.getNet();
	}
	
	/**
	 * @return The number of organisms in the population
	 */
	public int getPopulationSize(){
		return popSize;
	}
	
	/**
	 * Get an organism by index
	 * @param index The index of the organism
	 * @return The organism or null if index is < 0 or > popSize
	 */
	public Organism getOrganism(int index){
		if(index < 0 || index > popSize){
			return null;
		}
		return (Organism) networkPopulation.getOrganisms().get(index);
	}
	
	/**
	 * @return The best scoring network over the entire experiment
	 */
	public Network getBestNetwork(){
		return bestNetwork;
	}
	
	/*
	 * Returns a new population of networks with the specified number of input and 
	 * output nodes. @size determines the size of the population
	 * @author kuh1@aber.ac.uk
	 */
	private Population initialisePopulation(int size, int numInputs, int numOutputs){
		Population population;
		
		Genome startGenome = new Genome(1, numInputs, numOutputs, 0, numInputs + numOutputs, true, 0.8); 
		population = new Population(startGenome, size);
		
		//OR use an alternate method to generate the population. This can cause the first generation to have more than the minimal amount of nodes
		//population = new Population(size, numInputs, numOutputs, numInputs + numOutputs, true, 0.8);
		
		population.verify();
		return population;
	}
	
	/*
	 * Output information about the last generation to the console
	 * @author kuh1@aber.ac.uk
	 */
	private void consoleOut_lastGen(Population networkPopulation, int genNum){
		System.out.println();
		System.out.print("GENERATION ");
		System.out.println(genNum);
		System.out.print("HIGHEST FITNESS THIS GEN: ");
		System.out.println(bestFitnessThisGen);
		System.out.print("HIGHEST FITNESS SO FAR: ");
		System.out.println(networkPopulation.getHighest_fitness());
		System.out.println();
	}
	
	/*
	 * Output the given fitness to a .txt file.
	 * This could be used to note down the best fitness each gen
	 */
	private void writeToExperimentLog(double fitness){
		FileWriter writer;
		try {
			String theLine = fitness + "\n";
			File theFile = new File ("savedGenomes\\experimentLog.txt");
			writer = new FileWriter(theFile, true);
			writer.write(theLine);
			writer.flush();
		    writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
