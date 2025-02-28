import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * A model of a fox.
 * Foxes age, move, eat rabbits, and die(from hunger, sickness or getting eaten).
 * 
 * @author David J. Barnes and Michael Kölling, Jonny Guest and Jason Immanuel
 * @version 2025.02.10
 */

public class Fox extends Animal {          //BROWN SQUARE IN FIELD
    private String genes; 

    private int breedingAge;  
    private int maxAge;      
    private double breedingProbability;  
    private int maxLitterSize;  
    private double getSickProbability;  
    private double metabolism; 
    
    private static final int RABBIT_FOOD_VALUE = 25;
    private static final int MOUSE_FOOD_VALUE = 30;
    private static final Random rand = Randomizer.getRandom();
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    
    private int age;
    private int foodLevel;
    private int sicknessStepsRemaining = 0;
    private boolean isSick = false;
    private boolean randomAge = false;
    private int birthCount;
    
    /**
     * Create a fox. A fox can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the fox will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Fox(boolean randomAge, Field field, Location location, Color col, boolean isMale, String inheritedGene) {
        super(field, location, col, isMale);
        this.birthCount = 0;
        
         if (randomAge) {
            genes = Randomizer.generateGene();
        } else {
            genes = inheritedGene;
        }
        
        breedingAge = Integer.parseInt(genes.substring(0, 2));
        maxAge = Integer.parseInt(genes.substring(2, 5));
        breedingProbability = Integer.parseInt(genes.substring(5, 7)) / 100.0;
        maxLitterSize = Integer.parseInt(genes.substring(7, 9));
        getSickProbability = Integer.parseInt(genes.substring(9, 11)) / 100.0;
        metabolism = Integer.parseInt(genes.substring(11, 14)) / 100.0;
        
        if(randomAge) {
            genes = Randomizer.generateGene();
            age = rand.nextInt(maxAge);
            foodLevel = rand.nextInt(RABBIT_FOOD_VALUE);
        }
        else {
            age = 0;
            genes = inheritedGene;
            foodLevel = RABBIT_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the fox does most of the time: it hunts for
     * rabbit/mouse. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born foxes.
     */
    public void act(List<Animal> newFoxes) {
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
            giveBirth(newFoxes);           
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
     * Increase the age. This could result in the fox's death.
     */
    private void incrementAge() {
        age++;
        if(age > maxAge) {
            setDead();
        }
    }
    
    /**
     * Make this fox more hungry. This could result in the fox's death.
     */
    private void incrementHunger() {
        foodLevel -= metabolism;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for rabbits adjacent to the current location.
     * Only the first live rabbit is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood() {

        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    foodLevel = RABBIT_FOOD_VALUE;
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
     * Check whether or not this rabbit is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Animal> newFoxes) {
        randomAge = false;
    if (isMale()) {
        return;
    }
    
    if (!canBreed()) {
        return;
    }
    
    if (rand.nextDouble() > breedingProbability) {
        return; 
    }
                            
    Fox breedingMate = findMate();
    if (breedingMate == null) {
        return; 
    }
    
    List<Location> free = getField().getFreeAdjacentLocations(getLocation());
    int births = rand.nextInt(maxLitterSize) + 1;
    
    for (int b = 0; b < births && free.size() > 0; b++) {
        Location loc = free.remove(0);
        boolean newbornIsMale = rand.nextDouble() < 0.5;

        String parentGene1 = this.getGene();
        String parentGene2 = breedingMate.getGene();
        String offspringGene = parentGene1.substring(0, 7) + parentGene2.substring(7, 14);

        if (birthCount > 1) {
            offspringGene = randomlyMutateGene(offspringGene);
        }

        

        Fox young = new Fox(false, getField(), loc, getColor(), newbornIsMale, offspringGene);
        newFoxes.add(young);
    }
    birthCount += births;
    }
    
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed() {
    return rand.nextInt(maxLitterSize) + 1;
    }

    /**
     * A fox can breed if it has reached the breeding age.
     */
     private boolean canBreed() {
    if(age >= breedingAge) {
        List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
        for(Animal neighbor : neighbors) {
            if(neighbor instanceof Fox) {
                Fox foxNeighbor = (Fox) neighbor;
                if(foxNeighbor.isMale() != this.isMale()) {
                    return true; 
                }
            }
        }
    }
    return false;
    }
    
    /**
     * Any animal that is not already sick has a 1% chance of becoming sick.
     */
       public void getSick() {
    if (rand.nextDouble() <= getSickProbability && sicknessStepsRemaining == 0) {
        sicknessStepsRemaining = 5;
        isSick = true;
    }
    }
    
    /**
     * If sick, the animal will loose foodLevel twice as fast as normal.
     */
    public void applySickness() {
    if (sicknessStepsRemaining > 0) {
        foodLevel -= 2 * metabolism;
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
                    if (neighbour != null && neighbour instanceof Fox && neighbour.isAlive()) {  
                        Fox fox = (Fox) neighbour;  
                        if (!fox.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY) {
                            fox.setSick(); 
                            fox.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Fox scane the adjacent rooms for a mate, must be a Fox.
     */
    private Fox findMate() {
    List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
    for (Animal neighbor : neighbors) {
        if (neighbor instanceof Fox) {
            Fox foxNeighbor = (Fox) neighbor;
            if (foxNeighbor.isMale()) { 
                return foxNeighbor; 
            }
        }
    }
    return null; 
    }
    
    /**
     * returns genetic code of current Fox.
     */
    public String getGene(){
        return genes;
    }
    
    /**
     * return age of each individual fox.
     */
    public int getAge() {
    return this.age;
    }
    
    /**
     * return the number of births per fox
     */
     public int getBirthCount() {
        return birthCount;
    }
    
    /**
     * has a 20% chance of mutating any of the digits in the offspring gene string. 
     */
    private String randomlyMutateGene(String gene) {
    StringBuilder mutatedGene = new StringBuilder(gene);
    
    for(int i = 0; i < gene.length(); i++) {
        if(rand.nextDouble() <= 0.20) {
            char geneDigit = gene.charAt(i);
            
            int digit = Character.getNumericValue(geneDigit);  
            if(rand.nextDouble() <= 0.5) {
                digit = (digit + 1) % 10; 
            } else {
                digit = (digit - 1 + 10) % 10; 
            }
            
            mutatedGene.setCharAt(i, Character.forDigit(digit, 10));
        }
    }
    
    return mutatedGene.toString();
    }
}