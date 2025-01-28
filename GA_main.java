/*************************************************************************

	Genetic algortihm for Personnel Scheduling - Constraint Dependent

	Authors: D. Komninos
			 A. Kastellakis

	Date:	 2020
**************************************************************************/		 

import java.io.IOException;
import java.util.Random;

public class GA_main {

	public static void main(String[] args) throws IOException {

		// crossover probability
		double p_cross = 0.85;
		// mutation probability
		double p_mut = 0.015;
		// population size
		int popSize = 1000;
		// max number of iterations
		int iter_max = 200;
		
		if(args.length < 1) {}
		else if(args[0].equals("-i")) {
			popSize = Integer.parseInt(args[1]);
			iter_max = Integer.parseInt(args[2]);
			p_cross = Double.parseDouble(args[3]);
			p_mut = Double.parseDouble(args[4]);
		}

		System.out.println("Genetic Algorithm execution with Population = "+popSize+ " and iterations = "+iter_max);
		System.out.println("Crossover rate (Probability): "+p_cross);
		System.out.println("Mutation rate (Probability): "+p_mut);
		
		long startTime = System.currentTimeMillis();
		// initial population
		Population pop = new Population(popSize);
		
		// generate initial feasible population
		// hard constraints satisfied for every chromosome
		pop.InitialisePop();
		
		// calculate the fitness of each individual of initial population
		Fitness fit = new Fitness();
		
		for(int i = 0; i < popSize; i++)
			fit.calc_Fitness(pop.getIndividual(i));
		
		// Sort the population in descending order of penalty cost
		pop.FitnessSort();
		
		if(pop.getIndividual(popSize-1).getTotalPenalty() == 0) {
			System.out.println("\nSolution found!!");
			System.out.println("\nThe solution has fitness: " +pop.getIndividual(popSize-1).getTotalPenalty());
			System.out.println("\nThe chromosome is: ");
			pop.getIndividual(popSize-1).printIndividual();
			return;	
		}
		
		// count generations
		int generations = 0;

		/** Evolution of the genetic algorithm:
		 * 1. Rank Selection
		 * 2. Crossover
		 * 3. Mutation
		 * 4. Fitness Calculation
		*/
		while(iter_max > 0) {
			
			// Evolve a population
			Population newPopulation = new Population(popSize);
			 for (int i = 0; i < popSize; i++) {
	            Individual indiv1 = RankSelection(popSize, pop); // select parent 1
	            Individual indiv2 = RankSelection(popSize, pop); // select parent 2
	            Individual newIndiv = uni_cross(indiv1, indiv2, p_cross); // uniform crossover and create offspring
	            Individual mutated = rand_mut(newIndiv, p_mut); // randomly mutate offspring
	            newPopulation.setIndividual(i, mutated); // add to new population
	            fit.calc_Fitness(newPopulation.getIndividual(i)); // calculate fitness of each chromosome with respect to penalty cost
	     	}

	     	newPopulation.FitnessSort();	// sort new population in descending order of penalty cost
	     		
	     	pop = newPopulation;
	     	
	     	generations++;
	     	
			if(pop.getIndividual(popSize-1).getTotalPenalty() == 0) {
				System.out.println("\nSolution found in generation " + generations);
				System.out.println("\nThe solution has fitness: " +pop.getIndividual(popSize-1).getTotalPenalty());
				System.out.println("\nThe chromosome is: ");
				pop.getIndividual(popSize-1).printIndividual();
				long estTime = System.currentTimeMillis() - startTime;
				System.out.println("Elapsed Time: "+estTime/1000 +" sec");
				return;	
			}
	     	
	     	// calculate average fitness of chromosomes in this generation
	     	double sum_fit = 0;
	     	for(int i = 0; i < popSize; i++) {
	     		sum_fit += pop.getIndividual(i).getTotalPenalty();
	     	}
	     	double avg = (double) Math.round((sum_fit/popSize)*100d)/100d;
	     	System.out.println("The average fitness of generation " + generations + " is: " + avg);

	     	iter_max--;
		}
		
		long estTime = System.currentTimeMillis() - startTime;
		System.out.println("Elapsed Time: "+estTime/1000 +" sec");

		// Print the schedule after algorithm termination
		Individual bestIndividual = pop.getIndividual(popSize - 1);
		System.out.println("\nSchedule after termination:");
		System.out.println("Total penalty: " + bestIndividual.getTotalPenalty());
		System.out.println("The chromosome is: ");
		bestIndividual.printIndividual();
	}
	
	/**
	 * Selects an individual from the population using rank-based selection.
	 * 
	 * In rank-based selection, individuals are assigned ranks based on their fitness.
	 * The probability of selecting an individual is proportional to its rank.
	 * 
	 * Rank-based selection is chosen because it is less biased than roulette wheel selection.
	 * Also, it is less sensitive to scaling of fitness values. 
	 * 
	 * @param populationSize the size of the population
	 * @param pop the population from which to select an individual
	 * @return the selected individual
	 */
	private static Individual RankSelection(int populationSize, Population pop) {
		
		int[] ranks = new int[populationSize];
		int sum = 0;
		for(int i = 0; i < populationSize; i++) {
			ranks[i] = i+1; // Assign ranks to chromosomes
			sum += ranks[i]; // Calculate sum of all chromosome ranks in population
		}
		
		// Generate random number from interval (0,sum) - r.
		Random rand = new Random();
		int r = rand.nextInt((int)(sum+1));
		
		// Go through the population and sum ranks until you reach r.
		// When the sum s is greater then r, stop and return the chromosome where you are.
		int s = 0;
		for(int i = 0; i < populationSize; i++) {
			s = s + ranks[i];
			if(s > r)
				return pop.getIndividual(i);
		}

		return pop.getIndividual(rand.nextInt(populationSize-1));
	}
	
