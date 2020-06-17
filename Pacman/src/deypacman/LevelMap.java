/*
LevelMap Frame (where the gameplay occurs)
*/

package deypacman;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;

public class LevelMap extends javax.swing.JFrame {
    End endMenu;
    //String will be displayed on the endMenu, it tells the user the outcome of the game (outcome can be "You Win!" or "Game Over!")
    String outcome;
    
    MainMenu mainMenu;
    
    int dotCount = 123;
    int wallCount = 47;
    YellowDot dots[] = new YellowDot[dotCount];
    JPanel walls[] = new JPanel[wallCount];
    
    ArrayList<String> names = new ArrayList();
    String name = "";
    ArrayList<Integer> scores = new ArrayList();
    int score = 0;
    
    Thread character;
    Object obj = new Object();
    
    //declaring PacMan Object
    PacMan pacMan;
    
    //Thread ghost;
    //Object obj1 = new Object();
    
    //declaring Ghost objects 
    Ghost ghost1;
    Ghost ghost2;
    Ghost ghost3;
    Ghost ghost4;
    Ghost ghost5;
    Ghost ghost6;
    
    //sounds to use throughout the level map and when game over
    String filepath2 = System.getProperty("user.dir") + "/sounds/pacman_chomp.wav";
    Sounds chompMusic = new Sounds(); //declare chomp music object

    String filepath3 = System.getProperty("user.dir") + "/sounds/pacman_death.wav";
    Sounds deathMusic = new Sounds(); //declare death music object    
    
