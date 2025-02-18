import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * Write a description of class Snake here.
 *
 * @author Jonny Guest and Jason Immanuel
 * @version (a version number or a date)
 */

public class Snake extends Animal{            //PURPLE SQUARE IN FIELD
     private static final int BREEDING_AGE = 4;
    private static final int MAX_AGE = 20;
    private static final double BREEDING_PROBABILITY = 0.15;
    private static final int MAX_LITTER_SIZE = 3;
    
    private static final int MOUSE_FOOD_VALUE = 9;
    private static final int FROG_FOOD_VALUE = 9;
    private static final double GET_SICK_PROBABILITY = 0.01;
    
    private static final Random rand = Randomizer.getRandom();
    
    private int age;
    private int foodLevel;
    private int sicknessStepsRemaining = 0;
    
    public Snake(boolean randomAge, Field field, Location location, Color col){
         super(field, location, col);
         
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FROG_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = MOUSE_FOOD_VALUE;
        }
    }
    
      /**
     * This is what the Snake does most of the time: it hunts for
     * Frog/mouse. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born foxes.
     */
    public void act(List<Animal> newSnakes) {
        getSick();
        applySickness();
        
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newSnakes);            
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
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
     * Increase the age. This could result in the Snake's death.
     */
    private void incrementAge() {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
      /**
     * Make this Snake more hungry. This could result in the hawk's death.
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
            if(animal instanceof Frog){
                Frog frog = (Frog) animal;
                if(frog.isAlive()){
                    frog.setDead();
                    foodLevel = FROG_FOOD_VALUE;
                    return where;
                }
            }
            else if(animal instanceof Mouse){
                Mouse mouse = (Mouse) animal;
                if(mouse.isAlive()){
                    mouse.setDead();
                    foodLevel = MOUSE_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
     /**
     * Check whether or not this Snake is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newFoxes A list to return newly born foxes.
     */
    private void giveBirth(List<Animal> newSnakes) {
        // New hawks are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Snake young = new Snake(false, field, loc, getColor());
            newSnakes.add(young);
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
     * A Snake can breed if it has reached the breeding age.
     */
    private boolean canBreed() {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Every animal will have a 1% chance of getting sick every step. 
     * Also check if sicknessStepsRemainig is == 0 to avoid stacking sickness on the same animal.
     */
    private void getSick() {
    if (rand.nextDouble() <= GET_SICK_PROBABILITY && sicknessStepsRemaining == 0) {
        sicknessStepsRemaining = 5; 
    }
    }
    
    /**
     * If sick, the animal will loose foodLevel twice as fast as normal.
     */
    private void applySickness() {
    if (sicknessStepsRemaining > 0) {
        foodLevel -= 2; 
        sicknessStepsRemaining--; 
    }
    }
}

