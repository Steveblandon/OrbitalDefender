package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.util.Hashtable;

public class UIManager {

    //HUD components [constants]
    public final static String WAVE_BAR = "wave_bar";
    public final static String SCORE_BAR = "score_bar";
    public final static String ENERGY_BAR = "energy_bar";
    public final static String COOLDOWN_BAR = "cool_bar";
    public final static String PAUSE_BUTTON = "pause_button";
    public final static String FIRE_BUTTON = "fire_button";
    public final static String SHIELD_BUTTON = "shield_button";

    private Hashtable<String, Component> components;
    private Rect touchPoint; //for intersection purposes this represents the area on the screen where there was touch input
    private Rect compRect; //for intersection purposes this represents the area on the screen corresponding to a component
    private Component activeComp; //temporary holder for a component that was just touched

    public UIManager(){
        components = new Hashtable<>();
        touchPoint = new Rect();
        compRect = new Rect();
    }

    public boolean onTouchEvent(int x, int y){
        //this method checks to see if a touch coordinate intersects the area on the screen corresponding to a touchable object (like a button)
        for (Component component : components.values())
            if (component instanceof Button){
                touchPoint.set(x, y, x + 1, y + 1);
                float[] loc =  component.getLocation();
                compRect.set((int) loc[0], (int) loc[1],
                        (int) loc[0] + component.getBitmap().getWidth(), (int) loc[1] + component.getBitmap().getHeight());
                if (compRect.contains(touchPoint)){
                    ((Button) component).changeState();
                    activeComp = component;
                    return true;
                }
            }

        return false; //returns false if no component was touched
    }

    public void onButtonPressed(CoreView coreView, LogicControl ctrl, ObjectManager objMgr, GraphicsManager gxs, SoundManager sxs, ScoreManager scoreMgr){
        switch (activeComp.getTAG()){
            case PAUSE_BUTTON:
                if (coreView.isRunning()){
                    //make it so that the game waits one tick before pausing so that any last split second graphical updates can be rendered
                    coreView.delayedPause(1); //this makes sure the update cycle completes before pausing game
                    ctrl.changeNotification("Game Paused");
                }
                else if (!coreView.isRunning()){
                    coreView.resume();
                    ctrl.changeNotification("");
                }
                break;
            case FIRE_BUTTON:
                if (!coreView.isGamePaused()){ //make sure to only process this if game is not paused
                    Spaceship spaceship = objMgr.getShip();
                    GraphicsManager.SheetAnimation cannonAnimation = gxs.getAnimation(Spaceship.TAG);
                    if (spaceship.isReady()){ //spaceship has a cool down timer that gets activated automatically from every shot
                        //sxs.play(SoundManager.CANNON_BLAST); //OBSOLETE: no longer a cannon, no longer a projectile being launched
                        spaceship.shoot(objMgr.getLaser(gxs));
                        //cannonAnimation.play();
                        scoreMgr.firedShot();
                        ((StatusBar) components.get(ENERGY_BAR)).reduceValue(1);
                        ((StatusBar) components.get(COOLDOWN_BAR)).reduceValue(((StatusBar) components.get(COOLDOWN_BAR)).getFixedValue());
                    }
                }
                break;
            case SHIELD_BUTTON:
                if (!coreView.isGamePaused()) { //make sure to only process this if game is not paused
                    StatusBar energyBar = ((StatusBar) components.get(ENERGY_BAR));
                    //if (energyBar.getValue() > 0){ //only proceeds if there are any shields left to use
                        Spaceship player = objMgr.getShip();
                        Shield shield = objMgr.getShield(); //there should only be one active shield at any one time
                        if (shield.getLifespan() == 0) { //need to set its attributes for the first time if this is 0
                            shield.enable(player.getCenterX(), player.getCenterY(), shield.getPaint(),
                                    120, 0.5f); //roughly 5 seconds, uses up 50% of shield energy, starts out very inefficient
                            //give visual feedback of shields being used up
                            energyBar.reduceValue(1);
                        }
                        else if (shield.getLifetime() == 0){
                            shield.enable(player.getCenterX(), player.getCenterY(), shield.getPaint(),
                                    shield.getLifespan(), shield.getEnergyUse()); //simply enable with previous settings
                            //give visual feedback of shields being used up
                            energyBar.reduceValue(1);
                        }
                    //}
                }
                break;
        }
    }

