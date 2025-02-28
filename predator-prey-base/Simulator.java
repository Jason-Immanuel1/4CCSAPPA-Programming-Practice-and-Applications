import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.scene.paint.Color; 

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits, foxes, and grass.
 * 
 * @author David J. Barnes, Michael Kölling, Jeffery Raphael, Johnny Guest and Jason Immanuel
 * @version 2025.02.17
 */

public class Simulator {

    private static final double FOX_CREATION_PROBABILITY = 0.06;      
    private static final double RABBIT_CREATION_PROBABILITY = 0.2;    
    private static final double HAWK_CREATION_PROBABILITY = 0.03;    
    private static final double SNAKE_CREATION_PROBABILITY = 0.04;    
    private static final double FROG_CREATION_PROBABILITY = 0.08;     
    private static final double MOUSE_CREATION_PROBABILITY = 0.08;     
    private static final double GRASSHOPPER_CREATION_PROBABILITY = 0.2; 
    private static final double GRASS_CREATION_PROBABILITY = 1;   

    private List<Animal> animals;
    private Field field;
    private int step;
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width) {
        
        animals = new ArrayList<>();
        field = new Field(depth, width);

        reset();
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox, rabbit, hawk, frog, snake, grasshopper, mouse, grass.
     */
    public void simulateOneStep() {
        step++;
        List<Animal> newAnimals = new ArrayList<>();        

        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            animal.act(newAnimals);
            if(! animal.isAlive()) {
                it.remove();
            }
        }
               
        animals.addAll(newAnimals);
        field.getStats().generateCounts(field);
        
        System.out.println(field.getStats().getPopulationDetails(field));
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset() {
        step = 0;
        animals.clear();
        populate();
    }
    
    /**
     * Randomly populate the field with foxes, rabbits,hawks, frogs, snakes, grasshoppers, mice.
     */
    private void populate() {
        Random rand = Randomizer.getRandom();
        field.clear();
        
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= GRASSHOPPER_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Grasshopper grasshopper = new Grasshopper(true, field, location, Color.DARKBLUE, isMale, Randomizer.generateGene());
                    animals.add(grasshopper);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Rabbit rabbit = new Rabbit(true, field, location, Color.GREY, isMale, Randomizer.generateGene());
                    animals.add(rabbit);
                }
                else if(rand.nextDouble() <= FROG_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Frog frog = new Frog(true, field, location, Color.GREEN, isMale, Randomizer.generateGene());
                    animals.add(frog);
                }
                else if(rand.nextDouble() <= MOUSE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Mouse mouse = new Mouse(true, field, location, Color.ORANGE, isMale, Randomizer.generateGene());
                    animals.add(mouse);
                }
                else if(rand.nextDouble() <= SNAKE_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Snake snake = new Snake(true, field, location, Color.PURPLE, isMale, Randomizer.generateGene());
                    animals.add(snake);
                }
                else if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Fox fox = new Fox(true, field, location, Color.BROWN, isMale, Randomizer.generateGene());
                    animals.add(fox);
                }
                else if(rand.nextDouble() <= HAWK_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    boolean isMale = rand.nextDouble() < 0.5; 
                    Hawk hawk = new Hawk(true, field, location, Color.RED, isMale, Randomizer.generateGene());
                    animals.add(hawk);
                }
                else if(rand.nextDouble() <= GRASS_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    Grass grass = new Grass(field, location);
                    animals.add(grass);
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    public void delay(int millisec) {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
    
    public Field getField() {
        return field;
    }

    public int getStep() {
        return step;
    }
}