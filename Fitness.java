/**********************************************************************************************************

	The Fitness class is responsible for evaluating the fitness of an individual
    in a genetic algorithm for scheduling. It checks both hard and soft constraints
    and calculates the fitness score based on the number of violated constraints.
	The fitness score is just the total penalty of an individual.

	Authors: D. Komninos
			 A. Kastellakis

	Date:	 2020

	Code updated by: D. Komninos, 2025
***********************************************************************************************************/

public class Fitness {
	
	/* Hard constraints for the WHPP problem (number of shifts per day)
	   needed here again, but without day-off shifts that we are not interested in */
	private int[][] hard_constraints= { {10, 10, 5, 5, 5, 5, 5} , 
										{10, 10, 10, 5, 10, 5, 5} , 
										{5, 5, 5, 5, 5, 5, 5} };

	/* Although some weight here are similar, a different method for each soft constraint
	   is implemented in case someone wants to change the weights */
	private int[] soft_constraints_weights = {1000, 1000, 1000, 1000, 800, 800, 100, 100, 1, 1, 1};
	
	int[][] heuristic;	// hold heuristic for each chromosome and then set to individual

	public Fitness() {

	}
	
	/**
	 * Checks if the given individual is valid based on the hard constraints.
	 *
	 * @param id the individual to be checked
	 * @return true if the individual is valid, false otherwise
	 */
	public boolean isValid(Individual id) {
		
		int[][] feasible = new int[3][14]; // 3 shifts, 14 days
		int[][] genes = id.getChromosome();
		boolean valid = true;
		
		for (int i = 0; i < genes.length; i++) {
		    for (int j = 0; j < genes[i].length; j++) {
		    		if(genes[i][j] == 1) // morning shift
		    			feasible[0][j]++;
		    		else if (genes[i][j] == 2) // afternoon shift
		    			feasible[1][j]++;
		    		else if (genes[i][j] == 3) // night shift
		    			feasible[2][j]++;	    	
		    }           
		}
		
		for (int i = 0; i < feasible.length; i++) {
			for (int j = 0; j < feasible[i].length; j++) {
				if (j < 7) {
					if(feasible[i][j] > hard_constraints[i][j]) {
						valid = false;
						return valid;
					}
				} else if(feasible[i][j] > hard_constraints[i][j-7]) {
						valid = false;
						return valid;
				}
			}
		}
		return valid;		
	}

	/**
	 * Calculates the fitness of an individual based on the penalties of violated soft constraints.
	 * The fitness score is calculated as a percentage of the worst possible fitness.
	 *
	 * @param id the individual whose fitness is to be calculated
	 */
	public void calc_Fitness(Individual id) {
		int[][] chromosome = id.getChromosome();
		int total_penalty = 0;  // total penalty of violated constraints
		int fit = 0;       // penalty for each violated constraint

		// calculate penalties for each soft constraint
		fit = this.soft_const_1(chromosome);
		total_penalty += fit;

		fit = this.soft_const_2(chromosome);
		total_penalty += fit;

		fit = this.soft_const_3(chromosome);
		total_penalty += fit;
		
		fit = this.soft_const_4(chromosome);
		total_penalty += fit;

		fit = this.soft_const_5(chromosome);
		total_penalty += fit;

		fit = this.soft_const_6(chromosome);
		total_penalty += fit;

		fit = this.soft_const_7(chromosome);
		total_penalty += fit;

		fit = this.soft_const_8(chromosome);
		total_penalty += fit;

		fit = this.soft_const_9(chromosome);
		total_penalty += fit;

		fit = this.soft_const_10(chromosome);
		total_penalty += fit;

		fit = this.soft_const_11(chromosome);
		total_penalty += fit;
		
		// set fitness score (total penalty) for individual
		id.setTotalPenalty(total_penalty);
	}

	/** Each method below checks for the respective soft constraint.
	*   Returns the total penalty with respect to the weight of the corresponding violated constraint.
	*/