    public LevelMap(MainMenu m1) {
        initComponents();
        this.getContentPane().setBackground(Color.BLACK);
        mainMenu = m1;
        
        //3rd parameter is an empty string because PacMan isn't moving at the beginning of the game (so direction is set to "")
        //pacMan object instantiated
        pacMan = new PacMan(PacMan, PacManPic, "", LevelMap.class.getResource("pacmanUp.png"), LevelMap.class.getResource("pacmanDown.png"), LevelMap.class.getResource("pacmanLeft.png"), LevelMap.class.getResource("pacmanRight.png"));
        
        //ghosts are moing at the beginning of the game so 3rd param is not empty.
        //ghost objects are instantiated
        ghost1 = new Ghost(Ghost1,GhostPic1,"left", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        ghost2 = new Ghost(Ghost2,GhostPic2,"down", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        ghost3 = new Ghost(Ghost3,GhostPic3,"right", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        ghost4 = new Ghost(Ghost4,GhostPic4,"left", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        ghost5 = new Ghost(Ghost5,GhostPic5,"up", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        ghost6 = new Ghost(Ghost6,GhostPic6,"right", LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"), LevelMap.class.getResource("ghostLeft.png"), LevelMap.class.getResource("ghostRight.png"));
        
        //the method instantiates all YellowDot objects and puts them in the dots array
        populateDotArray();
        //the method puts all Wall panels in the walls array
        populateWallArray();
        
        //read in name & score information from the data file and insert them accordingly into the scores ArrayList & names ArrayList
        populateNamesAndScores();
        //sort the ArrayLists in decending order (so the top 5 high scores can be found in the 0-4 indexes)
        sortNamesAndScores(0, scores.size() - 1);
        
        //This method contains a thread, it also runs the thread
        characterThread();
        
    }
    //resetting the game when the user wants to Play Again (method is called from the "End" Frame)
    public void gameReset(){
        //reseting the score to 0
        score = 0;
        //displaying score feature was removed because it was causing a glitch in the pacman and ghost movement
        //lblScore.setText("Score: " + score);
        
        //reseting pacMan's position, direction, and icon
        pacMan.setXPos(240);
        pacMan.setYPos(200);
        pacMan.setDirection("");
        pacMan.setCharacterIcon(pacMan.getIconFileRight());
        
        //reseting the states of the dots
        for (int i = 0; i < dots.length; i++){
            dots[i].setEaten(false);
        }

        ghost1.setIsFull(false);
        ghost2.setIsFull(false);
        ghost3.setIsFull(false);
        ghost4.setIsFull(false);
        ghost5.setIsFull(false);
        ghost6.setIsFull(false);
        
        try {
            //unpause the pac thread
            synchronized(obj){
                obj.notify();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    //"character" Thread starts running here (character refers to pacman and the ghosts)
    public void characterThread(){
        
        character = new Thread(new Runnable(){
            
            public void run(){
                //"synchronized(obj)" allows the program to be paused & unpaused in different parts of the program as long as the command is nested within a "synchronized(obj)" statement
                //paused & unpaused refers to "obj.wait()" & "obj.notify()" (found in win() method and gameReset() method)
                synchronized(obj){
                    while(true){
                        //pacMan's constant moving feature
                        pacMotion();
                        //checking if pac man has eaten any dots / eaten all the dots
                        //alters the game accordingly
                        eatDot();
                        
                        //ghosts' constant moving feature
                        ghostMotion();
                        //checking if ghost has eaten pac-man
                        //alters the game accordingly
                        eatPacMan();
                    }
                }
            }
        });
        
        character.start();
    }
    
    //All the methods in this chunk related to the pacman character
    //pacMan's continuous motion feature
    public void pacMotion(){
        //pacMan pauses after every movement for the amount of milliseconds specified
        //below sets how fast pacMan travels
        try{
            Thread.sleep(50);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error has occured: " + e);
        }
        
        //program figures out which direction pacMan should move based on the direction it is pointing in, and then moves it 
        //pacMan's direction is controlled by the player's arrow key presses (see formKeyPressed(java.awt.event.KeyEvent evt) for more info)
        switch (pacMan.getDirection()){
                case "up":
                    //walls array is being passed because pacMan's move methods implement wall detection (so pacMan doesn't move through walls)
                    pacMan.moveUp(walls);
                    break;
                case "down":
                    pacMan.moveDown(walls);
                    break;
                case "left":
                    pacMan.moveLeft(walls);
                    break;
                case "right":
                    pacMan.moveRight(walls);
                    break;
                case "stop":
                    pacMan.stop();
        }
    }
    public void eatDot(){
        //dot eating feature:
        //check if pacMan has run into any of the remaining dots, if so, pacMan ate the dot
        for (int i = 0; i < dots.length; i++){
            //pacMan "running into" a dot means their locations overlap and the dot has not yet been eaten
            if(pacMan.getXPos() == dots[i].getXPos() && pacMan.getYPos() == dots[i].getYPos() && dots[i].getEaten() == false){
                chompMusic.playMusic(filepath2);
                //change state of dot & change it's icon
                dots[i].setEaten(true);
                //add to score
                score += 100;
                //the below line of code causes a glitch, so it was taken out (commented out)
                //lblScore.setText("Score: " + score);
                
                //if all the yellow dots are eaten, player wins and game ends
                if (score == 12300){
                    outcome = "You Win!";
                    end();
                }
            }
        }
    }
    
    //All the methods in this chunk related to the ghost characters
    //ghosts's continuous motion feature
    public void ghostMotion() {
        //ghost pauses after every movement for the amount of milliseconds specified
        //below sets how fast pacMan travels
        try{
            Thread.sleep(50);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error has occured: " + e);
        }
        
        //program moves the ghosts according to their direction
        //when a ghost hits a wall it starts traveling in the opposite direction
        switch(ghost1.getDirection()){
            case "left":
                ghost1.moveLeft(walls);
            case "right":
                ghost1.moveRight(walls);  
        }
        switch(ghost2.getDirection()){
            case "up":
                ghost2.moveUp(walls);
            case "down":
                ghost2.moveDown(walls);
        }
        switch(ghost3.getDirection()){
            case "left":
                ghost3.moveLeft(walls);
            case "right":
                ghost3.moveRight(walls);  
        }
        switch(ghost4.getDirection()){
            case "left":
                ghost4.moveLeft(walls);
            case "right":
                ghost4.moveRight(walls);  
        }
        switch(ghost5.getDirection()){
            case "up":
                ghost5.moveUp(walls);
            case "down":
                ghost5.moveDown(walls);
        }
        switch(ghost6.getDirection()){
            case "left":
                ghost6.moveLeft(walls);
            case "right":
                ghost6.moveRight(walls);  
        }
     
    }
    public void eatPacMan() {
        //if any ghostIsFull method returns true, then play death music and call lose method
        if (ghost1.ghostIsFull(pacMan) || ghost2.ghostIsFull(pacMan) || ghost3.ghostIsFull(pacMan)
                || ghost4.ghostIsFull(pacMan) || ghost5.ghostIsFull(pacMan) || ghost6.ghostIsFull(pacMan)) {
            deathMusic.playMusic(filepath3);
            outcome = "Game Over!";
            end();
        }

    }
    //what happens when the player wins/loses
    public void end(){
        //asking user for their name/nickname
        name = JOptionPane.showInputDialog("Enter a nickname.");
        //make sure player doesn't leave the field empty
        while (name == ""){
            name = JOptionPane.showInputDialog("Enter a nickname.");
        }
        
        //writing name & score to datafile
        writeNameAndScore();
        
        //search through scores ArrayList and insert the new score in correct index relative to the other numbers
        //also insert name in the same index in the names ArrayList
        insertNamesAndScores();
        
        //display gameover/win menu
        loadEndMenu();
        
        try {
            //pause the pac thread
            synchronized(obj){
                obj.wait();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
    }
    
    //All the methods in this chunk are related to the names & scores ArrayLists 
    //read names & scores from data file and insert them into their respective ArrayLists
    public void populateNamesAndScores(){
        //counter
        int i = 0;
        
        try{
            FileInputStream in = new FileInputStream(System.getProperty("user.dir") + "/saves/scores.txt");
            Scanner s = new Scanner(in);
            //continue until the end of the file
            while (s.hasNextLine()){
                //first field is the name
                names.add(s.nextLine());
                //second field is the person's score
                scores.add(Integer.parseInt(s.nextLine()));
                //increase counter
                i++;
            }
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }
    }
    //sort the scores ArrayList in decending order (alter the names ArrayList accordingly)
    public void sortNamesAndScores(int left, int right){
        //Sorting Algorithm: Quick Sort
        //any changes done to the scores array should be done to the names array (each index should correspond to a specific person)
        //sorting the scores ArrayList in DECENDING order
        
        //when the left and right indexes overlap: end the recursion, the 
        if (left >= right){
            return;
        }
        //pivot will be the middle-most index between the left and right indexes
        int pivot = scores.get((left + right) / 2);
        
        //left selector
        int l = left;
        //right selector
        int r = right;
        
        //repeat until left and right selector land on the same 
        while (l < r){
            //keep moving the left selector to the right until it lands on a number that is more than the pivot
            while (scores.get(l) > pivot){
                l++;
            }
            //keep moving the right selector to the left until it lands on a number that is less than the pivot
            while (pivot > scores.get(r)){
                r--;
            }
            //if the left selector is to the left of or overlapping with the right selector do the below:
            if (l <= r){
                //swap the values (value at l index swaps with r index)
                int sTemp = scores.get(l);
                scores.set(l, scores.get(r));
                scores.set(r, sTemp);
                //same changes should be made to the names array 
                String nTemp = names.get(l);
                names.set(l, names.get(r));
                names.set(r, nTemp);
                //more left selector one to the right, and the right selector one to the left (in prep for the recursive calls below)
                l++;
                r--;
            }
        }
        
        //recursively call the method for the partition to the left of the pivot
        sortNamesAndScores(left, r);
        //recursively call the method for the partition to the right of the pivot
        sortNamesAndScores(l, right);
    }
    //inserts the players new score & name into the names & scores ArrayLists relative to the other elements 
    public void insertNamesAndScores(){
        //scores ArrayList is in decending order
        //check each item in the ArrayList and check if the score belongs in that index (is the score larger than the number currently at that index?)
        //Whatever you change in the scores ArrayList, must be changed in the names ArrayList (each index represents 1 person (and they have a name and a score)
        //when the score and names are inserted, end the method run
        for (int i = 0; i < scores.size(); i++){
            if (score > scores.get(i)){
                scores.add(i, score);
                names.add(i, name);
                return;
            }
        }
        //if score is the least out of all the scores in the ArrayList, add it on to the end
        scores.add(score);
        names.add(name);
    }
    //write the player's new score & name into the data file
    public void writeNameAndScore(){
        //Got below code from: https://www.journaldev.com/878/java-write-to-file#:~:text=FileWriter%3A%20FileWriter%20is%20the%20simplest,number%20of%20writes%20is%20less.
        FileWriter fr = null;
        try {
            //FileOutputStream out = new FileOutputStream(System.getProperty("user.dir") + "/saves/scores.txt");
            //instantiating a FileWriter object, inputing the file & indicating we are amending/adding on to the file (that's what the "true" parameter does)
            fr = new FileWriter(System.getProperty("user.dir") + "/saves/scores.txt", true);
            //write the player's nickname & score on the next 2 consecutive lines of the datafile
            fr.write(name + "\n");
            fr.write(score + "\n");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error: " + e);
        }finally{
            //closing the file
            try {
                fr.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e);
            }
        }
    }
    
    //instantiate YellowDot objects and insert them into the dots array
    public void populateDotArray(){
        
        /*
        //Printed out the below text and copy-pasted from terminal to save myself time (so I didn't have to type out all of the above)
        for (int i = 4; i <= 126; i++){
            System.out.println("YellowDot y" + i +" = new YellowDot(Dot" + i + ", DotPic" + i + ");");
            System.out.println("dots[" + (i-4) + "] = y" + i + ";");
        }
        */
        
        YellowDot y4 = new YellowDot(Dot4, DotPic4);
        dots[0] = y4;
        YellowDot y5 = new YellowDot(Dot5, DotPic5);
        dots[1] = y5;
        YellowDot y6 = new YellowDot(Dot6, DotPic6);
        dots[2] = y6;
        YellowDot y7 = new YellowDot(Dot7, DotPic7);
        dots[3] = y7;
        YellowDot y8 = new YellowDot(Dot8, DotPic8);
        dots[4] = y8;
        YellowDot y9 = new YellowDot(Dot9, DotPic9);
        dots[5] = y9;
        YellowDot y10 = new YellowDot(Dot10, DotPic10);
        dots[6] = y10;
        YellowDot y11 = new YellowDot(Dot11, DotPic11);
        dots[7] = y11;
        YellowDot y12 = new YellowDot(Dot12, DotPic12);
        dots[8] = y12;
        YellowDot y13 = new YellowDot(Dot13, DotPic13);
        dots[9] = y13;
        YellowDot y14 = new YellowDot(Dot14, DotPic14);
        dots[10] = y14;
        YellowDot y15 = new YellowDot(Dot15, DotPic15);
        dots[11] = y15;
        YellowDot y16 = new YellowDot(Dot16, DotPic16);
        dots[12] = y16;
        YellowDot y17 = new YellowDot(Dot17, DotPic17);
        dots[13] = y17;
        YellowDot y18 = new YellowDot(Dot18, DotPic18);
        dots[14] = y18;
        YellowDot y19 = new YellowDot(Dot19, DotPic19);
        dots[15] = y19;
        YellowDot y20 = new YellowDot(Dot20, DotPic20);
        dots[16] = y20;
        YellowDot y21 = new YellowDot(Dot21, DotPic21);
        dots[17] = y21;
        YellowDot y22 = new YellowDot(Dot22, DotPic22);
        dots[18] = y22;
        YellowDot y23 = new YellowDot(Dot23, DotPic23);
        dots[19] = y23;
        YellowDot y24 = new YellowDot(Dot24, DotPic24);
        dots[20] = y24;
        YellowDot y25 = new YellowDot(Dot25, DotPic25);
        dots[21] = y25;
        YellowDot y26 = new YellowDot(Dot26, DotPic26);
        dots[22] = y26;
        YellowDot y27 = new YellowDot(Dot27, DotPic27);
        dots[23] = y27;
        YellowDot y28 = new YellowDot(Dot28, DotPic28);
        dots[24] = y28;
        YellowDot y29 = new YellowDot(Dot29, DotPic29);
        dots[25] = y29;
        YellowDot y30 = new YellowDot(Dot30, DotPic30);
        dots[26] = y30;
        YellowDot y31 = new YellowDot(Dot31, DotPic31);
        dots[27] = y31;
        YellowDot y32 = new YellowDot(Dot32, DotPic32);
        dots[28] = y32;
        YellowDot y33 = new YellowDot(Dot33, DotPic33);
        dots[29] = y33;
        YellowDot y34 = new YellowDot(Dot34, DotPic34);
        dots[30] = y34;
        YellowDot y35 = new YellowDot(Dot35, DotPic35);
        dots[31] = y35;
        YellowDot y36 = new YellowDot(Dot36, DotPic36);
        dots[32] = y36;
        YellowDot y37 = new YellowDot(Dot37, DotPic37);
        dots[33] = y37;
        YellowDot y38 = new YellowDot(Dot38, DotPic38);
        dots[34] = y38;
        YellowDot y39 = new YellowDot(Dot39, DotPic39);
        dots[35] = y39;
        YellowDot y40 = new YellowDot(Dot40, DotPic40);
        dots[36] = y40;
        YellowDot y41 = new YellowDot(Dot41, DotPic41);
        dots[37] = y41;
        YellowDot y42 = new YellowDot(Dot42, DotPic42);
        dots[38] = y42;
        YellowDot y43 = new YellowDot(Dot43, DotPic43);
        dots[39] = y43;
        YellowDot y44 = new YellowDot(Dot44, DotPic44);
        dots[40] = y44;
        YellowDot y45 = new YellowDot(Dot45, DotPic45);
        dots[41] = y45;
        YellowDot y46 = new YellowDot(Dot46, DotPic46);
        dots[42] = y46;
        YellowDot y47 = new YellowDot(Dot47, DotPic47);
        dots[43] = y47;
        YellowDot y48 = new YellowDot(Dot48, DotPic48);
        dots[44] = y48;
        YellowDot y49 = new YellowDot(Dot49, DotPic49);
        dots[45] = y49;
        YellowDot y50 = new YellowDot(Dot50, DotPic50);
        dots[46] = y50;
        YellowDot y51 = new YellowDot(Dot51, DotPic51);
        dots[47] = y51;
        YellowDot y52 = new YellowDot(Dot52, DotPic52);
        dots[48] = y52;
        YellowDot y53 = new YellowDot(Dot53, DotPic53);
        dots[49] = y53;
        YellowDot y54 = new YellowDot(Dot54, DotPic54);
        dots[50] = y54;
        YellowDot y55 = new YellowDot(Dot55, DotPic55);
        dots[51] = y55;
        YellowDot y56 = new YellowDot(Dot56, DotPic56);
        dots[52] = y56;
        YellowDot y57 = new YellowDot(Dot57, DotPic57);
        dots[53] = y57;
        YellowDot y58 = new YellowDot(Dot58, DotPic58);
        dots[54] = y58;
        YellowDot y59 = new YellowDot(Dot59, DotPic59);
        dots[55] = y59;
        YellowDot y60 = new YellowDot(Dot60, DotPic60);
        dots[56] = y60;
        YellowDot y61 = new YellowDot(Dot61, DotPic61);
        dots[57] = y61;
        YellowDot y62 = new YellowDot(Dot62, DotPic62);
        dots[58] = y62;
        YellowDot y63 = new YellowDot(Dot63, DotPic63);
        dots[59] = y63;
        YellowDot y64 = new YellowDot(Dot64, DotPic64);
        dots[60] = y64;
        YellowDot y65 = new YellowDot(Dot65, DotPic65);
        dots[61] = y65;
        YellowDot y66 = new YellowDot(Dot66, DotPic66);
        dots[62] = y66;
        YellowDot y67 = new YellowDot(Dot67, DotPic67);
        dots[63] = y67;
        YellowDot y68 = new YellowDot(Dot68, DotPic68);
        dots[64] = y68;
        YellowDot y69 = new YellowDot(Dot69, DotPic69);
        dots[65] = y69;
        YellowDot y70 = new YellowDot(Dot70, DotPic70);
        dots[66] = y70;
        YellowDot y71 = new YellowDot(Dot71, DotPic71);
        dots[67] = y71;
        YellowDot y72 = new YellowDot(Dot72, DotPic72);
        dots[68] = y72;
        YellowDot y73 = new YellowDot(Dot73, DotPic73);
        dots[69] = y73;
        YellowDot y74 = new YellowDot(Dot74, DotPic74);
        dots[70] = y74;
        YellowDot y75 = new YellowDot(Dot75, DotPic75);
        dots[71] = y75;
        YellowDot y76 = new YellowDot(Dot76, DotPic76);
        dots[72] = y76;
        YellowDot y77 = new YellowDot(Dot77, DotPic77);
        dots[73] = y77;
        YellowDot y78 = new YellowDot(Dot78, DotPic78);
        dots[74] = y78;
        YellowDot y79 = new YellowDot(Dot79, DotPic79);
        dots[75] = y79;
        YellowDot y80 = new YellowDot(Dot80, DotPic80);
        dots[76] = y80;
        YellowDot y81 = new YellowDot(Dot81, DotPic81);
        dots[77] = y81;
        YellowDot y82 = new YellowDot(Dot82, DotPic82);
        dots[78] = y82;
        YellowDot y83 = new YellowDot(Dot83, DotPic83);
        dots[79] = y83;
        YellowDot y84 = new YellowDot(Dot84, DotPic84);
        dots[80] = y84;
        YellowDot y85 = new YellowDot(Dot85, DotPic85);
        dots[81] = y85;
        YellowDot y86 = new YellowDot(Dot86, DotPic86);
        dots[82] = y86;
        YellowDot y87 = new YellowDot(Dot87, DotPic87);
        dots[83] = y87;
        YellowDot y88 = new YellowDot(Dot88, DotPic88);
        dots[84] = y88;
        YellowDot y89 = new YellowDot(Dot89, DotPic89);
        dots[85] = y89;
        YellowDot y90 = new YellowDot(Dot90, DotPic90);
        dots[86] = y90;
        YellowDot y91 = new YellowDot(Dot91, DotPic91);
        dots[87] = y91;
        YellowDot y92 = new YellowDot(Dot92, DotPic92);
        dots[88] = y92;
        YellowDot y93 = new YellowDot(Dot93, DotPic93);
        dots[89] = y93;
        YellowDot y94 = new YellowDot(Dot94, DotPic94);
        dots[90] = y94;
        YellowDot y95 = new YellowDot(Dot95, DotPic95);
        dots[91] = y95;
        YellowDot y96 = new YellowDot(Dot96, DotPic96);
        dots[92] = y96;
        YellowDot y97 = new YellowDot(Dot97, DotPic97);
        dots[93] = y97;
        YellowDot y98 = new YellowDot(Dot98, DotPic98);
        dots[94] = y98;
        YellowDot y99 = new YellowDot(Dot99, DotPic99);
        dots[95] = y99;
        YellowDot y100 = new YellowDot(Dot100, DotPic100);
        dots[96] = y100;
        YellowDot y101 = new YellowDot(Dot101, DotPic101);
        dots[97] = y101;
        YellowDot y102 = new YellowDot(Dot102, DotPic102);
        dots[98] = y102;
        YellowDot y103 = new YellowDot(Dot103, DotPic103);
        dots[99] = y103;
        YellowDot y104 = new YellowDot(Dot104, DotPic104);
        dots[100] = y104;
        YellowDot y105 = new YellowDot(Dot105, DotPic105);
        dots[101] = y105;
        YellowDot y106 = new YellowDot(Dot106, DotPic106);
        dots[102] = y106;
        YellowDot y107 = new YellowDot(Dot107, DotPic107);
        dots[103] = y107;
        YellowDot y108 = new YellowDot(Dot108, DotPic108);
        dots[104] = y108;
        YellowDot y109 = new YellowDot(Dot109, DotPic109);
        dots[105] = y109;
        YellowDot y110 = new YellowDot(Dot110, DotPic110);
        dots[106] = y110;
        YellowDot y111 = new YellowDot(Dot111, DotPic111);
        dots[107] = y111;
        YellowDot y112 = new YellowDot(Dot112, DotPic112);
        dots[108] = y112;
        YellowDot y113 = new YellowDot(Dot113, DotPic113);
        dots[109] = y113;
        YellowDot y114 = new YellowDot(Dot114, DotPic114);
        dots[110] = y114;
        YellowDot y115 = new YellowDot(Dot115, DotPic115);
        dots[111] = y115;
        YellowDot y116 = new YellowDot(Dot116, DotPic116);
        dots[112] = y116;
        YellowDot y117 = new YellowDot(Dot117, DotPic117);
        dots[113] = y117;
        YellowDot y118 = new YellowDot(Dot118, DotPic118);
        dots[114] = y118;
        YellowDot y119 = new YellowDot(Dot119, DotPic119);
        dots[115] = y119;
        YellowDot y120 = new YellowDot(Dot120, DotPic120);
        dots[116] = y120;
        YellowDot y121 = new YellowDot(Dot121, DotPic121);
        dots[117] = y121;
        YellowDot y122 = new YellowDot(Dot122, DotPic122);
        dots[118] = y122;
        YellowDot y123 = new YellowDot(Dot123, DotPic123);
        dots[119] = y123;
        YellowDot y124 = new YellowDot(Dot124, DotPic124);
        dots[120] = y124;
        YellowDot y125 = new YellowDot(Dot125, DotPic125);
        dots[121] = y125;
        YellowDot y126 = new YellowDot(Dot126, DotPic126);
        dots[122] = y126;
        
    }
    //insert Wall panels into the walls array
    public void populateWallArray(){
        /*
        //Printed out the below text and copy-pasted from terminal to save myself time (so I didn't have to type out all of the above)
        for (int i = 1; i <= walls.length; i++){
            System.out.println("walls[" + (i-1) + "] = Wall" + i + ";");
        }
        */
        
        walls[0] = Wall1;
        walls[1] = Wall2;
        walls[2] = Wall3;
        walls[3] = Wall4;
        walls[4] = Wall5;
        walls[5] = Wall6;
        walls[6] = Wall7;
        walls[7] = Wall8;
        walls[8] = Wall9;
        walls[9] = Wall10;
        walls[10] = Wall11;
        walls[11] = Wall12;
        walls[12] = Wall13;
        walls[13] = Wall14;
        walls[14] = Wall15;
        walls[15] = Wall16;
        walls[16] = Wall17;
        walls[17] = Wall18;
        walls[18] = Wall19;
        walls[19] = Wall20;
        walls[20] = Wall21;
        walls[21] = Wall22;
        walls[22] = Wall23;
        walls[23] = Wall24;
        walls[24] = Wall25;
        walls[25] = Wall26;
        walls[26] = Wall27;
        walls[27] = Wall28;
        walls[28] = Wall29;
        walls[29] = Wall30;
        walls[30] = Wall31;
        walls[31] = Wall32;
        walls[32] = Wall33;
        walls[33] = Wall34;
        walls[34] = Wall35;
        walls[35] = Wall36;
        walls[36] = Wall37;
        walls[37] = Wall38;
        walls[38] = Wall39;
        walls[39] = Wall40;
        walls[40] = Wall41;
        walls[41] = Wall42;
        walls[42] = Wall43;
        walls[43] = Wall44;
        walls[44] = Wall45;
        walls[45] = Wall46;
        walls[46] = Wall47;
        
    }
    
    //update and display the End Frame (endMenu)
    public void loadEndMenu(){
        //if the frame doesn't exist, create it so the program doesn't crash
        if (endMenu == null){
            endMenu = new End(this);
        }
        //display end menu, update the end menu's content, and hide current frame
        endMenu.setVisible(true);
        endMenu.updateDisplay();
        this.setVisible(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel466 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        Ghost1 = new javax.swing.JPanel();
        GhostPic1 = new javax.swing.JLabel();
        Ghost2 = new javax.swing.JPanel();
        GhostPic2 = new javax.swing.JLabel();
        Ghost3 = new javax.swing.JPanel();
        GhostPic3 = new javax.swing.JLabel();
        Ghost4 = new javax.swing.JPanel();
        GhostPic4 = new javax.swing.JLabel();
        Ghost5 = new javax.swing.JPanel();
        GhostPic5 = new javax.swing.JLabel();
        Ghost6 = new javax.swing.JPanel();
        GhostPic6 = new javax.swing.JLabel();
        PacMan = new javax.swing.JPanel();
        PacManPic = new javax.swing.JLabel();
        Wall1 = new javax.swing.JPanel();
        Wall2 = new javax.swing.JPanel();
        Wall3 = new javax.swing.JPanel();
        Wall4 = new javax.swing.JPanel();
        Wall5 = new javax.swing.JPanel();
        Wall6 = new javax.swing.JPanel();
        Wall7 = new javax.swing.JPanel();
        Wall8 = new javax.swing.JPanel();
        Wall9 = new javax.swing.JPanel();
        Wall10 = new javax.swing.JPanel();
        Wall11 = new javax.swing.JPanel();
        Wall12 = new javax.swing.JPanel();
        Wall13 = new javax.swing.JPanel();
        Wall14 = new javax.swing.JPanel();
        Wall15 = new javax.swing.JPanel();
        Wall16 = new javax.swing.JPanel();
        Wall17 = new javax.swing.JPanel();
        Wall18 = new javax.swing.JPanel();
        Wall19 = new javax.swing.JPanel();
        Wall20 = new javax.swing.JPanel();
        Wall21 = new javax.swing.JPanel();
        Wall22 = new javax.swing.JPanel();
        Wall23 = new javax.swing.JPanel();
        Wall24 = new javax.swing.JPanel();
        Wall25 = new javax.swing.JPanel();
        Wall26 = new javax.swing.JPanel();
        Wall27 = new javax.swing.JPanel();
        Wall28 = new javax.swing.JPanel();
        Wall29 = new javax.swing.JPanel();
        Wall30 = new javax.swing.JPanel();
        Wall31 = new javax.swing.JPanel();
        Wall32 = new javax.swing.JPanel();
        Wall33 = new javax.swing.JPanel();
        Wall34 = new javax.swing.JPanel();
        Wall35 = new javax.swing.JPanel();
        Wall36 = new javax.swing.JPanel();
        Wall37 = new javax.swing.JPanel();
        Wall38 = new javax.swing.JPanel();
        Wall39 = new javax.swing.JPanel();
        Wall40 = new javax.swing.JPanel();
        Wall41 = new javax.swing.JPanel();
        Wall42 = new javax.swing.JPanel();
        Wall43 = new javax.swing.JPanel();
        Wall44 = new javax.swing.JPanel();
        Wall45 = new javax.swing.JPanel();
        Wall46 = new javax.swing.JPanel();
        Wall47 = new javax.swing.JPanel();
        Dot4 = new javax.swing.JPanel();
        DotPic4 = new javax.swing.JLabel();
        Dot5 = new javax.swing.JPanel();
        DotPic5 = new javax.swing.JLabel();
        Dot6 = new javax.swing.JPanel();
        DotPic6 = new javax.swing.JLabel();
        Dot7 = new javax.swing.JPanel();
        DotPic7 = new javax.swing.JLabel();
        Dot8 = new javax.swing.JPanel();
        DotPic8 = new javax.swing.JLabel();
        Dot9 = new javax.swing.JPanel();
        DotPic9 = new javax.swing.JLabel();
        Dot10 = new javax.swing.JPanel();
        DotPic10 = new javax.swing.JLabel();
        Dot11 = new javax.swing.JPanel();
        DotPic11 = new javax.swing.JLabel();
        Dot12 = new javax.swing.JPanel();
        DotPic12 = new javax.swing.JLabel();
        Dot13 = new javax.swing.JPanel();
        DotPic13 = new javax.swing.JLabel();
        Dot14 = new javax.swing.JPanel();
        DotPic14 = new javax.swing.JLabel();
        Dot15 = new javax.swing.JPanel();
        DotPic15 = new javax.swing.JLabel();
        Dot16 = new javax.swing.JPanel();
        DotPic16 = new javax.swing.JLabel();
        Dot17 = new javax.swing.JPanel();
        DotPic17 = new javax.swing.JLabel();
        Dot18 = new javax.swing.JPanel();
        DotPic18 = new javax.swing.JLabel();
        Dot19 = new javax.swing.JPanel();
        DotPic19 = new javax.swing.JLabel();
        Dot20 = new javax.swing.JPanel();
        DotPic20 = new javax.swing.JLabel();
        Dot21 = new javax.swing.JPanel();
        DotPic21 = new javax.swing.JLabel();
        Dot22 = new javax.swing.JPanel();
        DotPic22 = new javax.swing.JLabel();
        Dot23 = new javax.swing.JPanel();
        DotPic23 = new javax.swing.JLabel();
        Dot24 = new javax.swing.JPanel();
        DotPic24 = new javax.swing.JLabel();
        Dot25 = new javax.swing.JPanel();
        DotPic25 = new javax.swing.JLabel();
        Dot26 = new javax.swing.JPanel();
        DotPic26 = new javax.swing.JLabel();
        Dot27 = new javax.swing.JPanel();
        DotPic27 = new javax.swing.JLabel();
        Dot28 = new javax.swing.JPanel();
        DotPic28 = new javax.swing.JLabel();
        Dot29 = new javax.swing.JPanel();
        DotPic29 = new javax.swing.JLabel();
        Dot30 = new javax.swing.JPanel();
        DotPic30 = new javax.swing.JLabel();
        Dot31 = new javax.swing.JPanel();
        DotPic31 = new javax.swing.JLabel();
        Dot32 = new javax.swing.JPanel();
        DotPic32 = new javax.swing.JLabel();
        Dot33 = new javax.swing.JPanel();
        DotPic33 = new javax.swing.JLabel();
        Dot34 = new javax.swing.JPanel();
        DotPic34 = new javax.swing.JLabel();
        Dot35 = new javax.swing.JPanel();
        DotPic35 = new javax.swing.JLabel();
        Dot36 = new javax.swing.JPanel();
        DotPic36 = new javax.swing.JLabel();
        Dot37 = new javax.swing.JPanel();
        DotPic37 = new javax.swing.JLabel();
        Dot38 = new javax.swing.JPanel();
        DotPic38 = new javax.swing.JLabel();
        Dot39 = new javax.swing.JPanel();
        DotPic39 = new javax.swing.JLabel();
        Dot40 = new javax.swing.JPanel();
        DotPic40 = new javax.swing.JLabel();
        Dot41 = new javax.swing.JPanel();
        DotPic41 = new javax.swing.JLabel();
        Dot42 = new javax.swing.JPanel();
        DotPic42 = new javax.swing.JLabel();
        Dot43 = new javax.swing.JPanel();
        DotPic43 = new javax.swing.JLabel();
        Dot44 = new javax.swing.JPanel();
        DotPic44 = new javax.swing.JLabel();
        Dot45 = new javax.swing.JPanel();
        DotPic45 = new javax.swing.JLabel();
        Dot46 = new javax.swing.JPanel();
        DotPic46 = new javax.swing.JLabel();
        Dot47 = new javax.swing.JPanel();
        DotPic47 = new javax.swing.JLabel();
        Dot48 = new javax.swing.JPanel();
        DotPic48 = new javax.swing.JLabel();
        Dot49 = new javax.swing.JPanel();
        DotPic49 = new javax.swing.JLabel();
        Dot50 = new javax.swing.JPanel();
        DotPic50 = new javax.swing.JLabel();
        Dot51 = new javax.swing.JPanel();
        DotPic51 = new javax.swing.JLabel();
        Dot52 = new javax.swing.JPanel();
        DotPic52 = new javax.swing.JLabel();
        Dot53 = new javax.swing.JPanel();
        DotPic53 = new javax.swing.JLabel();
        Dot54 = new javax.swing.JPanel();
        DotPic54 = new javax.swing.JLabel();
        Dot55 = new javax.swing.JPanel();
        DotPic55 = new javax.swing.JLabel();
        Dot56 = new javax.swing.JPanel();
        DotPic56 = new javax.swing.JLabel();
        Dot57 = new javax.swing.JPanel();
        DotPic57 = new javax.swing.JLabel();
        Dot58 = new javax.swing.JPanel();
        DotPic58 = new javax.swing.JLabel();
        Dot59 = new javax.swing.JPanel();
        DotPic59 = new javax.swing.JLabel();
        Dot60 = new javax.swing.JPanel();
        DotPic60 = new javax.swing.JLabel();
        Dot61 = new javax.swing.JPanel();
        DotPic61 = new javax.swing.JLabel();
        Dot62 = new javax.swing.JPanel();
        DotPic62 = new javax.swing.JLabel();
        Dot63 = new javax.swing.JPanel();
        DotPic63 = new javax.swing.JLabel();
        Dot64 = new javax.swing.JPanel();
        DotPic64 = new javax.swing.JLabel();
        Dot65 = new javax.swing.JPanel();
        DotPic65 = new javax.swing.JLabel();
        Dot66 = new javax.swing.JPanel();
        DotPic66 = new javax.swing.JLabel();
        Dot67 = new javax.swing.JPanel();
        DotPic67 = new javax.swing.JLabel();
        Dot68 = new javax.swing.JPanel();
        DotPic68 = new javax.swing.JLabel();
        Dot69 = new javax.swing.JPanel();
        DotPic69 = new javax.swing.JLabel();
        Dot70 = new javax.swing.JPanel();
        DotPic70 = new javax.swing.JLabel();
        Dot71 = new javax.swing.JPanel();
        DotPic71 = new javax.swing.JLabel();
        Dot72 = new javax.swing.JPanel();
        DotPic72 = new javax.swing.JLabel();
        Dot73 = new javax.swing.JPanel();
        DotPic73 = new javax.swing.JLabel();
        Dot74 = new javax.swing.JPanel();
        DotPic74 = new javax.swing.JLabel();
        Dot75 = new javax.swing.JPanel();
        DotPic75 = new javax.swing.JLabel();
        Dot76 = new javax.swing.JPanel();
        DotPic76 = new javax.swing.JLabel();
        Dot77 = new javax.swing.JPanel();
        DotPic77 = new javax.swing.JLabel();
        Dot78 = new javax.swing.JPanel();
        DotPic78 = new javax.swing.JLabel();
        Dot79 = new javax.swing.JPanel();
        DotPic79 = new javax.swing.JLabel();
        Dot80 = new javax.swing.JPanel();
        DotPic80 = new javax.swing.JLabel();
        Dot81 = new javax.swing.JPanel();
        DotPic81 = new javax.swing.JLabel();
        Dot82 = new javax.swing.JPanel();
        DotPic82 = new javax.swing.JLabel();
        Dot83 = new javax.swing.JPanel();
        DotPic83 = new javax.swing.JLabel();
        Dot84 = new javax.swing.JPanel();
        DotPic84 = new javax.swing.JLabel();
        Dot85 = new javax.swing.JPanel();
        DotPic85 = new javax.swing.JLabel();
        Dot86 = new javax.swing.JPanel();
        DotPic86 = new javax.swing.JLabel();
        Dot87 = new javax.swing.JPanel();
        DotPic87 = new javax.swing.JLabel();
        Dot88 = new javax.swing.JPanel();
        DotPic88 = new javax.swing.JLabel();
        Dot89 = new javax.swing.JPanel();
        DotPic89 = new javax.swing.JLabel();
        Dot90 = new javax.swing.JPanel();
        DotPic90 = new javax.swing.JLabel();
        Dot91 = new javax.swing.JPanel();
        DotPic91 = new javax.swing.JLabel();
        Dot92 = new javax.swing.JPanel();
        DotPic92 = new javax.swing.JLabel();
        Dot93 = new javax.swing.JPanel();
        DotPic93 = new javax.swing.JLabel();
        Dot94 = new javax.swing.JPanel();
        DotPic94 = new javax.swing.JLabel();
        Dot95 = new javax.swing.JPanel();
        DotPic95 = new javax.swing.JLabel();
        Dot96 = new javax.swing.JPanel();
        DotPic96 = new javax.swing.JLabel();
        Dot97 = new javax.swing.JPanel();
        DotPic97 = new javax.swing.JLabel();
        Dot98 = new javax.swing.JPanel();
        DotPic98 = new javax.swing.JLabel();
        Dot99 = new javax.swing.JPanel();
        DotPic99 = new javax.swing.JLabel();
        Dot100 = new javax.swing.JPanel();
        DotPic100 = new javax.swing.JLabel();
        Dot101 = new javax.swing.JPanel();
        DotPic101 = new javax.swing.JLabel();
        Dot102 = new javax.swing.JPanel();
        DotPic102 = new javax.swing.JLabel();
        Dot103 = new javax.swing.JPanel();
        DotPic103 = new javax.swing.JLabel();
        Dot104 = new javax.swing.JPanel();
        DotPic104 = new javax.swing.JLabel();
        Dot105 = new javax.swing.JPanel();
        DotPic105 = new javax.swing.JLabel();
        Dot106 = new javax.swing.JPanel();
        DotPic106 = new javax.swing.JLabel();
        Dot107 = new javax.swing.JPanel();
        DotPic107 = new javax.swing.JLabel();
        Dot108 = new javax.swing.JPanel();
        DotPic108 = new javax.swing.JLabel();
        Dot109 = new javax.swing.JPanel();
        DotPic109 = new javax.swing.JLabel();
        Dot110 = new javax.swing.JPanel();
        DotPic110 = new javax.swing.JLabel();
        Dot111 = new javax.swing.JPanel();
        DotPic111 = new javax.swing.JLabel();
        Dot112 = new javax.swing.JPanel();
        DotPic112 = new javax.swing.JLabel();
        Dot113 = new javax.swing.JPanel();
        DotPic113 = new javax.swing.JLabel();
        Dot114 = new javax.swing.JPanel();
        DotPic114 = new javax.swing.JLabel();
        Dot115 = new javax.swing.JPanel();
        DotPic115 = new javax.swing.JLabel();
        Dot116 = new javax.swing.JPanel();
        DotPic116 = new javax.swing.JLabel();
        Dot117 = new javax.swing.JPanel();
        DotPic117 = new javax.swing.JLabel();
        Dot118 = new javax.swing.JPanel();
        DotPic118 = new javax.swing.JLabel();
        Dot119 = new javax.swing.JPanel();
        DotPic119 = new javax.swing.JLabel();
        Dot120 = new javax.swing.JPanel();
        DotPic120 = new javax.swing.JLabel();
        Dot121 = new javax.swing.JPanel();
        DotPic121 = new javax.swing.JLabel();
        Dot122 = new javax.swing.JPanel();
        DotPic122 = new javax.swing.JLabel();
        Dot123 = new javax.swing.JPanel();
        DotPic123 = new javax.swing.JLabel();
        Dot124 = new javax.swing.JPanel();
        DotPic124 = new javax.swing.JLabel();
        Dot125 = new javax.swing.JPanel();
        DotPic125 = new javax.swing.JLabel();
        Dot126 = new javax.swing.JPanel();
        DotPic126 = new javax.swing.JLabel();

        jPanel466.setBackground(new java.awt.Color(0, 0, 0));
        jPanel466.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel466Layout = new javax.swing.GroupLayout(jPanel466);
        jPanel466.setLayout(jPanel466Layout);
        jPanel466Layout.setHorizontalGroup(
            jPanel466Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel466Layout.setVerticalGroup(
            jPanel466Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLabel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText(" Score:  ");
        jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 255), 3));
        jLabel1.setMaximumSize(new java.awt.Dimension(100, 40));
        jLabel1.setMinimumSize(new java.awt.Dimension(100, 40));
        jLabel1.setOpaque(true);
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 40));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Pacman");
        setBackground(new java.awt.Color(0, 0, 0));
        setSize(new java.awt.Dimension(500, 420));
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        Ghost1.setBackground(new Color(0,0,0,0));
        Ghost1.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost1Layout = new javax.swing.GroupLayout(Ghost1);
        Ghost1.setLayout(Ghost1Layout);
        Ghost1Layout.setHorizontalGroup(
            Ghost1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost1Layout.createSequentialGroup()
                .addComponent(GhostPic1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost1Layout.setVerticalGroup(
            Ghost1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost1Layout.createSequentialGroup()
                .addComponent(GhostPic1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Ghost1, gridBagConstraints);

        Ghost2.setBackground(new Color(0,0,0,0));
        Ghost2.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost2Layout = new javax.swing.GroupLayout(Ghost2);
        Ghost2.setLayout(Ghost2Layout);
        Ghost2Layout.setHorizontalGroup(
            Ghost2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost2Layout.createSequentialGroup()
                .addComponent(GhostPic2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost2Layout.setVerticalGroup(
            Ghost2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost2Layout.createSequentialGroup()
                .addComponent(GhostPic2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Ghost2, gridBagConstraints);

        Ghost3.setBackground(new Color(0,0,0,0));
        Ghost3.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost3Layout = new javax.swing.GroupLayout(Ghost3);
        Ghost3.setLayout(Ghost3Layout);
        Ghost3Layout.setHorizontalGroup(
            Ghost3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost3Layout.createSequentialGroup()
                .addComponent(GhostPic3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost3Layout.setVerticalGroup(
            Ghost3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost3Layout.createSequentialGroup()
                .addComponent(GhostPic3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Ghost3, gridBagConstraints);

        Ghost4.setBackground(new Color(0,0,0,0));
        Ghost4.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost4Layout = new javax.swing.GroupLayout(Ghost4);
        Ghost4.setLayout(Ghost4Layout);
        Ghost4Layout.setHorizontalGroup(
            Ghost4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost4Layout.createSequentialGroup()
                .addComponent(GhostPic4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost4Layout.setVerticalGroup(
            Ghost4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost4Layout.createSequentialGroup()
                .addComponent(GhostPic4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Ghost4, gridBagConstraints);

        Ghost5.setBackground(new Color(0,0,0,0));
        Ghost5.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost5Layout = new javax.swing.GroupLayout(Ghost5);
        Ghost5.setLayout(Ghost5Layout);
        Ghost5Layout.setHorizontalGroup(
            Ghost5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost5Layout.createSequentialGroup()
                .addComponent(GhostPic5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost5Layout.setVerticalGroup(
            Ghost5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost5Layout.createSequentialGroup()
                .addComponent(GhostPic5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Ghost5, gridBagConstraints);

        Ghost6.setBackground(new Color(0,0,0,0));
        Ghost6.setPreferredSize(new java.awt.Dimension(20, 20));

        GhostPic6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/ghostRight.png"))); // NOI18N

        javax.swing.GroupLayout Ghost6Layout = new javax.swing.GroupLayout(Ghost6);
        Ghost6.setLayout(Ghost6Layout);
        Ghost6Layout.setHorizontalGroup(
            Ghost6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost6Layout.createSequentialGroup()
                .addComponent(GhostPic6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Ghost6Layout.setVerticalGroup(
            Ghost6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Ghost6Layout.createSequentialGroup()
                .addComponent(GhostPic6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Ghost6, gridBagConstraints);

        PacMan.setBackground(new Color(0,0,0,0));
        PacMan.setPreferredSize(new java.awt.Dimension(20, 20));

        PacManPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/pacmanRight.png"))); // NOI18N
        PacManPic.setText("jLabel1");

        javax.swing.GroupLayout PacManLayout = new javax.swing.GroupLayout(PacMan);
        PacMan.setLayout(PacManLayout);
        PacManLayout.setHorizontalGroup(
            PacManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PacManLayout.createSequentialGroup()
                .addComponent(PacManPic, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        PacManLayout.setVerticalGroup(
            PacManLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PacManLayout.createSequentialGroup()
                .addComponent(PacManPic, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 10;
        getContentPane().add(PacMan, gridBagConstraints);

        Wall1.setBackground(new java.awt.Color(0, 0, 0));
        Wall1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall1.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall1Layout = new javax.swing.GroupLayout(Wall1);
        Wall1.setLayout(Wall1Layout);
        Wall1Layout.setHorizontalGroup(
            Wall1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall1Layout.setVerticalGroup(
            Wall1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        getContentPane().add(Wall1, gridBagConstraints);

        Wall2.setBackground(new java.awt.Color(0, 0, 0));
        Wall2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall2.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall2Layout = new javax.swing.GroupLayout(Wall2);
        Wall2.setLayout(Wall2Layout);
        Wall2Layout.setHorizontalGroup(
            Wall2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall2Layout.setVerticalGroup(
            Wall2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall2, gridBagConstraints);

        Wall3.setBackground(new java.awt.Color(0, 0, 0));
        Wall3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall3.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall3Layout = new javax.swing.GroupLayout(Wall3);
        Wall3.setLayout(Wall3Layout);
        Wall3Layout.setHorizontalGroup(
            Wall3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall3Layout.setVerticalGroup(
            Wall3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall3, gridBagConstraints);

        Wall4.setBackground(new java.awt.Color(0, 0, 0));
        Wall4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall4.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall4Layout = new javax.swing.GroupLayout(Wall4);
        Wall4.setLayout(Wall4Layout);
        Wall4Layout.setHorizontalGroup(
            Wall4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall4Layout.setVerticalGroup(
            Wall4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall4, gridBagConstraints);

        Wall5.setBackground(new java.awt.Color(0, 0, 0));
        Wall5.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall5.setPreferredSize(new java.awt.Dimension(40, 20));

        javax.swing.GroupLayout Wall5Layout = new javax.swing.GroupLayout(Wall5);
        Wall5.setLayout(Wall5Layout);
        Wall5Layout.setHorizontalGroup(
            Wall5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall5Layout.setVerticalGroup(
            Wall5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(Wall5, gridBagConstraints);

        Wall6.setBackground(new java.awt.Color(0, 0, 0));
        Wall6.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall6.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall6Layout = new javax.swing.GroupLayout(Wall6);
        Wall6.setLayout(Wall6Layout);
        Wall6Layout.setHorizontalGroup(
            Wall6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall6Layout.setVerticalGroup(
            Wall6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall6, gridBagConstraints);

        Wall7.setBackground(new java.awt.Color(0, 0, 0));
        Wall7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall7.setPreferredSize(new java.awt.Dimension(20, 100));

        javax.swing.GroupLayout Wall7Layout = new javax.swing.GroupLayout(Wall7);
        Wall7.setLayout(Wall7Layout);
        Wall7Layout.setHorizontalGroup(
            Wall7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall7Layout.setVerticalGroup(
            Wall7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 5;
        getContentPane().add(Wall7, gridBagConstraints);

        Wall8.setBackground(new java.awt.Color(0, 0, 0));
        Wall8.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall8.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall8Layout = new javax.swing.GroupLayout(Wall8);
        Wall8.setLayout(Wall8Layout);
        Wall8Layout.setHorizontalGroup(
            Wall8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall8Layout.setVerticalGroup(
            Wall8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 12;
        getContentPane().add(Wall8, gridBagConstraints);

        Wall9.setBackground(new java.awt.Color(0, 0, 0));
        Wall9.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall9.setPreferredSize(new java.awt.Dimension(20, 100));

        javax.swing.GroupLayout Wall9Layout = new javax.swing.GroupLayout(Wall9);
        Wall9.setLayout(Wall9Layout);
        Wall9Layout.setHorizontalGroup(
            Wall9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall9Layout.setVerticalGroup(
            Wall9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = 5;
        getContentPane().add(Wall9, gridBagConstraints);

        Wall10.setBackground(new java.awt.Color(0, 0, 0));
        Wall10.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall10.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall10Layout = new javax.swing.GroupLayout(Wall10);
        Wall10.setLayout(Wall10Layout);
        Wall10Layout.setHorizontalGroup(
            Wall10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall10Layout.setVerticalGroup(
            Wall10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall10, gridBagConstraints);

        Wall11.setBackground(new java.awt.Color(0, 0, 0));
        Wall11.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall11.setPreferredSize(new java.awt.Dimension(40, 20));

        javax.swing.GroupLayout Wall11Layout = new javax.swing.GroupLayout(Wall11);
        Wall11.setLayout(Wall11Layout);
        Wall11Layout.setHorizontalGroup(
            Wall11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall11Layout.setVerticalGroup(
            Wall11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(Wall11, gridBagConstraints);

        Wall12.setBackground(new java.awt.Color(0, 0, 0));
        Wall12.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall12.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall12Layout = new javax.swing.GroupLayout(Wall12);
        Wall12.setLayout(Wall12Layout);
        Wall12Layout.setHorizontalGroup(
            Wall12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall12Layout.setVerticalGroup(
            Wall12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall12, gridBagConstraints);

        Wall13.setBackground(new java.awt.Color(0, 0, 0));
        Wall13.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall13.setPreferredSize(new java.awt.Dimension(460, 20));

        javax.swing.GroupLayout Wall13Layout = new javax.swing.GroupLayout(Wall13);
        Wall13.setLayout(Wall13Layout);
        Wall13Layout.setHorizontalGroup(
            Wall13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall13Layout.setVerticalGroup(
            Wall13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 20;
        gridBagConstraints.gridwidth = 23;
        getContentPane().add(Wall13, gridBagConstraints);

        Wall14.setBackground(new java.awt.Color(0, 0, 0));
        Wall14.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall14.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall14Layout = new javax.swing.GroupLayout(Wall14);
        Wall14.setLayout(Wall14Layout);
        Wall14Layout.setHorizontalGroup(
            Wall14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall14Layout.setVerticalGroup(
            Wall14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall14, gridBagConstraints);

        Wall15.setBackground(new java.awt.Color(0, 0, 0));
        Wall15.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall15.setPreferredSize(new java.awt.Dimension(20, 420));

        javax.swing.GroupLayout Wall15Layout = new javax.swing.GroupLayout(Wall15);
        Wall15.setLayout(Wall15Layout);
        Wall15Layout.setHorizontalGroup(
            Wall15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall15Layout.setVerticalGroup(
            Wall15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 21;
        getContentPane().add(Wall15, gridBagConstraints);

        Wall16.setBackground(new java.awt.Color(0, 0, 0));
        Wall16.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall16.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall16Layout = new javax.swing.GroupLayout(Wall16);
        Wall16.setLayout(Wall16Layout);
        Wall16Layout.setHorizontalGroup(
            Wall16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall16Layout.setVerticalGroup(
            Wall16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall16, gridBagConstraints);

        Wall17.setBackground(new java.awt.Color(0, 0, 0));
        Wall17.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall17.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall17Layout = new javax.swing.GroupLayout(Wall17);
        Wall17.setLayout(Wall17Layout);
        Wall17Layout.setHorizontalGroup(
            Wall17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall17Layout.setVerticalGroup(
            Wall17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall17, gridBagConstraints);

        Wall18.setBackground(new java.awt.Color(0, 0, 0));
        Wall18.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall18.setPreferredSize(new java.awt.Dimension(140, 20));

        javax.swing.GroupLayout Wall18Layout = new javax.swing.GroupLayout(Wall18);
        Wall18.setLayout(Wall18Layout);
        Wall18Layout.setHorizontalGroup(
            Wall18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall18Layout.setVerticalGroup(
            Wall18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 7;
        getContentPane().add(Wall18, gridBagConstraints);

        Wall19.setBackground(new java.awt.Color(0, 0, 0));
        Wall19.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall19.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall19Layout = new javax.swing.GroupLayout(Wall19);
        Wall19.setLayout(Wall19Layout);
        Wall19Layout.setHorizontalGroup(
            Wall19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall19Layout.setVerticalGroup(
            Wall19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall19, gridBagConstraints);

        Wall20.setBackground(new java.awt.Color(0, 0, 0));
        Wall20.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall20.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall20Layout = new javax.swing.GroupLayout(Wall20);
        Wall20.setLayout(Wall20Layout);
        Wall20Layout.setHorizontalGroup(
            Wall20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall20Layout.setVerticalGroup(
            Wall20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall20, gridBagConstraints);

        Wall21.setBackground(new java.awt.Color(0, 0, 0));
        Wall21.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall21.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall21Layout = new javax.swing.GroupLayout(Wall21);
        Wall21.setLayout(Wall21Layout);
        Wall21Layout.setHorizontalGroup(
            Wall21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall21Layout.setVerticalGroup(
            Wall21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall21, gridBagConstraints);

        Wall22.setBackground(new java.awt.Color(0, 0, 0));
        Wall22.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall22.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall22Layout = new javax.swing.GroupLayout(Wall22);
        Wall22.setLayout(Wall22Layout);
        Wall22Layout.setHorizontalGroup(
            Wall22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall22Layout.setVerticalGroup(
            Wall22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall22, gridBagConstraints);

        Wall23.setBackground(new java.awt.Color(0, 0, 0));
        Wall23.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall23.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall23Layout = new javax.swing.GroupLayout(Wall23);
        Wall23.setLayout(Wall23Layout);
        Wall23Layout.setHorizontalGroup(
            Wall23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall23Layout.setVerticalGroup(
            Wall23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall23, gridBagConstraints);

        Wall24.setBackground(new java.awt.Color(0, 0, 0));
        Wall24.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall24.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall24Layout = new javax.swing.GroupLayout(Wall24);
        Wall24.setLayout(Wall24Layout);
        Wall24Layout.setHorizontalGroup(
            Wall24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall24Layout.setVerticalGroup(
            Wall24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall24, gridBagConstraints);

        Wall25.setBackground(new java.awt.Color(0, 0, 0));
        Wall25.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall25.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall25Layout = new javax.swing.GroupLayout(Wall25);
        Wall25.setLayout(Wall25Layout);
        Wall25Layout.setHorizontalGroup(
            Wall25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall25Layout.setVerticalGroup(
            Wall25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall25, gridBagConstraints);

        Wall26.setBackground(new java.awt.Color(0, 0, 0));
        Wall26.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall26.setPreferredSize(new java.awt.Dimension(20, 100));

        javax.swing.GroupLayout Wall26Layout = new javax.swing.GroupLayout(Wall26);
        Wall26.setLayout(Wall26Layout);
        Wall26Layout.setHorizontalGroup(
            Wall26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall26Layout.setVerticalGroup(
            Wall26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridheight = 5;
        getContentPane().add(Wall26, gridBagConstraints);

        Wall27.setBackground(new java.awt.Color(0, 0, 0));
        Wall27.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall27.setPreferredSize(new java.awt.Dimension(40, 20));

        javax.swing.GroupLayout Wall27Layout = new javax.swing.GroupLayout(Wall27);
        Wall27.setLayout(Wall27Layout);
        Wall27Layout.setHorizontalGroup(
            Wall27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall27Layout.setVerticalGroup(
            Wall27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(Wall27, gridBagConstraints);

        Wall28.setBackground(new java.awt.Color(0, 0, 0));
        Wall28.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall28.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall28Layout = new javax.swing.GroupLayout(Wall28);
        Wall28.setLayout(Wall28Layout);
        Wall28Layout.setHorizontalGroup(
            Wall28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall28Layout.setVerticalGroup(
            Wall28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 21;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall28, gridBagConstraints);

        Wall29.setBackground(new java.awt.Color(0, 0, 0));
        Wall29.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall29.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall29Layout = new javax.swing.GroupLayout(Wall29);
        Wall29.setLayout(Wall29Layout);
        Wall29Layout.setHorizontalGroup(
            Wall29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall29Layout.setVerticalGroup(
            Wall29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 8;
        getContentPane().add(Wall29, gridBagConstraints);

        Wall30.setBackground(new java.awt.Color(0, 0, 0));
        Wall30.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall30.setPreferredSize(new java.awt.Dimension(20, 100));

        javax.swing.GroupLayout Wall30Layout = new javax.swing.GroupLayout(Wall30);
        Wall30.setLayout(Wall30Layout);
        Wall30Layout.setHorizontalGroup(
            Wall30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall30Layout.setVerticalGroup(
            Wall30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridheight = 5;
        getContentPane().add(Wall30, gridBagConstraints);

        Wall31.setBackground(new java.awt.Color(0, 0, 0));
        Wall31.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall31.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall31Layout = new javax.swing.GroupLayout(Wall31);
        Wall31.setLayout(Wall31Layout);
        Wall31Layout.setHorizontalGroup(
            Wall31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall31Layout.setVerticalGroup(
            Wall31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall31, gridBagConstraints);

        Wall32.setBackground(new java.awt.Color(0, 0, 0));
        Wall32.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall32.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall32Layout = new javax.swing.GroupLayout(Wall32);
        Wall32.setLayout(Wall32Layout);
        Wall32Layout.setHorizontalGroup(
            Wall32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall32Layout.setVerticalGroup(
            Wall32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 14;
        getContentPane().add(Wall32, gridBagConstraints);

        Wall33.setBackground(new java.awt.Color(0, 0, 0));
        Wall33.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall33.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall33Layout = new javax.swing.GroupLayout(Wall33);
        Wall33.setLayout(Wall33Layout);
        Wall33Layout.setHorizontalGroup(
            Wall33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall33Layout.setVerticalGroup(
            Wall33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 12;
        getContentPane().add(Wall33, gridBagConstraints);

        Wall34.setBackground(new java.awt.Color(0, 0, 0));
        Wall34.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall34.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall34Layout = new javax.swing.GroupLayout(Wall34);
        Wall34.setLayout(Wall34Layout);
        Wall34Layout.setHorizontalGroup(
            Wall34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall34Layout.setVerticalGroup(
            Wall34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 21;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall34, gridBagConstraints);

        Wall35.setBackground(new java.awt.Color(0, 0, 0));
        Wall35.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall35.setPreferredSize(new java.awt.Dimension(40, 20));

        javax.swing.GroupLayout Wall35Layout = new javax.swing.GroupLayout(Wall35);
        Wall35.setLayout(Wall35Layout);
        Wall35Layout.setHorizontalGroup(
            Wall35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall35Layout.setVerticalGroup(
            Wall35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridwidth = 2;
        getContentPane().add(Wall35, gridBagConstraints);

        Wall36.setBackground(new java.awt.Color(0, 0, 0));
        Wall36.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall36.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall36Layout = new javax.swing.GroupLayout(Wall36);
        Wall36.setLayout(Wall36Layout);
        Wall36Layout.setHorizontalGroup(
            Wall36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall36Layout.setVerticalGroup(
            Wall36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall36, gridBagConstraints);

        Wall37.setBackground(new java.awt.Color(0, 0, 0));
        Wall37.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall37.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall37Layout = new javax.swing.GroupLayout(Wall37);
        Wall37.setLayout(Wall37Layout);
        Wall37Layout.setHorizontalGroup(
            Wall37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall37Layout.setVerticalGroup(
            Wall37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall37, gridBagConstraints);

        Wall38.setBackground(new java.awt.Color(0, 0, 0));
        Wall38.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall38.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall38Layout = new javax.swing.GroupLayout(Wall38);
        Wall38.setLayout(Wall38Layout);
        Wall38Layout.setHorizontalGroup(
            Wall38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall38Layout.setVerticalGroup(
            Wall38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall38, gridBagConstraints);

        Wall39.setBackground(new java.awt.Color(0, 0, 0));
        Wall39.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall39.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall39Layout = new javax.swing.GroupLayout(Wall39);
        Wall39.setLayout(Wall39Layout);
        Wall39Layout.setHorizontalGroup(
            Wall39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall39Layout.setVerticalGroup(
            Wall39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 16;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall39, gridBagConstraints);

        Wall40.setBackground(new java.awt.Color(0, 0, 0));
        Wall40.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall40.setPreferredSize(new java.awt.Dimension(20, 60));

        javax.swing.GroupLayout Wall40Layout = new javax.swing.GroupLayout(Wall40);
        Wall40.setLayout(Wall40Layout);
        Wall40Layout.setHorizontalGroup(
            Wall40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall40Layout.setVerticalGroup(
            Wall40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 17;
        gridBagConstraints.gridheight = 3;
        getContentPane().add(Wall40, gridBagConstraints);

        Wall41.setBackground(new java.awt.Color(0, 0, 0));
        Wall41.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall41.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall41Layout = new javax.swing.GroupLayout(Wall41);
        Wall41.setLayout(Wall41Layout);
        Wall41Layout.setHorizontalGroup(
            Wall41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall41Layout.setVerticalGroup(
            Wall41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall41, gridBagConstraints);

        Wall42.setBackground(new java.awt.Color(0, 0, 0));
        Wall42.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall42.setPreferredSize(new java.awt.Dimension(60, 20));

        javax.swing.GroupLayout Wall42Layout = new javax.swing.GroupLayout(Wall42);
        Wall42.setLayout(Wall42Layout);
        Wall42Layout.setHorizontalGroup(
            Wall42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall42Layout.setVerticalGroup(
            Wall42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 18;
        gridBagConstraints.gridwidth = 3;
        getContentPane().add(Wall42, gridBagConstraints);

        Wall43.setBackground(new java.awt.Color(0, 0, 0));
        Wall43.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall43.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall43Layout = new javax.swing.GroupLayout(Wall43);
        Wall43.setLayout(Wall43Layout);
        Wall43Layout.setHorizontalGroup(
            Wall43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall43Layout.setVerticalGroup(
            Wall43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall43, gridBagConstraints);

        Wall44.setBackground(new java.awt.Color(0, 0, 0));
        Wall44.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall44.setPreferredSize(new java.awt.Dimension(100, 20));

        javax.swing.GroupLayout Wall44Layout = new javax.swing.GroupLayout(Wall44);
        Wall44.setLayout(Wall44Layout);
        Wall44Layout.setHorizontalGroup(
            Wall44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall44Layout.setVerticalGroup(
            Wall44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 5;
        getContentPane().add(Wall44, gridBagConstraints);

        Wall45.setBackground(new java.awt.Color(0, 0, 0));
        Wall45.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall45.setPreferredSize(new java.awt.Dimension(460, 20));

        javax.swing.GroupLayout Wall45Layout = new javax.swing.GroupLayout(Wall45);
        Wall45.setLayout(Wall45Layout);
        Wall45Layout.setHorizontalGroup(
            Wall45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall45Layout.setVerticalGroup(
            Wall45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 23;
        getContentPane().add(Wall45, gridBagConstraints);

        Wall46.setBackground(new java.awt.Color(0, 0, 0));
        Wall46.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall46.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout Wall46Layout = new javax.swing.GroupLayout(Wall46);
        Wall46.setLayout(Wall46Layout);
        Wall46Layout.setHorizontalGroup(
            Wall46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall46Layout.setVerticalGroup(
            Wall46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 6;
        getContentPane().add(Wall46, gridBagConstraints);

        Wall47.setBackground(new java.awt.Color(0, 0, 0));
        Wall47.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 255, 51), 2, true));
        Wall47.setPreferredSize(new java.awt.Dimension(20, 420));

        javax.swing.GroupLayout Wall47Layout = new javax.swing.GroupLayout(Wall47);
        Wall47.setLayout(Wall47Layout);
        Wall47Layout.setHorizontalGroup(
            Wall47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        Wall47Layout.setVerticalGroup(
            Wall47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = 21;
        getContentPane().add(Wall47, gridBagConstraints);

        Dot4.setBackground(new Color(0,0,0,0));
        Dot4.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot4Layout = new javax.swing.GroupLayout(Dot4);
        Dot4.setLayout(Dot4Layout);
        Dot4Layout.setHorizontalGroup(
            Dot4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot4Layout.createSequentialGroup()
                .addComponent(DotPic4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot4Layout.setVerticalGroup(
            Dot4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot4Layout.createSequentialGroup()
                .addComponent(DotPic4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot4, gridBagConstraints);

        Dot5.setBackground(new Color(0,0,0,0));
        Dot5.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot5Layout = new javax.swing.GroupLayout(Dot5);
        Dot5.setLayout(Dot5Layout);
        Dot5Layout.setHorizontalGroup(
            Dot5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot5Layout.createSequentialGroup()
                .addComponent(DotPic5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot5Layout.setVerticalGroup(
            Dot5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot5Layout.createSequentialGroup()
                .addComponent(DotPic5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot5, gridBagConstraints);

        Dot6.setBackground(new Color(0,0,0,0));
        Dot6.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot6Layout = new javax.swing.GroupLayout(Dot6);
        Dot6.setLayout(Dot6Layout);
        Dot6Layout.setHorizontalGroup(
            Dot6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot6Layout.createSequentialGroup()
                .addComponent(DotPic6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot6Layout.setVerticalGroup(
            Dot6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot6Layout.createSequentialGroup()
                .addComponent(DotPic6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot6, gridBagConstraints);

        Dot7.setBackground(new Color(0,0,0,0));
        Dot7.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot7Layout = new javax.swing.GroupLayout(Dot7);
        Dot7.setLayout(Dot7Layout);
        Dot7Layout.setHorizontalGroup(
            Dot7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot7Layout.createSequentialGroup()
                .addComponent(DotPic7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot7Layout.setVerticalGroup(
            Dot7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot7Layout.createSequentialGroup()
                .addComponent(DotPic7, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot7, gridBagConstraints);

        Dot8.setBackground(new Color(0,0,0,0));
        Dot8.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot8Layout = new javax.swing.GroupLayout(Dot8);
        Dot8.setLayout(Dot8Layout);
        Dot8Layout.setHorizontalGroup(
            Dot8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot8Layout.createSequentialGroup()
                .addComponent(DotPic8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot8Layout.setVerticalGroup(
            Dot8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot8Layout.createSequentialGroup()
                .addComponent(DotPic8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot8, gridBagConstraints);

        Dot9.setBackground(new Color(0,0,0,0));
        Dot9.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot9Layout = new javax.swing.GroupLayout(Dot9);
        Dot9.setLayout(Dot9Layout);
        Dot9Layout.setHorizontalGroup(
            Dot9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot9Layout.createSequentialGroup()
                .addComponent(DotPic9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot9Layout.setVerticalGroup(
            Dot9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot9Layout.createSequentialGroup()
                .addComponent(DotPic9, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot9, gridBagConstraints);

        Dot10.setBackground(new Color(0,0,0,0));
        Dot10.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot10Layout = new javax.swing.GroupLayout(Dot10);
        Dot10.setLayout(Dot10Layout);
        Dot10Layout.setHorizontalGroup(
            Dot10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot10Layout.createSequentialGroup()
                .addComponent(DotPic10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot10Layout.setVerticalGroup(
            Dot10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot10Layout.createSequentialGroup()
                .addComponent(DotPic10, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot10, gridBagConstraints);

        Dot11.setBackground(new Color(0,0,0,0));
        Dot11.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot11Layout = new javax.swing.GroupLayout(Dot11);
        Dot11.setLayout(Dot11Layout);
        Dot11Layout.setHorizontalGroup(
            Dot11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot11Layout.createSequentialGroup()
                .addComponent(DotPic11, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot11Layout.setVerticalGroup(
            Dot11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot11Layout.createSequentialGroup()
                .addComponent(DotPic11, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot11, gridBagConstraints);

        Dot12.setBackground(new Color(0,0,0,0));
        Dot12.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot12Layout = new javax.swing.GroupLayout(Dot12);
        Dot12.setLayout(Dot12Layout);
        Dot12Layout.setHorizontalGroup(
            Dot12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot12Layout.createSequentialGroup()
                .addComponent(DotPic12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot12Layout.setVerticalGroup(
            Dot12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot12Layout.createSequentialGroup()
                .addComponent(DotPic12, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot12, gridBagConstraints);

        Dot13.setBackground(new Color(0,0,0,0));
        Dot13.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot13Layout = new javax.swing.GroupLayout(Dot13);
        Dot13.setLayout(Dot13Layout);
        Dot13Layout.setHorizontalGroup(
            Dot13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot13Layout.createSequentialGroup()
                .addComponent(DotPic13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot13Layout.setVerticalGroup(
            Dot13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot13Layout.createSequentialGroup()
                .addComponent(DotPic13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot13, gridBagConstraints);

        Dot14.setBackground(new Color(0,0,0,0));
        Dot14.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot14Layout = new javax.swing.GroupLayout(Dot14);
        Dot14.setLayout(Dot14Layout);
        Dot14Layout.setHorizontalGroup(
            Dot14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot14Layout.createSequentialGroup()
                .addComponent(DotPic14, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot14Layout.setVerticalGroup(
            Dot14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot14Layout.createSequentialGroup()
                .addComponent(DotPic14, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 10;
        getContentPane().add(Dot14, gridBagConstraints);

        Dot15.setBackground(new Color(0,0,0,0));
        Dot15.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot15Layout = new javax.swing.GroupLayout(Dot15);
        Dot15.setLayout(Dot15Layout);
        Dot15Layout.setHorizontalGroup(
            Dot15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot15Layout.createSequentialGroup()
                .addComponent(DotPic15, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot15Layout.setVerticalGroup(
            Dot15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot15Layout.createSequentialGroup()
                .addComponent(DotPic15, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot15, gridBagConstraints);

        Dot16.setBackground(new Color(0,0,0,0));
        Dot16.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot16Layout = new javax.swing.GroupLayout(Dot16);
        Dot16.setLayout(Dot16Layout);
        Dot16Layout.setHorizontalGroup(
            Dot16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot16Layout.createSequentialGroup()
                .addComponent(DotPic16, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot16Layout.setVerticalGroup(
            Dot16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot16Layout.createSequentialGroup()
                .addComponent(DotPic16, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 21;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot16, gridBagConstraints);

        Dot17.setBackground(new Color(0,0,0,0));
        Dot17.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot17Layout = new javax.swing.GroupLayout(Dot17);
        Dot17.setLayout(Dot17Layout);
        Dot17Layout.setHorizontalGroup(
            Dot17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot17Layout.createSequentialGroup()
                .addComponent(DotPic17, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot17Layout.setVerticalGroup(
            Dot17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot17Layout.createSequentialGroup()
                .addComponent(DotPic17, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot17, gridBagConstraints);

        Dot18.setBackground(new Color(0,0,0,0));
        Dot18.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot18Layout = new javax.swing.GroupLayout(Dot18);
        Dot18.setLayout(Dot18Layout);
        Dot18Layout.setHorizontalGroup(
            Dot18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot18Layout.createSequentialGroup()
                .addComponent(DotPic18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot18Layout.setVerticalGroup(
            Dot18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot18Layout.createSequentialGroup()
                .addComponent(DotPic18, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot18, gridBagConstraints);

        Dot19.setBackground(new Color(0,0,0,0));
        Dot19.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot19Layout = new javax.swing.GroupLayout(Dot19);
        Dot19.setLayout(Dot19Layout);
        Dot19Layout.setHorizontalGroup(
            Dot19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot19Layout.createSequentialGroup()
                .addComponent(DotPic19, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot19Layout.setVerticalGroup(
            Dot19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot19Layout.createSequentialGroup()
                .addComponent(DotPic19, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot19, gridBagConstraints);

        Dot20.setBackground(new Color(0,0,0,0));
        Dot20.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot20Layout = new javax.swing.GroupLayout(Dot20);
        Dot20.setLayout(Dot20Layout);
        Dot20Layout.setHorizontalGroup(
            Dot20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot20Layout.createSequentialGroup()
                .addComponent(DotPic20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot20Layout.setVerticalGroup(
            Dot20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot20Layout.createSequentialGroup()
                .addComponent(DotPic20, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot20, gridBagConstraints);

        Dot21.setBackground(new Color(0,0,0,0));
        Dot21.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot21Layout = new javax.swing.GroupLayout(Dot21);
        Dot21.setLayout(Dot21Layout);
        Dot21Layout.setHorizontalGroup(
            Dot21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot21Layout.createSequentialGroup()
                .addComponent(DotPic21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot21Layout.setVerticalGroup(
            Dot21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot21Layout.createSequentialGroup()
                .addComponent(DotPic21, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot21, gridBagConstraints);

        Dot22.setBackground(new Color(0,0,0,0));
        Dot22.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot22Layout = new javax.swing.GroupLayout(Dot22);
        Dot22.setLayout(Dot22Layout);
        Dot22Layout.setHorizontalGroup(
            Dot22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot22Layout.createSequentialGroup()
                .addComponent(DotPic22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot22Layout.setVerticalGroup(
            Dot22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot22Layout.createSequentialGroup()
                .addComponent(DotPic22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot22, gridBagConstraints);

        Dot23.setBackground(new Color(0,0,0,0));
        Dot23.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N
        DotPic23.setToolTipText("");

        javax.swing.GroupLayout Dot23Layout = new javax.swing.GroupLayout(Dot23);
        Dot23.setLayout(Dot23Layout);
        Dot23Layout.setHorizontalGroup(
            Dot23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot23Layout.createSequentialGroup()
                .addComponent(DotPic23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot23Layout.setVerticalGroup(
            Dot23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot23Layout.createSequentialGroup()
                .addComponent(DotPic23, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot23, gridBagConstraints);

        Dot24.setBackground(new Color(0,0,0,0));
        Dot24.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot24Layout = new javax.swing.GroupLayout(Dot24);
        Dot24.setLayout(Dot24Layout);
        Dot24Layout.setHorizontalGroup(
            Dot24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot24Layout.createSequentialGroup()
                .addComponent(DotPic24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot24Layout.setVerticalGroup(
            Dot24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot24Layout.createSequentialGroup()
                .addComponent(DotPic24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot24, gridBagConstraints);

        Dot25.setBackground(new Color(0,0,0,0));
        Dot25.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot25Layout = new javax.swing.GroupLayout(Dot25);
        Dot25.setLayout(Dot25Layout);
        Dot25Layout.setHorizontalGroup(
            Dot25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot25Layout.createSequentialGroup()
                .addComponent(DotPic25, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot25Layout.setVerticalGroup(
            Dot25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot25Layout.createSequentialGroup()
                .addComponent(DotPic25, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot25, gridBagConstraints);

        Dot26.setBackground(new Color(0,0,0,0));
        Dot26.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot26Layout = new javax.swing.GroupLayout(Dot26);
        Dot26.setLayout(Dot26Layout);
        Dot26Layout.setHorizontalGroup(
            Dot26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot26Layout.createSequentialGroup()
                .addComponent(DotPic26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot26Layout.setVerticalGroup(
            Dot26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot26Layout.createSequentialGroup()
                .addComponent(DotPic26, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 21;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot26, gridBagConstraints);

        Dot27.setBackground(new Color(0,0,0,0));
        Dot27.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot27Layout = new javax.swing.GroupLayout(Dot27);
        Dot27.setLayout(Dot27Layout);
        Dot27Layout.setHorizontalGroup(
            Dot27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot27Layout.createSequentialGroup()
                .addComponent(DotPic27, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot27Layout.setVerticalGroup(
            Dot27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot27Layout.createSequentialGroup()
                .addComponent(DotPic27, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot27, gridBagConstraints);

        Dot28.setBackground(new Color(0,0,0,0));
        Dot28.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot28Layout = new javax.swing.GroupLayout(Dot28);
        Dot28.setLayout(Dot28Layout);
        Dot28Layout.setHorizontalGroup(
            Dot28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot28Layout.createSequentialGroup()
                .addComponent(DotPic28, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot28Layout.setVerticalGroup(
            Dot28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot28Layout.createSequentialGroup()
                .addComponent(DotPic28, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 6;
        getContentPane().add(Dot28, gridBagConstraints);

        Dot29.setBackground(new Color(0,0,0,0));
        Dot29.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot29Layout = new javax.swing.GroupLayout(Dot29);
        Dot29.setLayout(Dot29Layout);
        Dot29Layout.setHorizontalGroup(
            Dot29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot29Layout.createSequentialGroup()
                .addComponent(DotPic29, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot29Layout.setVerticalGroup(
            Dot29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot29Layout.createSequentialGroup()
                .addComponent(DotPic29, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot29, gridBagConstraints);

        Dot30.setBackground(new Color(0,0,0,0));
        Dot30.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot30Layout = new javax.swing.GroupLayout(Dot30);
        Dot30.setLayout(Dot30Layout);
        Dot30Layout.setHorizontalGroup(
            Dot30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot30Layout.createSequentialGroup()
                .addComponent(DotPic30, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot30Layout.setVerticalGroup(
            Dot30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot30Layout.createSequentialGroup()
                .addComponent(DotPic30, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot30, gridBagConstraints);

        Dot31.setBackground(new Color(0,0,0,0));
        Dot31.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic31.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot31Layout = new javax.swing.GroupLayout(Dot31);
        Dot31.setLayout(Dot31Layout);
        Dot31Layout.setHorizontalGroup(
            Dot31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot31Layout.createSequentialGroup()
                .addComponent(DotPic31, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot31Layout.setVerticalGroup(
            Dot31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot31Layout.createSequentialGroup()
                .addComponent(DotPic31, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 21;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot31, gridBagConstraints);

        Dot32.setBackground(new Color(0,0,0,0));
        Dot32.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot32Layout = new javax.swing.GroupLayout(Dot32);
        Dot32.setLayout(Dot32Layout);
        Dot32Layout.setHorizontalGroup(
            Dot32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot32Layout.createSequentialGroup()
                .addComponent(DotPic32, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot32Layout.setVerticalGroup(
            Dot32Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot32Layout.createSequentialGroup()
                .addComponent(DotPic32, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot32, gridBagConstraints);

        Dot33.setBackground(new Color(0,0,0,0));
        Dot33.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot33Layout = new javax.swing.GroupLayout(Dot33);
        Dot33.setLayout(Dot33Layout);
        Dot33Layout.setHorizontalGroup(
            Dot33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot33Layout.createSequentialGroup()
                .addComponent(DotPic33, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot33Layout.setVerticalGroup(
            Dot33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot33Layout.createSequentialGroup()
                .addComponent(DotPic33, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot33, gridBagConstraints);

        Dot34.setBackground(new Color(0,0,0,0));
        Dot34.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot34Layout = new javax.swing.GroupLayout(Dot34);
        Dot34.setLayout(Dot34Layout);
        Dot34Layout.setHorizontalGroup(
            Dot34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot34Layout.createSequentialGroup()
                .addComponent(DotPic34, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot34Layout.setVerticalGroup(
            Dot34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot34Layout.createSequentialGroup()
                .addComponent(DotPic34, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot34, gridBagConstraints);

        Dot35.setBackground(new Color(0,0,0,0));
        Dot35.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot35Layout = new javax.swing.GroupLayout(Dot35);
        Dot35.setLayout(Dot35Layout);
        Dot35Layout.setHorizontalGroup(
            Dot35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot35Layout.createSequentialGroup()
                .addComponent(DotPic35, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot35Layout.setVerticalGroup(
            Dot35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot35Layout.createSequentialGroup()
                .addComponent(DotPic35, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot35, gridBagConstraints);

        Dot36.setBackground(new Color(0,0,0,0));
        Dot36.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot36Layout = new javax.swing.GroupLayout(Dot36);
        Dot36.setLayout(Dot36Layout);
        Dot36Layout.setHorizontalGroup(
            Dot36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot36Layout.createSequentialGroup()
                .addComponent(DotPic36, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot36Layout.setVerticalGroup(
            Dot36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot36Layout.createSequentialGroup()
                .addComponent(DotPic36, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot36, gridBagConstraints);

        Dot37.setBackground(new Color(0,0,0,0));
        Dot37.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot37Layout = new javax.swing.GroupLayout(Dot37);
        Dot37.setLayout(Dot37Layout);
        Dot37Layout.setHorizontalGroup(
            Dot37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot37Layout.createSequentialGroup()
                .addComponent(DotPic37, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot37Layout.setVerticalGroup(
            Dot37Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot37Layout.createSequentialGroup()
                .addComponent(DotPic37, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot37, gridBagConstraints);

        Dot38.setBackground(new Color(0,0,0,0));
        Dot38.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic38.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot38Layout = new javax.swing.GroupLayout(Dot38);
        Dot38.setLayout(Dot38Layout);
        Dot38Layout.setHorizontalGroup(
            Dot38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot38Layout.createSequentialGroup()
                .addComponent(DotPic38, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot38Layout.setVerticalGroup(
            Dot38Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot38Layout.createSequentialGroup()
                .addComponent(DotPic38, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot38, gridBagConstraints);

        Dot39.setBackground(new Color(0,0,0,0));
        Dot39.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic39.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot39Layout = new javax.swing.GroupLayout(Dot39);
        Dot39.setLayout(Dot39Layout);
        Dot39Layout.setHorizontalGroup(
            Dot39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot39Layout.createSequentialGroup()
                .addComponent(DotPic39, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot39Layout.setVerticalGroup(
            Dot39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot39Layout.createSequentialGroup()
                .addComponent(DotPic39, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 8;
        getContentPane().add(Dot39, gridBagConstraints);

        Dot40.setBackground(new Color(0,0,0,0));
        Dot40.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic40.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot40Layout = new javax.swing.GroupLayout(Dot40);
        Dot40.setLayout(Dot40Layout);
        Dot40Layout.setHorizontalGroup(
            Dot40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot40Layout.createSequentialGroup()
                .addComponent(DotPic40, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot40Layout.setVerticalGroup(
            Dot40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot40Layout.createSequentialGroup()
                .addComponent(DotPic40, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 10;
        getContentPane().add(Dot40, gridBagConstraints);

        Dot41.setBackground(new Color(0,0,0,0));
        Dot41.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic41.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot41Layout = new javax.swing.GroupLayout(Dot41);
        Dot41.setLayout(Dot41Layout);
        Dot41Layout.setHorizontalGroup(
            Dot41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot41Layout.createSequentialGroup()
                .addComponent(DotPic41, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot41Layout.setVerticalGroup(
            Dot41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot41Layout.createSequentialGroup()
                .addComponent(DotPic41, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot41, gridBagConstraints);

        Dot42.setBackground(new Color(0,0,0,0));
        Dot42.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic42.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot42Layout = new javax.swing.GroupLayout(Dot42);
        Dot42.setLayout(Dot42Layout);
        Dot42Layout.setHorizontalGroup(
            Dot42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot42Layout.createSequentialGroup()
                .addComponent(DotPic42, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot42Layout.setVerticalGroup(
            Dot42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot42Layout.createSequentialGroup()
                .addComponent(DotPic42, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 12;
        getContentPane().add(Dot42, gridBagConstraints);

        Dot43.setBackground(new Color(0,0,0,0));
        Dot43.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic43.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot43Layout = new javax.swing.GroupLayout(Dot43);
        Dot43.setLayout(Dot43Layout);
        Dot43Layout.setHorizontalGroup(
            Dot43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot43Layout.createSequentialGroup()
                .addComponent(DotPic43, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot43Layout.setVerticalGroup(
            Dot43Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot43Layout.createSequentialGroup()
                .addComponent(DotPic43, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot43, gridBagConstraints);

        Dot44.setBackground(new Color(0,0,0,0));
        Dot44.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic44.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot44Layout = new javax.swing.GroupLayout(Dot44);
        Dot44.setLayout(Dot44Layout);
        Dot44Layout.setHorizontalGroup(
            Dot44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot44Layout.createSequentialGroup()
                .addComponent(DotPic44, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot44Layout.setVerticalGroup(
            Dot44Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot44Layout.createSequentialGroup()
                .addComponent(DotPic44, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot44, gridBagConstraints);

        Dot45.setBackground(new Color(0,0,0,0));
        Dot45.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic45.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot45Layout = new javax.swing.GroupLayout(Dot45);
        Dot45.setLayout(Dot45Layout);
        Dot45Layout.setHorizontalGroup(
            Dot45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot45Layout.createSequentialGroup()
                .addComponent(DotPic45, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot45Layout.setVerticalGroup(
            Dot45Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot45Layout.createSequentialGroup()
                .addComponent(DotPic45, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot45, gridBagConstraints);

        Dot46.setBackground(new Color(0,0,0,0));
        Dot46.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic46.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot46Layout = new javax.swing.GroupLayout(Dot46);
        Dot46.setLayout(Dot46Layout);
        Dot46Layout.setHorizontalGroup(
            Dot46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot46Layout.createSequentialGroup()
                .addComponent(DotPic46, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot46Layout.setVerticalGroup(
            Dot46Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot46Layout.createSequentialGroup()
                .addComponent(DotPic46, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot46, gridBagConstraints);

        Dot47.setBackground(new Color(0,0,0,0));
        Dot47.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic47.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot47Layout = new javax.swing.GroupLayout(Dot47);
        Dot47.setLayout(Dot47Layout);
        Dot47Layout.setHorizontalGroup(
            Dot47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot47Layout.createSequentialGroup()
                .addComponent(DotPic47, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot47Layout.setVerticalGroup(
            Dot47Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot47Layout.createSequentialGroup()
                .addComponent(DotPic47, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot47, gridBagConstraints);

        Dot48.setBackground(new Color(0,0,0,0));
        Dot48.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic48.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot48Layout = new javax.swing.GroupLayout(Dot48);
        Dot48.setLayout(Dot48Layout);
        Dot48Layout.setHorizontalGroup(
            Dot48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot48Layout.createSequentialGroup()
                .addComponent(DotPic48, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot48Layout.setVerticalGroup(
            Dot48Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot48Layout.createSequentialGroup()
                .addComponent(DotPic48, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot48, gridBagConstraints);

        Dot49.setBackground(new Color(0,0,0,0));
        Dot49.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic49.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot49Layout = new javax.swing.GroupLayout(Dot49);
        Dot49.setLayout(Dot49Layout);
        Dot49Layout.setHorizontalGroup(
            Dot49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot49Layout.createSequentialGroup()
                .addComponent(DotPic49, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot49Layout.setVerticalGroup(
            Dot49Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot49Layout.createSequentialGroup()
                .addComponent(DotPic49, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot49, gridBagConstraints);

        Dot50.setBackground(new Color(0,0,0,0));
        Dot50.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic50.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot50Layout = new javax.swing.GroupLayout(Dot50);
        Dot50.setLayout(Dot50Layout);
        Dot50Layout.setHorizontalGroup(
            Dot50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot50Layout.createSequentialGroup()
                .addComponent(DotPic50, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot50Layout.setVerticalGroup(
            Dot50Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot50Layout.createSequentialGroup()
                .addComponent(DotPic50, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot50, gridBagConstraints);

        Dot51.setBackground(new Color(0,0,0,0));
        Dot51.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic51.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot51Layout = new javax.swing.GroupLayout(Dot51);
        Dot51.setLayout(Dot51Layout);
        Dot51Layout.setHorizontalGroup(
            Dot51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot51Layout.createSequentialGroup()
                .addComponent(DotPic51, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot51Layout.setVerticalGroup(
            Dot51Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot51Layout.createSequentialGroup()
                .addComponent(DotPic51, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot51, gridBagConstraints);

        Dot52.setBackground(new Color(0,0,0,0));
        Dot52.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic52.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot52Layout = new javax.swing.GroupLayout(Dot52);
        Dot52.setLayout(Dot52Layout);
        Dot52Layout.setHorizontalGroup(
            Dot52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot52Layout.createSequentialGroup()
                .addComponent(DotPic52, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot52Layout.setVerticalGroup(
            Dot52Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot52Layout.createSequentialGroup()
                .addComponent(DotPic52, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot52, gridBagConstraints);

        Dot53.setBackground(new Color(0,0,0,0));
        Dot53.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic53.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot53Layout = new javax.swing.GroupLayout(Dot53);
        Dot53.setLayout(Dot53Layout);
        Dot53Layout.setHorizontalGroup(
            Dot53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot53Layout.createSequentialGroup()
                .addComponent(DotPic53, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot53Layout.setVerticalGroup(
            Dot53Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot53Layout.createSequentialGroup()
                .addComponent(DotPic53, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot53, gridBagConstraints);

        Dot54.setBackground(new Color(0,0,0,0));
        Dot54.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic54.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot54Layout = new javax.swing.GroupLayout(Dot54);
        Dot54.setLayout(Dot54Layout);
        Dot54Layout.setHorizontalGroup(
            Dot54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot54Layout.createSequentialGroup()
                .addComponent(DotPic54, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot54Layout.setVerticalGroup(
            Dot54Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot54Layout.createSequentialGroup()
                .addComponent(DotPic54, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 23;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot54, gridBagConstraints);

        Dot55.setBackground(new Color(0,0,0,0));
        Dot55.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic55.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot55Layout = new javax.swing.GroupLayout(Dot55);
        Dot55.setLayout(Dot55Layout);
        Dot55Layout.setHorizontalGroup(
            Dot55Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot55Layout.createSequentialGroup()
                .addComponent(DotPic55, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot55Layout.setVerticalGroup(
            Dot55Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot55Layout.createSequentialGroup()
                .addComponent(DotPic55, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot55, gridBagConstraints);

        Dot56.setBackground(new Color(0,0,0,0));
        Dot56.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic56.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot56Layout = new javax.swing.GroupLayout(Dot56);
        Dot56.setLayout(Dot56Layout);
        Dot56Layout.setHorizontalGroup(
            Dot56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot56Layout.createSequentialGroup()
                .addComponent(DotPic56, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot56Layout.setVerticalGroup(
            Dot56Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot56Layout.createSequentialGroup()
                .addComponent(DotPic56, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot56, gridBagConstraints);

        Dot57.setBackground(new Color(0,0,0,0));
        Dot57.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic57.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot57Layout = new javax.swing.GroupLayout(Dot57);
        Dot57.setLayout(Dot57Layout);
        Dot57Layout.setHorizontalGroup(
            Dot57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot57Layout.createSequentialGroup()
                .addComponent(DotPic57, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot57Layout.setVerticalGroup(
            Dot57Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot57Layout.createSequentialGroup()
                .addComponent(DotPic57, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot57, gridBagConstraints);

        Dot58.setBackground(new Color(0,0,0,0));
        Dot58.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic58.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot58Layout = new javax.swing.GroupLayout(Dot58);
        Dot58.setLayout(Dot58Layout);
        Dot58Layout.setHorizontalGroup(
            Dot58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot58Layout.createSequentialGroup()
                .addComponent(DotPic58, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot58Layout.setVerticalGroup(
            Dot58Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot58Layout.createSequentialGroup()
                .addComponent(DotPic58, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 22;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot58, gridBagConstraints);

        Dot59.setBackground(new Color(0,0,0,0));
        Dot59.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic59.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot59Layout = new javax.swing.GroupLayout(Dot59);
        Dot59.setLayout(Dot59Layout);
        Dot59Layout.setHorizontalGroup(
            Dot59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot59Layout.createSequentialGroup()
                .addComponent(DotPic59, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot59Layout.setVerticalGroup(
            Dot59Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot59Layout.createSequentialGroup()
                .addComponent(DotPic59, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot59, gridBagConstraints);

        Dot60.setBackground(new Color(0,0,0,0));
        Dot60.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic60.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot60Layout = new javax.swing.GroupLayout(Dot60);
        Dot60.setLayout(Dot60Layout);
        Dot60Layout.setHorizontalGroup(
            Dot60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot60Layout.createSequentialGroup()
                .addComponent(DotPic60, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot60Layout.setVerticalGroup(
            Dot60Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot60Layout.createSequentialGroup()
                .addComponent(DotPic60, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot60, gridBagConstraints);

        Dot61.setBackground(new Color(0,0,0,0));
        Dot61.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic61.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot61Layout = new javax.swing.GroupLayout(Dot61);
        Dot61.setLayout(Dot61Layout);
        Dot61Layout.setHorizontalGroup(
            Dot61Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot61Layout.createSequentialGroup()
                .addComponent(DotPic61, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot61Layout.setVerticalGroup(
            Dot61Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot61Layout.createSequentialGroup()
                .addComponent(DotPic61, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot61, gridBagConstraints);

        Dot62.setBackground(new Color(0,0,0,0));
        Dot62.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic62.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot62Layout = new javax.swing.GroupLayout(Dot62);
        Dot62.setLayout(Dot62Layout);
        Dot62Layout.setHorizontalGroup(
            Dot62Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot62Layout.createSequentialGroup()
                .addComponent(DotPic62, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot62Layout.setVerticalGroup(
            Dot62Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot62Layout.createSequentialGroup()
                .addComponent(DotPic62, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot62, gridBagConstraints);

        Dot63.setBackground(new Color(0,0,0,0));
        Dot63.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic63.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot63Layout = new javax.swing.GroupLayout(Dot63);
        Dot63.setLayout(Dot63Layout);
        Dot63Layout.setHorizontalGroup(
            Dot63Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot63Layout.createSequentialGroup()
                .addComponent(DotPic63, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot63Layout.setVerticalGroup(
            Dot63Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot63Layout.createSequentialGroup()
                .addComponent(DotPic63, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot63, gridBagConstraints);

        Dot64.setBackground(new Color(0,0,0,0));
        Dot64.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic64.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot64Layout = new javax.swing.GroupLayout(Dot64);
        Dot64.setLayout(Dot64Layout);
        Dot64Layout.setHorizontalGroup(
            Dot64Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot64Layout.createSequentialGroup()
                .addComponent(DotPic64, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot64Layout.setVerticalGroup(
            Dot64Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot64Layout.createSequentialGroup()
                .addComponent(DotPic64, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot64, gridBagConstraints);

        Dot65.setBackground(new Color(0,0,0,0));
        Dot65.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic65.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot65Layout = new javax.swing.GroupLayout(Dot65);
        Dot65.setLayout(Dot65Layout);
        Dot65Layout.setHorizontalGroup(
            Dot65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot65Layout.createSequentialGroup()
                .addComponent(DotPic65, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot65Layout.setVerticalGroup(
            Dot65Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot65Layout.createSequentialGroup()
                .addComponent(DotPic65, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot65, gridBagConstraints);

        Dot66.setBackground(new Color(0,0,0,0));
        Dot66.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic66.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot66Layout = new javax.swing.GroupLayout(Dot66);
        Dot66.setLayout(Dot66Layout);
        Dot66Layout.setHorizontalGroup(
            Dot66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot66Layout.createSequentialGroup()
                .addComponent(DotPic66, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot66Layout.setVerticalGroup(
            Dot66Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot66Layout.createSequentialGroup()
                .addComponent(DotPic66, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot66, gridBagConstraints);

        Dot67.setBackground(new Color(0,0,0,0));
        Dot67.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic67.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot67Layout = new javax.swing.GroupLayout(Dot67);
        Dot67.setLayout(Dot67Layout);
        Dot67Layout.setHorizontalGroup(
            Dot67Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot67Layout.createSequentialGroup()
                .addComponent(DotPic67, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot67Layout.setVerticalGroup(
            Dot67Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot67Layout.createSequentialGroup()
                .addComponent(DotPic67, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot67, gridBagConstraints);

        Dot68.setBackground(new Color(0,0,0,0));
        Dot68.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic68.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot68Layout = new javax.swing.GroupLayout(Dot68);
        Dot68.setLayout(Dot68Layout);
        Dot68Layout.setHorizontalGroup(
            Dot68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot68Layout.createSequentialGroup()
                .addComponent(DotPic68, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot68Layout.setVerticalGroup(
            Dot68Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot68Layout.createSequentialGroup()
                .addComponent(DotPic68, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot68, gridBagConstraints);

        Dot69.setBackground(new Color(0,0,0,0));
        Dot69.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic69.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot69Layout = new javax.swing.GroupLayout(Dot69);
        Dot69.setLayout(Dot69Layout);
        Dot69Layout.setHorizontalGroup(
            Dot69Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot69Layout.createSequentialGroup()
                .addComponent(DotPic69, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot69Layout.setVerticalGroup(
            Dot69Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot69Layout.createSequentialGroup()
                .addComponent(DotPic69, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 18;
        getContentPane().add(Dot69, gridBagConstraints);

        Dot70.setBackground(new Color(0,0,0,0));
        Dot70.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic70.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot70Layout = new javax.swing.GroupLayout(Dot70);
        Dot70.setLayout(Dot70Layout);
        Dot70Layout.setHorizontalGroup(
            Dot70Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot70Layout.createSequentialGroup()
                .addComponent(DotPic70, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot70Layout.setVerticalGroup(
            Dot70Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot70Layout.createSequentialGroup()
                .addComponent(DotPic70, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot70, gridBagConstraints);

        Dot71.setBackground(new Color(0,0,0,0));
        Dot71.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic71.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot71Layout = new javax.swing.GroupLayout(Dot71);
        Dot71.setLayout(Dot71Layout);
        Dot71Layout.setHorizontalGroup(
            Dot71Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot71Layout.createSequentialGroup()
                .addComponent(DotPic71, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot71Layout.setVerticalGroup(
            Dot71Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot71Layout.createSequentialGroup()
                .addComponent(DotPic71, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 18;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot71, gridBagConstraints);

        Dot72.setBackground(new Color(0,0,0,0));
        Dot72.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic72.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot72Layout = new javax.swing.GroupLayout(Dot72);
        Dot72.setLayout(Dot72Layout);
        Dot72Layout.setHorizontalGroup(
            Dot72Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot72Layout.createSequentialGroup()
                .addComponent(DotPic72, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot72Layout.setVerticalGroup(
            Dot72Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot72Layout.createSequentialGroup()
                .addComponent(DotPic72, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot72, gridBagConstraints);

        Dot73.setBackground(new Color(0,0,0,0));
        Dot73.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic73.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot73Layout = new javax.swing.GroupLayout(Dot73);
        Dot73.setLayout(Dot73Layout);
        Dot73Layout.setHorizontalGroup(
            Dot73Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot73Layout.createSequentialGroup()
                .addComponent(DotPic73, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot73Layout.setVerticalGroup(
            Dot73Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot73Layout.createSequentialGroup()
                .addComponent(DotPic73, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 12;
        getContentPane().add(Dot73, gridBagConstraints);

        Dot74.setBackground(new Color(0,0,0,0));
        Dot74.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic74.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot74Layout = new javax.swing.GroupLayout(Dot74);
        Dot74.setLayout(Dot74Layout);
        Dot74Layout.setHorizontalGroup(
            Dot74Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot74Layout.createSequentialGroup()
                .addComponent(DotPic74, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot74Layout.setVerticalGroup(
            Dot74Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot74Layout.createSequentialGroup()
                .addComponent(DotPic74, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot74, gridBagConstraints);

        Dot75.setBackground(new Color(0,0,0,0));
        Dot75.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic75.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot75Layout = new javax.swing.GroupLayout(Dot75);
        Dot75.setLayout(Dot75Layout);
        Dot75Layout.setHorizontalGroup(
            Dot75Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot75Layout.createSequentialGroup()
                .addComponent(DotPic75, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot75Layout.setVerticalGroup(
            Dot75Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot75Layout.createSequentialGroup()
                .addComponent(DotPic75, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot75, gridBagConstraints);

        Dot76.setBackground(new Color(0,0,0,0));
        Dot76.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic76.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot76Layout = new javax.swing.GroupLayout(Dot76);
        Dot76.setLayout(Dot76Layout);
        Dot76Layout.setHorizontalGroup(
            Dot76Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot76Layout.createSequentialGroup()
                .addComponent(DotPic76, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot76Layout.setVerticalGroup(
            Dot76Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot76Layout.createSequentialGroup()
                .addComponent(DotPic76, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 16;
        getContentPane().add(Dot76, gridBagConstraints);

        Dot77.setBackground(new Color(0,0,0,0));
        Dot77.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic77.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot77Layout = new javax.swing.GroupLayout(Dot77);
        Dot77.setLayout(Dot77Layout);
        Dot77Layout.setHorizontalGroup(
            Dot77Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot77Layout.createSequentialGroup()
                .addComponent(DotPic77, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot77Layout.setVerticalGroup(
            Dot77Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot77Layout.createSequentialGroup()
                .addComponent(DotPic77, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot77, gridBagConstraints);

        Dot78.setBackground(new Color(0,0,0,0));
        Dot78.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic78.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot78Layout = new javax.swing.GroupLayout(Dot78);
        Dot78.setLayout(Dot78Layout);
        Dot78Layout.setHorizontalGroup(
            Dot78Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot78Layout.createSequentialGroup()
                .addComponent(DotPic78, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot78Layout.setVerticalGroup(
            Dot78Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot78Layout.createSequentialGroup()
                .addComponent(DotPic78, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 10;
        getContentPane().add(Dot78, gridBagConstraints);

        Dot79.setBackground(new Color(0,0,0,0));
        Dot79.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic79.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot79Layout = new javax.swing.GroupLayout(Dot79);
        Dot79.setLayout(Dot79Layout);
        Dot79Layout.setHorizontalGroup(
            Dot79Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot79Layout.createSequentialGroup()
                .addComponent(DotPic79, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot79Layout.setVerticalGroup(
            Dot79Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot79Layout.createSequentialGroup()
                .addComponent(DotPic79, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot79, gridBagConstraints);

        Dot80.setBackground(new Color(0,0,0,0));
        Dot80.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic80.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot80Layout = new javax.swing.GroupLayout(Dot80);
        Dot80.setLayout(Dot80Layout);
        Dot80Layout.setHorizontalGroup(
            Dot80Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot80Layout.createSequentialGroup()
                .addComponent(DotPic80, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot80Layout.setVerticalGroup(
            Dot80Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot80Layout.createSequentialGroup()
                .addComponent(DotPic80, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot80, gridBagConstraints);

        Dot81.setBackground(new Color(0,0,0,0));
        Dot81.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic81.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot81Layout = new javax.swing.GroupLayout(Dot81);
        Dot81.setLayout(Dot81Layout);
        Dot81Layout.setHorizontalGroup(
            Dot81Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot81Layout.createSequentialGroup()
                .addComponent(DotPic81, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot81Layout.setVerticalGroup(
            Dot81Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot81Layout.createSequentialGroup()
                .addComponent(DotPic81, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 13;
        getContentPane().add(Dot81, gridBagConstraints);

        Dot82.setBackground(new Color(0,0,0,0));
        Dot82.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic82.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot82Layout = new javax.swing.GroupLayout(Dot82);
        Dot82.setLayout(Dot82Layout);
        Dot82Layout.setHorizontalGroup(
            Dot82Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot82Layout.createSequentialGroup()
                .addComponent(DotPic82, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot82Layout.setVerticalGroup(
            Dot82Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot82Layout.createSequentialGroup()
                .addComponent(DotPic82, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot82, gridBagConstraints);

        Dot83.setBackground(new Color(0,0,0,0));
        Dot83.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic83.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot83Layout = new javax.swing.GroupLayout(Dot83);
        Dot83.setLayout(Dot83Layout);
        Dot83Layout.setHorizontalGroup(
            Dot83Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot83Layout.createSequentialGroup()
                .addComponent(DotPic83, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot83Layout.setVerticalGroup(
            Dot83Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot83Layout.createSequentialGroup()
                .addComponent(DotPic83, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 10;
        getContentPane().add(Dot83, gridBagConstraints);

        Dot84.setBackground(new Color(0,0,0,0));
        Dot84.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic84.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot84Layout = new javax.swing.GroupLayout(Dot84);
        Dot84.setLayout(Dot84Layout);
        Dot84Layout.setHorizontalGroup(
            Dot84Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot84Layout.createSequentialGroup()
                .addComponent(DotPic84, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot84Layout.setVerticalGroup(
            Dot84Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot84Layout.createSequentialGroup()
                .addComponent(DotPic84, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot84, gridBagConstraints);

        Dot85.setBackground(new Color(0,0,0,0));
        Dot85.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic85.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot85Layout = new javax.swing.GroupLayout(Dot85);
        Dot85.setLayout(Dot85Layout);
        Dot85Layout.setHorizontalGroup(
            Dot85Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot85Layout.createSequentialGroup()
                .addComponent(DotPic85, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot85Layout.setVerticalGroup(
            Dot85Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot85Layout.createSequentialGroup()
                .addComponent(DotPic85, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 8;
        getContentPane().add(Dot85, gridBagConstraints);

        Dot86.setBackground(new Color(0,0,0,0));
        Dot86.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic86.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot86Layout = new javax.swing.GroupLayout(Dot86);
        Dot86.setLayout(Dot86Layout);
        Dot86Layout.setHorizontalGroup(
            Dot86Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot86Layout.createSequentialGroup()
                .addComponent(DotPic86, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot86Layout.setVerticalGroup(
            Dot86Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot86Layout.createSequentialGroup()
                .addComponent(DotPic86, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot86, gridBagConstraints);

        Dot87.setBackground(new Color(0,0,0,0));
        Dot87.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic87.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot87Layout = new javax.swing.GroupLayout(Dot87);
        Dot87.setLayout(Dot87Layout);
        Dot87Layout.setHorizontalGroup(
            Dot87Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot87Layout.createSequentialGroup()
                .addComponent(DotPic87, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot87Layout.setVerticalGroup(
            Dot87Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot87Layout.createSequentialGroup()
                .addComponent(DotPic87, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot87, gridBagConstraints);

        Dot88.setBackground(new Color(0,0,0,0));
        Dot88.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic88.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot88Layout = new javax.swing.GroupLayout(Dot88);
        Dot88.setLayout(Dot88Layout);
        Dot88Layout.setHorizontalGroup(
            Dot88Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot88Layout.createSequentialGroup()
                .addComponent(DotPic88, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot88Layout.setVerticalGroup(
            Dot88Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot88Layout.createSequentialGroup()
                .addComponent(DotPic88, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot88, gridBagConstraints);

        Dot89.setBackground(new Color(0,0,0,0));
        Dot89.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic89.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot89Layout = new javax.swing.GroupLayout(Dot89);
        Dot89.setLayout(Dot89Layout);
        Dot89Layout.setHorizontalGroup(
            Dot89Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot89Layout.createSequentialGroup()
                .addComponent(DotPic89, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot89Layout.setVerticalGroup(
            Dot89Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot89Layout.createSequentialGroup()
                .addComponent(DotPic89, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot89, gridBagConstraints);

        Dot90.setBackground(new Color(0,0,0,0));
        Dot90.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic90.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot90Layout = new javax.swing.GroupLayout(Dot90);
        Dot90.setLayout(Dot90Layout);
        Dot90Layout.setHorizontalGroup(
            Dot90Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot90Layout.createSequentialGroup()
                .addComponent(DotPic90, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot90Layout.setVerticalGroup(
            Dot90Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot90Layout.createSequentialGroup()
                .addComponent(DotPic90, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 7;
        getContentPane().add(Dot90, gridBagConstraints);

        Dot91.setBackground(new Color(0,0,0,0));
        Dot91.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic91.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot91Layout = new javax.swing.GroupLayout(Dot91);
        Dot91.setLayout(Dot91Layout);
        Dot91Layout.setHorizontalGroup(
            Dot91Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot91Layout.createSequentialGroup()
                .addComponent(DotPic91, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot91Layout.setVerticalGroup(
            Dot91Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot91Layout.createSequentialGroup()
                .addComponent(DotPic91, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 16;
        getContentPane().add(Dot91, gridBagConstraints);

        Dot92.setBackground(new Color(0,0,0,0));
        Dot92.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic92.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot92Layout = new javax.swing.GroupLayout(Dot92);
        Dot92.setLayout(Dot92Layout);
        Dot92Layout.setHorizontalGroup(
            Dot92Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot92Layout.createSequentialGroup()
                .addComponent(DotPic92, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot92Layout.setVerticalGroup(
            Dot92Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot92Layout.createSequentialGroup()
                .addComponent(DotPic92, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot92, gridBagConstraints);

        Dot93.setBackground(new Color(0,0,0,0));
        Dot93.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic93.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot93Layout = new javax.swing.GroupLayout(Dot93);
        Dot93.setLayout(Dot93Layout);
        Dot93Layout.setHorizontalGroup(
            Dot93Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot93Layout.createSequentialGroup()
                .addComponent(DotPic93, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot93Layout.setVerticalGroup(
            Dot93Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot93Layout.createSequentialGroup()
                .addComponent(DotPic93, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot93, gridBagConstraints);

        Dot94.setBackground(new Color(0,0,0,0));
        Dot94.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic94.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot94Layout = new javax.swing.GroupLayout(Dot94);
        Dot94.setLayout(Dot94Layout);
        Dot94Layout.setHorizontalGroup(
            Dot94Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot94Layout.createSequentialGroup()
                .addComponent(DotPic94, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot94Layout.setVerticalGroup(
            Dot94Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot94Layout.createSequentialGroup()
                .addComponent(DotPic94, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot94, gridBagConstraints);

        Dot95.setBackground(new Color(0,0,0,0));
        Dot95.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic95.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot95Layout = new javax.swing.GroupLayout(Dot95);
        Dot95.setLayout(Dot95Layout);
        Dot95Layout.setHorizontalGroup(
            Dot95Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot95Layout.createSequentialGroup()
                .addComponent(DotPic95, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot95Layout.setVerticalGroup(
            Dot95Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot95Layout.createSequentialGroup()
                .addComponent(DotPic95, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 12;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot95, gridBagConstraints);

        Dot96.setBackground(new Color(0,0,0,0));
        Dot96.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic96.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot96Layout = new javax.swing.GroupLayout(Dot96);
        Dot96.setLayout(Dot96Layout);
        Dot96Layout.setHorizontalGroup(
            Dot96Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot96Layout.createSequentialGroup()
                .addComponent(DotPic96, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot96Layout.setVerticalGroup(
            Dot96Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot96Layout.createSequentialGroup()
                .addComponent(DotPic96, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot96, gridBagConstraints);

        Dot97.setBackground(new Color(0,0,0,0));
        Dot97.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic97.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot97Layout = new javax.swing.GroupLayout(Dot97);
        Dot97.setLayout(Dot97Layout);
        Dot97Layout.setHorizontalGroup(
            Dot97Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot97Layout.createSequentialGroup()
                .addComponent(DotPic97, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot97Layout.setVerticalGroup(
            Dot97Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot97Layout.createSequentialGroup()
                .addComponent(DotPic97, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 13;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot97, gridBagConstraints);

        Dot98.setBackground(new Color(0,0,0,0));
        Dot98.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic98.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot98Layout = new javax.swing.GroupLayout(Dot98);
        Dot98.setLayout(Dot98Layout);
        Dot98Layout.setHorizontalGroup(
            Dot98Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot98Layout.createSequentialGroup()
                .addComponent(DotPic98, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot98Layout.setVerticalGroup(
            Dot98Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot98Layout.createSequentialGroup()
                .addComponent(DotPic98, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 15;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot98, gridBagConstraints);

        Dot99.setBackground(new Color(0,0,0,0));
        Dot99.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic99.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot99Layout = new javax.swing.GroupLayout(Dot99);
        Dot99.setLayout(Dot99Layout);
        Dot99Layout.setHorizontalGroup(
            Dot99Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot99Layout.createSequentialGroup()
                .addComponent(DotPic99, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot99Layout.setVerticalGroup(
            Dot99Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot99Layout.createSequentialGroup()
                .addComponent(DotPic99, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 17;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot99, gridBagConstraints);

        Dot100.setBackground(new Color(0,0,0,0));
        Dot100.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic100.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot100Layout = new javax.swing.GroupLayout(Dot100);
        Dot100.setLayout(Dot100Layout);
        Dot100Layout.setHorizontalGroup(
            Dot100Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot100Layout.createSequentialGroup()
                .addComponent(DotPic100, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot100Layout.setVerticalGroup(
            Dot100Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot100Layout.createSequentialGroup()
                .addComponent(DotPic100, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot100, gridBagConstraints);

        Dot101.setBackground(new Color(0,0,0,0));
        Dot101.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic101.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot101Layout = new javax.swing.GroupLayout(Dot101);
        Dot101.setLayout(Dot101Layout);
        Dot101Layout.setHorizontalGroup(
            Dot101Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot101Layout.createSequentialGroup()
                .addComponent(DotPic101, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot101Layout.setVerticalGroup(
            Dot101Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot101Layout.createSequentialGroup()
                .addComponent(DotPic101, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot101, gridBagConstraints);

        Dot102.setBackground(new Color(0,0,0,0));
        Dot102.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic102.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot102Layout = new javax.swing.GroupLayout(Dot102);
        Dot102.setLayout(Dot102Layout);
        Dot102Layout.setHorizontalGroup(
            Dot102Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot102Layout.createSequentialGroup()
                .addComponent(DotPic102, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot102Layout.setVerticalGroup(
            Dot102Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot102Layout.createSequentialGroup()
                .addComponent(DotPic102, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot102, gridBagConstraints);

        Dot103.setBackground(new Color(0,0,0,0));
        Dot103.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic103.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot103Layout = new javax.swing.GroupLayout(Dot103);
        Dot103.setLayout(Dot103Layout);
        Dot103Layout.setHorizontalGroup(
            Dot103Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot103Layout.createSequentialGroup()
                .addComponent(DotPic103, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot103Layout.setVerticalGroup(
            Dot103Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot103Layout.createSequentialGroup()
                .addComponent(DotPic103, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot103, gridBagConstraints);

        Dot104.setBackground(new Color(0,0,0,0));
        Dot104.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic104.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot104Layout = new javax.swing.GroupLayout(Dot104);
        Dot104.setLayout(Dot104Layout);
        Dot104Layout.setHorizontalGroup(
            Dot104Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot104Layout.createSequentialGroup()
                .addComponent(DotPic104, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot104Layout.setVerticalGroup(
            Dot104Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot104Layout.createSequentialGroup()
                .addComponent(DotPic104, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot104, gridBagConstraints);

        Dot105.setBackground(new Color(0,0,0,0));
        Dot105.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic105.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot105Layout = new javax.swing.GroupLayout(Dot105);
        Dot105.setLayout(Dot105Layout);
        Dot105Layout.setHorizontalGroup(
            Dot105Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot105Layout.createSequentialGroup()
                .addComponent(DotPic105, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot105Layout.setVerticalGroup(
            Dot105Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot105Layout.createSequentialGroup()
                .addComponent(DotPic105, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot105, gridBagConstraints);

        Dot106.setBackground(new Color(0,0,0,0));
        Dot106.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic106.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot106Layout = new javax.swing.GroupLayout(Dot106);
        Dot106.setLayout(Dot106Layout);
        Dot106Layout.setHorizontalGroup(
            Dot106Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot106Layout.createSequentialGroup()
                .addComponent(DotPic106, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot106Layout.setVerticalGroup(
            Dot106Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot106Layout.createSequentialGroup()
                .addComponent(DotPic106, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot106, gridBagConstraints);

        Dot107.setBackground(new Color(0,0,0,0));
        Dot107.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic107.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot107Layout = new javax.swing.GroupLayout(Dot107);
        Dot107.setLayout(Dot107Layout);
        Dot107Layout.setHorizontalGroup(
            Dot107Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot107Layout.createSequentialGroup()
                .addComponent(DotPic107, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot107Layout.setVerticalGroup(
            Dot107Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot107Layout.createSequentialGroup()
                .addComponent(DotPic107, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot107, gridBagConstraints);

        Dot108.setBackground(new Color(0,0,0,0));
        Dot108.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic108.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot108Layout = new javax.swing.GroupLayout(Dot108);
        Dot108.setLayout(Dot108Layout);
        Dot108Layout.setHorizontalGroup(
            Dot108Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot108Layout.createSequentialGroup()
                .addComponent(DotPic108, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot108Layout.setVerticalGroup(
            Dot108Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot108Layout.createSequentialGroup()
                .addComponent(DotPic108, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        getContentPane().add(Dot108, gridBagConstraints);

        Dot109.setBackground(new Color(0,0,0,0));
        Dot109.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic109.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot109Layout = new javax.swing.GroupLayout(Dot109);
        Dot109.setLayout(Dot109Layout);
        Dot109Layout.setHorizontalGroup(
            Dot109Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot109Layout.createSequentialGroup()
                .addComponent(DotPic109, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot109Layout.setVerticalGroup(
            Dot109Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot109Layout.createSequentialGroup()
                .addComponent(DotPic109, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 5;
        getContentPane().add(Dot109, gridBagConstraints);

        Dot110.setBackground(new Color(0,0,0,0));
        Dot110.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic110.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot110Layout = new javax.swing.GroupLayout(Dot110);
        Dot110.setLayout(Dot110Layout);
        Dot110Layout.setHorizontalGroup(
            Dot110Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot110Layout.createSequentialGroup()
                .addComponent(DotPic110, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot110Layout.setVerticalGroup(
            Dot110Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot110Layout.createSequentialGroup()
                .addComponent(DotPic110, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot110, gridBagConstraints);

        Dot111.setBackground(new Color(0,0,0,0));
        Dot111.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic111.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot111Layout = new javax.swing.GroupLayout(Dot111);
        Dot111.setLayout(Dot111Layout);
        Dot111Layout.setHorizontalGroup(
            Dot111Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot111Layout.createSequentialGroup()
                .addComponent(DotPic111, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot111Layout.setVerticalGroup(
            Dot111Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot111Layout.createSequentialGroup()
                .addComponent(DotPic111, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 3;
        getContentPane().add(Dot111, gridBagConstraints);

        Dot112.setBackground(new Color(0,0,0,0));
        Dot112.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic112.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot112Layout = new javax.swing.GroupLayout(Dot112);
        Dot112.setLayout(Dot112Layout);
        Dot112Layout.setHorizontalGroup(
            Dot112Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot112Layout.createSequentialGroup()
                .addComponent(DotPic112, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot112Layout.setVerticalGroup(
            Dot112Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot112Layout.createSequentialGroup()
                .addComponent(DotPic112, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 4;
        getContentPane().add(Dot112, gridBagConstraints);

        Dot113.setBackground(new Color(0,0,0,0));
        Dot113.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic113.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot113Layout = new javax.swing.GroupLayout(Dot113);
        Dot113.setLayout(Dot113Layout);
        Dot113Layout.setHorizontalGroup(
            Dot113Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot113Layout.createSequentialGroup()
                .addComponent(DotPic113, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot113Layout.setVerticalGroup(
            Dot113Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot113Layout.createSequentialGroup()
                .addComponent(DotPic113, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 19;
        gridBagConstraints.gridy = 9;
        getContentPane().add(Dot113, gridBagConstraints);

        Dot114.setBackground(new Color(0,0,0,0));
        Dot114.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic114.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot114Layout = new javax.swing.GroupLayout(Dot114);
        Dot114.setLayout(Dot114Layout);
        Dot114Layout.setHorizontalGroup(
            Dot114Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot114Layout.createSequentialGroup()
                .addComponent(DotPic114, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot114Layout.setVerticalGroup(
            Dot114Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot114Layout.createSequentialGroup()
                .addComponent(DotPic114, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 20;
        gridBagConstraints.gridy = 8;
        getContentPane().add(Dot114, gridBagConstraints);

        Dot115.setBackground(new Color(0,0,0,0));
        Dot115.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic115.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot115Layout = new javax.swing.GroupLayout(Dot115);
        Dot115.setLayout(Dot115Layout);
        Dot115Layout.setHorizontalGroup(
            Dot115Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot115Layout.createSequentialGroup()
                .addComponent(DotPic115, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot115Layout.setVerticalGroup(
            Dot115Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot115Layout.createSequentialGroup()
                .addComponent(DotPic115, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 11;
        gridBagConstraints.gridy = 16;
        getContentPane().add(Dot115, gridBagConstraints);

        Dot116.setBackground(new Color(0,0,0,0));
        Dot116.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic116.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot116Layout = new javax.swing.GroupLayout(Dot116);
        Dot116.setLayout(Dot116Layout);
        Dot116Layout.setHorizontalGroup(
            Dot116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot116Layout.createSequentialGroup()
                .addComponent(DotPic116, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot116Layout.setVerticalGroup(
            Dot116Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot116Layout.createSequentialGroup()
                .addComponent(DotPic116, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 16;
        getContentPane().add(Dot116, gridBagConstraints);

        Dot117.setBackground(new Color(0,0,0,0));
        Dot117.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic117.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot117Layout = new javax.swing.GroupLayout(Dot117);
        Dot117.setLayout(Dot117Layout);
        Dot117Layout.setHorizontalGroup(
            Dot117Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot117Layout.createSequentialGroup()
                .addComponent(DotPic117, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot117Layout.setVerticalGroup(
            Dot117Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot117Layout.createSequentialGroup()
                .addComponent(DotPic117, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot117, gridBagConstraints);

        Dot118.setBackground(new Color(0,0,0,0));
        Dot118.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic118.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot118Layout = new javax.swing.GroupLayout(Dot118);
        Dot118.setLayout(Dot118Layout);
        Dot118Layout.setHorizontalGroup(
            Dot118Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot118Layout.createSequentialGroup()
                .addComponent(DotPic118, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot118Layout.setVerticalGroup(
            Dot118Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot118Layout.createSequentialGroup()
                .addComponent(DotPic118, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot118, gridBagConstraints);

        Dot119.setBackground(new Color(0,0,0,0));
        Dot119.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic119.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot119Layout = new javax.swing.GroupLayout(Dot119);
        Dot119.setLayout(Dot119Layout);
        Dot119Layout.setHorizontalGroup(
            Dot119Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot119Layout.createSequentialGroup()
                .addComponent(DotPic119, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot119Layout.setVerticalGroup(
            Dot119Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot119Layout.createSequentialGroup()
                .addComponent(DotPic119, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot119, gridBagConstraints);

        Dot120.setBackground(new Color(0,0,0,0));
        Dot120.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic120.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot120Layout = new javax.swing.GroupLayout(Dot120);
        Dot120.setLayout(Dot120Layout);
        Dot120Layout.setHorizontalGroup(
            Dot120Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot120Layout.createSequentialGroup()
                .addComponent(DotPic120, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot120Layout.setVerticalGroup(
            Dot120Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot120Layout.createSequentialGroup()
                .addComponent(DotPic120, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot120, gridBagConstraints);

        Dot121.setBackground(new Color(0,0,0,0));
        Dot121.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic121.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot121Layout = new javax.swing.GroupLayout(Dot121);
        Dot121.setLayout(Dot121Layout);
        Dot121Layout.setHorizontalGroup(
            Dot121Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot121Layout.createSequentialGroup()
                .addComponent(DotPic121, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot121Layout.setVerticalGroup(
            Dot121Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot121Layout.createSequentialGroup()
                .addComponent(DotPic121, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 19;
        getContentPane().add(Dot121, gridBagConstraints);

        Dot122.setBackground(new Color(0,0,0,0));
        Dot122.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic122.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot122Layout = new javax.swing.GroupLayout(Dot122);
        Dot122.setLayout(Dot122Layout);
        Dot122Layout.setHorizontalGroup(
            Dot122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot122Layout.createSequentialGroup()
                .addComponent(DotPic122, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot122Layout.setVerticalGroup(
            Dot122Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot122Layout.createSequentialGroup()
                .addComponent(DotPic122, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot122, gridBagConstraints);

        Dot123.setBackground(new Color(0,0,0,0));
        Dot123.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic123.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot123Layout = new javax.swing.GroupLayout(Dot123);
        Dot123.setLayout(Dot123Layout);
        Dot123Layout.setHorizontalGroup(
            Dot123Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot123Layout.createSequentialGroup()
                .addComponent(DotPic123, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot123Layout.setVerticalGroup(
            Dot123Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot123Layout.createSequentialGroup()
                .addComponent(DotPic123, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 1;
        getContentPane().add(Dot123, gridBagConstraints);

        Dot124.setBackground(new Color(0,0,0,0));
        Dot124.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic124.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot124Layout = new javax.swing.GroupLayout(Dot124);
        Dot124.setLayout(Dot124Layout);
        Dot124Layout.setHorizontalGroup(
            Dot124Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot124Layout.createSequentialGroup()
                .addComponent(DotPic124, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot124Layout.setVerticalGroup(
            Dot124Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot124Layout.createSequentialGroup()
                .addComponent(DotPic124, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 11;
        getContentPane().add(Dot124, gridBagConstraints);

        Dot125.setBackground(new Color(0,0,0,0));
        Dot125.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic125.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot125Layout = new javax.swing.GroupLayout(Dot125);
        Dot125.setLayout(Dot125Layout);
        Dot125Layout.setHorizontalGroup(
            Dot125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot125Layout.createSequentialGroup()
                .addComponent(DotPic125, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot125Layout.setVerticalGroup(
            Dot125Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot125Layout.createSequentialGroup()
                .addComponent(DotPic125, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 15;
        getContentPane().add(Dot125, gridBagConstraints);

        Dot126.setBackground(new Color(0,0,0,0));
        Dot126.setPreferredSize(new java.awt.Dimension(20, 20));

        DotPic126.setIcon(new javax.swing.ImageIcon(getClass().getResource("/deypacman/yellowDot.png"))); // NOI18N

        javax.swing.GroupLayout Dot126Layout = new javax.swing.GroupLayout(Dot126);
        Dot126.setLayout(Dot126Layout);
        Dot126Layout.setHorizontalGroup(
            Dot126Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot126Layout.createSequentialGroup()
                .addComponent(DotPic126, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        Dot126Layout.setVerticalGroup(
            Dot126Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Dot126Layout.createSequentialGroup()
                .addComponent(DotPic126, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 17;
        getContentPane().add(Dot126, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        
        //getting the keycode
        int k = evt.getKeyCode();
        
        //depending on which arrow key the user presses, pacMan's direction gets set to the corresponding value
        //the pacMotion() method above runs continuously, and uses these directions to determine where pacMan moves next
        switch(k){
            case KeyEvent.VK_UP:
                pacMan.setDirection("up");
                break;
            case KeyEvent.VK_DOWN:
                pacMan.setDirection("down");
                break;
            case KeyEvent.VK_LEFT:
                pacMan.setDirection("left");
                break;
            case KeyEvent.VK_RIGHT:
                pacMan.setDirection("right");
                break;
        }
        
    }//GEN-LAST:event_formKeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel Dot10;
    private javax.swing.JPanel Dot100;
    private javax.swing.JPanel Dot101;
    private javax.swing.JPanel Dot102;
    private javax.swing.JPanel Dot103;
    private javax.swing.JPanel Dot104;
    private javax.swing.JPanel Dot105;
    private javax.swing.JPanel Dot106;
    private javax.swing.JPanel Dot107;
    private javax.swing.JPanel Dot108;
    private javax.swing.JPanel Dot109;
    private javax.swing.JPanel Dot11;
    private javax.swing.JPanel Dot110;
    private javax.swing.JPanel Dot111;
    private javax.swing.JPanel Dot112;
    private javax.swing.JPanel Dot113;
    private javax.swing.JPanel Dot114;
    private javax.swing.JPanel Dot115;
    private javax.swing.JPanel Dot116;
    private javax.swing.JPanel Dot117;
    private javax.swing.JPanel Dot118;
    private javax.swing.JPanel Dot119;
    private javax.swing.JPanel Dot12;
    private javax.swing.JPanel Dot120;
    private javax.swing.JPanel Dot121;
    private javax.swing.JPanel Dot122;
    private javax.swing.JPanel Dot123;
    private javax.swing.JPanel Dot124;
    private javax.swing.JPanel Dot125;
    private javax.swing.JPanel Dot126;
    private javax.swing.JPanel Dot13;
    private javax.swing.JPanel Dot14;
    private javax.swing.JPanel Dot15;
    private javax.swing.JPanel Dot16;
    private javax.swing.JPanel Dot17;
    private javax.swing.JPanel Dot18;
    private javax.swing.JPanel Dot19;
    private javax.swing.JPanel Dot20;
    private javax.swing.JPanel Dot21;
    private javax.swing.JPanel Dot22;
    private javax.swing.JPanel Dot23;
    private javax.swing.JPanel Dot24;
    private javax.swing.JPanel Dot25;
    private javax.swing.JPanel Dot26;
    private javax.swing.JPanel Dot27;
    private javax.swing.JPanel Dot28;
    private javax.swing.JPanel Dot29;
    private javax.swing.JPanel Dot30;
    private javax.swing.JPanel Dot31;
    private javax.swing.JPanel Dot32;
    private javax.swing.JPanel Dot33;
    private javax.swing.JPanel Dot34;
    private javax.swing.JPanel Dot35;
    private javax.swing.JPanel Dot36;
    private javax.swing.JPanel Dot37;
    private javax.swing.JPanel Dot38;
    private javax.swing.JPanel Dot39;
    private javax.swing.JPanel Dot4;
    private javax.swing.JPanel Dot40;
    private javax.swing.JPanel Dot41;
    private javax.swing.JPanel Dot42;
    private javax.swing.JPanel Dot43;
    private javax.swing.JPanel Dot44;
    private javax.swing.JPanel Dot45;
    private javax.swing.JPanel Dot46;
    private javax.swing.JPanel Dot47;
    private javax.swing.JPanel Dot48;
    private javax.swing.JPanel Dot49;
    private javax.swing.JPanel Dot5;
    private javax.swing.JPanel Dot50;
    private javax.swing.JPanel Dot51;
    private javax.swing.JPanel Dot52;
    private javax.swing.JPanel Dot53;
    private javax.swing.JPanel Dot54;
    private javax.swing.JPanel Dot55;
    private javax.swing.JPanel Dot56;
    private javax.swing.JPanel Dot57;
    private javax.swing.JPanel Dot58;
    private javax.swing.JPanel Dot59;
    private javax.swing.JPanel Dot6;
    private javax.swing.JPanel Dot60;
    private javax.swing.JPanel Dot61;
    private javax.swing.JPanel Dot62;
    private javax.swing.JPanel Dot63;
    private javax.swing.JPanel Dot64;
    private javax.swing.JPanel Dot65;
    private javax.swing.JPanel Dot66;
    private javax.swing.JPanel Dot67;
    private javax.swing.JPanel Dot68;
    private javax.swing.JPanel Dot69;
    private javax.swing.JPanel Dot7;
    private javax.swing.JPanel Dot70;
    private javax.swing.JPanel Dot71;
    private javax.swing.JPanel Dot72;
    private javax.swing.JPanel Dot73;
    private javax.swing.JPanel Dot74;
    private javax.swing.JPanel Dot75;
    private javax.swing.JPanel Dot76;
    private javax.swing.JPanel Dot77;
    private javax.swing.JPanel Dot78;
    private javax.swing.JPanel Dot79;
    private javax.swing.JPanel Dot8;
    private javax.swing.JPanel Dot80;
    private javax.swing.JPanel Dot81;
    private javax.swing.JPanel Dot82;
    private javax.swing.JPanel Dot83;
    private javax.swing.JPanel Dot84;
    private javax.swing.JPanel Dot85;
    private javax.swing.JPanel Dot86;
    private javax.swing.JPanel Dot87;
    private javax.swing.JPanel Dot88;
    private javax.swing.JPanel Dot89;
    private javax.swing.JPanel Dot9;
    private javax.swing.JPanel Dot90;
    private javax.swing.JPanel Dot91;
    private javax.swing.JPanel Dot92;
    private javax.swing.JPanel Dot93;
    private javax.swing.JPanel Dot94;
    private javax.swing.JPanel Dot95;
    private javax.swing.JPanel Dot96;
    private javax.swing.JPanel Dot97;
    private javax.swing.JPanel Dot98;
    private javax.swing.JPanel Dot99;
    private javax.swing.JLabel DotPic10;
    private javax.swing.JLabel DotPic100;
    private javax.swing.JLabel DotPic101;
    private javax.swing.JLabel DotPic102;
    private javax.swing.JLabel DotPic103;
    private javax.swing.JLabel DotPic104;
    private javax.swing.JLabel DotPic105;
    private javax.swing.JLabel DotPic106;
    private javax.swing.JLabel DotPic107;
    private javax.swing.JLabel DotPic108;
    private javax.swing.JLabel DotPic109;
    private javax.swing.JLabel DotPic11;
    private javax.swing.JLabel DotPic110;
    private javax.swing.JLabel DotPic111;
    private javax.swing.JLabel DotPic112;
    private javax.swing.JLabel DotPic113;
    private javax.swing.JLabel DotPic114;
    private javax.swing.JLabel DotPic115;
    private javax.swing.JLabel DotPic116;
    private javax.swing.JLabel DotPic117;
    private javax.swing.JLabel DotPic118;
    private javax.swing.JLabel DotPic119;
    private javax.swing.JLabel DotPic12;
    private javax.swing.JLabel DotPic120;
    private javax.swing.JLabel DotPic121;
    private javax.swing.JLabel DotPic122;
    private javax.swing.JLabel DotPic123;
    private javax.swing.JLabel DotPic124;
    private javax.swing.JLabel DotPic125;
    private javax.swing.JLabel DotPic126;
    private javax.swing.JLabel DotPic13;
    private javax.swing.JLabel DotPic14;
    private javax.swing.JLabel DotPic15;
    private javax.swing.JLabel DotPic16;
    private javax.swing.JLabel DotPic17;
    private javax.swing.JLabel DotPic18;
    private javax.swing.JLabel DotPic19;
    private javax.swing.JLabel DotPic20;
    private javax.swing.JLabel DotPic21;
    private javax.swing.JLabel DotPic22;
    private javax.swing.JLabel DotPic23;
    private javax.swing.JLabel DotPic24;
    private javax.swing.JLabel DotPic25;
    private javax.swing.JLabel DotPic26;
    private javax.swing.JLabel DotPic27;
    private javax.swing.JLabel DotPic28;
    private javax.swing.JLabel DotPic29;
    private javax.swing.JLabel DotPic30;
    private javax.swing.JLabel DotPic31;
    private javax.swing.JLabel DotPic32;
    private javax.swing.JLabel DotPic33;
    private javax.swing.JLabel DotPic34;
    private javax.swing.JLabel DotPic35;
    private javax.swing.JLabel DotPic36;
    private javax.swing.JLabel DotPic37;
    private javax.swing.JLabel DotPic38;
    private javax.swing.JLabel DotPic39;
    private javax.swing.JLabel DotPic4;
    private javax.swing.JLabel DotPic40;
    private javax.swing.JLabel DotPic41;
    private javax.swing.JLabel DotPic42;
    private javax.swing.JLabel DotPic43;
    private javax.swing.JLabel DotPic44;
    private javax.swing.JLabel DotPic45;
    private javax.swing.JLabel DotPic46;
    private javax.swing.JLabel DotPic47;
    private javax.swing.JLabel DotPic48;
    private javax.swing.JLabel DotPic49;
    private javax.swing.JLabel DotPic5;
    private javax.swing.JLabel DotPic50;
    private javax.swing.JLabel DotPic51;
    private javax.swing.JLabel DotPic52;
    private javax.swing.JLabel DotPic53;
    private javax.swing.JLabel DotPic54;
    private javax.swing.JLabel DotPic55;
    private javax.swing.JLabel DotPic56;
    private javax.swing.JLabel DotPic57;
    private javax.swing.JLabel DotPic58;
    private javax.swing.JLabel DotPic59;
    private javax.swing.JLabel DotPic6;
    private javax.swing.JLabel DotPic60;
    private javax.swing.JLabel DotPic61;
    private javax.swing.JLabel DotPic62;
    private javax.swing.JLabel DotPic63;
    private javax.swing.JLabel DotPic64;
    private javax.swing.JLabel DotPic65;
    private javax.swing.JLabel DotPic66;
    private javax.swing.JLabel DotPic67;
    private javax.swing.JLabel DotPic68;
    private javax.swing.JLabel DotPic69;
    private javax.swing.JLabel DotPic7;
    private javax.swing.JLabel DotPic70;
    private javax.swing.JLabel DotPic71;
    private javax.swing.JLabel DotPic72;
    private javax.swing.JLabel DotPic73;
    private javax.swing.JLabel DotPic74;
    private javax.swing.JLabel DotPic75;
    private javax.swing.JLabel DotPic76;
    private javax.swing.JLabel DotPic77;
    private javax.swing.JLabel DotPic78;
    private javax.swing.JLabel DotPic79;
    private javax.swing.JLabel DotPic8;
    private javax.swing.JLabel DotPic80;
    private javax.swing.JLabel DotPic81;
    private javax.swing.JLabel DotPic82;
    private javax.swing.JLabel DotPic83;
    private javax.swing.JLabel DotPic84;
    private javax.swing.JLabel DotPic85;
    private javax.swing.JLabel DotPic86;
    private javax.swing.JLabel DotPic87;
    private javax.swing.JLabel DotPic88;
    private javax.swing.JLabel DotPic89;
    private javax.swing.JLabel DotPic9;
    private javax.swing.JLabel DotPic90;
    private javax.swing.JLabel DotPic91;
    private javax.swing.JLabel DotPic92;
    private javax.swing.JLabel DotPic93;
    private javax.swing.JLabel DotPic94;
    private javax.swing.JLabel DotPic95;
    private javax.swing.JLabel DotPic96;
    private javax.swing.JLabel DotPic97;
    private javax.swing.JLabel DotPic98;
    private javax.swing.JLabel DotPic99;
    private javax.swing.JPanel Ghost1;
    private javax.swing.JPanel Ghost2;
    private javax.swing.JPanel Ghost3;
    private javax.swing.JPanel Ghost4;
    private javax.swing.JPanel Ghost5;
    private javax.swing.JPanel Ghost6;
    private javax.swing.JLabel GhostPic1;
    private javax.swing.JLabel GhostPic2;
    private javax.swing.JLabel GhostPic3;
    private javax.swing.JLabel GhostPic4;
    private javax.swing.JLabel GhostPic5;
    private javax.swing.JLabel GhostPic6;
    private javax.swing.JPanel PacMan;
    private javax.swing.JLabel PacManPic;
    private javax.swing.JPanel Wall1;
    private javax.swing.JPanel Wall10;
    private javax.swing.JPanel Wall11;
    private javax.swing.JPanel Wall12;
    private javax.swing.JPanel Wall13;
    private javax.swing.JPanel Wall14;
    private javax.swing.JPanel Wall15;
    private javax.swing.JPanel Wall16;
    private javax.swing.JPanel Wall17;
    private javax.swing.JPanel Wall18;
    private javax.swing.JPanel Wall19;
    private javax.swing.JPanel Wall2;
    private javax.swing.JPanel Wall20;
    private javax.swing.JPanel Wall21;
    private javax.swing.JPanel Wall22;
    private javax.swing.JPanel Wall23;
    private javax.swing.JPanel Wall24;
    private javax.swing.JPanel Wall25;
    private javax.swing.JPanel Wall26;
    private javax.swing.JPanel Wall27;
    private javax.swing.JPanel Wall28;
    private javax.swing.JPanel Wall29;
    private javax.swing.JPanel Wall3;
    private javax.swing.JPanel Wall30;
    private javax.swing.JPanel Wall31;
    private javax.swing.JPanel Wall32;
    private javax.swing.JPanel Wall33;
    private javax.swing.JPanel Wall34;
    private javax.swing.JPanel Wall35;
    private javax.swing.JPanel Wall36;
    private javax.swing.JPanel Wall37;
    private javax.swing.JPanel Wall38;
    private javax.swing.JPanel Wall39;
    private javax.swing.JPanel Wall4;
    private javax.swing.JPanel Wall40;
    private javax.swing.JPanel Wall41;
    private javax.swing.JPanel Wall42;
    private javax.swing.JPanel Wall43;
    private javax.swing.JPanel Wall44;
    private javax.swing.JPanel Wall45;
    private javax.swing.JPanel Wall46;
    private javax.swing.JPanel Wall47;
    private javax.swing.JPanel Wall5;
    private javax.swing.JPanel Wall6;
    private javax.swing.JPanel Wall7;
    private javax.swing.JPanel Wall8;
    private javax.swing.JPanel Wall9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel466;
    // End of variables declaration//GEN-END:variables
}
