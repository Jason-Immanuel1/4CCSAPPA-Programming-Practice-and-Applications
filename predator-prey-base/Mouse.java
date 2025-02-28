import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.LinkedList;
import javafx.scene.paint.Color; 

/**
 * A model of a mouse.
 * mouse age, move, breed, die (from hunger, age or getting eaten), hunt, get sick and pass on sickness
 *
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.02.26
 */
public class Mouse extends Animal{           //ORANGE SQUARE IN FIELD
       private String genes; 
    
       private int breedingAge;  
       private int maxAge;      
       private double breedingProbability;  
       private int maxLitterSize;  
       private double getSickProbability;  
       private double metabolism; 
        
       private static final int GRASSHOPPER_FOOD_VALUE = 30;
       private static final double PASS_ON_SICKNESS_PROBABILLITY = 0.05;
        
       private static final Random rand = Randomizer.getRandom();
        
       private int age;
       private int foodLevel;
       private int sicknessStepsRemaining = 0;
       private boolean isSick = false;
       private boolean randomAge = false;
       private int birthCount;
    
    public Mouse(boolean randomAge, Field field, Location location, Color col, boolean isMale, String inheritedGene){
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
            foodLevel = rand.nextInt(GRASSHOPPER_FOOD_VALUE);
        }
        else {
            age = 0;
            genes = inheritedGene;
            foodLevel = GRASSHOPPER_FOOD_VALUE;
        }
    }
    
      /**
     * This is what the mouse does most of the time: it hunts for
     * grasshoppers. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born foxes.
     */
     public void act(List<Animal> newMice) {
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
            giveBirth(newMice);           
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
     * Increase the age. This could result in the mouse's death.
     */
    private void incrementAge() {
        age++;
        if(age > maxAge) {
            setDead();
        }
    }
    
      /**
     * Make this mouse more hungry. This could result in the mouse's death.
     */
    private void incrementHunger() {
        foodLevel -= metabolism;
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
            if(animal instanceof Grasshopper){
                Grasshopper grasshopper = (Grasshopper) animal;
                if(grasshopper.isAlive()){
                    grasshopper.setDead();
                    foodLevel = GRASSHOPPER_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
     /**
     * Check whether or not this fox is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newMice A list to return newly born mice.
     */
    private void giveBirth(List<Animal> newMice) {
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
                                
    Mouse breedingMate = findMate();
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


        Mouse young = new Mouse(false, getField(), loc, getColor(), newbornIsMale, offspringGene);
        newMice.add(young);
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
     * A mouse can breed if it has reached the breeding age.
     */
     private boolean canBreed() {
    if(age >= breedingAge) {
        List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
        for(Animal neighbor : neighbors) {
            if(neighbor instanceof Mouse) {
                Mouse mouseNeighbor = (Mouse) neighbor;
                if(mouseNeighbor.isMale() != this.isMale()) {
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
                    if (neighbour != null && neighbour instanceof Mouse && neighbour.isAlive()) {  
                        Mouse mouse = (Mouse) neighbour;  
                        if (!mouse.isSick() && rand.nextDouble() <= PASS_ON_SICKNESS_PROBABILLITY) {
                            mouse.setSick(); 
                            mouse.setSicknessSteps(5);  
                        }
                    }
                }
            }
        }
    }
    
    /**
     * mouse scane the adjacent rooms for a mate, must be a mouse.
     */
    private Mouse findMate() {
    List<Animal> neighbors = getField().getLivingNeighbours(getLocation());
    for (Animal neighbor : neighbors) {
        if (neighbor instanceof Mouse) {
            Mouse mouseNeighbor = (Mouse) neighbor;
            if (mouseNeighbor.isMale()) { 
                return mouseNeighbor; 
            }
        }
    }
    return null; 
    }
    
    /**
     * returns genetic code of current mouse.
     */
    public String getGene(){
        return genes;
    }
    
    /**
     * return age of each individual mouse.
     */
    public int getAge() {
    return this.age;
    }
    
    /**
     * return the number of births per mouse
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