	/**
	 * Evaluates the soft constraint for the given chromosome.
	 * The soft constraint ensures that the total working hours for each employee
	 * do not exceed 70 hours in a 14-day period. If the constraint is violated,
	 * a penalty is applied.
	 *
	 * @param chromosome a 2D array representing the schedule of employees.
	 *                   Each row corresponds to an employee, and each column
	 *                   corresponds to a day. The value at each position indicates
	 *                   the type of shift (1 for morning, 2 for afternoon, 3 for night).
	 * @return the total penalty for all employees who violated the soft constraint.
	 */
	public int soft_const_1(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0; // count violations for employee i
			int total_hours = 0;
			for (int j = 0; j < chromosome[i].length; j++) {
				if (chromosome[i][j] == 1) // morning shift
					total_hours += Individual.MORNING_SHIFT;
				if (chromosome[i][j] == 2) // afternoon shift
					total_hours += Individual.AFTERNOON_SHIFT;
				if (chromosome[i][j] == 3) // night shift
					total_hours += Individual.NIGHT_SHIFT;

				// mark violation if total hours exceed 70
				if (total_hours > 70) {
					viol++;
					total_hours = 0; // reset total hours after counting a violation
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[0];
		}
		return total_penalty;
	}

	/**
	 * Evaluates the soft constraint for consecutive working days in a given chromosome.
	 * This method checks each employee's schedule to ensure that no employee works more than 7 consecutive days.
	 * If an employee works 8 consecutive days, it counts as a violation.
	 * The total number of violations is multiplied by a weight factor and returned as the fitness penalty.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row corresponds to an employee, 
	 *                   and each column corresponds to a day. A value of 0 indicates a day off, and any other value indicates a working day.
	 * @return The fitness penalty based on the number of violations of the soft constraint.
	 */
	public int soft_const_2(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int count = 0; // count consecutive working days for employee i
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (chromosome[i][j] == 0) // day-off
					count = 0;	// initialize count
				else {
					count++;	// +1 day of work
					// mark violation if more than 7 consecutive working days
					if (count > 7) {
						viol++;
						count = 1; // reset count to 1 to include current day
					}
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[1];
		}
		return total_penalty;
	}

	// check soft constraint c3 - max 4 consecutive night shifts
	/**
	 * Evaluates the soft constraint for night shifts in the given chromosome.
	 * This constraint ensures that no employee is assigned more than 4 consecutive night shifts.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row corresponds to an employee,
	 *                   and each column corresponds to a day. The value 3 indicates a night shift.
	 * @return The total penalty score for violations of the soft constraint.
	 */
	public int soft_const_3(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int count = 0; // count consecutive night shifts for employee i
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (chromosome[i][j] != 3) // not a night shift
					count = 0;	// initialize count
				else {
					count++;	// +1 night shift
					// mark violation if more than 4 consecutive night shifts
					if (count > 4) { // found more than 4 consecutive night shifts
						viol++;
						count = 1; // reset count to 1 to include current night shift
					}
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[2];
		}
		return total_penalty;
	}

	
	/**
	 * Calculates the total penalty for violations of the soft constraint 4, which checks for 
	 * morning shifts immediately following night shifts for each employee over a 14-day period.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row corresponds 
	 *                   to an employee, and each column corresponds to a day in the 14-day period.
	 *                   The value at chromosome[i][j] represents the shift type for employee i on day j.
	 *                   Shift types are represented as integers, where 3 indicates a night shift and 
	 *                   1 indicates a morning shift.
	 * @return The total penalty for all employees based on the number of violations of the soft 
	 *         constraint 4. The penalty is calculated by multiplying the number of violations by 
	 *         the weight of the soft constraint 4.
	 */
	public int soft_const_4(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0;	// count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j < chromosome[i].length - 1) {  // avoid out of bounds errors
					if (chromosome[i][j] == 3 && chromosome[i][j+1] == 1) // morning shift after night
						viol++;
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[3];
		}
		return total_penalty;
	}

