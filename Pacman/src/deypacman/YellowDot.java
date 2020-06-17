/*
YellowDot Class
 */
package deypacman;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;

public class YellowDot {
    private JPanel dot;
    private JLabel dotPic;
    private boolean eaten;
    private int xPos;
    private int yPos;
    
    //Constructor with just JPanel and JLabel (dot must have these in order to exist as a YellowDot)
    public YellowDot(JPanel dot, JLabel dotPic){
        this.dot = dot;
        this.dotPic = dotPic;
        xPos = dot.getLocation().x;
        yPos = dot.getLocation().y;
        eaten = false;
    }
    //constructor that assigns a specific value to eaten (along side the default JPanel & JLabel) 
    public YellowDot(JPanel dot, JLabel dotPic, boolean eaten){
        this(dot, dotPic);
        this.eaten = eaten;
    }
    
    //getter and setter for whether or not the dot has been "eaten"
    public boolean getEaten(){
        return eaten;
    }
    public void setEaten(boolean isEatenFlag){
        eaten = isEatenFlag;
        //erase the dot if it has been eaten
        if (eaten){
            dotPic.setIcon(new ImageIcon("src\\deypacman\\blackSquare.png"));
        }
        //if it hasn't been eaten, display the dot
        else{
            dotPic.setIcon(new ImageIcon("src\\deypacman\\yellowDot.png"));
        }
    }
    
    //getters for x and y position
    //no setters for x & y position, since the dots position never changes
    public int getXPos(){
        return xPos;
    }
    public int getYPos(){
        return yPos;
    }

    
}
