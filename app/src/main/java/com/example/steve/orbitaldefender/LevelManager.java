package com.example.steve.orbitaldefender;


import android.util.Log;


public class LevelManager {

    private ObjectManager objMgr;
    private GraphicsManager gxs;
    private LogicControl control;
    private ScreenSize display;
    private int level;
    private final int LAST_LEVEL = 4;
    private final int GAME_START_LVL = 1; //prior levels are for practice/tutorial, starting with this level the actual challenge starts
    private int step; //each level can have steps to orchestrate different timings for things to occur in the same level
    private int delayValue; //delay for spawning things at different times, valued at number of ticks
    private boolean delayed;
    private boolean gameFinished;
    private boolean levelCompleted;
    private boolean gameOver; //for when ship is destroyed or planet heavily damaged
    private int entitiesCt; //count of entities created per level

    public LevelManager(ObjectManager objMgr, GraphicsManager gxs, LogicControl control, ScreenSize display){
        this.objMgr = objMgr;
        this.gxs = gxs;
        this.control = control;
        this.display = display;

    }

    public void update(){
        //this method updates current level or initiates a new level
        switch (level){
            case 0:
                break;
            case 1:
                //level 1, one large asteroid falling vertically
                objMgr.getAsteroid(gxs).modify(gxs, "large", -100, -100, 45, (int) (Math.random() * 10), 5);
                break;
            case 2:
                //2 small asteroids, 1 regular
                objMgr.getAsteroid(gxs).modify(gxs, "small", 500, -1, 70, 2, 5);
                objMgr.getAsteroid(gxs).modify(gxs, "", 250, -1, 50, 4, 3);
                objMgr.getAsteroid(gxs).modify(gxs, "small", 750, -1, 30, 6, 4);
                break;
            case 3: //level 2, 3 regular asteroids
                objMgr.getAsteroid(gxs).modify(gxs, "", 500, -1, 70, 2, 5);
                objMgr.getAsteroid(gxs).modify(gxs, "", 250, -1, 50, 4, 3);
                objMgr.getAsteroid(gxs).modify(gxs, "", 750, -1, 30, 6, 4);
                break;
            case 4: //level 3, 2 large asteroids
                objMgr.getAsteroid(gxs).modify(gxs, "large", -10, -10, 90, 5, 5);
                objMgr.getAsteroid(gxs).modify(gxs, "large", 500, -1, 90, 5, 5);
                break;
            default:
                //finished game
                gameFinished = true;
                Log.i("levelManager", "the game has been completed");
                break;
        }
    }

    public void monitor(){
        //this method monitors the current level and executes actions accordingly

        //deal with delays accordingly
        if (delayed) {
            delayValue--;
            if (delayValue == 0){
                step++;
                delayed = false;
                update();
            }
        }

        //check how many entities are active
        //notify system level was completed if number of entities has reached zero
        if (objMgr.getActiveAsteroids() == 0 && !gameFinished && !gameOver){
            levelCompleted = true;
            //some pre-level-increase code should go here like a flash on the screen with a count down to the next level or a box with upgrades
            //score update should also probably go here
        }
    }

    private void delay(int ticks){
        delayValue = ticks;
        delayed = true;
    }

    public void increaseLevel(){
        if (isLevelCompleted()){
            level++;
            step = 0;
            levelCompleted = false;
            objMgr.noActiveAsteroids();
            update();
        }
    }

    public void gameOver(){
        gameOver = true;
        //destroy all currently active asteroids
        for (WorldObject object : objMgr.getObjects()) //first round should destroy any regular/large asteroids
            if (object instanceof Asteroid) ((Asteroid) object).explode(objMgr, gxs);
        for (WorldObject object : objMgr.getObjects()) //second round should destroy all the remaining small asteroids
            if (object instanceof Asteroid) ((Asteroid) object).explode(objMgr, gxs);
        control.changeNotification("Game Over");
    }
    
    
    public int getLevel(){ return level; }
    public int getEntitiesCount(){ return entitiesCt; }
    public boolean isGameFinished(){ return gameFinished; }
    public boolean isLevelCompleted(){ return levelCompleted; }
    public boolean isLastLevel() { return (level == LAST_LEVEL); }
    public boolean isGamePractice() { return (level < GAME_START_LVL); }
    public boolean isGameOver() { return gameOver; }
}
