import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * A model of a snake.
 * Snakes age, move, breed, die (from hunger, sickness, age or getting eaten), hunt, get sick and pass on sickness
 *
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */

public class Snake extends Animal{          //PURPLE SQUARE IN FIELD
    // Genetic and Biological Attributes
    private String genes;
    private int breedingAge;
    private int maxAge;
    private double breedingProbability;
    private int maxLitterSize;
    private double getSickProbability;
    private double metabolism;
    
    // Constants
    private static final int MOUSE_FOOD_VALUE = 25;
    private static final int FROG_FOOD_VALUE = 25;
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
     * Create a new Snake. A snake may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the snake will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param col The color of the snake.
     * @param isMale If true, the snake is male.
     * @param inheritedGene If the snake has age zero, it will inherite genes.
     */
    public Snake(boolean randomAge, Field field, Location location, Color col, boolean isMale, String inheritedGene){
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
            foodLevel = rand.nextInt(FROG_FOOD_VALUE);
        }
        else {
            age = 0;
            genes = inheritedGene;
            foodLevel = MOUSE_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the Snake does most of the time: it hunts for
     * Mice and Frogs. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newSnake A list to return newly born snakes.
     */
    public void act(List<Animal> newSnakes) {
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
            giveBirth(newSnakes);           
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
     * Increase the age. This could result in the Snake's death.
     */
    private void incrementAge() {
        age++;
        if(age > maxAge) {
            setDead();
        }
    }
    
    /**
     * Make this Snake more hungry. This could result in the Snake's death.
     */
    private void incrementHunger() {
        foodLevel -= metabolism;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for Mice and Frogs adjacent to the current location.
     * Only the first live Mice or Frog is eaten.
     * @return Where food was found, or null if it wasn't.
     */
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
     * @param newSnakes A list to return newly born snakes.
     */
    private void giveBirth(List<Animal> newSnakes) {
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
                            
    Snake breedingMate = findMate();
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

        Snake young = new Snake(false, getField(), loc, getColor(), newbornIsMale, offspringGene);
        newSnakes.add(young);
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
     * A Snake can breed if it has reached the breeding age.
     */
     private boolean canBreed() {
    if(age >= breedingAge) {
        List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
        for(Animal neighbor : neighbors) {
            if(neighbor instanceof Snake) {
                Snake snakeNeighbor = (Snake) neighbor;
                if(snakeNeighbor.isMale() != this.isMale()) {
                    return true; 
                }
            }
        }
    }
    return false;
    }
    
    /**
     * Any snake that is not already sick has a 1% chance of becoming sick.
     */
       public void getSick() {
    if (rand.nextDouble() <= getSickProbability && sicknessStepsRemaining == 0) {
        sicknessStepsRemaining = 5;
        isSick = true;
    }
    }
    
    /**
     * If sick, the snake will loose foodLevel twice as fast as normal.
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
                    if (neighbour != null && neighbour instanceof Snake && neighbour.isAlive()) {  
                        Snake snake = (Snake) neighbour;  
                        if (!snake.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY) {
                            snake.setSick(); 
                            snake.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
    
     /**
     * The Snake scans the adjacent rooms for a mate which must be a snake.
     */
    private Snake findMate() {
    List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
    for (Animal neighbor : neighbors) {
        if (neighbor instanceof Snake) {
            Snake snakeNeighbor = (Snake) neighbor;
            if (snakeNeighbor.isMale()) { 
                return snakeNeighbor; 
            }
        }
    }
    return null; 
    }
    
    /**
     *@return genes The genetic code of current snake.
     */
    public String getGene(){
        return genes;
    }
    
    /**
     *@return age The age of each individual snake.
     */
    public int getAge() {
    return this.age;
    }
    
    /**
     *@return birthCount The number of births per snake
     */
     public int getBirthCount() {
        return birthCount;
    }
    
    /**
     * has a 20% chance of mutating any of the digits in the offspring gene string. 
     * makes sure to the number cannot go out of bounds on the desired range
     * 
     * @param gene The gene of the Frog.
     * @return mutatedGene.toString() The mutated gene of the Frog.
     */
    public static String randomlyMutateGene(String genes) {
        
    int breedingAge = Integer.parseInt(genes.substring(0, 2));
    int maxAge = Integer.parseInt(genes.substring(2, 5));
    double breedingProbability = Integer.parseInt(genes.substring(5, 7)) / 100.0;
    int maxLitterSize = Integer.parseInt(genes.substring(7, 9));
    double getSickProbability = Integer.parseInt(genes.substring(9, 11)) / 100.0;
    double metabolism = Integer.parseInt(genes.substring(11, 14)) / 100.0;

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            breedingAge = Math.min(breedingAge + 1, 50); 
        } else {
            breedingAge = Math.max(breedingAge - 1, 5);  
        }
    }

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            maxAge = Math.min(maxAge + 1, 120);  
        } else {
            maxAge = Math.max(maxAge - 1, 40);  
        }
    }

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            breedingProbability = Math.min(breedingProbability + 0.01, 1.0);  
        } else {
            breedingProbability = Math.max(breedingProbability - 0.01, 0.0);  
        }
    }

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            maxLitterSize = Math.min(maxLitterSize + 1, 12);  
        } else {
            maxLitterSize = Math.max(maxLitterSize - 1, 1);  
        }
    }

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            getSickProbability = Math.min(getSickProbability + 0.01, 1.0);  
        } else {
            getSickProbability = Math.max(getSickProbability - 0.01, 0.0);   
        }
    }

    if (rand.nextDouble() <= 0.20) {
        if (rand.nextDouble() <= 0.50) {
            metabolism = Math.min(metabolism + 0.01, 1.0);  
        } else {
            metabolism = Math.max(metabolism - 0.01, 0.25);   
        }
    }

    StringBuilder mutatedGene = new StringBuilder();
    
    if (breedingAge < 10) {
        mutatedGene.append("0").append(breedingAge);
    } else {
        mutatedGene.append(breedingAge);
    }

    if (maxAge < 100) {
        mutatedGene.append("0").append(maxAge);
    } else {
        mutatedGene.append(maxAge);
    }

    int breedingProbabilityInt = (int)(breedingProbability * 100);  
    if (breedingProbabilityInt < 10) {
        mutatedGene.append("0").append(breedingProbabilityInt);
    } else {
        mutatedGene.append(breedingProbabilityInt);
    }

    if (maxLitterSize < 10) {
        mutatedGene.append("0").append(maxLitterSize);
    } else {
        mutatedGene.append(maxLitterSize);
    }

    int getSickProbabilityInt = (int)(getSickProbability * 100);  
    if (getSickProbabilityInt < 10) {
        mutatedGene.append("0").append(getSickProbabilityInt);
    } else {
        mutatedGene.append(getSickProbabilityInt);
    }

    int metabolismInt = (int)(metabolism * 100);  
    if (metabolismInt < 100) {
        mutatedGene.append("0").append(metabolismInt);
    } else {
        mutatedGene.append(metabolismInt);
    }
    
    return mutatedGene.toString();
    }
}