	/**
	 * Uniform Crossover Operator implementation.
	 * Performs crossover operation between two parent individuals to produce an offspring.
	 * 
	 * @param id1 The first parent individual.
	 * @param id2 The second parent individual.
	 * @param p_cross The probability of performing crossover.
	 * @return The offspring individual resulting from the crossover operation, or the fittest parent if crossover is not performed.
	 */
	private static Individual uni_cross(Individual id1, Individual id2, double p_cross) {

		Individual offspring = new Individual();

		if(Math.random() <= p_cross) {	// create offspring
		
						
			int[][] parent1 = id1.getChromosome();
			int[][] parent2 = id2.getChromosome();
			
			int[][] offspring_genes = offspring.getChromosome();
			
			double probability;
			// crossover alternately
			for(int j = 0; j < 14; j++) {
				probability = Math.random();
				for(int i = 0; i < 30; i++) {	// choose genes from one of the parents with probability 50%
					if(probability <= 0.5) 
						offspring_genes[i][j] = parent1[i][j];
					else 
						offspring_genes[i][j] = parent2[i][j];
				}
			}
			
			offspring.setChromosome(offspring_genes);
			return offspring;
		} else {	// return fittest parent
			if(id1.getTotalPenalty() > id2.getTotalPenalty()) {
				offspring.setChromosome(id1.getChromosome());
				return offspring;
			}
			else {
				offspring.setChromosome(id2.getChromosome());
				return offspring;
			}
		}
	}
	
	/**
	 * Two-point Crossover Operator implementation.
	 * Performs a two-point crossover operation between two parent individuals to produce an offspring.
	 * The crossover points are selected randomly within specified ranges.
	 * If the crossover probability is not met, the fittest parent is returned.
	 *
	 * @param id1 The first parent individual.
	 * @param id2 The second parent individual.
	 * @param p_cross The probability of performing the crossover.
	 * @return The offspring individual resulting from the crossover, or the fittest parent if crossover is not performed.
	 */
	@SuppressWarnings("unused")
	private static Individual cross2p(Individual id1, Individual id2, double p_cross) {

		Individual offspring = new Individual();

		if(Math.random() <= p_cross) {	// create offspring	
					
					
			int[][] parent1 = id1.getChromosome();
			int[][] parent2 = id2.getChromosome();
			
			int[][] offspring_genes = offspring.getChromosome();
			
			Random rand = new Random();
			//Select a random cross point between 0-6
			int p1 = rand.nextInt(7);
			
			//Select a random cross point between 7-13
			int p2 = rand.nextInt(7);
			p2 = p2 +7;
			
			double probability;
				
			for(int j = 0; j < 14; j++)	{
				probability = Math.random();
				for(int i = 0; i < 30; i++){
					if(j <= p1) {
						if(probability <= p_cross)
							offspring_genes[i][j] = parent1[i][j];
						else
							offspring_genes[i][j] = parent2[i][j];

					} else if(j > p1 && j < p2) {
						if(probability<= p_cross)
							offspring_genes[i][j] = parent1[i][j];
						else
							offspring_genes[i][j] = parent2[i][j];

					} else if(j >= p2) {
						if(probability <= p_cross)
							offspring_genes[i][j] = parent1[i][j];
						else
							offspring_genes[i][j] = parent2[i][j];					
					}					
				}
			}			
			
			offspring.setChromosome(offspring_genes);
			return offspring;
		}
		else {	// return fittest parent
			if(id1.getTotalPenalty() > id2.getTotalPenalty())
				return id1;
			else
				return id2;
		}
	}

	/**
	 * Randomly mutates the genes of the given offspring with a specified probability.
	 *
	 * @param offspring The individual whose genes are to be mutated.
	 * @param p_mut The probability of mutation for each gene.
	 * @return The mutated offspring.
	 */
	private static Individual rand_mut(Individual offspring, double p_mut) {

		for(int genes = 0; genes < 14; genes++) {
			
			// mutation with probability p_mut
			if(Math.random() <= p_mut) 	
				// mutate column to maintain diversity
				offspring.createGene(offspring, genes);
		}
		return offspring;
	}
	
	/**
	 * Applies a shift mutation to the given offspring with a specified probability.
	 * 
	 * This kind of mutation just shifts the genes in a cyclic manner byone position and after some generations, 
	 * there is a chance a chromosome will form its initial genes, reducing the diversity of the population.
	 * 
	 * This method is not used in the current implementation and is provided for reference and experimentation.
	 * 
	 * @param offspring The individual to be mutated.
	 * @param p_mut The probability of mutation for each gene.
	 * @return The mutated offspring.
	 */
	@SuppressWarnings("unused")
	private static Individual shift_mut(Individual offspring, double p_mut) {

		for(int genes = 0; genes < 14; genes++) {
			
			// mutation with probability p_mut
			if(Math.random() <= p_mut) 	
				// mutate column to maintain diversity
				offspring.ShiftGene(offspring, genes);
		}
		return offspring;
	}

}