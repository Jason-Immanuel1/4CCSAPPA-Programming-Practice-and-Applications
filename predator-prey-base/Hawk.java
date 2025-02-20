import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * Write a description of class Hawk here.
 *
 * @author Jonny Guest and Jason Immanuel
 * @version (a version number or a date)
 */

public class Hawk extends Animal{                //RED SQUARE IN FIELD
    
    
    private static final int BREEDING_AGE = 3;
    private static final int MAX_AGE = 20;
    private static final double BREEDING_PROBABILITY = 0.12;
    private static final int MAX_LITTER_SIZE = 2;
    
    private static final int FOX_FOOD_VALUE = 9;
    private static final int SNAKE_FOOD_VALUE = 9;
    private static final double GET_SICK_PROBABILITY = 0.01;
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    
    private static final Random rand = Randomizer.getRandom();
    
    private int age;
    private int foodLevel;
    private int sicknessStepsRemaining = 0;
    private boolean isSick = false;
    
    public Hawk(boolean randomAge, Field field, Location location, Color col){
         super(field, location, col);
         
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FOX_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = SNAKE_FOOD_VALUE;
        }
    }
    
      /**
     * This is what the hawk does most of the time: it hunts for
     * snake/fox. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born foxes.
     */
    public void act(List<Animal> newHawks) {
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
            giveBirth(newHawks);           
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { // No food found - try to move to a free location.
                newLocation = getField().getPredatorFreeAdjacentLocation(getLocation());
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
     * Increase the age. This could result in the hawk's death.
     */
    private void incrementAge() {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
      /**
     * Make this hawk more hungry. This could result in the hawk's death.
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
            if(animal instanceof Fox){
                Fox fox = (Fox) animal;
                if(fox.isAlive()){
                    fox.setDead();
                    foodLevel = FOX_FOOD_VALUE;
                    return where;
                }
            }
            else if(animal instanceof Snake){
                Snake snake = (Snake) animal;
                if(snake.isAlive()){
                    snake.setDead();
                    foodLevel = SNAKE_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
     /**
     * Check whether or not this hawk is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newHawks A list to return newly born hawks.
     */
    private void giveBirth(List<Animal> newHawks) {
        // New hawks are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getPredatorFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Hawk young = new Hawk(false, field, loc, getColor());
            newHawks.add(young);
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
     * A hawk can breed if it has reached the breeding age.
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
    if (sicknessStepsRemaining == 0) {
        isSick = false;  
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
                    if (neighbour != null && neighbour instanceof Hawk && neighbour.isAlive()) { 
                        Hawk hawk = (Hawk) neighbour;  
                        if (!hawk.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY) {
                            hawk.setSick(); 
                            hawk.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
}
