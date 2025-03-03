import java.util.List;
import java.util.Random;
import javafx.scene.paint.Color; 
import java.util.Iterator;

/**
 * A model of a rabbit.
 * Rabbits age, move, breed, die (from hunger, sickness, age or getting eaten), hunt, get sick and pass on sickness
 * 
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */

public class Rabbit extends Animal {            //GREY SQUARE IN FIELD
    // Genetic and Biological Attributes
    private String genes; 
    private int breedingAge; 
    private int maxAge; 
    private double breedingProbability; 
    private int maxLitterSize; 
    private double getSickProbability; 
    private double metabolism; 
    
    // Constants
    private static final int GRASS_FOOD_VALUE = 20; 
    private static final double PASS_ON_SICKNESS_PROBABILITY = 0.05; 
    
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
     * Create a new Rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the rabbit will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param col The color of the rabbit.
     * @param isMale If true, the rabbit is male.
     * @param inheritedGene If the rabbit has age zero, it will inherite genes.
     */
    public Rabbit(boolean randomAge, Field field, Location location, Color col, boolean isMale, String inheritedGene) {
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
        foodLevel = rand.nextInt(GRASS_FOOD_VALUE);
    } else {
        age = 0;
        genes = inheritedGene;
        foodLevel = GRASS_FOOD_VALUE;
    }
    }

    
    /**
     * This is what the Rabbit does most of the time: it hunts for
     * Grass. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newRabbits A list to return newly born rabbits.
     */
    public void act(List<Animal> newRabbits) {
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
            giveBirth(newRabbits);           
            Location newLocation = findFood();
            if(newLocation == null) { 
                newLocation = getField().getFreeAdjacentLocation(getLocation());
                }
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
     * Look for Grass adjacent to the current location.
     * Only the first live Grass is eaten.
     * @return Where food was found, or null if it wasn't.
     */
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
     * Increase the age. This could result in the Rabbit's death.
     */
    private void incrementAge() {
        age++;
        if(age > maxAge) {
            setDead();
        }
    }
    
    /**
     * Make this Rabbit more hungry. This could result in the Rabbit's death.
     */
    private void incrementHunger() {
        foodLevel -= metabolism;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this Rabbit is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Animal> newRabbits) {
    randomAge = false;

    if (isMale() || !canBreed() || rand.nextDouble() > breedingProbability) {
        return;
    }

    Rabbit breedingMate = findMate();
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

    Rabbit young = new Rabbit(false, getField(), loc, getColor(), newbornIsMale, offspringGene);
    newRabbits.add(young);
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
     * A rabbit can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed() {
    if(age >= breedingAge) {
        List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
        for(Animal neighbor : neighbors) {
            if(neighbor instanceof Rabbit) {
                Rabbit rabbitNeighbor = (Rabbit) neighbor;
                if(rabbitNeighbor.isMale() != this.isMale()) {
                    return true; 
                }
            }
        }
    }
    return false;
    }
    
    /**
     * Any animal that is not already sick has a random chance of becoming sick.
     */
    public void getSick() {
    if (rand.nextDouble() <= getSickProbability && sicknessStepsRemaining == 0) {
        isSick = true;  
        sicknessStepsRemaining = 5;  
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
                    if (neighbour != null && neighbour instanceof Rabbit && neighbour.isAlive()) {  
                        Rabbit rabbit = (Rabbit) neighbour;  
                        if (!rabbit.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILITY) {
                            rabbit.setSick(); 
                            rabbit.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
    
     /**
     * The Rabbit scans the adjacent rooms for a mate which must be a rabbit.
     */
    private Rabbit findMate() {
    List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
    for (Animal neighbor : neighbors) {
        if (neighbor instanceof Rabbit) {
            Rabbit rabbitNeighbor = (Rabbit) neighbor;
            if (rabbitNeighbor.isMale()) { 
                return rabbitNeighbor; 
            }
        }
    }
    return null; 
    }
    
    /**
     *@return genes genetic code of current Rabbit.
     */
    public String getGene(){
        return genes;
    }
    
    /**
     * return age of each individual rabbit.
     */
    public int getAge() {
    return this.age;
    }
    
    /**
     * return the number of births per rabbit
     */
     public int getBirthCount() {
        return birthCount;
    }
    
    /**
     * has a 20% chance of mutating any of the digits in the offspring gene string. 
     * 
     * @param gene The gene of the Rabbit.
     * @return mutatedGene.toString() The mutated gene of the Rabbit.
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