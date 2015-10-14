package com.example.steve.orbitaldefender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.MotionEvent;

import com.example.steve.basicgame.R;


public class LogicControl {

    //regular fields
    private Paint paint, notiPaint, hudPaint;
    private int activePointer, secondPointer;
    private CoreView coreView; //to be be able to kill the engine
    private ScreenSize display;
    private SoundManager sxs;
    private GraphicsManager gxs;
    private ObjectManager objMgr;
    private LevelManager lvlMgr;
    private ScoreManager scoreMgr;
    private UIManager ui;
    private int padding; //for the HUD
    private float dmu; //for inputHandler, down-move-up, the three touch actions we log, 0 for happened, 1 for it didn't, reset on every up
    private String notification; //displayed in middle of screen
    private int ticksTimer; //used to delay a piece of code from executing

    public LogicControl(Context context, CoreView coreView, ScreenSize display) {
        this.display = display;
        this.coreView = coreView;

        //load images to be used with entities
        gxs = new GraphicsManager(context);
        gxs.load(Spaceship.TAG, R.drawable.orbital_defender_v2, 0.4f);
        gxs.load(Shield.TAG, R.drawable.shield, 0.4f);
        gxs.load(Laser.TAG, R.drawable.pulsed_laser, 0.4f);
        gxs.load(Asteroid.TAG, R.drawable.asteroid_regular);
        gxs.load(Asteroid.small_TAG, R.drawable.asteroid_small);
        gxs.load(Asteroid.large_TAG, R.drawable.asteroid_large);
        gxs.load(Planet.TAG, R.drawable.earth_strip, display.getWidth(), 0.5f);
        gxs.load(Planet.glow_TAG, R.drawable.earth_glow, display.getWidth() + 10, 0.5f);
        gxs.loadBgd(R.drawable.background, display);

        //load UI images to be used
        gxs.load(UIManager.WAVE_BAR, R.drawable.wave_bar, 0.35f);
        gxs.load(UIManager.SCORE_BAR, R.drawable.score_bar, 0.35f);
        gxs.load(UIManager.ENERGY_BAR, R.drawable.energy_bar, 0.35f);
        gxs.load(UIManager.COOLDOWN_BAR, R.drawable.energy_bar, 0.2f);
        gxs.load(UIManager.PAUSE_BUTTON , R.drawable.pause_button, 0.5f);
        gxs.load(UIManager.FIRE_BUTTON , R.drawable.fire_button, 0.4f);
        gxs.load(UIManager.SHIELD_BUTTON, R.drawable.shield_button, 0.4f);

        //alternative UI images
        gxs.load("wave_bar", R.drawable.wave_bar, 0.35f);
        gxs.load("score_bar", R.drawable.score_bar, 0.35f);
        gxs.load("pause_button2", R.drawable.pause_button, 0.5f);
        gxs.load("fire_button2", R.drawable.fire_button, 0.4f);
        gxs.load("shield_button2", R.drawable.shield_button, 0.4f);

        //load animations to be used
        //gxs.load("cannon_firing", R.drawable.cannon_firing_ssf3_offset0_21568x408px, 0.4f);
        //gxs.new SheetAnimation(Spaceship.TAG, gxs.get("cannon_firing"), 3, 30, 1);


        //load sounds to be used
        sxs = new SoundManager(context);
        sxs.load(SoundManager.CANNON_BLAST, R.raw.cannon_blast);

        //initialize player object
        objMgr = new ObjectManager();
        Bitmap img = gxs.get(Spaceship.TAG); //placeholder image reference
        Spaceship spaceship = new Spaceship(img, display.getWidth() / 2 - img.getWidth() / 2,
                display.getHeight() * 0.7f - img.getHeight() / 2);
        objMgr.add(spaceship); //place spaceship at 80% to the bottom center of screen


        //initialize all other initial game objects
        img = gxs.get(Planet.TAG);
        objMgr.add(new Planet(img, 0, display.getHeight() - img.getHeight() * 0.95f));
        paint = new Paint();
        Shield shield = new Shield(gxs.get(Shield.TAG), 0, 0, paint, 0, 0); //attributes should be modified through the level system or when in use
        shield.disable(); //object is created but needs to be enabled to set its attributes
        objMgr.add(shield);

        //initialize level system
        lvlMgr = new LevelManager(objMgr, gxs, this, display);
        lvlMgr.update(); //initializes the first level
        
        //initialize score system
        scoreMgr = new ScoreManager(lvlMgr);

        //initialize HUD components
        ui = new UIManager();
        padding = 10;
        Bitmap img2; //secondary placeholder variable
        float[] loc; //placeholder object for locational purposes
        float x, y; //placeholders used only for long calculations
        img = gxs.get(UIManager.WAVE_BAR); // primary placeholder variable
        ui.new DisplayBar(UIManager.WAVE_BAR, img, padding, padding); //place at top left corner of screen
        img = gxs.get(UIManager.SCORE_BAR);
        x = display.getWidth() - img.getWidth() - padding;
        ui.new DisplayBar(UIManager.SCORE_BAR, img, x, padding); //place at top right corner of screen
        //img = gxs.get(UIManager.PAUSE_BUTTON + "_idle");
        //img2 = gxs.get(UIManager.PAUSE_BUTTON + "_pressed");
        img2 = img = gxs.get(UIManager.PAUSE_BUTTON); //currently only using one image
        x = (display.getWidth() / 2) - (img.getWidth() / 2);
        ui.new Button(UIManager.PAUSE_BUTTON, img, img2, x, padding); //place at top middle of screen
        //img = gxs.get(UIManager.SHIELD_BAR); //filler color will be light blue
        //loc = ui.getComponents().get(UIManager.WAVE_BAR).getLocation();
        //y = loc[1] + ui.getComponents().get(UIManager.WAVE_BAR).getBitmap().getHeight();
        //ui.new StatusBar(UIManager.SHIELD_BAR, img, 2, Color.argb(255, 170, 238, 255), padding, y + padding); //right below wave bar
        img = gxs.get(UIManager.ENERGY_BAR); //filler color will be light red / light orange
        loc = ui.getComponents().get(UIManager.SCORE_BAR).getLocation();
        x = display.getWidth() - padding - img.getWidth();
        y = loc[1] + ui.getComponents().get(UIManager.SCORE_BAR).getBitmap().getHeight();
        ui.new StatusBar(UIManager.ENERGY_BAR, img, 5, Color.argb(255, 255, 179, 128), x, y + padding); //right below score bar
        img = gxs.get(UIManager.COOLDOWN_BAR);
        loc = ui.getComponents().get(UIManager.ENERGY_BAR).getLocation();
        x = display.getWidth() - padding - img.getWidth();
        y = loc[1] + ui.getComponents().get(UIManager.ENERGY_BAR).getBitmap().getHeight();
        ui.new StatusBar(UIManager.COOLDOWN_BAR, img, spaceship.getTimetoCoolDown(), Color.argb(255, 255, 147, 65), x, y); //right below ammo bar

        //img = gxs.get(UIManager.FIRE_BUTTON + "_idle");
        //img2 = gxs.get(UIManager.FIRE_BUTTON + "_pressed");
        img2 = img = gxs.get(UIManager.FIRE_BUTTON); //currently only using one image
        x = display.getWidth() - padding - img.getWidth();
        y = display.getHeight() * 0.5f;
        ui.new Button(UIManager.FIRE_BUTTON, img, img2, x, y); //place at a % of the screen's height on the right side of the screen
        //img = gxs.get(UIManager.SHIELD_BUTTON + "_idle");
        //img2 = gxs.get(UIManager.SHIELD_BUTTON + "_pressed");
        img2 = img = gxs.get(UIManager.SHIELD_BUTTON); //currently only using one image
        loc = ui.getComponents().get(UIManager.FIRE_BUTTON).getLocation();
        x = display.getWidth() - padding - img.getWidth();
        y = loc[1] + ui.getComponents().get(UIManager.FIRE_BUTTON).getBitmap().getHeight() + padding;
        ui.new Button(UIManager.SHIELD_BUTTON, img, img2, x, y); //place below the fire button at the right side of the screen



        paint = new Paint();
        paint.setAntiAlias(true);

        //initialize paint for drawing notification text on screen
        notiPaint = new Paint();
        notiPaint.setARGB(255, 170, 238, 255); //light blue
        notiPaint.setTextAlign(Paint.Align.CENTER);
        float textSize = notiPaint.getTextSize();
        notiPaint.setTextSize(textSize * 8); //scale text
        //initialize paint for drawing status bar text
        hudPaint = new Paint();
        hudPaint.setARGB(255, 170, 238, 255); //light blue
        textSize = hudPaint.getTextSize();
        hudPaint.setTextSize(textSize * 5); //scale text
        hudPaint.setTypeface(Typeface.MONOSPACE);


        dmu = .000f;
        notification = "";
    }

