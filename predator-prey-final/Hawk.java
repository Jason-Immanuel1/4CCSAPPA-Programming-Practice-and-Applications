import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * A model of a hawk.
 * Hawks age, move, breed, die (from hunger, sickness or age), hunt, get sick and pass on sickness
 * 
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */

public class Hawk extends Animal{           //RED SQUARE IN FIELD
    // Genetic and Biological Attributes
    private String genes;
    private int breedingAge;
    private int maxAge;
    private double breedingProbability;
    private int maxLitterSize;
    private double getSickProbability;
    private double metabolism;
    
    // Constants
    private static final int FOX_FOOD_VALUE = 35;
    private static final int SNAKE_FOOD_VALUE = 30;
    private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
    
    // Random Number Generator
    private static final Random rand = Randomizer.getRandom();
    
    // Lifecycle and State Attributes
    private int age;
    private int foodLevel;
    private int sicknessStepsRemaining = 0;
    private boolean isSick = false;
    private boolean randomAge = false;
    private int birthCount;
    
    /**
     * Create a new Hawk. A hawk may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the hawk will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param col The color of the hawk.
     * @param isMale If true, the hawk is male.
     * @param inheritedGene If the hawk has age zero, it will inherite genes.
     */
    public Hawk(boolean randomAge, Field field, Location location, Color col, boolean isMale, String inheritedGene){
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
            foodLevel = rand.nextInt(FOX_FOOD_VALUE);
        }
        else {
            age = 0;
            genes = inheritedGene;
            foodLevel = SNAKE_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the Hawk does most of the time: it hunts for
     * Foxes and Snakes. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newHawks A list to return newly born hawks.
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
     * Increase the age. This could result in the Hawk's death.
     */
    private void incrementAge() {
        age++;
        if(age > maxAge) {
            setDead();
        }
    }
    
    /**
     * Make this Hawk more hungry. This could result in the Hawk's death.
     */
    private void incrementHunger() {
        foodLevel -= metabolism;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for Foxes and Snakes adjacent to the current location.
     * Only the first live Fox or Snake is eaten.
     * @return Where food was found, or null if it wasn't.
     */
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
     * Check whether or not this Hawk is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newHawk A list to return newly born hawks.
     */
    private void giveBirth(List<Animal> newHawks) {
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
                            
    Hawk breedingMate = findMate();
    if (breedingMate == null) {
        return; 
    }
    
    List<Location> free = getField().getFreeAdjacentLocations(getLocation());
    int safeMaxLitterSize = Math.max(1, maxLitterSize); // Ensure at least 1
    int births = rand.nextInt(safeMaxLitterSize) + 1;
    
    for (int b = 0; b < births && free.size() > 0; b++) {
        Location loc = free.remove(0);
        boolean newbornIsMale = rand.nextDouble() < 0.5;

        String parentGene1 = this.getGene();
        String parentGene2 = breedingMate.getGene();
        String offspringGene = parentGene1.substring(0, 7) + parentGene2.substring(7, 14);

        if (birthCount > 1) {
            offspringGene = randomlyMutateGene(offspringGene);
        }

        

        Hawk young = new Hawk(false, getField(), loc, getColor(), newbornIsMale, offspringGene);
        newHawks.add(young);
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
     * A hawk can breed if it has reached the breeding age.
     */
     private boolean canBreed() {
    if(age >= breedingAge) {
        List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
        for(Animal neighbor : neighbors) {
            if(neighbor instanceof Hawk) {
                Hawk hawkNeighbor = (Hawk) neighbor;
                if(hawkNeighbor.isMale() != this.isMale()) {
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
    
     /**
     * The Hawk scans the adjacent rooms for a mate which must be a hawk.
     */
    private Hawk findMate() {
    List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
    for (Animal neighbor : neighbors) {
        if (neighbor instanceof Hawk) {
            Hawk hawkNeighbor = (Hawk) neighbor;
            if (hawkNeighbor.isMale()) { 
                return hawkNeighbor; 
            }
        }
    }
    return null; 
    }
    
    /**
     * returns genetic code of current hawk.
     */
    public String getGene(){
        return genes;
    }
    
    /**
     * return age of each individual hawk.
     */
    public int getAge() {
    return this.age;
    }
    
    /**
     * return the number of births per hawk
     */
     public int getBirthCount() {
        return birthCount;
    }
    
    /**
     * has a 20% chance of mutating any of the digits in the offspring gene string. 
     * 
     * @param gene The gene of the Hawk.
     * @return mutatedGene.toString() The mutated gene of the Hawk.
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
