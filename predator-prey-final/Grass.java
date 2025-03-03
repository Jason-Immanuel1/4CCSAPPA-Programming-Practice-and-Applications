import java.util.List;
import javafx.scene.paint.Color;

/**
 * A model of grass
 * Grass grows and dies (from age or getting eaten) 
 * 
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */

public class Grass extends Animal {         //LIME GREEN SQUARE IN FIELD

    // Constants
    private static final int MAX_AGE = 100;
    private static final double GROWTH_PROBABILITY = 0.2;
    
    // Lifecycle Attributes
    private int age;

    /**
     * Create a new Grass.
     * 
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Grass(Field field, Location location) {
        super(field, location, Color.LIME, false);
        age = 0;
    }

    @Override
    public void act(List<Animal> newAnimals) {
        incrementAge();
        if (isAlive()) {
            grow(newAnimals);
        }
    }

    private void incrementAge() {
        age++;
        if (age > MAX_AGE) {
            setDead();
        }
    }

    private void grow(List<Animal> newAnimals) {
        Field field = getField();
        List<Location> adjacentLocations = field.getFreeAdjacentLocations(getLocation());
        for (Location loc : adjacentLocations) {
            if (Math.random() <= GROWTH_PROBABILITY) {
                Grass newGrass = new Grass(field, loc);
                newAnimals.add(newGrass);
            }
        }
    }
}

 