    public void inputHandler(MotionEvent event){
        Spaceship spaceship =  objMgr.getShip();
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointer = event.getPointerId(event.getActionIndex());
                if (ui.onTouchEvent((int) event.getX(), (int) event.getY())) {
                    //button was touched
                    ui.onButtonPressed(coreView, this, objMgr, gxs, sxs, scoreMgr);
                    dmu = .200f; //for when a button is touched
                } else {
                    if (!coreView.isGamePaused())
                        spaceship.setLastX(event.getX());
                    dmu = .100f;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerId(event.getActionIndex()) == activePointer) {
                    if (dmu == .100f)
                        if (!coreView.isGamePaused())
                            spaceship.setAngle(event.getX());
                    //dmu = .010f;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getPointerId(event.getActionIndex()) == activePointer) {
                    if (dmu == .100f)
                        if (!coreView.isGamePaused())
                            spaceship.setLastAngle(event.getX());
                    for (UIManager.Component component : ui.getComponents().values())
                        if (component instanceof UIManager.Button && ((UIManager.Button) component).getState() == 1)
                            ((UIManager.Button) component).resetState();

                }
                dmu = 0;
                break;
            case MotionEvent.ACTION_POINTER_DOWN: //occurs when there is more than one finger on screen
                secondPointer = event.getPointerId(event.getActionIndex());
                if (ui.onTouchEvent((int) event.getX(secondPointer), (int) event.getY(secondPointer))) {
                    //button was touched
                    ui.onButtonPressed(coreView, this, objMgr, gxs, sxs, scoreMgr);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP: //occurs when there is more than one finger on screen
                for (UIManager.Component component : ui.getComponents().values())
                    if (component instanceof UIManager.Button && ((UIManager.Button) component).getState() == 1)
                        ((UIManager.Button) component).resetState();
                break;

        }
    }

