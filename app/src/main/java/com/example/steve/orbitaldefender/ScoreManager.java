package com.example.steve.orbitaldefender;

import android.graphics.Color;
import android.util.Log;

import java.util.Hashtable;

public class ScoreManager {

    private LevelManager lvlMgr;
    private int totalScore;
    private int[][] highScores; //to keep track of all the high scores and achievements unlocked through each run through
    private boolean[] achievementsUnlocked; //to save for ever high score, in the order listed below
    private Hashtable<String, Boolean> ftBonusUnlocks; //first time bonus unlocks, for every achievement
    private boolean ftBonusUnlocked; //whether a bonus has been unlocked for the first time
    private boolean allAchievementsUnlocked; //to record if all non-endgame achievements have been unlocked
    private boolean astHitPlanet, astHitPlanet_ns; //if true the tcoBonus can no longer unlocked for the run through or the nsBonus for that level
    private Object[] scoreRecord; //first index keeps highscores array, second index keeps achievements unlocked
    private int smallAstHitCt, regAstHitCt, largeAstHitCt; //these variables keep count of asteroids hit per level
    private int smallAstHitfrCt, regAstHitfrCt; //these variables keep count of asteroids hit at far range per level
    private int smallAstHitPlanetCt, regAstHitPlanetCt, largeAstHitPlanetCt; //these variables keep count of asteroids that hit the planet
    private int missedShots; //this variable keeps track of missed shots
    private int planetHits; //keeps track of planet hits, except for large asteroid, that's instant game over
    private int scoreCount; //used to count down score in the read out
    private int farRangedShots; //to keep track of far ranged shots
    private boolean shotsFired; //so that player doesn't get the mmBonus if level was completed but no shots at all were fired.


    //constants
    private final int maxScore = 99999999; //highest possibly achieve-able score
    private final int maxHighScores = 10; //max number of high scores to keep in record
    private final int maxAchievements = 5; //how many achievements there currently are
    private final int smallAstHitPts = 100; //score points gained for hitting small asteroid
    private final int regAstHitPts = 50; //score points gained for hitting regular asteroid
    private final int largeAstHitPts = 10; //score points gained for hitting large asteroid
    private final int smallAstHitfrPts = 20; //score points gained for hitting small asteroid at far range
    private final int regAstHitfrPts = 10; //score points gained for hitting regular asteroid at far range
    private final String FR_BONUS = "fr";
    private final int frBonus = 20; //multiplied by current level, gives total bonus points gained for hitting all asteroids at [far range]
    private final String MM_BONUS = "mm";
    private final int mmBonus = 40; //multiplied by current level, gives total bonus points gained for not missing a single shot [marksman]
    private final String PD_BONUS = "pd";
    private final int pdBonus = 10; //multiplied by current level, gives total bonus points gained for not letting a single asteroid hit the planet [planet defender]
    private final String NS_BONUS = "ns";
    private final int nsBonus = 20; //multiplied by current level, gives total bonus points gained for not using shields and still no damage to the planet [no shields]
    private final String TCO_BONUS = "tco";
    private final int tcoBonus = 1000000; //bonus points gained for finishing the game with zero damage to the planet [the chosen one]
    private final int ftBonus = 100; //bonus points gained for unlocking an achievement for the first time in a playthrough
    private final int aaBonus = 10000; //bonus points gained for unlocking all non-endgame achievements
    private final int smallAstHitPlanetPts = -500; //score penalty for letting small asteroid hit the planet
    private final int regAstHitPlanetPts = -1000; //score penalty for letting regular asteroid hit the planet
    private final int largeAstHitPlanetPts = -10000; //score penalty for letting large asteroid hit the planet
    private final int maxPlanetHits = 10; //max hits the planet takes to game over
    private final int smallAstDmg = 1; //small asteroid hitting planet hit count
    private final int regAstDmg = 2; //regular asteroid hitting planet hit count
    private final int largeAstDmg = maxPlanetHits; //large asteroid hitting planet is game over

    public ScoreManager(LevelManager lvlMgr){
        this.lvlMgr = lvlMgr;
        highScores = new int[10][5];
        achievementsUnlocked = new boolean[maxAchievements];
        for (int i = 0; i < achievementsUnlocked.length; i++) achievementsUnlocked[i] = false;
        scoreRecord = new Object[]{highScores, achievementsUnlocked};
        ftBonusUnlocks = new Hashtable<>();
        ftBonusUnlocks.put(FR_BONUS,false); //far ranged achievement
        ftBonusUnlocks.put(MM_BONUS,false); //marksman achievement
        ftBonusUnlocks.put(PD_BONUS,false); //planet defender achievement
        ftBonusUnlocks.put(NS_BONUS,false); //no shields achievement
        ftBonusUnlocks.put(TCO_BONUS,false); //the chosen one achievement
    }

