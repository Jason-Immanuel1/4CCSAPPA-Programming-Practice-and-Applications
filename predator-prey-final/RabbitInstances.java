import java.util.Random;

/**
 * Write a description of class RabbitInstances here.
 *
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */
public class RabbitInstances
{
    private String genes; 

    private static final int BREEDING_AGE = 5;
    
    private static final int MAX_AGE = 40;
    
    private static final double BREEDING_PROBABILITY = 0.12;
    
    private static final int MAX_LITTER_SIZE = 4;
    
    private static final double GET_SICK_PROBABILITY = 0.01;
    
    private static final int GRASS_FOOD_VALUE = 9;
    
    private static final Random rand = Randomizer.getRandom();
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    private int sicknessStepsRemaining = 0;
    
    private int age;
    private int foodLevel;
    private boolean isSick = false;
}