    public void update(int ticks) {
        //monitor level before updating other game objects
        lvlMgr.monitor();

        //lets all game objects be updated before a new frame is drawn
        for (WorldObject object : objMgr.getObjects()) {
            if (object.isVisible()) { //only allow game object to update if its visible
                if (object instanceof Asteroid) {
                    ((Asteroid) object).move(display);
                    ((Asteroid) object).rotate();
                }
                if (object instanceof Planet) {
                    ((Planet) object).scroll(display);
                    Asteroid asteroid = ((Planet) object).intersectsAsteroid(objMgr.getObjects());
                    if (asteroid != null){
                        //handle what happens with the asteroid that came in contact with the planet
                        //for the time being, just disintegrate asteroid and have some explosion particle effects
                        Explosion explosion = new Explosion(asteroid.getCenterX(), asteroid.getY() + asteroid.getBitmap().getHeight(),
                                360 * asteroid.getSize(), 10, Color.argb(255, 172, 167, 147), 3);
                        objMgr.add(explosion);
                        int scorePts = scoreMgr.hitPlanet(asteroid.getSize());
                        scoreMgr.setInstantScore(scorePts, asteroid.getCenterX(), asteroid.getCenterY(), objMgr);
                        //disable asteroid
                        asteroid.disable();
                        Log.i("update()", "asteroid hit planet");
                    }
                }
                if (object instanceof Laser) {
                    ((Laser) object).rotate();
                    ((Laser) object).move();
                    //disable projectile if it goes outside the screen view or hits an asteroid
                    Asteroid asteroid = ((Laser) object).intersectsAsteroid(objMgr.getObjects());
                    if (object.getX() + object.getBitmap().getWidth() < 0 || object.getX() > display.getWidth() ||
                            object.getY() + object.getBitmap().getHeight() < 0 || asteroid != null){
                        object.disable(); //disable the projectile so it can be reused at a later time
                        if (asteroid != null) {
                            int scorePts = scoreMgr.hitAsteroid(asteroid.getSize()) 
                                    + scoreMgr.isFarRanged(asteroid.getCenterX(), asteroid.getCenterY(), 
                                    objMgr.getShip().getCenterX(), objMgr.getShip().getCenterY(),
                                    display.getHeight() / 2, asteroid.getSize());
                            scoreMgr.setInstantScore(scorePts, asteroid.getCenterX(), asteroid.getCenterY(), objMgr);
                            asteroid.explode(objMgr, gxs);
                            Log.i("asteroid destroyed", "lefT:" + objMgr.getActiveAsteroids());
                        }
                        else scoreMgr.missedShot(); //no asteroid hit means nothing was hit
                    }
                }
                if (object instanceof Explosion) ((Explosion) object).update();
                if (object instanceof InstantScore) ((InstantScore) object).update();
                if (object instanceof Spaceship) continue; //spaceship updates will be handled separately later
                if (object instanceof Shield) {
                    //fade shield and check for collisions against any asteroids
                    Shield shield = (Shield) object;
                    shield.fade();
                    shield.destabilize(); //only does so if its being intercepted
                    Asteroid asteroid = shield.intersectsAsteroid(objMgr.getObjects());
                    if (asteroid != null) {
                        int scorePts = scoreMgr.hitAsteroid(asteroid.getSize());
                        scoreMgr.setInstantScore(scorePts, asteroid.getCenterX(), asteroid.getCenterY(), objMgr);
                        asteroid.explode(objMgr, gxs);
                        shield.intercepted(); //shield focuses all energy into the asteroid to destroy so it destabilizes
                    }
                }
            }
        }
        //player  updates; handled last so other world object updates can be taken into consideration if need be
        Spaceship spaceship =  objMgr.getShip();
        //GraphicsManager.SheetAnimation cannonAnimation = gxs.getAnimation(Spaceship.TAG);
        spaceship.coolDown();
        ((UIManager.StatusBar) ui.getComponents().get(UIManager.COOLDOWN_BAR)).increaseValue(1);
        spaceship.rotate();
        Asteroid asteroid = spaceship.intersectsAsteroid(objMgr.getObjects());
        if (asteroid != null) {
            //game over, do some sort of explosion effect and game over
            objMgr.add(new Explosion(spaceship.getCenterX(), spaceship.getCenterY(), 720, 10, Color.argb(255, 226, 226, 226), 2)); //light gray color
            spaceship.disable();
            lvlMgr.gameOver();
        }
        //if (cannonAnimation.isPlaying()) cannonAnimation.update(ticks);

        //update background
        gxs.scrollBgd(1, display.getWidth());

        //do some particle effects cleaning up
        for (int i = 0; i < objMgr.getObjects().size(); i++){
            if (!objMgr.getObjects().get(i).isVisible() && objMgr.getObjects().get(i) instanceof Explosion){
                objMgr.remove(i);
                Log.i("CleanUp","removing inactive explosion objects");
            }
        }
    }

