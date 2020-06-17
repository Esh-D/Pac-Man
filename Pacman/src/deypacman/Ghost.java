package deypacman;

/*
 * Prathiba Dhanesh
 * June 14th 2020
 * Ghost Class
 */

import java.awt.Rectangle;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Ghost extends AbstractCharacter {
    
    private boolean isFull; 
    private JPanel ghost;
    private JLabel ghostPic;   
    
    //constructor calls upon superclass's constructor & assigns "isFull" to the default value of false
    public Ghost(JPanel character, JLabel characterPic, String direction, URL iconFileUp, URL iconFileDown, URL iconFileLeft, URL iconFileRight){
        super(character, characterPic, direction, iconFileUp, iconFileDown, iconFileLeft, iconFileRight);
        isFull = false;
    }    
    
    //setters and getters
    public void setIsFull(boolean isFullFlag){
        this.isFull = isFullFlag;
    }
    
    public boolean getIsFull(){
        return isFull;
    }
    
    public boolean ghostIsFull(PacMan pac) {
        
        Rectangle characterBounds = (pac.getCharacter()).getBounds();
        Rectangle ghostBounds = (this.getCharacter()).getBounds();
        
        //check if ghost has collided with pacman
        if (characterBounds.intersects(ghostBounds)) {
            pac.setAlive(false);
            pac.setDirection("stop");
            this.isFull = true;
            return true;
        }
        return false;
        
    }
    
    //Move the character in the direction specified (comments for "moveUp" method apply to all 4 move methods)
    public void moveUp(JPanel[] walls){
        //change which direction the character's picture is facing
        setCharacterIcon(iconFileUp);
        
        //idk why it's 20, but its the only way it works
        //move character up by a certain amount
        character.setLocation(xPos, yPos - 20);
        //update position of character
        setYPos(yPos - 20);
        
        //override above movement if the player hits a wall
        ifWallHit(walls);
    }
    public void moveDown(JPanel[] walls){
        setCharacterIcon(iconFileDown);
        
        character.setLocation(xPos, yPos + 10);
        setYPos(yPos + 10);
        
        ifWallHit(walls);
    }
    public void moveLeft(JPanel[] walls){
        setCharacterIcon(iconFileLeft);
        
        //idk why it's 20, but its the only way it works
        character.setLocation(xPos - 20, yPos);
        setXPos(xPos - 20);
        
        ifWallHit(walls);
    }
    public void moveRight(JPanel[] walls){
        setCharacterIcon(iconFileRight);
        
        character.setLocation(xPos + 10, yPos);
        setXPos(xPos + 10);
        
        ifWallHit(walls);
    }
    
    //check if user hit a wall, if they did, undo their previous movement
    public void ifWallHit(JPanel[] walls){
        
        Rectangle characterBounds = character.getBounds();
        
        //checking all the walls to see if the character intersects with it
        for (int i = 0; i < walls.length; i++){
            Rectangle wallBounds = walls[i].getBounds();

            //check if character has collided with a wall
            if (characterBounds.intersects(wallBounds)) {
                //if so, undo their previous movement (previous movement is based on the direction they are traveling in)
                switch(direction){
                    case "up":
                        character.setLocation(xPos, yPos + 10);
                        setYPos(yPos + 10);
                        this.setDirection("down");
                        break;
                    case "down":
                        character.setLocation(xPos, yPos - 10);
                        setYPos(yPos - 10);
                        this.setDirection("up");
                        break;
                    case "left":
                        //personally, IDK why this is "20" and all the others are "10", but this is the only way it works
                        character.setLocation(xPos + 20, yPos);
                        setXPos(xPos + 20);
                        this.setDirection("right");
                        break;
                    case "right":
                        character.setLocation(xPos - 10, yPos);
                        setXPos(xPos - 10);
                        this.setDirection("left");
                        break;
                }
            }
        }
        
    }
    
    
}
