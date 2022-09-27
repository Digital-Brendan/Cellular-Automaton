import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a snake.
 * Snakes age, move, eat hedgehog, and die.
 */
public class Snake extends Animal
{
    // Characteristics shared by all snakes (class variables).
    
    // The age at which a snake can start to breed.
    private static final int BREEDING_AGE = 30;
    // The age to which a snake can live.
    private static final int MAX_AGE = 100;
    // The likelihood of a snake breeding.
    private static final double BREEDING_PROBABILITY = 0.33;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 20;
    // The food value of a single hedgehog. In effect, this is the
    // number of steps a snake can go before it has to eat again.
    private static final int HEDGEHOG_FOOD_VALUE = 9;
    private static final int FROG_FOOD_VALUE = 8;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Amount of the day/night cycle at which the animal sleeps
    private static final double SLEEP_START_TIME = 0.15;
    // Duration of sleep
    private static final int SLEEP_LENGTH = 30;
    
    // Individual characteristics (instance fields).
    // The snake's age.
    private int age;
    // The snake's food level, which is increased by eating hedgehogs.
    private int foodLevel;

    /**
     * Create a snake. A snake can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the snake will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param inheritsDisease Whether animal parent was infected
     */
    public Snake(boolean randomAge, Field field, Location location, boolean inheritsDisease)
    {
        super(field, location, inheritsDisease);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(HEDGEHOG_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = HEDGEHOG_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the snake does most of the time: it hunts for
     * hedgehhogs. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newSnakes A list to return newly born snakes.
     * @param cycleProgress Amount of day/night cycle completed by simulation
     */
    public void act(List<Animal> newSnakes, double cycleProgress)
    {
        // Become older and advance disease 
        incrementAge();
        diseaseTick();
        
        if(isAlive()) {
            // Sleep
            sleep(SLEEP_START_TIME, age, SLEEP_LENGTH, cycleProgress);
            
            if (!asleep){
                giveBirth(newSnakes);            
                
                // Move towards a source of food if found.
                Location newLocation = findFood();
                if(newLocation == null) { 
                    // No food found - try to move to a free location.
                    newLocation = getField().freeAdjacentLocation(getLocation());
                }
                // See if it was possible to move.
                if(newLocation != null) {
                    setLocation(newLocation);
                }
                else {
                    // Overcrowding.
                    setDead();
                }
                incrementHunger();
            }
        }
    }

    /**
     * Increase the age. This could result in the snake's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this snake more hungry. This could result in the snake's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for hedgehogs adjacent to the current location.
     * Only the first live hedgehog is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Hedgehog) {
                Hedgehog hedgehog = (Hedgehog) animal;
                if(hedgehog.isAlive()) { 
                    hedgehog.setDead();
                    if(foodLevel < HEDGEHOG_FOOD_VALUE)
                        foodLevel = HEDGEHOG_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this snake is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newSnakes A list to return newly born snakes.
     */
    private void giveBirth(List<Animal> newSnakes)
    {
        // New snakes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        
        // Add offspring to adjacent cells if free
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Snake young = new Snake(false, field, loc, isDiseased);
            newSnakes.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A snake can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && canAnimalBreed();
    }
}