    public void render(Canvas canvas) {
        //horizontal background scroll
        if (gxs.getBgdX() > 0)
            //draw a following copy of the background
            canvas.drawBitmap(gxs.getBgd(),gxs.getBgdX() - gxs.getBgd().getWidth(), 0,null);
        canvas.drawBitmap(gxs.getBgd(), gxs.getBgdX(), 0, null);
        //game objects
        for (WorldObject object : objMgr.getObjects()) { //loops through all game objects and draws them on screen if they're visible
            if (object.isVisible()) {

                if (object instanceof Spaceship) continue; //spaceship updates will be handled separately later
                else if (object instanceof Laser) canvas.drawBitmap(object.getBitmap(), object.getMatrix(), null);
                else if (object instanceof Asteroid) canvas.drawBitmap(object.getBitmap(), object.getMatrix(), null); //changing matrix object
                else if (object instanceof Planet){
                    canvas.drawBitmap(object.getBitmap(), object.getX(), object.getY(), null); //main image
                    canvas.drawBitmap(object.getBitmap(), object.getX() - object.getBitmap().getWidth(), object.getY(), null); //follow image
                    Bitmap earthGlowImg = gxs.get(Planet.glow_TAG);
                    canvas.drawBitmap(earthGlowImg, -10, object.getY() - (earthGlowImg.getHeight() * 0.95f), null); //glow from planet, static
                }
                else if (object instanceof Shield) continue;
                else if (object instanceof InstantScore){
                    InstantScore instantScore = (InstantScore) object;
                    canvas.drawText(instantScore.getScorePts(), instantScore.getX(), instantScore.getY(), instantScore.getPaint());
                }
                else if (object instanceof Explosion) continue;
                else canvas.drawBitmap(object.getBitmap(), object.getX(), object.getY(), null); //static image object
            }
        }

        //main object drawn last so it shows up on top of everything else
        Spaceship spaceship =  objMgr.getShip();
        /*GraphicsManager.SheetAnimation cannonAnimation = gxs.getAnimation(Spaceship.TAG);
        if (cannonAnimation.isPlaying())
            canvas.drawBitmap(cannonAnimation.getFrame(),
                    cannonAnimation.rotate(spaceship.getCurAngle(), spaceship.getPivot(), spaceship.getX(), spaceship.getY(), 0.21568f), paint);
        else*/
        if (spaceship.isVisible()) canvas.drawBitmap(spaceship.getBitmap(), spaceship.getMatrix(), paint);

        //explosions could show on top of spaceship
        for (WorldObject object : objMgr.getObjects())
            if (object.isVisible() && object instanceof Explosion)
                //drawing wont show if canvas is updated outside of this method so must update particles here
                for (Explosion.Particle particle : ((Explosion) object).getParticles())
                    canvas.drawCircle(particle.getX(), particle.getY(), particle.getSize(), particle.getPaint());

        //draw shield so it shows on top of spaceship
        Shield shield = objMgr.getShield();
        if (shield.isVisible())
            canvas.drawBitmap(shield.getBitmap(), shield.getMatrix(), shield.getPaint());

        //UI rendering so it shows on top of everything else
        for (UIManager.Component component : ui.getComponents().values()){
            if (component instanceof UIManager.StatusBar)
                canvas.drawRect(((UIManager.StatusBar) component).getRect(), ((UIManager.StatusBar) component).getPaint());
            float[] loc = component.getLocation();
            canvas.drawBitmap(component.getBitmap(), loc[0], loc[1], null);
        }
        //text on screen
        if (notification != "")
            canvas.drawText(notification, display.getWidth()/2, display.getHeight()/2, notiPaint);
        if (lvlMgr.isGameFinished()) notification = "Game Finished";
        if (lvlMgr.isGameOver()) notification = "Game Over";

        float[] loc = ui.getComponents().get(UIManager.WAVE_BAR).getLocation();
        float x = loc[0];
        float y = loc[1] + Math.abs(hudPaint.ascent());
        if (lvlMgr.getLevel() < 10)
            canvas.drawText("WAVE 0" + lvlMgr.getLevel(), x + padding*4, y  + padding, hudPaint);
        else canvas.drawText("WAVE " + lvlMgr.getLevel(), x + padding*4, y  + padding, hudPaint);
        //display score
        loc = ui.getComponents().get(UIManager.SCORE_BAR).getLocation();
        x = loc[0];
        y = loc[1] + Math.abs(hudPaint.ascent());
        String score = "";
        for (int i = 0; i < (""+scoreMgr.getMaxScore()).length() - (""+scoreMgr.getTotalScore()).length(); i++)
            score += "0";
        x += ui.getComponents().get(UIManager.SCORE_BAR).getBitmap().getWidth();
        score += scoreMgr.getTotalScore();
        hudPaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(score, x - padding * 2, y + padding, hudPaint);
        hudPaint.setTextAlign(Paint.Align.LEFT); //reset alignment
        
        //score readout at the end of every level
        if (lvlMgr.isLevelCompleted() || lvlMgr.isGameOver()){
            String[] readout;
            if (ticksTimer > 0) {
                ticksTimer--;
                readout = scoreMgr.getScoreReadout(false, (UIManager.StatusBar) ui.getComponents().get(UIManager.ENERGY_BAR));
            }
            else readout = scoreMgr.getScoreReadout(true, (UIManager.StatusBar) ui.getComponents().get(UIManager.ENERGY_BAR));
            if (readout == null) lvlMgr.increaseLevel();
            else {
                float oriTextSize = notiPaint.getTextSize();
                notiPaint.setTextSize(oriTextSize * 0.8f); //slightly smaller
                canvas.drawText("wave " + lvlMgr.getLevel() + " finished!",
                        display.getWidth() / 2, display.getHeight() / 5 + notiPaint.ascent() + notiPaint.descent(), notiPaint);
                notiPaint.setTextSize(oriTextSize * 0.4f); //slightly smaller
                String[] splitText = breakText(readout[0], 50);
                float offset = 0;
                if (splitText == null)
                    canvas.drawText(readout[0], display.getWidth() / 2, display.getHeight() / 5, notiPaint);
                else{
                    canvas.drawText(splitText[0], display.getWidth() / 2, display.getHeight() / 5, notiPaint);
                    canvas.drawText(splitText[1], display.getWidth() / 2, display.getHeight()/5 - notiPaint.ascent(), notiPaint);
                    offset = notiPaint.ascent();
                }
                notiPaint.setTextSize(oriTextSize * 1.2f); //slightly larger
                canvas.drawText(readout[1] + " Points", display.getWidth() / 2, display.getHeight()/5 - notiPaint.ascent() - offset, notiPaint);
                notiPaint.setTextSize(oriTextSize); //reset text size
                if (readout[2] != null) {
                    ticksTimer = 60;
                    Log.i("readout", "setting timer to " + ticksTimer);
                }
            }
        }
    }
    public String[] breakText (String text, int maxIndex){
        if (text.length() > maxIndex){
            String[] splitString = new String[2]; //seperate into 2 strings
            boolean split = false;
            int i = 0;
            while (!split){
                if (text.charAt(maxIndex - i) == ' '){ //this algorithm is as to make sure words don't get split
                    splitString[1] = text.substring(maxIndex - i);
                    splitString[0] = text.substring(0, maxIndex - i);
                    split = true;
                }
                else i++;
            }
            return splitString;
        }
        return null;
    }

    public void changeNotification(String string){
        notification = string;
    }
}