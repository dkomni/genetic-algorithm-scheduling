/***********************************************************************************************************

	Individual class is the representation of a chromosome.
	
	Contains the genes, the hard constraints, total penalty cost, fitness score 
	and a boolean indicator of feasibility, as well as the respective set() and get() methods.

	Also, there are two methods for implementing mutation operations, createGene() and ShiftGene(), where 
	the former creates a random gene and the latter cyclically shifts a gene.

	printIndividual() method prints the chromosome in a readable format.

	Class implements Comparable interface in order to sort the population in descending order of penalty
	cost (fitness score) by overriding the compareTo() method.

	Authors: D. Komninos
			 A. Kastellakis

	Date:	 2020

	Code updated by: D. Komninos, 2025
***********************************************************************************************************/

import java.util.Random;

public class Individual implements Comparable<Individual> {
	
	// global - define hours of shifts
	public static int MORNING_SHIFT = 8;
	public static int AFTERNOON_SHIFT = 8;
	public static int NIGHT_SHIFT = 10;
	
	private int [][] genes;

	/**
	 * Hard constraints for the WHPP problem (number of shifts per day).
	 * Every row represents a shift and every column a day.
	 * For shifts, 0: off, 1: morning, 2: afternoon, 3: night
	 */
	private int [][] hard_constraints = { {5, 5, 10, 15, 10, 15, 15} , 
										  {10, 10, 5, 5, 5, 5, 5} , 
										  {10, 10, 10, 5, 10, 5, 5} , 
										  {5, 5, 5, 5, 5, 5, 5} };

	private int total_penalty;										 
	private boolean feasible;
	
	private char[] Days = {'M', 'T', 'W', 'T', 'F', 'S', 'S'};

    // Create a random individual
	public Individual() {
		
		 this.genes = new int[30][14];
		 this.feasible = true;
		 this.total_penalty = 0;
	}
	
	/**
	 * Generates a new individual by creating a chromosome matrix with random shifts.
	 * The chromosome matrix is a 30x14 matrix where each element represents a shift.
	 * The shifts are generated randomly but must satisfy certain hard constraints.
	 * 
	 * The method uses a random number generator to assign shifts to each element in the matrix.
	 * It ensures that the number of shifts assigned to each element does not exceed the 
	 * specified hard constraints.
	 * 
	 * The hard constraints are checked for each shift assignment. If a generated shift 
	 * violates the constraints, a new shift is generated until a feasible one is found.
	 * 
	 * Once the chromosome matrix is generated, it is set as the individual's chromosome.
	 */
	public void createIndividual() {
		
		Random rand = new Random();
		int [][] matrix = new int[30][14]; // chromosome matrix (30 employees, 14 days)
		int num;
		int j;
		
		for (int columns = 0; columns < 14; columns++) {
			int [] count = new int[4]; // count the shifts assigned
			
		    for (int rows = 0; rows < 30; rows++) {
		        do { 
		        	num = rand.nextInt(4);
			        count[num]++;
					// fix j for correspondence with hard constraints matrix columns
			        if(columns < 7) 
			        	j = columns;
			        else
			        	j = columns-7;
		        } while(count[num] > hard_constraints[num][j]);
			
		        matrix[rows][columns] = num;
		    }           
		}	
		this.setChromosome(matrix);
	}

	/**
	 * Creates a mutated gene for the given offspring at the specified mutation point.
	 * The mutation respects hard constraints to ensure feasible shifts.
	 *
	 * @param offspring The individual whose gene is to be mutated.
	 * @param mutation_point The column index in the chromosome where the mutation occurs.
	 */
	public void createGene(Individual offspring, int mutation_point) {

		Random rand = new Random();
		int[][] mutatedGene = offspring.getChromosome();
		int num;
		int j;

		int[] count = new int[4];	// count the shifts assigned

		// for each employee, change the shift of the mutation_point column
		// to create its mutated version, with respect to hard constraints
		for(int rows = 0; rows < 30; rows++) {
			do { 
		        num = rand.nextInt(4); // generate random shift
			    count[num]++;
				// fix j for correspondence with hard constraints matrix columns
			    if(mutation_point < 7) 
			        j = mutation_point;
			    else 
			        j = mutation_point-7;
			    
		    } while(count[num] > hard_constraints[num][j]);
			
		    mutatedGene[rows][mutation_point] = num;
		}
		offspring.setChromosome(mutatedGene);
	}
	
	/**
	 * Shifts the genes in the specified column of the offspring's chromosome in a cyclic manner.
	 * The gene at the last row is moved to the first row, and all other genes are shifted down by one position.
	 *
	 * @param offspring The individual whose chromosome will be mutated.
	 * @param p The index of the column in the chromosome to be shifted.
	 */
	public void ShiftGene(Individual offspring, int p) {

		int[][] mutatedGene = offspring.getChromosome();

		int[] shift = new int[30];
		
		for(int i = 0; i < 30; i++)
			shift[i] = mutatedGene[i][p];
		
		mutatedGene[0][p]= shift[29]; // cyclic shift
		
		// shift the rest of the genes
		for(int i = 1; i < 30; i++)
			mutatedGene[i][p] = shift[i-1];	
				
		offspring.setChromosome(mutatedGene);
	}
	
	// Print the individual (a schedule of 30 employees for 14 days)
	public void printIndividual() {
		
		System.out.printf("\n         ");
		for(int i = 0; i < 14; i++) {
			if(i < 7)
				System.out.printf("%-5c", this.Days[i]);
			else
				System.out.printf("%-5c", this.Days[i-7]);
		}
		
		System.out.println("\n");

		for (int i = 0; i < 30; i++) {
			    System.out.printf("Emp%d     ", i+1);

		    for (int j = 0; j < 14; j++)
		    	System.out.printf("%-5d", this.genes[i][j]);
				
		    System.out.println();
		}
		System.out.println("\n");
	}

	/*********************************************
	* Setters and Getters
	*********************************************/
	public boolean isFeasible() {
		return feasible;
	}

	public void setFeasible(boolean feasible) {
		this.feasible = feasible;
	}

	public int[][] getChromosome() {
		return genes;
	}

	public void setChromosome(int[][] chromosome) {
		this.genes = chromosome;
	}
	
	public void setTotalPenalty (int total_penalty) {
		this.total_penalty = total_penalty;
	}

	public int getTotalPenalty() {
		return this.total_penalty;
	}
	
	@Override
	public int compareTo(Individual compareIdividual) {
		
		int compareFitness =  ((Individual) compareIdividual).getTotalPenalty();
		//descending order
		return compareFitness - this.getTotalPenalty();
	}
}