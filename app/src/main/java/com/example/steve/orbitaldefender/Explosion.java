package com.example.steve.orbitaldefender;


import android.graphics.Paint;

public class Explosion extends WorldObject{

    public final static String TAG  = WorldObject.class.getSimpleName();
    private Particle[] particles;

    public Explosion(float x, float y, int particleCount,int offset, int color, int type) {
        super(null, x, y);
        orchestrate(x, y, particleCount, offset, 0, color, type);
    }

    public Explosion(float x, float y, int particleCount, int offset, int maxSize, int color, int type) {
        super(null, x, y);
        orchestrate(x, y, particleCount, offset, maxSize, color, type);
    }

    private void orchestrate(float x, float y, int particleCount, int offset, int maxSize, int color, int type){
        particles = new Particle[particleCount];
        switch (type){
            case 3: //creates a wedge like effect, divided into 6 groups
                int e = 0;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 1
                    particles[i] = new Particle(x, y, maxSize, color, -45);
                e = (particleCount/6)*1;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 2
                    particles[i] = new Particle(x, y, maxSize, color, -135);
                e = (particleCount/6)*2;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 3
                    particles[i] = new Particle(x + offset, y + offset, maxSize, color, -55);
                e = (particleCount/6)*3;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 4
                    particles[i] = new Particle(x - offset, y - offset, maxSize, color, -145);
                e = (particleCount/6)*4;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 5
                    particles[i] = new Particle(x + offset, y + offset, maxSize, color, -65);
                e = (particleCount/6)*5;
                for (int i = 0 + e; i < particleCount/6 + e; i++) //initiate all the particle objects, group 6
                    particles[i] = new Particle(x - offset, y - offset, maxSize, color, -155);
            case 2: //particles are divided into 5 groups, one spawns at given coordinate, the rest higher, then lower, to the left, and to the right
                e = 0;
                for (int i = 0 + e; i < particleCount/5 + e; i++) //initiate all the particle objects, group 1
                    particles[i] = new Particle(x, y, maxSize, color);
                e = (particleCount/5)*1;
                for (int i = 0 + e; i < particleCount/5 + e; i++) //initiate all the particle objects, group 2
                    particles[i] = new Particle(x + offset, y, maxSize, color);
                e = (particleCount/5)*2;
                for (int i = 0 + e; i < particleCount/5 + e; i++) //initiate all the particle objects, group 3
                    particles[i] = new Particle(x - offset, y, maxSize, color);
                e = (particleCount/5)*3;
                for (int i = 0 + e; i < particleCount/5 + e; i++) //initiate all the particle objects, group 4
                    particles[i] = new Particle(x, y + offset, maxSize, color);
                e = (particleCount/5)*4;
                for (int i = 0 + e; i < particleCount/5 + e; i++) //initiate all the particle objects, group 5
                    particles[i] = new Particle(x, y - offset, maxSize, color);
                break;
            case 1: //particles begin from random locations around the given coordinate, larger particleCount recommended
                for (int i = 0; i < particleCount; i++) {//initiate all the particle objects
                    int sign = (int) (Math.random() * 2);
                    if (sign >= 1) sign = 1;
                    else if (sign < 1) sign = -1;
                    float xOffset = (float) (Math.random() * offset) * sign;
                    sign = (int) (Math.random() * 2);
                    if (sign >= 1) sign = 1;
                    else if (sign < 1) sign = -1;
                    float yOffset = (float) (Math.random() * offset) * sign;
                    particles[i] = new Particle(x + xOffset, y + yOffset, maxSize, color);
                }
                break;
            default: //regular explosion, all particles begin from the center of object exploding, particle size is random
                for (int i = 0; i < particleCount; i++) //initiate all the particle objects
                    particles[i] = new Particle(x, y, maxSize, color);
                break;
        }
    }


    public void update(){
        int particleUpdates = 0;
        for (Particle particle : particles){
            if (particle.isVisible()){
                particle.update();
                particleUpdates++;
            }
        }
        if (particleUpdates == 0) {
            disable(); //if there were no particle updates all particles have faded away so this object is no longer needed
        }
    }

    public Particle[] getParticles(){ return particles; }


    public class Particle extends WorldObject{
    //for the time being particles are just small circles that scatter from the point of impact (meant for explosion effects)

        private float direction; //angle (in degrees)
        private float speed; // variable
        private final float maxSpeed = 4;
        private int size;
        private int maxSize = 7;
        private Paint paint; //color property to handle its color and transparency (alpha)
        private int age, decay; //age goes up every tick, decay is rate by which particle fades away
        private final int lifespan = 30; //number of ticks before completely fading out

        public Particle(float x, float y, int maxSize, int color) {
            super(null, x, y);
            createParticle(x, y, maxSize, color, 0);
        }

        public Particle(float x, float y, int maxSize, int color, int direction) {
            super(null, x, y);
            createParticle(x, y, maxSize, color, direction);
        }

        public void createParticle(float x, float y, int maxSize, int color, int direction){
            if (maxSize > 0) this.maxSize = maxSize;
            if (direction == 0) this.direction = (float) Math.random() * 360;
            else this.direction = direction;
            if (direction < 180) direction = -direction; //negate direction so coordinates can decrease (go left or up)
            else direction = direction - 180;
            size = (int) (Math.random() * this.maxSize) + 1;
            age = 0;
            speed = (float) (Math.random() * maxSpeed) + 1;
            decay = 255 / lifespan;
            paint = new Paint();
            paint.setColor(color);
        }

        public void update(){
            if (age < lifespan){
                float x = getX();
                float y = getY();
                if (age > 0){
                    x += Math.cos(Math.toRadians(direction)) * speed;
                    y += Math.sin(Math.toRadians(direction)) * speed;
                    if (paint.getAlpha() > 0) paint.setAlpha(paint.getAlpha() - decay);
                }
                setLocation(x, y);
                age++;
            }
            else {
                disable(); //disable particle once it has faded away and reached its lifespan
            }
        }


        public int getSize(){ return size; }
        public Paint getPaint(){ return paint; }
    }
}
