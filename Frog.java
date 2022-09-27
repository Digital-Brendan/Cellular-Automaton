import java.util.List;
import java.util.Random;

/**
 * A simple model of a frog.
 * Frogs age, move, breed, and die.
 */
public class Frog extends Animal
{
    // Characteristics shared by all frogs (class variables).

    // The age at which a frog can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a frog can live.
    private static final int MAX_AGE = 40;
    // The likelihood of a frog breeding.
    private static final double BREEDING_PROBABILITY = 0.35;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 5;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    // Amount of the day/night cycle at which the animal sleeps
    private static final double SLEEP_START_TIME = 0.7;
    // Duration of sleep
    private static final int SLEEP_LENGTH = 20;
    
    // Individual characteristics (instance fields).
    
    // The frog's age.
    private int age;

    /**
     * Create a new frog. A frog may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the frog will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param inheritsDisease Whether animal parent was infected
     */
    public Frog(boolean randomAge, Field field, Location location, boolean inheritsDisease)
    {
        super(field, location, inheritsDisease);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }
    
    /**
     * Determines the possible behaviour per simulation step. They can sleep, give birth, or move.
     * 
     * @param newFrogs A list to return newly born frogs.
     * @param cycleProgress Amount of day/night cycle completed by simulation
     */
    public void act(List<Animal> newFrogs, double cycleProgress)
    {
        // Become older and advance disease 
        incrementAge();
        diseaseTick();
        
        if(isAlive()) {
            // Sleep
            sleep(SLEEP_START_TIME, age, SLEEP_LENGTH, cycleProgress);
            
            if (!asleep) {
                giveBirth(newFrogs);      
                
                // Try to move into a free location.
                Location newLocation = getField().freeAdjacentLocation(getLocation());
                if(newLocation != null) {
                    setLocation(newLocation);
                }
                else {
                    // Overcrowding.
                    setDead();
                }
            }
        }
    }

    /**
     * Increase the age.
     * This could result in the frog's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this frog is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newFrogs A list to return newly born frogs.
     */
    private void giveBirth(List<Animal> newFrogs)
    {
        // New frogs are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        
        // Add offspring to free adjacent cells
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Frog young = new Frog(false, field, loc, isDiseased);
            newFrogs.add(young);
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
     * A frog can breed if it has reached the breeding age.
     * @return true if the frog can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && canAnimalBreed();
    }
}
