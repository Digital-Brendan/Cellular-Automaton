import java.util.List;
import java.util.Random;

/**
 * A class representing shared characteristics of animals.
 */
public abstract class Animal
{
    // How long an animal is diseased for
    private static final int DISEASE_LENGTH = 20;
    // Probability of randomly becoming diseased
    private static final double DISEASE_PROB_RANDOM = 0.0002;
    // Probability of death per tick from disease
    private static final double DISEASE_DEATH_PROB = 0.20;
    // Probability of transmitting disease to an animal nearby
    private static final double DISEASE_TRANSMISSION_PROB = 0.04;
    // Probability of offspring having disease once born
    private static final double DISEASE_NEW_OFFSPRING_PROB = 0.75;
    
    // The threshold for the cycleProgress value when compared to the sleep start time.
    private static final double SLEEP_THRESHOLD = 0.05;
    
    // Whether the animal is alive or not.
    private boolean alive;
    // The animal's field.
    private Field field;
    // The animal's position in the field.
    private Location location;
    
    // Whether the animal has breeded this act or not.
    //private boolean hasBreeded;
    // The animal's sex.
    private String sex;
    
    // If animal is diseased
    protected boolean isDiseased;
    // Steps remaining until animal is not diseased
    protected int diseasedLevel;

    // If animal is asleep
    protected boolean asleep = false;
    // Age animal will be post-sleep
    protected int wakeUpAge = 0;
    
    /**
     * Create a new animal at location in field.
     * 
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param inheritsDisease Whether or not parent was diseased
     */
    public Animal(Field field, Location location, boolean inheritsDisease)
    {
        alive = true;
        this.field = field;
        setLocation(location);
        
        // Assign each animal as male or female
        sex = assignRandomSex();
        
        // Disease inheritance from parent
        Random rand = Randomizer.getRandom();
        isDiseased = inheritsDisease && (rand.nextDouble() <= DISEASE_NEW_OFFSPRING_PROB);
        diseasedLevel = (isDiseased ? DISEASE_LENGTH : 0);
    }
    
    /**
     * Checks whether animal currently is diseased
     * @return Whether diseased or not
     */
    public boolean isDiseased()
    {
        return isDiseased;
    }
    
    /**
     * Give animal disease for specific number of steps
     */
    public void giveDisease()
    {
        isDiseased = true;
        diseasedLevel = DISEASE_LENGTH;
    }
    
    /**
     * Deals with disease: transmission, random infection, disease end, and death
     * 
     * CHALLENGE TASK
     */
    protected void diseaseTick()
    {
        Random rand = new Random();
        // If already diseased, transmit to nearby
        if (isDiseased && alive) {
            List<Location> locationsNearby = field.adjacentLocations(location);
            for (Location nearby : locationsNearby) {
                Object o = field.getObjectAt(nearby);
                
                // If animal exists nearby and within probability to transmit, infect animal
                if (o != null && o instanceof Animal && rand.nextDouble() <= DISEASE_TRANSMISSION_PROB) {
                    Animal animal = (Animal)o;
                    animal.giveDisease();
                }
            }
        }
        
        // Randomly infect current animal
        if (!isDiseased && rand.nextDouble() <= DISEASE_PROB_RANDOM){
            giveDisease();
        }
        
        // If no longer infected, stop
        if (diseasedLevel == 0) {
            isDiseased = false;
            return;
        }
        
        // Become less infected
        diseasedLevel--;
        if (rand.nextDouble() <= DISEASE_DEATH_PROB){
            setDead();
        }
    }
    /**
     * Make this animal act - that is: make it do
     * whatever it wants/needs to do.
     * 
     * @param newAnimals A list to receive newly born animals.
     * @param cycleProgress How far through day/night cycle the simulation is
     */
    abstract public void act(List<Animal> newAnimals, double cycleProgress);
    
    /**
     * Handle animal sleep. Calculate changes while resting.
     * 
     * @param sleepTime Time of day that animal sleeps
     * @param currentAge Current age of animal
     * @param sleepLength How long animal sleeps
     * @param cycleProgress How far through day/night cycle the simulation is
     */
    protected void sleep(double sleepTime, int currentAge, int sleepLength, double cycleProgress)
    {
        // Begin sleeping if not asleep and correct time of day
        if (!asleep && (cycleProgress >= sleepTime) && (cycleProgress <= sleepTime + SLEEP_THRESHOLD)) {
            asleep = true;
            wakeUpAge = currentAge + sleepLength;
        }
        
        // Stop sleeping
        if (asleep && (currentAge == wakeUpAge)) {
            asleep = false;
        }
    }
    
    /**
     * Check whether the animal is alive or not.
     * @return true if the animal is still alive.
     */
    protected boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the animal is no longer alive.
     * It is removed from the field.
     */
    protected void setDead()
    {
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
    protected Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the animal at the new location in the given field.
     * @param newLocation The animal's new location.
     */
    protected void setLocation(Location newLocation)
    {
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
    protected Field getField()
    {
        return field;
    }
    
    /**
     * Picks a random sex for the animal.
     *
     * @return The randomly chosen sex (M: male, F: female)
     */
    private String assignRandomSex()
    {
        Random random = Randomizer.getRandom();
        if(random.nextInt(2) == 1)
        {
            return "M";
        }
        return "F";
    }

    /**
     * Accessor method for the animal's sex
     *
     * @return The animal's sex.
     */
    protected String getSex()
    {
        return sex;
    }
    
    /**
     * Checks if perfect breeding conditions are present for the animal and its mate.
     *
     * @return Wether animal is currently ready to breed
     */
    protected boolean canAnimalBreed()
    {
        List<Location> all = field.adjacentLocations(getLocation());
        for (Location loc : all)
        {
            Object otherAnimal = field.getObjectAt(loc);
            
            // If adjacent animal exists and is same species 
            if (otherAnimal != null && otherAnimal.getClass().equals(this.getClass()) && match((Animal) otherAnimal))
            {
                return true;
            }
        }   
        return false;
    }

    /**
     * Helper method to check if further breeding conditions are met.
     *
     * @param otherAnimal The mate's object reference.
     * @return Wether both animals 
     */
    private boolean match(Animal otherAnimal)
    {
        return ( !otherAnimal.getSex().equals(getSex()) );
    }
}
