import java.util.Random;

/**
 * Provide control over the randomization of the simulation. By using the
 * shared, fixed-seed randomizer, repeated runs will perform exactly the same
 * (which helps with testing). Set 'useShared' to false to get different random
 * behaviour every time.
 *
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29
 */

public class Randomizer {
  
    private static final int SEED = 1111;
    private static final Random rand = new Random(SEED);
    private static final boolean useShared = true;
    


    /**
     * Provide a random generator.
     * @return A random object.
     */
    public static Random getRandom() {
        if (useShared) {
            return rand;
        }
        else {
            return new Random();
        }
    }
    
    /**
     * Create a random 14 digit number to decide the characteristics of an animal.
     */
    public static String generateGene() {
    StringBuilder geneBuilder = new StringBuilder();  
    
    int breedingAge = Randomizer.getRandom().nextInt(79) + 12;
    if(breedingAge < 10) {
        geneBuilder.append("0").append(breedingAge);
    } else {
        geneBuilder.append(breedingAge);
    }
    
    int lifeSpan = Randomizer.getRandom().nextInt(111) + 10;
    if(lifeSpan < 100) {
        geneBuilder.append("0").append(lifeSpan);
    } else {
        geneBuilder.append(lifeSpan);
    }
    
    int breedingProbabillity = Randomizer.getRandom().nextInt(51);
    if(breedingProbabillity < 10) {
        geneBuilder.append("0").append(breedingProbabillity);
    } else {
        geneBuilder.append(breedingProbabillity);
    }
    
    int litterSize = Randomizer.getRandom().nextInt(13);
    if(litterSize < 10) {
        geneBuilder.append("0").append(litterSize);
    } else {
        geneBuilder.append(litterSize);
    }
    
    int diseaseProbabillity = Randomizer.getRandom().nextInt(51);
    if(diseaseProbabillity < 10) {
        geneBuilder.append("0").append(diseaseProbabillity);
    } else {
        geneBuilder.append(diseaseProbabillity);
    }
    
    int metabolism = Randomizer.getRandom().nextInt(76) + 25;
    if(metabolism < 100) {
        geneBuilder.append("0").append(metabolism);
    } else {
        geneBuilder.append(metabolism);
    }
    
    return geneBuilder.toString();
    }

    /**
     * Reset the randomization.
     * This will have no effect if randomization is not through
     * a shared Random generator.
     */
    public static void reset() {
        if (useShared) {
            rand.setSeed(SEED);
        }
    }
}