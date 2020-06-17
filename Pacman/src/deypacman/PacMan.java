/*
Eshika Dey
PacMan Class, superclass: AbstractCharacter
 */
package deypacman;

import java.net.URL;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PacMan extends AbstractCharacter{
    //indicates whether pacMan is alive or has been eaten by ghosts
    private boolean alive;
    
    //constructor calls upon a superclass constructor & assigns "alive" to the default value of true
    public PacMan(JPanel character, JLabel characterPic, String direction, URL iconFileUp, URL iconFileDown, URL iconFileLeft, URL iconFileRight){
        super(character, characterPic, direction, iconFileUp, iconFileDown, iconFileLeft, iconFileRight);
        alive = true;
    }
    //constructor for if user doesn't insert direction and files, also calls upon a superclass constructor
    public PacMan(JPanel character, JLabel characterPic){
        super(character, characterPic);
        alive = true;
    }
    
    //setter and getter for "alive" boolean
    public void setAlive(boolean deadFlag){
        alive = deadFlag;
    }
    public boolean getAlive(){
        return alive;
    }
}
