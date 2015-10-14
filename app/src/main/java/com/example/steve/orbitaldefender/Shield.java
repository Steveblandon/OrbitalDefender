package com.example.steve.orbitaldefender;

import android.graphics.Bitmap;
import android.graphics.Paint;

import java.util.List;


public class Shield extends WorldObject {

    public final static String TAG  = Shield.class.getSimpleName();
    private int alpha;
    private int fixedAlpha; //to what the variable alpha is reset to each time is enabled
    private Paint paint;
    private int lifespan; //how many ticks the shield is active
    private int decay; //rate at which the shield decays
    private int life; //how many ticks the shield has been active
    private float energyUse; //percentage of total shield energy use
    private int blink; //to orchestrate the destabilizing blink

    public Shield(Bitmap bitmap, float cx, float cy, Paint paint, int lifespan, float energyUse) {
        super(bitmap, cx - (bitmap.getWidth() / 2), cy - (bitmap.getWidth() / 2));
        fixedAlpha = paint.getAlpha();
        enable(cx, cy, paint, lifespan, energyUse);
    }

    /*
    public void rotate(float angle, float pivot){
            getMatrix().reset();
            getMatrix().postRotate(angle, getBitmap().getWidth() / 2, (getBitmap().getHeight() * (pivot * 0.75f)));
            getMatrix().postTranslate(getX(), getY());
    }*/

    public void fade(){
        alpha -= decay;
        life++;
        if (alpha <= 0) {
            disable();
        }
    }

    public void destabilize(){
        //when it intercepts an asteroid, blink than disable
        switch (blink){
            case 1:
                alpha = fixedAlpha;
                blink++;
                break;
            case 2:
                alpha = fixedAlpha/6;
                blink++;
                break;
            case 3:
                alpha = fixedAlpha/2 + fixedAlpha/4;
                blink++;
                break;
            case 4:
                blink = 0;
                disable();
                break;
        }
    }

    public void enable(float cx, float cy, Paint paint, int lifespan, float energyUse){
        super.enable(cx - (getBitmap().getWidth() / 2), cy - (getBitmap().getWidth() / 2), true);
        alpha = fixedAlpha;
        this.paint = paint;
        this.lifespan = lifespan;
        this.energyUse = energyUse;
        life = 0;
        if (lifespan == 0) decay = (int) Math.ceil(fixedAlpha);
        else decay = (int) Math.ceil(fixedAlpha/(float)lifespan);
    }

    public void disable(){
        super.disable();
        life = 0;
    }

    public Asteroid intersectsAsteroid(List<WorldObject> objects){
        //collision detection; return any asteroid object it intersects with
        for (WorldObject object : objects)
            if (object instanceof Asteroid && object.isVisible()){
                //try to get the distance from asteroid to shield, and do intersection with that
                double dist = Math.sqrt( (Math.pow(object.getCenterX() - getCenterX(), 2)
                        + Math.pow(object.getCenterY() - getCenterY(), 2) ));
                if (dist < getBitmap().getWidth() / 2.5){ //almost a third, so that the asteroid dips a bit into the shield before exploding
                    return (Asteroid) object;
                }
            }
        return null; //if there was no collision
    }

    public float getEnergyUse(){ return energyUse * (life/lifespan); }

    public Paint getPaint(){
        paint.setAlpha(alpha);
        return paint;
    }

    public void intercepted(){
        //called when shield intercepts asteroid, actives destabilization but doesn't restart it
        if (blink == 0) blink = 1;
    }

    public int getLifespan() { return lifespan; }
    public int getLifetime() { return life; }
}
