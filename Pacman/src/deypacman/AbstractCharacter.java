/*
Eshika Dey
Super class of all characters (subclasses: PacMan Class & Ghost Class)
 */
package deypacman;

import java.awt.Rectangle;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.net.URL;


abstract public class AbstractCharacter {
    protected JPanel character; 
    protected JLabel characterPic;
    //file path of character's image in all 4 directions
    protected URL iconFileUp;
    protected URL iconFileDown;
    protected URL iconFileLeft;
    protected URL iconFileRight;
    protected String direction;
    protected int xPos;
    protected int yPos;
    
    //constructor without the image files & direction (also, character must have a corresponding JPanel & JLabel to exist as a character)
    public AbstractCharacter(JPanel character, JLabel characterPic){
        this.character = character;
        this.characterPic = characterPic;
        xPos = character.getLocation().x;
        yPos = character.getLocation().y;
        direction = "";
    }
    //constructor with the image files  & direction (also, character must have a corresponding JPanel & JLabel to exist as a character)
    public AbstractCharacter(JPanel character, JLabel characterPic, String direction, URL iconFileUp, URL iconFileDown, URL iconFileLeft, URL iconFileRight){
        this(character, characterPic);
        this.direction = direction;
        this.iconFileUp = iconFileUp;
        this.iconFileDown = iconFileDown;
        this.iconFileLeft = iconFileLeft;
        this.iconFileRight = iconFileRight;
        //character is default facing right
        setCharacterIcon(iconFileRight);
    }
    
    //only getters for character & characterPic
    public JPanel getCharacter(){
        return character;
    }
    public JLabel getCharacterPic(){
        return characterPic;
    }
    
    //Move the character in the direction specified (comments for "moveUp" method apply to all 4 move methods)
    public void moveUp(JPanel[] walls){
        //change which direction the character's picture is facing
        setCharacterIcon(iconFileUp);
        
        //move character up by a certain amount
        character.setLocation(xPos, yPos - 10);
        //update position of character
        setYPos(yPos - 10);
        
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
        
        character.setLocation(xPos - 10, yPos);
        setXPos(xPos - 10);
        
        ifWallHit(walls);
    }
    public void moveRight(JPanel[] walls){
        setCharacterIcon(iconFileRight);
        
        character.setLocation(xPos + 10, yPos);
        setXPos(xPos + 10);
        
        ifWallHit(walls);
    }
    public void stop(){
        setCharacterIcon(LevelMap.class.getResource("blackSquare.png"));
    }
    
    //change the character's icon (it is based on which direction the character is traveling)
    public void setCharacterIcon(URL filePath){
        characterPic.setIcon(new ImageIcon(filePath));
    }
    
    //check if user hit a wall, if they did, undo their previous movement
    public void ifWallHit(JPanel[] walls){
        
        Rectangle characterBounds = character.getBounds();
        
        //checking all the walls to see if the cloned character intersects with it
        for (int i = 0; i < walls.length; i++){
            Rectangle wallBounds = walls[i].getBounds();

            //check if character has collided with a wall
            if (characterBounds.intersects(wallBounds)) {
                //if so, undo their previous movement (previous movement is based on the direction they are traveling in)
                switch(direction){
                    case "up":
                        character.setLocation(xPos, yPos + 10);
                        setYPos(yPos + 10);
                        break;
                    case "down":
                        character.setLocation(xPos, yPos - 10);
                        setYPos(yPos - 10);
                        break;
                    case "left":
                        //personally, IDK why this is "20" and all the others are "10", but this is the only way it works
                        character.setLocation(xPos + 20, yPos);
                        setXPos(xPos + 20);
                    case "right":
                        character.setLocation(xPos - 10, yPos);
                        setXPos(xPos - 10);
                }
            }
        }
        
    }
    
    //setters and getters for the image files of the character in all 4 directions
    public void setIconFileUp(URL filePath){
        iconFileUp = filePath;
    }
    public URL getIconFileUp(){
        return iconFileUp;
    }
    public void setIconFileDown(URL filePath){
        iconFileDown = filePath;
    }
    public URL getIconFileDown(){
        return iconFileDown;
    }
    public void setIconFileLeft(URL filePath){
        iconFileLeft = filePath;
    }
    public URL getIconFileLeft(){
        return iconFileLeft;
    }
    public void setIconFileRight(URL filePath){
        iconFileRight = filePath;
    }
    public URL getIconFileRight(){
        return iconFileRight;
    }
    
    //setter and getter for direction character is traveling in
    public void setDirection(String direction){
        this.direction = direction;
    }
    public String getDirection(){
        return direction;
    }
    
    //Setters and getters for x and y position of character
    public void setXPos(int pos){
        character.setLocation(pos, yPos);
        xPos = pos;
    }
    public int getXPos(){
        return xPos;
    }
    public void setYPos(int pos){
        character.setLocation(xPos, pos);
        yPos = pos;
    }
    public int getYPos(){
        return yPos;
    }
    
}

