import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing hedgehogs, snakes, coyotees, birds, and frogs.
 */
public class Simulator
{
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 120;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 70;
    // The number of ticks a "long" simulation should last.
    private static final int LONG_SIMULATION_LENGTH = 4000;
    // Delay applied to simulation
    private static final int delay = 15;
    
    // The probability that a coyote will be created in any given grid position.
    private static final double COYOTE_CREATION_PROBABILITY = 0.035;
    // The probability that a hedgehog will be created in any given grid position.
    private static final double HEDGEHOG_CREATION_PROBABILITY = 0.1;    
    // The probability that a snake will be created in any given grid position.
    private static final double SNAKE_CREATION_PROBABILITY = 0.03;  
    // The probability that a bird will be created in any given grid position.
    private static final double BIRD_CREATION_PROBABILITY = 0.04;
    // The probability that a FROG will be created in any given grid position.
    private static final double FROG_CREATION_PROBABILITY = 0.018;    
    
    // Tick length of day/night cycle
    private static final int DAY_NIGHT_CYCLE_LENGTH = 500;
    
    // Music to be played as background
    private static final String musicPath = "music.wav";
    
    // List of animals in the field.
    private List<Animal> animals;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    // Current time of day
    private int currentTime = 0;
    
    // Whether animals should be randomly introduced to ecosystem
    private boolean randomIntroduction = true;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        animals = new ArrayList<>();
        field = new Field(depth, width);
        
        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        
        // Assign each species a colour for the grid
        view.setColor(Hedgehog.class, Color.ORANGE);
        view.setColor(Coyote.class, Color.BLUE);
        view.setColor(Snake.class, Color.RED);
        view.setColor(Frog.class, Color.GREEN);
        view.setColor(Bird.class, Color.DARK_GRAY);
        
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period.
     * (4000 steps by default)
     */
    public void runLongSimulation()
    {
        simulate(LONG_SIMULATION_LENGTH);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        // Make music object to play background audio
        Music backgroundMusic = new Music(musicPath);
        
        // Repeatedly run simulation steps
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * animal.
     */
    public void simulateOneStep()
    {
        step++;
        
        // Keep track of time and percentage of time passed
        currentTime = (currentTime + 1) % DAY_NIGHT_CYCLE_LENGTH;
        double cycleProgress = (double) currentTime / DAY_NIGHT_CYCLE_LENGTH;
        
        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();  
        
        // Let all hedgehogs act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            animal.act(newAnimals, cycleProgress);
            if(!animal.isAlive()) {
                it.remove();
            }
        }
        
        // Add the newly born animals to the main lists.
        animals.addAll(newAnimals);
        
        // Update labels of simulation window
        view.showStatus(step, field, cycleProgress, DAY_NIGHT_CYCLE_LENGTH);
        
        // Possibly reintroduce new animals of each species
        animalIntroduction(cycleProgress);
        
        // Slows down simulation for more pleasant viewing
        delay(delay);
    }
    
    /**
     * Randomly introduces animals from outside visible ecosystem to encourage reproduction
     * and predator/prey competition. Can be disabled - enabled by default.
     * 
     * CHALLENGE TASK
     * 
     * @param cycleProgress Amount of day/night cycle passed already
     */
    private void animalIntroduction(double cycleProgress)
    {
        // After what amount of day can animals be introduced
        double introductionThreshold = 0.2;
        
        // Number of times to possibly add new animals
        int possibleIntroductions = 20;
        
        if(cycleProgress > introductionThreshold && randomIntroduction){
            Random random = Randomizer.getRandom();
            for (int i = 0; i < possibleIntroductions; i++){
                // Find a random location on grid
                Location location = new Location(random.nextInt(field.getDepth()), random.nextInt(field.getWidth()));
                Animal animal = null;
                
                // Decide which animal to add based on probabilities
                if(random.nextDouble() <= COYOTE_CREATION_PROBABILITY) { animal = new Coyote(true, field, location, false); }
                
                else if (random.nextDouble() <= HEDGEHOG_CREATION_PROBABILITY) { animal = new Hedgehog(true, field, location, false); }
                
                else if(random.nextDouble() <= SNAKE_CREATION_PROBABILITY) { animal = new Snake(true, field, location, false); }
                
                else if(random.nextDouble() <= BIRD_CREATION_PROBABILITY) { animal = new Bird(true, field, location, false); }
                
                else if(random.nextDouble() <= FROG_CREATION_PROBABILITY) { animal = new Frog(true, field, location, false); }
                
                // If animal exists, add it to grid
                if (animal != null){
                    animals.add(animal);
                }
            }
        }
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        // Reset counters
        step = 0;
        currentTime = 0;
        
        // Repopulate grid
        animals.clear();
        populate();
        
        // Show the starting state in the view.
        view.showStatus(step, field, (double) currentTime / DAY_NIGHT_CYCLE_LENGTH, DAY_NIGHT_CYCLE_LENGTH);
    }
    
    /**
     * Randomly populate the field with animals.
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        
        // Navigate through grid and randomly select an animal to place in it 
        // (may not add animal to every cell)
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                Location location = new Location(row, col);
                Animal animal = null;
                
                // Create appropriate animal (hasn't inherited disease to begin with)
                if(rand.nextDouble() <= COYOTE_CREATION_PROBABILITY) {
                    animal = new Coyote(true, field, location, false); 
                }
                else if (rand.nextDouble() <= HEDGEHOG_CREATION_PROBABILITY) {
                    animal = new Hedgehog(true, field, location, false);
                }
                else if(rand.nextDouble() <= SNAKE_CREATION_PROBABILITY) {
                    animal = new Snake(true, field, location, false);
                }
                else if(rand.nextDouble() <= BIRD_CREATION_PROBABILITY) {
                    animal = new Bird(true, field, location, false);
                }
                else if(rand.nextDouble() <= FROG_CREATION_PROBABILITY) {
                    animal = new Frog(true, field, location, false);
                }
                
                // If animal exists, add it to collection
                if (animal != null){
                    animals.add(animal);
                }
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
}