	/**
	 * Calculates the total penalty for violations of the soft constraint 5.
	 * Soft constraint 5 checks for instances where an employee has a morning shift
	 * immediately following an afternoon shift, which is not allowed.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row
	 *                   corresponds to an employee, and each column corresponds to a day.
	 *                   The value at chromosome[i][j] indicates the shift type for employee i on day j.
	 *                   (e.g., 1 for morning shift, 2 for afternoon shift).
	 * @return The total penalty for all employees based on the number of violations of soft constraint 5.
	 */
	public int soft_const_5(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0;	// count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j < chromosome[i].length - 1) {  // avoid out of bounds errors
					if (chromosome[i][j] == 2 && chromosome[i][j+1] == 1)  // morning shift after afternoon
						viol++;
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[4];
		}
		return total_penalty;
	}

	/**
	 * Calculates the total penalty for violations of the soft constraint that 
	 * prevents an afternoon shift from following a night shift for each employee.
	 *
	 * This method iterates through a chromosome representing the schedule of 
	 * employees and checks for instances where an afternoon shift (represented 
	 * by the value 3) is scheduled immediately after a night shift (represented 
	 * by the value 2). For each such violation, a penalty is added to the total 
	 * penalty based on the weight of the soft constraint.
	 *
	 * @param chromosome A 2D array representing the schedule of employees, where 
	 *                   each row corresponds to an employee and each column 
	 *                   corresponds to a day in the schedule.
	 * @return The total penalty for all employees for violations of the soft 
	 *         constraint.
	 */
	public int soft_const_6(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0;	// count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j < chromosome[i].length - 1) {  // avoid out of bounds errors
					if (chromosome[i][j] == 3 && chromosome[i][j+1] == 2) // afternoon shift after night
						viol++;		
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[5];
		}
		return total_penalty;
	} 

	/**
	 * Calculates the penalty for violating the constraint of having four consecutive night shifts
	 * without at least two days off afterwards for each employee.
	 *
	 * @param chromosome A 2D array representing the schedule of employees, where each row corresponds to an employee
	 *                   and each column corresponds to a day. The value 3 represents a night shift.
	 * @return The total penalty for all employees based on the number of violations of the constraint.
	 */
	public int soft_const_7(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int count = 0; // count consecutive night shifts for employee i
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (chromosome[i][j] != 3) { // not a night shift assigned
					count = 0;	// initialize count
				}
				else {
					count++;			// +1 day of night shift
					if (count == 4) {  // found 4 consecutive night shifts
						if (j < chromosome[i].length - 2) {	// avoid out of bounds errors
							if (chromosome[i][j+1] != 0 && chromosome[i][j+2] != 0)
								viol++;
						} else
							viol++; // just mark a violation
						count = 0; // reset count after counting a violation
					}
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[6];
		}
		return total_penalty;
	}

	/**
	 * Calculates the penalty for violating the constraint of having seven consecutive shifts
	 * without at least two days off afterwards for each employee.
	 * This method iterates through each employee's schedule and counts the number
	 * of consecutive shifts. If an employee has 7 consecutive shifts, it checks
	 * if the next two days are also shifts, which constitutes a violation. The
	 * penalty is calculated based on the number of such violations.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row
	 *                   corresponds to an employee, and each column corresponds to
	 *                   a day. A value of 0 indicates no shift assigned, and any
	 *                   other value indicates a shift assigned.
	 * @return The total penalty for all employees based on the number of violations.
	 */
	public int soft_const_8(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int count = 0; // count consecutive night shifts for employee i
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (chromosome[i][j] == 0) { // not a shift assigned
					count = 0;	// initialize count
				}
				else {
					count++;			// +1 day of shift
					if (count == 7) { // found 7 consecutive shifts
						if (j < chromosome[i].length - 2 && chromosome[i][j + 1] != 0 && chromosome[i][j + 2] != 0) {
							viol++;
						} else
							viol++; // just mark a violation
						count = 0; // reset count after counting a violation
					}
				}
			}

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[7];
		}
		return total_penalty;
	}

	/**
	 * Calculates the total penalty for violations of soft constraint 9 for all employees.
	 * The constraint checks for patterns where an employee has a working day (non-zero) followed by 
	 * a day off (0) and then another working day (non-zero) within a 14-day period.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row corresponds
	 *                   to an employee, and each column corresponds to a day in the schedule.
	 * @return The total penalty for all employees based on the number of violations of soft constraint 9.
	 */
	public int soft_const_9(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j < chromosome[i].length - 2) {	// avoid out of bound errors
					if (chromosome[i][j] != 0 && chromosome[i][j+1] == 0 && chromosome[i][j+2] != 0)
						viol++;
				}
			}
			
			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[8];
		}
		return total_penalty;
	}

	/**
	 * Calculates the penalty for violations of a specific soft constraint in the given chromosome.
	 * The constraint checks for patterns where an employee has a day off (0) followed by a working day (non-zero)
	 * and then another day off (0) within a 14-day period.
	 *
	 * @param chromosome A 2D array representing the schedule of employees. Each row corresponds to an employee,
	 *                   and each column corresponds to a day in the schedule.
	 * @return The total penalty for all employees based on the number of violations of the soft constraint.
	 */
	public int soft_const_10(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0; // count violations for employee i
			for (int j = 0; j < chromosome[i].length; j++) {
				if (j < chromosome[i].length - 2) {	// avoid out of bound errors
					if (chromosome[i][j] == 0 && chromosome[i][j+1] != 0 && chromosome[i][j+2] == 0)
						viol++;
				}
			}
			
			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[9];
		}
		return total_penalty;
	}

	
	/**
	 * Calculates the penalty for violating soft constraint 11 for a given chromosome.
	 * Soft constraint 11 checks that an employee works at most one weekend (of the two in a 14-day period).
	 * 
	 * @param chromosome A 2D array representing the chromosome, where each row corresponds to an employee's schedule.
	 * @return The total penalty for all employees based on the number of violations of soft constraint 11.
	 */
	public int soft_const_11(int[][] chromosome) {

		int total_penalty = 0; // total penalty for all employees

		for (int i = 0; i < chromosome.length; i++) {
			int viol = 0;	// count violations for employee i
			if (chromosome[i][5] != 0 && chromosome[i][6] != 0 
				&& chromosome[i][12] != 0 && chromosome[i][13] != 0)
					viol++;

			// add penalty for the number of violations
			total_penalty += viol * this.soft_constraints_weights[10];
		}
		return total_penalty;
	}
}

