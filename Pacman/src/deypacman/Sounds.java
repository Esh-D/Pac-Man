package deypacman;


import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.InputStream;

/*
 * Prathiba Dhanesh
 * June 14th 2020
 * This is a class to handle the sound effects/music that will be played throughout
 * the game.
 */

public class Sounds {
    
    public void playMusic(String musicLoc) {
        
        try {
            
            File musicPath = new File(musicLoc);
            
            if (musicPath.exists()) {
                
                //instantiate object to transfer music file to program
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(musicPath);
                Clip clip = AudioSystem.getClip();//use clip class to play music
                clip.open(audioInput); 
                clip.start();//starts to play audio
                //clip.loop(Clip.LOOP_CONTINUOUSLY); //loops music
                
            } else {
                System.out.println("Can't find file");
            }
            
        }
        catch(Exception ex) {
            ex.printStackTrace();//prints out error message
        }       
    }
    
    
}