    public void setInstantScore(int scorePts, float cx, float cy, ObjectManager objectManager){
        //check list if there are any disabled instantscore objects and reuse, otherwise create a new one
        InstantScore instantScore = null;
        for (WorldObject object: objectManager.getObjects())
            if (object instanceof InstantScore){
                instantScore = (InstantScore) object;
                instantScore.enable(scorePts, cx, cy);
            }
        if (instantScore == null){
            //create new object
            Log.i("setInstantScore()", "creating new instantScore object");
            objectManager.add(new InstantScore(scorePts, cx, cy, Color.argb(255, 170, 238, 255), objectManager));
        }

    }

    public int hitAsteroid(int asteroidSize){
        //called when projectile intercepts an asteroid
        int pts = 0;
        switch (asteroidSize){
            case 1:
                smallAstHitCt++;
                pts = smallAstHitPts;
                break;
            case 2:
                regAstHitCt++;
                pts = regAstHitPts;
                break;
            case 3:
                largeAstHitCt++;
                pts = largeAstHitPts;
                break;
        }
        return pts;
    }

    public int isFarRanged(float cx1, float cy1, float cx2, float cy2, int halfScreenHeight, int asteroidSize){
        //called when a projectile hits an asteroid, this calculates the distance between the asteroid before being hit and the cannon
        //then if it is far ranged, adds onto the current score
        int pts = 0;
        if (asteroidSize < 3){ //only applies to size 1 and size 2 asteroids
            double dist = Math.sqrt( (Math.pow(cx1 - cx2, 2) + Math.pow(cy1 - cy2, 2) ));
            if (dist > halfScreenHeight){
                switch (asteroidSize){
                    case 1:
                        smallAstHitfrCt++;
                        pts = smallAstHitfrPts;
                        break;
                    case 2:
                        regAstHitfrCt++;
                        pts = regAstHitfrPts;
                        break;
                }
            }
        }
        if (pts != 0) farRangedShots++;
        return pts;
    }

    public int hitPlanet(int asteroidSize){
        //determines score penalty based on asteroid size, reduces from max hits the planet can take before game over.
        int pts = 0;
        switch(asteroidSize){
            case 1:
                planetHits += smallAstDmg;
                smallAstHitPlanetCt++;
                pts = smallAstHitPlanetPts;
                break;
            case 2:
                planetHits += regAstDmg;
                regAstHitPlanetCt++;
                pts = regAstHitPlanetPts;
                break;
            case 3:
                planetHits += largeAstDmg;
                largeAstHitPlanetCt++;
                pts = largeAstHitPlanetPts;
                break;
        }
        if (planetHits >= maxPlanetHits) lvlMgr.gameOver();
        return pts;
    }

    public void missedShot(){
        //called by a projectile that is about to get disabled from not hitting anything.
        missedShots++;
    }

    public void firedShot(){
        //called by a projectile to notify manager a shot was fired
        shotsFired = true;
    }

    private String[] calcScoreReadOut(String[] readout, int hitCount, int points, String insert, int presetMessage, boolean mutate){
        int multiplier = 1;
        //calculate readout
        if (scoreCount == 0) {
            scoreCount = hitCount * points;
            readout[2] = "+" + scoreCount;
        }
        if (scoreCount > 100 || scoreCount < -100){ //first calculate multiplier to accelerate the readout
            String mult = "";
            String score = scoreCount + "";
            for (int i = 0; i < score.length() - 1; i++)
                mult += "0";
            mult = 1 + mult;
            multiplier = Integer.parseInt(mult);
        }
        switch (presetMessage){
            case 1:
                readout[0] = "" + hitCount + " " + insert + " asteroid(s) destroyed";
                break;
            case 2:
                readout[0] = "Bonus: " + hitCount + " " + insert + " asteroid(s) destroyed at far range";
                break;
            case 3: //fr_bonus
                readout[0] = insert + ": Sharpshooter, only long ranged hits";
                break;
            case 4: //mm_bonus
                readout[0] = insert + ": Marksman, no shots missed";
                break;
            case 5: //pd_bonus
                readout[0] = insert + ": Planet Defender, no damage to planet";
                break;
            case 6: //ns_bonus
                readout[0] = insert + ": Sharpshooter, no shields used and no damage to planet";
                break;
            case 7: //tco_bonus
                readout[0] = insert + ": The Chosen One, beat the game with no damage to planet";
                break;
            case 8: //unlocked all achievements
                readout[0] = insert + ": you've unlocked all non-endgame achievements!";
                break;
            case 9: //penalties
                readout[0] = "Penalty: " + hitCount + " " + insert + " asteroid(s) hit the planet";

        }

        if (mutate && readout[2] == null){
            if (scoreCount < 0){ //negative, applying penalty to total score
                readout[1] = "" + scoreCount;
                scoreCount += 1 * multiplier;
                if (totalScore - multiplier >= 0) //only apply penalty until total score reaches zero
                    totalScore -= 1 * multiplier;
                else { //this should take care of any left over points
                    scoreCount = 0;
                    totalScore = 0;
                }
            }
            else { //positive, applying points and bonuses to total score
                readout[1] = "+" + scoreCount;
                scoreCount -= 1 * multiplier;
                totalScore += 1 * multiplier;
            }
        }
        else {
            if (scoreCount > 0) readout[1] = "+" + scoreCount;
            else readout[1] = "" + scoreCount;
        }
        return readout;
    }

