package com.example.steve.orbitaldefender;

import android.graphics.Color;
import android.util.Log;

public class Asteroid extends WorldObject {

    public final static String TAG  = Asteroid.class.getSimpleName();
    public final static String small_TAG = "asteroid_small";
    public final static String large_TAG  = "asteroid_large";
    private float rotation;
    private final float MaxDirection = 45f;
    private float direction;
    private float rot = 2;
    private float speed = 10;
    private int size;


    public Asteroid(GraphicsManager gxs, String size, float x, float y) {
        super(null, x, y);
        modify(gxs, size, x, y, (float) (Math.random() * MaxDirection) + 10, (float) (Math.random() * speed), (float) (Math.random() * rot));
    }
    
    public Asteroid(GraphicsManager gxs, String size, float x, float y, float direction, float speed, float rot){
        //this constructor is to be used to be more specific about the asteroid's attributes
        super(null, x, y);
        modify(gxs, size, x, y , direction, speed, rot);
    }

    public void disable(ObjectManager objMgr){
        super.disable();
        objMgr.asteroidDisabled();
    }

    public void modify(GraphicsManager gxs, String size, float x, float y, float direction, float speed, float rot){
        //this method is to be used to modify asteroid's attributes during runtime
        int sizeVal = 2;
        switch (size){
            case "small":
                setBitmap(gxs.get(small_TAG));
                sizeVal = 1;
                break;
            case "large":
                setBitmap(gxs.get(large_TAG));
                sizeVal = 3;
                break;
            default:
                setBitmap(gxs.get(TAG));
        }
        if (y == -1) y = -getBitmap().getHeight();
        setLocation(x, y);
        rotation = (float) (Math.random() * 360); //so it starts at a random angle
        this.size = sizeVal;
        this.direction = direction;
        this.speed = speed;
        this.rot = rot;
    }

    public void rotate(){
        //note: programmatically a positive rotation is clockwise (as opposed mathematically it would be counter-clockwise)
        if (rotation >= 360) rotation = rotation - 360;
        rotation += rot;
        getMatrix().reset(); //without this the bitmap, postTranslate() would cause the bitmap to slide across the screen
        getMatrix().postRotate(rotation, getBitmap().getWidth() / 2, getBitmap().getHeight() / 2);
        getMatrix().postTranslate(getX(), getY());
    }

    public void move(ScreenSize screenSize){
        //move diagonally, when its out of view, travel another screen width before entering view from opposite side
        if (getX() > screenSize.getWidth() + 100) setLocation(-getBitmap().getWidth(), getY());
        else if (getX() < -getBitmap().getWidth() * 2) {
            direction --;
            setLocation(getX() + getBitmap().getWidth(), getY()); //move towards screen
        }
        else
            setLocation(getX() + speed * (float) Math.cos(Math.toRadians(direction)),
                getY() + speed * (float) Math.sin(Math.toRadians(direction)));
        //move to top of screen if it reaches the bottom [in actuality it should disable itself if it reaches the bottom but doesn't touch the planet]
        if (getY() > screenSize.getHeight() + 100) setLocation((float) (Math.random() * screenSize.getWidth() / 2), -getBitmap().getHeight());
        //disable if goes way beyond top of screen
        if (getY() < -getBitmap().getHeight() * 2) {
            direction = direction - 180; //do a 180, turn around
            setLocation((float) (Math.random() * screenSize.getWidth() / 2), -getBitmap().getHeight());
        }
    }
    
    private void split(ObjectManager objMgr, GraphicsManager gxs){
        //this method causes the asteroid to split into chunks depending on its size and adds them onto the list of active objects
        if (size > 1){
            for (int i = 0; i < size - 1; i++){
                Asteroid asteroid = objMgr.getAsteroid(gxs);
                asteroid.modify(gxs, "small", getX(), getY(), (float) (Math.random() * 90),
                        (float) (Math.random() * speed) + speed/2, (float) (Math.random() * rot) + rot/2);
            }
            //turn this source asteroid into a chunk
            modify(gxs, "small", getX(), getY(), (float) (Math.random() * 90),
                    (float) (Math.random() * speed) + speed/2, (float) (Math.random() * rot) + rot/2);
        }
    }

    public void explode(ObjectManager objMgr, GraphicsManager gxs){
        //create explosion particle effect
        int color = Color.argb(255, 172, 167, 147); //color similar to that of the asteroid, light brownish
        Explosion explosion;
        if (getSize() == 3) explosion = new Explosion(getCenterX(), getCenterY(), 60 * getSize(), 20, 0, color, 1);
        else explosion = new Explosion(getCenterX(), getCenterY(), 60 * getSize(), 0, 0, color, 0);
        objMgr.add(explosion);
        if(getSize() == 1) {
            disable();
            objMgr.asteroidDisabled();
        }
        else {
            Log.i("projectile impact", "splitting asteroid");
            split(objMgr, gxs);
        }
    }
    
    public int getSize(){ return size; }
}
