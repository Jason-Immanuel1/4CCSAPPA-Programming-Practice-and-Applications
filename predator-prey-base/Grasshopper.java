import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * Write a description of class Grasshopper here.
 *
 * @author Jonny Guest and Jason Immanuel
 * @version (a version number or a date)
 */

public class Grasshopper extends Animal{         //DARKBLUE SQUARE IN FIELD
     private static final int BREEDING_AGE = 4;
    private static final int MAX_AGE = 20;
    private static final double BREEDING_PROBABILITY = 0.20;
    private static final int MAX_LITTER_SIZE = 4;
    
    private static final int GRASS_FOOD_VALUE = 9;
    private static final double GET_SICK_PROBABILITY = 0.01;
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    
    private static final Random rand = Randomizer.getRandom();
    
    private int age;
    private int foodLevel;
    private int sicknessStepsRemaining = 0;
    private boolean isSick = false;
    
    public Grasshopper(boolean randomAge, Field field, Location location, Color col){
         super(field, location, col);
         
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = GRASS_FOOD_VALUE;
        }
    }
    
      /**
     * This is what the Grasshopper does most of the time: it hunts for
     * Grass. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born grasshoppers.
     */
    public void act(List<Animal> newGrasshoppers) {
        if (!isSick()) {
            getSick();  
        }
        if (isSick()) {
            applySickness();
            passOnSickness();
        }
            
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newGrasshoppers);           
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { // No food found - try to move to a free location.
                newLocation = getField().getFreeAdjacentLocation(getLocation());
                }
            // See if it was possible to move.
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
     * Increase the age. This could result in the grasshopper's death.
     */
    private void incrementAge() {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
      /**
     * Make this grasshopper more hungry. This could result in the grasshopper's death.
     */
    private void incrementHunger() {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    private Location findFood(){
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        
        while(it.hasNext()){
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Grass){
                Grass grass = (Grass) animal;
                if(grass.isAlive()){
                    grass.setDead();
                    foodLevel = GRASS_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
     /**
     * Check whether or not this grasshopper is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newGrasshoppers A list to return newly born grasshopper's.
     */
    private void giveBirth(List<Animal> newGrasshoppers) {
        // New hawks are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Grasshopper young = new Grasshopper(false, field, loc, getColor());
            newGrasshoppers.add(young);
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
     * A grasshopper can breed if it has reached the breeding age.
     */
    private boolean canBreed() {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Any animal that is not already sick has a 1% chance of becoming sick.
     */
       public void getSick() {
    if (rand.nextDouble() <= GET_SICK_PROBABILITY && sicknessStepsRemaining == 0) {
        sicknessStepsRemaining = 5;
        isSick = true;
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
    
    /**
     * If a neighbouring animal is of the same species, and the current animal is sick, it has a 5% chance of passing this on to its neighbouring animal.
     */
    private void passOnSickness() {
        if(isSick) {
            List<Animal> neighbours = getField().getLivingNeighbours(getLocation());
            if(neighbours != null) { 
                for(Animal neighbour : neighbours) {
                    if (neighbour != null && neighbour instanceof Grasshopper && neighbour.isAlive()) {  
                        Grasshopper grasshopper= (Grasshopper) neighbour;  
                        if (!grasshopper.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY) {
                            grasshopper.setSick(); 
                            grasshopper.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
}

