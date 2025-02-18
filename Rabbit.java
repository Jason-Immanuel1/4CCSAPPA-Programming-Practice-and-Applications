import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color; 

/**
 * A simple model of a rabbit.
 * Rabbits age, move, breed, and die.
 * 
 * @author David J. Barnes, Michael Kölling and Jeffery Raphael, Jonny Guest and Jason Immanuel
 * @version 2025.02.10
 */

public class Rabbit extends Animal {          //GREY SQUARE IN FIELD

    private static final int BREEDING_AGE = 5;
    private static final int MAX_AGE = 40;
    private static final double BREEDING_PROBABILITY = 0.12;
    private static final int MAX_LITTER_SIZE = 4;
    private static final Random rand = Randomizer.getRandom();
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    private static final double GET_SICK_PROBABILITY = 0.01;
    private int sicknessStepsRemaining = 0;
    
    private static final int GRASS_FOOD_VALUE = 9;
    
    private int age;
    private int foodLevel;
    private boolean isSick = false;
    

    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the rabbit will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Rabbit(boolean randomAge, Field field, Location location, Color col) {
        super(field, location, col);
        age = 0;
        
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
        }
        else{
            age = 0;
            foodLevel = GRASS_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the rabbit does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * @param newRabbits A list to return newly born rabbits.
     */
    public void act(List<Animal> newRabbits) {
        getSick();
        applySickness();
        passOnSickness();
        
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newRabbits);            
            // Try to move into a free location.
            Location newLocation = getField().getFreeAdjacentLocation(getLocation());
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age.
     * This could result in the rabbit's death.
     */
    private void incrementAge() {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    
    private void incrementHunger() {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this rabbit is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Animal> newRabbits) {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Rabbit young = new Rabbit(false, field, loc, getColor());
            newRabbits.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed() {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A rabbit can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed() {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Any animal that is not already sick has a 1% chance of becoming sick.
     */
    public void getSick() {
    if (rand.nextDouble() <= GET_SICK_PROBABILITY && sicknessStepsRemaining == 0) {
        isSick = true;  // Directly setting the boolean
        sicknessStepsRemaining = 5;  // Directly setting sickness steps
    }
    }
    
    /**
     * If sick, the animal will loose foodLevel twice as fast as normal.
     */
    public void applySickness() {
    if (sicknessStepsRemaining > 0) {
        foodLevel -= 2; 
        sicknessStepsRemaining--; 
        
    }
    }
    
    private void passOnSickness(){
    if(isSick){
        List<Animal> neighbours = getField().getLivingNeighbours(getLocation());
        for(Animal neighbour : neighbours){
            if(neighbour instanceof Rabbit && !neighbour.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY){
                neighbour.setSick();
                neighbour.setSicknessSteps(5);
            }
        }
    }
    }

}