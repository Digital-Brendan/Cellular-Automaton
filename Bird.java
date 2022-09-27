import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a bird.
 * Birds age, move, eat frog, and die.
 */
public class Bird extends Animal
{
    // Characteristics shared by all birdes (class variables).
    
    // The age at which a bird can start to breed.
    private static final int BREEDING_AGE = 20;
    // The age to which a bird can live.
    private static final int MAX_AGE = 150;
    // The likelihood of a bird breeding.
    private static final double BREEDING_PROBABILITY = 0.55;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 10;
    // The food value of a single frog. In effect, this is the
    // number of steps a bird can go before it has to eat again.
    private static final int FROG_FOOD_VALUE = 11;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Amount of the day/night cycle at which the animal sleeps
    private static final double SLEEP_START_TIME = 0.6;
    // Duration of sleep
    private static final int SLEEP_LENGTH = 35;
    
    // Individual characteristics (instance fields).
    // The bird's age.
    private int age;
    // The bird's food level, which is increased by eating frogs.
    private int foodLevel;

    /**
     * Create a bird. A bird can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the bird will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param inheritsDisease Whether animal parent was infected
     */
    public Bird(boolean randomAge, Field field, Location location, boolean inheritsDisease)
    {
        super(field, location, inheritsDisease);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FROG_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = FROG_FOOD_VALUE;
        }
    }
    
    /**
     * Determines the possible behaviour per simulation step. They can sleep, give birth, eat, or move.
     * 
     * @param field The field currently occupied.
     * @param newBirds A list to return newly born birds
     * @param cycleProgress Amount of day/night cycle completed by simulation
     */
    public void act(List<Animal> newBirds, double cycleProgress)
    {
        // Become older and advance disease 
        incrementAge();
        diseaseTick();
        
        if(isAlive()) {
            // Sleep
            sleep(SLEEP_START_TIME, age, SLEEP_LENGTH, cycleProgress);
            
            if (!asleep) {
                giveBirth(newBirds);         
                
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
            }
            
            incrementHunger();
        }
    }

    /**
     * Increase the age. This could result in the bird's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this bird more hungry. This could result in the bird's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for frogs adjacent to the current location.
     * Only the first live frog is eaten.
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
            if(animal instanceof Frog) {
                Frog frog = (Frog) animal;
                if(frog.isAlive()) { 
                    frog.setDead();
                    foodLevel = FROG_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this bird is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newBirds A list to return newly born birdes.
     */
    private void giveBirth(List<Animal> newBirds)
    {
        // New birds are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        
        // Add offspring to adjacent cells if free
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Bird young = new Bird(false, field, loc, isDiseased);
            newBirds.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return births The number of births (may be zero).
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
     * A bird can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && canAnimalBreed();
    }
}
