import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

/**
 * Plays background music while simulation runs.
 */
public class Music
{
    /**
     * Constructor for Music class.
     * @param file Music file to be played.
     */
    public Music(String file){
        playMusic(file);
    }
    
    /**
     * Plays music file from provided path
     * 
     * @param fileLocation File to open and loop.
     */
    private void playMusic(String fileLocation){
        try {
            File musicPath = new File(fileLocation);
            
            if (musicPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                
                // Play and loop
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                JOptionPane.showMessageDialog(null, "Music not found");
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
