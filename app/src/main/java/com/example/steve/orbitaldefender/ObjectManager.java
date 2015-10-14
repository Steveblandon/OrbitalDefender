package com.example.steve.orbitaldefender;


import android.graphics.Bitmap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObjectManager {

    private CopyOnWriteArrayList<WorldObject> objects;
    private Spaceship spaceship; //for easy access purposes
    private Shield shield; //for easy access purposes
    private int activeAsteroids;

    public ObjectManager(){
        objects = new CopyOnWriteArrayList<>(); //thread safe arraylist
    }

    public void add(WorldObject object){
        //add a single entity to storage
        objects.add(object);
        if (object instanceof Spaceship) spaceship = (Spaceship) object; //there should only be one copy of this object in the objects list
        if (object instanceof Shield) shield = (Shield) object;
    }

    public void remove(int index){
        //remove entity at given index
        objects.remove(index);
    }

    public Spaceship getShip(){ return spaceship; }

    public Shield getShield(){ return shield; }

    public void noActiveAsteroids() { activeAsteroids = 0; }

    public void asteroidDisabled() { activeAsteroids--; }

    public int getActiveAsteroids() { return activeAsteroids; }

    public Asteroid getAsteroid(GraphicsManager gxs){
        //this method returns either a blank new target/entity (asteroid object) or one that has been already loaded and is inactive
        Asteroid asteroid = null;
        for (WorldObject object : objects)
            if (object instanceof Asteroid && !object.isVisible()){
                asteroid = (Asteroid) object;
            }
        if (asteroid == null){ //no disabled asteroid objects were found, create a new one
            asteroid = new Asteroid(gxs, 0, 0, 1);
            add(asteroid);
        }
        else asteroid.enable(0, -asteroid.getBitmap().getHeight(), true); //enable the asteroid outside the viewPort
        activeAsteroids++;
        return asteroid;
    }

    //special method for recycling projectile objects
    public Laser getLaser(GraphicsManager graphicsManager){
        //do some calculations to determine where exactly bullet spawns (barrel tip of spaceship)
        Bitmap img = graphicsManager.get(Laser.TAG);
        Spaceship spaceship = (Spaceship) getShip();
        //check collection of world objects for any inactive bullet objects, otherwise create a new one
        for (WorldObject object : objects){
            if (object instanceof Laser && !object.isVisible()) {
                    object.enable(spaceship.getCenterX(), spaceship.getCenterY(), true);
                    return (Laser) object;
                }
        }
        WorldObject newBullet = new Laser(img, spaceship.getCenterX(), spaceship.getCenterY());
        objects.add(newBullet);
        return (Laser) newBullet;
    }

    public List<WorldObject> getObjects(){
        return objects;
    }
}