    public Hashtable<String, Component> getComponents(){ return components; }


    public abstract class Component {
        
        private String TAG; //the unique tag for this object
        private Bitmap bitmap; //visual representation
        private float x, y; //location
        
        public Component (String TAG, Bitmap bitmap, float x, float y){
            this.TAG = TAG;
            this.bitmap = bitmap;
            this.x = x;
            this.y = y;
            components.put(TAG, this);
        }

        public void setBitmap(Bitmap bitmap){ this.bitmap = bitmap; }
        
        public Bitmap getBitmap() { return bitmap; }
        public String getTAG() { return TAG; }
        public float[] getLocation() {return new float[]{x,y}; }
    }
    
    public class Button extends Component{
        
        private int state; //0 for idle, 1 for pressed
        private Bitmap[] bitmaps; //visual representation when button is idle and pressed

        public Button(String tag, Bitmap idle, Bitmap pressed, float x, float y){
            super(tag, idle, x, y);
            bitmaps = new Bitmap[]{idle, pressed};
            state = 0;
        }

        public void changeState(){
            //simply change state to pressed
            state = 1;
        }

        public void resetState(){
            //needs to be called whenever the button is no longer pressed
            state = 0; //idle
        }

        public int getState(){ return state; }

        public Bitmap getBitmap(){ return bitmaps[state]; }

        public void setBitmaps(Bitmap[] bitmaps){ this.bitmaps = bitmaps; }
    }

    public class DisplayBar extends Component {
        //meant for just displaying a static HUD component on top of which some text is shown

        public DisplayBar(String TAG, Bitmap bitmap, float x, float y) {
            super(TAG, bitmap, x, y);
        }
    }

    public class StatusBar extends Component{
        //status bar display how much is left of something (ammo, shields...etc)
        
        private int value;
        private int fixedValue; //max value for this status bar that the player currently has
        private Rect rect;
        private Paint paint;

        public StatusBar(String tag, Bitmap image, int startingValue, int color, float x, float y){
            super(tag, image, x, y);
            fixedValue = startingValue;
            value = fixedValue; //(100%)
            Log.i("StatusBar", "value" + value);
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color);
            rect = new Rect();
            rect.set((int ) x, (int) y, (int) x + image.getWidth(), (int) y + image.getHeight());
        }

        public void reduceValue(int amount){
            value -= amount;
            if (value <= 0) value = 0;
            Log.i("reducedValue", "value:" + value);
        }

        public void increaseValue(int amount){
            value += amount;
            if (value > fixedValue) value = fixedValue;
        }

        public void setFixedValue(int value){
            fixedValue = Math.abs(value);
            resetValue();
        }

        public void resetValue(){
            value = fixedValue;
        }

        public Rect getRect(){
            //as the value decreases the rectangle shrinks to the middle of the status bar
            float[] location =  getLocation();
            float x = location[0];
            float y = location[1];
            float width = getBitmap().getWidth() * (value/(float) fixedValue); //a percentage of the frame image
            float xOffset = 0; //to decrease rect from left to right instead of right to left
            xOffset = getBitmap().getWidth() - width;
            rect.set((int) (x + xOffset), (int) y, (int) (x + width), (int) y + getBitmap().getHeight());
            return rect;
        }

        public Paint getPaint(){ return paint; }

        public int getFixedValue() { return fixedValue; }
        public int getValue(){ return value; }
    }
}
