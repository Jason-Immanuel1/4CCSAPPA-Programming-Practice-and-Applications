 

import java.util.List;
import javafx.scene.paint.Color;

public class Grass extends Animal {

    private static final int MAX_AGE = 100;
    private static final double GROWTH_PROBABILITY = 0.3;
    private int age;

    public Grass(Field field, Location location) {
        super(field, location, Color.GREEN);
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
