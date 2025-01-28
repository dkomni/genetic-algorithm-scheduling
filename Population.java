/*******************************************************************************************************************************
	Population class is the population of chromosomes.

	It contains an array of Individual objects, which represent the chromosomes in the population, the size of the population
	and the number of feasible chromosomes.

	The class provides methods to initialise the population, sort the chromosomes by fitness
	and the respective set() and get() methods.

	Authors: D. Komninos
			 A. Kastellakis

	Date:	 2020

	Code updated by: D. Komninos, 2025
*******************************************************************************************************************************/

import java.util.Arrays;

public class Population {
	
	Individual [] chromosomes;
	private int populationSize;
	private int feasibleSize;
	
	public Population(int popSize) {
		
		this.chromosomes = new Individual[popSize];
		this.setPopulationSize(popSize);
		this.setFeasibleSize(0);
		 
	}
	
	/**
	 * Initializes the population by creating individuals and evaluating their feasibility.
	 * Each individual is created and checked for validity using the Fitness class.
	 * If an individual is valid, it is marked as feasible and the feasible size of the population is incremented.
	 * Otherwise, the individual is marked as not feasible.
	 */
	public void InitialisePop() {
		 
		Fitness fit = new Fitness();
		 
		for(int i = 0; i < this.getPopulationSize(); i++) {
			chromosomes[i] = new Individual();
			chromosomes[i].createIndividual();
			
			if(fit.isValid(chromosomes[i])) {
				chromosomes[i].setFeasible(true);
				this.setFeasibleSize(this.getFeasibleSize() + 1);
			} else
				chromosomes[i].setFeasible(false);
		}
		setChromosomes(chromosomes);
	}
	
	// Sort the chromosomes by fitness in descending order
	public void FitnessSort() {	 
		Arrays.sort(this.getChromosomes());
	}
	
	// Get Individual object at index
	public Individual getIndividual(int index) {
		return chromosomes[index];
	}
	
	// Set Individual object at index
	public void setIndividual(int index, Individual indiv) {
		chromosomes[index] = indiv;
	}
	
	/*********************************************
	* Setters and Getters
	*********************************************/
	public Individual[] getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(Individual[] chromosomes) {
		this.chromosomes = chromosomes;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public void setPopulationSize(int populationSize) {
		this.populationSize = populationSize;
	}

	public int getFeasibleSize() {
		return feasibleSize;
	}

	public void setFeasibleSize(int feasibleSize) {
		this.feasibleSize = feasibleSize;
	}
}