    public String[] getScoreReadout(boolean mutate, UIManager.StatusBar Energy){
        //this returns a score in sequence to display to the player, it should be recalled until it returns null
        //on the screen, score is displayed like this -> "# small asteroids hit", second line "+###" (score points)
        //the "+###" counts down to zero as its added to the total score. When the "+###" reaches zero the next score is displayed.
        String[] readout = new String[3];
        if (!lvlMgr.isGameOver()){ //only add any points gained if its not game over
            if (smallAstHitCt > 0){
                readout = calcScoreReadOut(readout, smallAstHitCt, smallAstHitPts, "small", 1, mutate);
                if (scoreCount == 0) smallAstHitCt = 0;
                return readout;
            }
            else if (regAstHitCt > 0){
                readout = calcScoreReadOut(readout, regAstHitCt, regAstHitPts, "regular", 1, mutate);
                if (scoreCount == 0) regAstHitCt = 0;
                return readout;
            }
            else if (largeAstHitCt > 0){
                readout = calcScoreReadOut(readout, largeAstHitCt, largeAstHitPts, "large", 1, mutate);
                if (scoreCount == 0) largeAstHitCt = 0;
                return readout;
            }
            else if (smallAstHitfrCt > 0){
                readout = calcScoreReadOut(readout, smallAstHitfrCt, smallAstHitfrPts, "small", 2, mutate);
                if (scoreCount == 0) smallAstHitfrCt = 0;
                return readout;
            }
            else if (regAstHitfrCt > 0){
                readout = calcScoreReadOut(readout, regAstHitfrCt, regAstHitfrPts, "regular", 2, mutate);
                if (scoreCount == 0) regAstHitfrCt = 0;
                return readout;
            }
            else if (ftBonusUnlocked){
                int presetMsg = 0;
                for(String s : ftBonusUnlocks.keySet())
                    if (ftBonusUnlocks.get(s)){
                        switch (s){ //chose preset message based on achievement unlocked
                            case FR_BONUS: presetMsg = 3; break;
                            case MM_BONUS: presetMsg = 4; break;
                            case PD_BONUS: presetMsg = 5; break;
                            case NS_BONUS: presetMsg = 6; break;
                            case TCO_BONUS: presetMsg = 7; break;
                        }
                        readout = calcScoreReadOut(readout, lvlMgr.getLevel(), ftBonus, "Achievement Unlocked", presetMsg, mutate);
                        if (scoreCount == 0) {
                            ftBonusUnlocks.put(s,false);
                            ftBonusUnlocked = false;
                        }
                        return readout;
                    }
            }
            else if (farRangedShots == lvlMgr.getEntitiesCount() && !lvlMgr.isGamePractice()){
                readout = calcScoreReadOut(readout, lvlMgr.getLevel(), frBonus, "Bonus", 3, mutate);
                if (scoreCount == 0) {
                    farRangedShots = -1;
                    if (ftBonusUnlocks.get(FR_BONUS) == false && !achievementsUnlocked[0]){
                        ftBonusUnlocks.put(FR_BONUS, true);
                        ftBonusUnlocked = true;
                        achievementUnlocked(0);
                    }
                }
                return readout;
            }
            else if (missedShots == 0  && shotsFired && !lvlMgr.isGamePractice()){
                readout = calcScoreReadOut(readout, lvlMgr.getLevel(), mmBonus, "Bonus", 4, mutate);
                if (scoreCount == 0) {
                    missedShots = -1;
                    if (ftBonusUnlocks.get(MM_BONUS) == false && !achievementsUnlocked[1]){
                        ftBonusUnlocks.put(MM_BONUS, true);
                        ftBonusUnlocked = true;
                        achievementUnlocked(1);
                    }
                }
                return readout;
            }
            else if (planetHits == 0  && !lvlMgr.isGamePractice()){
                readout = calcScoreReadOut(readout, lvlMgr.getLevel(), pdBonus, "Bonus", 5, mutate);
                if (scoreCount == 0) {
                    planetHits = -1;
                    if (ftBonusUnlocks.get(PD_BONUS) == false && !achievementsUnlocked[2]){
                        ftBonusUnlocks.put(PD_BONUS, true);
                        ftBonusUnlocked = true;
                        achievementUnlocked(2);
                    }
                }
                return readout;
            }
            else if (Energy.getValue() == Energy.getFixedValue() && (planetHits == 0 || planetHits == -1)
                    && !lvlMgr.isGamePractice()){
                readout = calcScoreReadOut(readout, lvlMgr.getLevel(), nsBonus, "Bonus", 6, mutate);
                if (scoreCount == 0){
                    planetHits = -2;
                    if (ftBonusUnlocks.get(NS_BONUS) == false && !achievementsUnlocked[3]){
                        ftBonusUnlocks.put(NS_BONUS, true);
                        ftBonusUnlocked = true;
                        achievementUnlocked(3);
                    }

                }
                return readout;
            }
            else if (lvlMgr.isLastLevel() && (planetHits == 0 || planetHits == -1 || planetHits == -2)
                    && !lvlMgr.isGamePractice()){
                readout = calcScoreReadOut(readout, 1, tcoBonus, "Bonus", 7, mutate);
                if (scoreCount == 0){
                    planetHits = -3;
                    if (ftBonusUnlocks.get(TCO_BONUS) == false && !achievementsUnlocked[4]){
                        ftBonusUnlocks.put(TCO_BONUS, true);
                        ftBonusUnlocked = true;
                        achievementUnlocked(4);
                    }
                }
                return readout;
            }
            else if (allAchievementsUnlocked){
                readout = calcScoreReadOut(readout, 1, aaBonus, "Bonus", 8, mutate);
                if (scoreCount == 0) allAchievementsUnlocked = false;
            }
        }

        //penalties; if there are any, they're read out whether game is over or level is completed prior to adding total score to highscore
        if (smallAstHitPlanetCt > 0){
            readout = calcScoreReadOut(readout, smallAstHitPlanetCt, smallAstHitPlanetPts, "small", 9, mutate);
            if (scoreCount == 0) smallAstHitPlanetCt = 0;
            return readout;
        }
        else if (regAstHitPlanetCt > 0){
            readout = calcScoreReadOut(readout, regAstHitPlanetCt, regAstHitPlanetPts, "regular", 9, mutate);
            if (scoreCount == 0) regAstHitPlanetCt = 0;
            return readout;
        }
        else if (largeAstHitPlanetCt > 0){
            readout = calcScoreReadOut(readout, largeAstHitPlanetCt, largeAstHitPlanetPts, "large", 9, mutate);
            if (scoreCount == 0) largeAstHitPlanetCt = 0;
            return readout;
        }

        reset();
        return null;
    }

    public void reset(){
        //called after each level is finished, resets all variables that hold data for the current level
        missedShots = 0;
        farRangedShots = 0;
        if (planetHits < 0) planetHits = 0; //only reset if there has been no damage to planet
    }

    public void fullReset(){
        //TO-DO: called after is game over or game is finished, add prompt player to put a tag and add to the high score
        for (int i = 0; i < achievementsUnlocked.length; i++) achievementsUnlocked[i] = false;
        for(String s : ftBonusUnlocks.keySet()) ftBonusUnlocks.put(s,false);

        reset();
    }

    private void achievementUnlocked(int index){
        achievementsUnlocked[index] = true;
        int unlocks = 0;
        for (int i = 0; i < achievementsUnlocked.length; i++)
            if (achievementsUnlocked[i]) unlocks++;
        if (unlocks == achievementsUnlocked.length) allAchievementsUnlocked = true;

    }

    public int getTotalScore(){ return totalScore; }
    public int getMaxScore(){ return maxScore; }
    public boolean shotsFired(){ return shotsFired; }
}
