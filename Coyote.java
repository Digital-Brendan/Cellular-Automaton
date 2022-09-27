import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a coyote.
 * Coyotees age, move, eat hedgehog, and die.
 * 
 */
public class Coyote extends Animal
{
    // Characteristics shared by all coyotees (class variables).
    
    // The age at which a coyote can start to breed.
    private static final int BREEDING_AGE = 15;
    // The age to which a coyote can live.
    private static final int MAX_AGE = 150;
    // The likelihood of a coyote breeding.
    private static final double BREEDING_PROBABILITY = 0.27;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 400;
    // The food value of a single hedgehog. In effect, this is the
    // number of steps a coyote can go before it has to eat again.
    private static final int HEDGEHOG_FOOD_VALUE = 9;
    private static final int BIRD_FOOD_VALUE = 11;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Amount of the day/night cycle at which the animal sleeps
    private static final double SLEEP_START_TIME = 0.3;
    // Duration of sleep
    private static final int SLEEP_LENGTH = 40;
    
    // Individual characteristics (instance fields).
    // The coyote's age.
    private int age;
    // The coyote's food level, which is increased by eating hedgehogs.
    private int foodLevel;

    /**
     * Create a coyote. A coyote can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the coyote will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param inheritsDisease Whether animal parent was infected
     */
    public Coyote(boolean randomAge, Field field, Location location, boolean inheritsDisease)
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
     * Determines the possible behaviour per simulation step. They can sleep, give birth, eat, or move.
     * 
     * @param field The field currently occupied.
     * @param newCoyotees A list to return newly born coyotees.
     * @param cycleProgress Amount of day/night cycle completed by simulation
     */
    public void act(List<Animal> newCoyotees, double cycleProgress)
    {
        // Become older and advance disease 
        incrementAge();
        diseaseTick();
        
        if(isAlive()) {
            // Sleep
            sleep(SLEEP_START_TIME, age, SLEEP_LENGTH, cycleProgress);
            
            if (!asleep) {
                giveBirth(newCoyotees);            
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
     * Increase the age. This could result in the coyote's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this coyote more hungry. This could result in the coyote's death.
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
     * Only the first live prey is eaten.
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
            
            // Only eat compatible prey: hedgehogs or birds
            if(animal instanceof Hedgehog) {
                Hedgehog hedgehog = (Hedgehog) animal;
                if(hedgehog.isAlive()) { 
                    hedgehog.setDead();
                    if(foodLevel < HEDGEHOG_FOOD_VALUE)
                        foodLevel = HEDGEHOG_FOOD_VALUE;
                    return where;
                }
            }
            else if(animal instanceof Bird) {
                Bird bird = (Bird) animal;
                if(bird.isAlive()) { 
                    bird.setDead();
                    foodLevel = BIRD_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
    
    /**
     * Check whether or not this coyote is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newCoyotees A list to return newly born coyotees.
     */
    private void giveBirth(List<Animal> newCoyotees)
    {
        // New coyotees are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        
        // Add offspring to free adjacent cells
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Coyote young = new Coyote(false, field, loc, isDiseased);
            newCoyotees.add(young);
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
     * A coyote can breed if it has reached the breeding age.
     * @return true if the coyote can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && canAnimalBreed();
    }
}
