import java.util.List;
import javafx.scene.paint.Color; 
import java.util.Random;

/**
 * A class representing shared characteristics of animals.
 * 
 * @author Jonny Guest and Jason Immanuel
 * @version 2025.03.01
 */

public abstract class Animal {
    
    private boolean alive;
    private Field field;
    private Location location;
    private String name;
    private Counter counter;
    private Color color = Color.BLACK;
    private static final Random rand = Randomizer.getRandom();
    
    protected int sicknessStepsRemaining; 
    private boolean isSick;
    private static final double GET_SICK_PROBABILITY = 1;
    private int foodLevel;
    private boolean isMale;
    private boolean isAlive = true;
    
    
    /**
     * Create a new animal at location in field.
     * 
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    
    public Animal(Field field, Location location, Color col, boolean isMale) {
        alive = true;
        this.field = field;
        setLocation(location);
        setColor(col);
        this.name = name; 
        this.counter = new Counter(name); 
        this.isSick = false;
        this.sicknessStepsRemaining = 0;
        this.isMale = isMale;
    }
    
    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do.
     * @param newAnimals A list to receive newly born animals.
     */
    abstract public void act(List<Animal> newAnimals);

    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    protected boolean isAlive() {
        return alive;
    }
    
    /**
     * Indicate that the animal is no longer alive.
     * It is removed from the field.
     */
    protected void setDead() {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }
    
    /**
     * Return the animal's location.
     * @return The animal's location.
     */
    protected Location getLocation() {
        return location;
    }
    
    /**
     * Place the animal at the new location in the given field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation) {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
        }
    
    /**
     * Return the animal's field.
     * @return The animal's field.
     */
    protected Field getField() {
        return field;
    }
    
    /**
     * Changes the color of the animal
     */
    public void setColor(Color col) {
        color = col;
    }

    /**
     * Returns the animal's color
     */
    public Color getColor() {
        return color;
    }   
    
    public int getCount() {
        return counter.getCount();
    }
    
    public boolean isSick() {
        return isSick;
    }
    
    public void setSick() {
    this.isSick = true;
    }

    protected void setSicknessSteps(int steps) {
    this.sicknessStepsRemaining = steps;
    }
    
    public boolean isMale() {
        return isMale;
    }

}