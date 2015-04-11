import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.Iterator; 
import org.gwoptics.graphics.graph2D.Graph2D; 
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation; 
import org.gwoptics.graphics.graph2D.traces.RollingLine2DTrace; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Evolutionary_Bugs_Program extends PApplet {






ArrayList<WorldBug> bugs;
ArrayList<Entity> foodList;
ArrayList<Entity> fireList;

Master bugMaster;
PShape bugShape;
PShape adultBugShape;
PShape oldestBugShape;
PShape foodShape;
PShape fireShape;

int totalTime;
int bestTime;
int bestBug;
int timeSinceBestBug;
int bestFamily;
int timeSinceBestFamily;

SimpleData totalPop;
SimpleData adultPop;
SimpleData childPop;
SimpleData oldestBug;
SimpleData oldestFamily;
SimpleData averageBug;
SimpleData fireCount;
SimpleData foodCount;

RollingLine2DTrace totTrace, adlTrace, chlTrace, oldTrace, famTrace, avgTrace, fiTrace, fdTrace;

Graph2D graph;

// TODO: REDO SENSOR DIRECTION SO IT'S ACTUALLY FROM THE REFERENCE POINT OF THE BUG INCLUDING IT'S ROTATION!!!!!!!
public void setup(){
  size(SCREEN_WIDTH + GRAPH_WIDTH, SCREEN_HEIGHT, P2D);
  frameRate(TICKS_PER_SEC);
  background(255);
  smooth();
  
  
  bugs = new ArrayList<WorldBug>(MAX_BUGS);
  foodList = new ArrayList<Entity>(MAX_FOOD);
  fireList = new ArrayList<Entity>(MAX_FIRE);
  
  bugMaster = new MasterImplementation1();
  bugShape = createBugShape(CHILD);
  adultBugShape = createBugShape(ADULT);
  oldestBugShape = createBugShape(OLDEST);
  foodShape = createFoodShape();
  fireShape = createFireShape();
  
  totalTime = 0;
  bestTime = 0;
  bestFamily = 0;
  
  // Everything for graphing
  totalPop = new SimpleData(GRAPH_MAX_Y/MAX_BUGS);
  adultPop = new SimpleData(GRAPH_MAX_Y/MAX_BUGS);
  childPop = new SimpleData(GRAPH_MAX_Y/MAX_BUGS);
  oldestBug = new SimpleData((double)1/15);
  oldestFamily = new SimpleData((double)1/80);
  averageBug = new SimpleData((double)1/15);
  fireCount = new SimpleData(GRAPH_MAX_Y/(MAX_FIRE+MAX_FOOD));
  foodCount = new SimpleData(GRAPH_MAX_Y/(MAX_FIRE+MAX_FOOD));
  
  totTrace = new RollingLine2DTrace(totalPop, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  totTrace.setTraceColour(255, 0, 0);
  totTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  adlTrace = new RollingLine2DTrace(adultPop, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  adlTrace.setTraceColour(0, 0, 225);
  adlTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  chlTrace = new RollingLine2DTrace(childPop, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  chlTrace.setTraceColour(100, 120, 225);
  chlTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  oldTrace = new RollingLine2DTrace(oldestBug, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  oldTrace.setTraceColour(225, 225, 225);
  oldTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  famTrace = new RollingLine2DTrace(oldestFamily, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  famTrace.setTraceColour(225, 132, 0);
  famTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  avgTrace = new RollingLine2DTrace(averageBug, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  avgTrace.setTraceColour(150, 150, 150);
  avgTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  fiTrace = new RollingLine2DTrace(fireCount, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  fiTrace.setTraceColour(60, 0, 0);
  fiTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  fdTrace = new RollingLine2DTrace(foodCount, GRAPH_REFRESH_RATE, GRAPH_X_SPEED);
  fdTrace.setTraceColour(0, 60, 0);
  fdTrace.setLineWidth(GRAPH_LINE_WEIGHT);
  
  graph = new Graph2D(this, GRAPH_WIDTH, GRAPH_HEIGHT, false);
  
  graph.addTrace(fiTrace);
  graph.addTrace(fdTrace);
  graph.addTrace(adlTrace);
  graph.addTrace(chlTrace);
  graph.addTrace(avgTrace);
  graph.addTrace(famTrace);
  graph.addTrace(totTrace);
  graph.addTrace(oldTrace);
  
  graph.position.y = SCREEN_HEIGHT - GRAPH_HEIGHT;
  graph.position.x = SCREEN_WIDTH;
  
  graph.setYAxisMax(GRAPH_MAX_Y);
  graph.setAxisColour(225, 225, 225);
  graph.setYAxisTickSpacing(100);
  graph.setXAxisMax(10f);
  
  graph.getXAxis().setDrawAxisLabel(false);
  graph.getYAxis().setDrawAxisLabel(false);
  
  graph.getXAxis().setDrawTickLabels(false);
  graph.getYAxis().setDrawTickLabels(false);
  
  shapeMode(CENTER);
  fill(0);
}

public void draw(){
  background(255);
  
  Entity wEnt;  // world entity, used when iterating food and fire entities
  WorldBug bug; // the bug used for iterating
  WorldBug oBug;// the bug used for comparing
  WorldBug oldBug = null; // the bug to display as the oldest
  
  ArrayList<Sensor> sensors; // sensors that sense stuff
  float dist;   // the distance between the bug and whatever it's measuring against
  int dir;      // the direction of the sensor we want
  ArrayList<Mating> mates = new ArrayList<Mating>();
 
  totalPop.reset();
  adultPop.reset();
  childPop.reset();
  oldestBug.reset();
  fireCount.reset();
  foodCount.reset();
  averageBug.reset();
  oldestFamily.reset();
 
  if (bugs.size() <= MIN_BUGS) {
    int currSize = bugs.size();
    
    background(0);
    //killAll();
    totalTime = 0;
    // spawn the starting bugs
    for (int i = 0; i < START_BUGS - currSize; i++) {
      bugs.add(new WorldBug(bugMaster.init()));
    }
  }
 
  // chance to create new food
  if (foodList.size() < MAX_FOOD) {
    if (PApplet.parseInt(random(SPAWN_CHANCE_FOOD)) == 1) {
      foodList.add(new Entity(LIFE_FOOD, foodShape));
    }
  }
  // Iterate through food list, tick then remove if too old
  for (Iterator<Entity> it = foodList.iterator(); it.hasNext(); ) {
    wEnt = it.next();
    if (wEnt.tick() == DEAD) {
      it.remove();
    }
  }
  
  // chance to create new fire
  if (fireList.size() < MAX_FIRE) {
    if (PApplet.parseInt(random(SPAWN_CHANCE_FIRE)) == 1) {
      fireList.add(new Entity(LIFE_FIRE, fireShape));
    }
  }
  
  // Iterate through fire list, tick then remove if too old
  for (Iterator<Entity> it = fireList.iterator(); it.hasNext(); ) {
    wEnt = it.next();
    if (wEnt.tick() == DEAD) {
      it.remove();
    }
  }
  
  // loop through each bug
  for (Iterator<WorldBug> bugIt = bugs.iterator(); bugIt.hasNext(); ) {
    bug = bugIt.next();
    sensors = new ArrayList<Sensor>(NUM_SENSORS);
  
    // initialize the sensors
    for (int i = 0; i < NUM_SENSORS; i++ ) {
      sensors.add(new Sensor());
    }
    
    // loop each fire
    for (Iterator<Entity> it = fireList.iterator(); it.hasNext(); ) {
      wEnt = it.next();
      dist = bug.getDistance(wEnt.x, wEnt.y);
      
      // if touching kill bug
      if (dist <= FIRE_S/2) {
        bugMaster.died(bug.bug);
        bugIt.remove();
      // if inside sensor radius, trigger sensor
      } else if (dist <= SENSOR_DIST_FIRE + FIRE_S/2) {
        sensors.get(sensorDir( bug.x,  bug.y,  wEnt.x,  wEnt.y, bug.rot)).fire = true;
      }
    }    
    
    // loop each food
    for (Iterator<Entity> it = foodList.iterator(); it.hasNext(); ) {
      wEnt = it.next();
      dist = bug.getDistance(wEnt.x, wEnt.y);
      
      // if touching feed bug, kill food
      if (dist <= FOOD_S/2) {
        bug.fed();
        it.remove();
      // if inside sensor radius, trigger sensor
      } else if (dist <= SENSOR_DIST_FOOD + FOOD_S/2) {
        sensors.get(sensorDir( bug.x,  bug.y,  wEnt.x,  wEnt.y, bug.rot)).food = true;
      }
    }    
        
    
    // check that the mate is still fine
    if (bug.mate != null){
      if (bug.mate.bug1.alive && bug.mate.bug2.alive){
        
      } else {
        bug.mate = null;
      }
    }
    
    // loop each bug
    for (Iterator<WorldBug> subBugIt = bugs.iterator(); subBugIt.hasNext(); ) {
      oBug = subBugIt.next();
      // make sure we aren't testing with the same bug
      if (oBug != bug) {
        // only care about other bugs if they're an adult.
        if (oBug.isAdult()){
          dist = bug.getDistance(oBug.x, oBug.y);
          
          // if bug is currently breeding
          if (bug.mate != null) {
            // if bug is breeding with oBug
            if (bug.mate.inPair(oBug)) {
              // if bug is still within range of oBug, tick
              if ( dist <= REPRODUCTION_DIST) {
                // if long enough, reproduce
                if (bug.mate.tick(bug)) {
                  if (bugs.size() < MAX_BUGS) {
                    mates.add(bug.mate);
                  }
                }
                sensors.get( sensorDir(bug.x, bug.y, oBug.x, oBug.y, bug.rot) ).mate = true;
              // if bug is not within range, delete the mate
              } else {
                bug.mate = null;
                oBug.mate = null;
              }
            }
            
          } else {
            // if bug and oBug are able to breed and are within range
            if ( dist <= REPRODUCTION_DIST && bug.canReproduce() && oBug.canReproduce()) {
              Mating newMate = new Mating(bug, oBug);
              bug.mate = newMate;
              oBug.mate = newMate;
              sensors.get( sensorDir(bug.x, bug.y, oBug.x, oBug.y, bug.rot) ).mate = true;
            }
          } 
         
          // if bug is within sensor range 
          if ( dist <= SENSOR_DIST_BUGS ) {
            sensors.get( sensorDir(bug.x, bug.y, oBug.x, oBug.y, bug.rot) ).bug = true;
          }
        }
      }
    }
    
    // give sensor to bug
    if ( bug.tick(sensors) == DEAD ) {
      bugMaster.died(bug.bug);
      bugIt.remove();
    } else {
      // if the bug is older than the oldest one found sofar, set it
      if ( bug.totalLife > oldestBug.getRawData() ){
        oldestBug.set(bug.totalLife);
        oldBug = bug;
      }
      
      if (bug.familyLine > oldestFamily.getRawData()){
        oldestFamily.set(bug.familyLine);
      }
      
      // if the bug is an adult, add it to adult pop, otherwise add it to child pop
      if ( bug.isAdult() ) {
        adultPop.add(1);
      } else {
        childPop.add(1);
      }
      
      // add the bug life to the average
      averageBug.add(bug.totalLife);
      
    }
  }
  
  // Add mating that was determined to result in a birth.
  for (Mating mate : mates){
    if (mate.bug1.mate != null && mate.bug2.mate != null) {
      bugs.add(new WorldBug(bugMaster.reproduce(mate.bug1.bug, mate.bug2.bug), PApplet.parseInt((mate.bug1.x + mate.bug2.x)/2), PApplet.parseInt((mate.bug1.y + mate.bug2.y)/2), PApplet.parseInt(random(DEG)), max(mate.bug1.familyLine, mate.bug2.familyLine)));
      mate.bug1.mate = null;
      mate.bug2.mate = null;
    }
  }
  
  if ( oldestBug.getRawData() > bestBug ) {
    bestBug = oldestBug.getRawDataInt();
    timeSinceBestBug = 0;
  }
  
  if ( oldestFamily.getRawData() > bestFamily ) {
    bestFamily = oldestFamily.getRawDataInt();
    timeSinceBestFamily = 0;
  }
  
  totalPop.set(bugs.size());
  fireCount.set(fireList.size());
  foodCount.set(foodList.size());
  averageBug.set(averageBug.getRawData()/bugs.size());
  
  if (oldBug != null){
    oldBug.oldest();
  }
  
  fill(0);
  rect(SCREEN_WIDTH, 0, GRAPH_WIDTH, SCREEN_HEIGHT);
  
  textAlign(LEFT, TOP);
  textSize(12);
  fill(225);
  totalTime++;
  if (totalTime > bestTime){
    bestTime = totalTime;
  }
  //if (bestBug > 0 ){
    timeSinceBestBug++;
  //}
  //if (bestFamily > 0){
    timeSinceBestFamily++;
  //}
  text("Time: " + ceil(totalTime/TICKS_PER_SEC), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING);
  text("Best: " + ceil(bestTime/TICKS_PER_SEC), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 1);
  text("Oldest Bug alive: " + ceil(oldestBug.getRawDataInt()/TICKS_PER_SEC), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 2);
  text("Best Ever Bug: " + ceil(bestBug/TICKS_PER_SEC), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 3);
  text("Time since Best Bug: " + ceil(timeSinceBestBug/TICKS_PER_SEC), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 4);
  text("Total Bugs: " + ceil(bugs.size()), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 5);
  text("Total Adults: " + ceil(adultPop.getRawDataInt()), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 6);
  text("Total Children: " + ceil(childPop.getRawDataInt()), SCREEN_WIDTH+DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 7);
  text("Average Bug: " + ceil(averageBug.getRawDataInt()/TICKS_PER_SEC), SCREEN_WIDTH + DATA_COL_WIDTH + DATA_LEFT_PADDING, DATA_TOP_PADDING);
  text("Best Family: " + ceil(oldestFamily.getRawDataInt()/TICKS_PER_SEC), SCREEN_WIDTH + DATA_COL_WIDTH + DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 1);
  text("Best Ever Family: " + ceil(bestFamily/TICKS_PER_SEC), SCREEN_WIDTH + DATA_COL_WIDTH + DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 2);
  text("Time since Best Family: " + ceil(timeSinceBestFamily/TICKS_PER_SEC), SCREEN_WIDTH + DATA_COL_WIDTH + DATA_LEFT_PADDING, DATA_TOP_PADDING + DATA_ROW_HEIGHT * 3);
  textSize(8);

  fill(0);
  
  graph.draw();
}

/* ========================
 * SHAPE CREATION FUNCTIONS
 * ======================== */
public PShape createBugShape(int type){
  PShape s; 
  s = createShape();
  s.beginShape();
  switch (type){
    case CHILD:
      s.fill(100, 120, 225);
      break;
    case ADULT:
      s.fill(0, 0, 225);
      break;
    case OLDEST:
      s.fill(0, 0, 100);
      break;
  }
  s.noStroke();
  s.vertex(0, 0);
  s.vertex(0, 2*B_S);
  s.vertex(2*B_S, 2*B_S);
  s.vertex(2*B_S, 0);
  s.vertex(B_S, -B_S);
  s.endShape(CLOSE);
  return s;
}

public PShape createFireShape(){
  PShape s;
  s = createShape(ELLIPSE, 0, 0, FIRE_S, FIRE_S);
  s.setFill(color(225, 0, 0));
  s.setStroke(false);
  return s;
}

public PShape createFoodShape(){
  PShape s;
  s = createShape(ELLIPSE, 0, 0, FOOD_S, FOOD_S);
  s.setFill(color(0, 225, 0));
  s.setStroke(false);
  return s;
}

/* =====================
 * GEOMETRICAL FUNCTIONS
 * ===================== */
public int sensorDir(float bX, float bY, float pX, float pY, float rot){
    return (ceil((degrees(atan2(bX - pX, pY - bY))+(-(rot-75)))/(DEG/12))+15) % 12;
}

/* ========================
 * BUG MANAGEMENT FUNCTIONS
 * ======================== */
public void killAll(){
  WorldBug bug;
  for (Iterator<WorldBug> bugIt = bugs.iterator(); bugIt.hasNext(); ) {
    bug = bugIt.next();
    bug.death();
    
    bugIt.remove();
  }
}
// constants defining state of world
public final int SCREEN_WIDTH = 650; // not including graph
public final int SCREEN_HEIGHT = 650;
public final int TICKS_PER_SEC = 30;
public final int START_BUGS = 15;
public final int SPAWN_BORDER = 25; // the distance from the edge something can spawn

public final int B_S = 5;     // coordinates for bug shape (size will be roughly twice this), collision will always be in the rear center
public final int FIRE_S = 30; // Diameter of Fire Shape
public final int FOOD_S = 15; // Diameter of Food Shape

// constants defining maximum number of items available
public final int MAX_FOOD = 10;
public final int MAX_FIRE = 5;
public final int MAX_BUGS = 35;
public final int MIN_BUGS = 2; // number of bugs that the game restarts at

// constants defining the chance of food or fire randomly spawning (1 in X chance)
public final int SPAWN_CHANCE_FOOD = 40;
public final int SPAWN_CHANCE_FIRE = 100;

// constants defining how long anything remains on the board
public final int LIFE_FOOD = 500;
public final int LIFE_FIRE = 200;
public final int LIFE_BUGS = 800; 

// constants defining how much food increases life.
// Defined in score = (EXP^(turns+DISP))*MULT+ADD where score is how much life goes up by and turns is how many times the bug has eaten before
public final float FOOD_SCORE_EXP = 0.78f;
public final int FOOD_SCORE_DISP = -20;
public final int FOOD_SCORE_MULT = 5;
public final int FOOD_SCORE_ADD = 30;

// constants defining the distance from the edge of an object a sensor has to be before it returns true 
public final int SENSOR_DIST_FOOD = 200;
public final int SENSOR_DIST_FIRE = 50;
public final int SENSOR_DIST_BUGS = 200;

// constants defining arguments relating to reproduction
public final int REPRODUCTION_DIST = 50;    // The distance the two bugs have to be apart to reproduce
public final int REPRODUCTION_TIME = 20;    // The time the bugs have to be within this distance to reproduce
public final int REPRODUCTION_CYCLE = 300;  // The time before a bug can reproduce. Reset after each reproduction.

// constants defining limitations on bugs movement
public final int MAX_ROT = 30;  // Maximum amount a bug can rotate in any one turn
public final int MAX_ACC = 5;  // Maximum amount a bug can accelerate in any one turn
public final int MAX_VEL = 5; // Maximum speed a bug can have
public final int MIN_VEL = 0;  // Minimum speed a bug can have

// constants defining 
public final int START_PAR_MAX_ROT = 25;       // The starting range that rotation parameters can be in
public final int START_PAR_MAX_ACC = 1;        // The starting range that acceleration parameters can be in
public final int START_PAR_MIN_ROT = -25;       // The starting range that rotation parameters can be in
public final int START_PAR_MIN_ACC = -1;        // The starting range that acceleration parameters can be in
public final int START_THR_ROT = 200;      // The starting range (0-X) that the rotation threshold can be in
public final int START_THR_ACC = 200;      // The starting range (0-X) that the acceleration threshold can be in
public final float ROT_MUTATION = 0.02f;         // The maximum amount to mutate the rotation of each ofspring by
public final float ACC_MUTATION = 0.001f;        // The maximum amount to mutate the acceleration of each ofspring by
public final float THR_MUTATION = 0.1f;           // The maximum amount to mutate the threshold of each ofspring by

// constants renaming booleans
public final Boolean DEAD = false;
public final Boolean ALIVE = true;

// representing types of bugs
public final int CHILD = 0;
public final int ADULT = 1;
public final int OLDEST = 2;

/* =======================
 * GRAPH RELATED CONSTANTS
 * ======================= */
public final long GRAPH_REFRESH_RATE = 500;
public final float GRAPH_X_SPEED = 0.01f;
public final int GRAPH_WIDTH = 500;
public final int GRAPH_HEIGHT = 300;
public final int GRAPH_MAX_Y = 1000;
public final int GRAPH_LINE_WEIGHT = 1;

public final int DATA_COL_WIDTH = 200;
public final int DATA_ROW_HEIGHT = 14;
public final int DATA_LEFT_PADDING = 10;
public final int DATA_TOP_PADDING = 5;

/* =================================
 * PERMANANT CONSTANTS DO NOT CHANGE
 * ================================= */
 
public final int NUM_SENSORS = 12; // the number of sensors a bug has
public final int NUM_PAR = 50;     // the number of parameters a bug has for each output

// Mathamatical constants
public final int DEG = 360;   // the amount of degrees in a single rotation


/* ===================
 * DEBUGGING CONSTANTS
 * =================== */
 
public final Boolean SHOW_LIFE = true; // show the life counter at the bottom right of each bug
public final Boolean SHOW_REP = true;  // show whether or not the bug can reproduce at the top right of each bug
public final Boolean SHOW_TREP = true; // show the time left till the bug can reproduce top left of each bug
// the location of a non-dynamic entity on the board.
class Entity {
  int x;           // x coordinates
  int y;           // y coordinates
  int time;        // time left till it dies
  PShape entShape; // shape representing the entity
  
  // constructor of an entity with given coordinates
  Entity(int xCoord, int yCoord, int time, PShape s) {
    this.x = xCoord;
    this.y = yCoord;
    this.time = time;
    this.entShape = s;
  }
  
  // constructor of an entity with a random location
  Entity(int time, PShape s) {
    this.x = PApplet.parseInt(random(SPAWN_BORDER, SCREEN_WIDTH - SPAWN_BORDER));
    this.y = PApplet.parseInt(random(SPAWN_BORDER, SCREEN_HEIGHT - SPAWN_BORDER));
    this.time = time;
    this.entShape = s;
  }
  
  // returns true if the entity remains alive false if it dies
  //  decreases time remaining by 1 each time, and displays the shape
  public Boolean tick(){
    this.time--;
    if (this.time > 0) {
      shape(this.entShape, x, y);
      return true;
    } else {
      return false;
    }
  }
}
/*
 * STUFF I WANT TO KNOW
 * - Total Population
 * - Adult Population
 * - Child Population
 * - Oldest Bug
 */
 
class SimpleData implements ILine2DEquation{
  private double data;
  private double multiplier;
  
  SimpleData(double multiplier){
    this.data = 0;
    this.multiplier = multiplier;
  }
  
  public double computePoint(double x,int pos) {
    return data * multiplier;
  }
  
  public void set(double input){
    data = input;
  }
  
  public void add(double input){
    data += input;
  }
  
  public void reset(){
    data = 0;
  }
  
  public double getRawData(){
    return data;
  }
  
  public int getRawDataInt(){
    return (int)data;
  }
  
  public double getMultiplier(){
    return multiplier;
  }
  
  public double getMData(){
    return data * multiplier;
  }
} 
class MasterImplementation1 extends Master {
   
  public Bug reproduce(Bug bug1, Bug bug2){
    
    // takes a number of parameters from one function and the rest from the other.
    //  the older bug provides more parameters
    // applies noise to each of the parameters, creates a bug out of them.
   
   
    float[] bug1A;
    float[] bug1R;
    float[] bug2A;
    float[] bug2R;
    if (random(-1, 1) > 0) {
      bug1A = bug1.getAPar();
      bug1R = bug1.getRPar();
      bug2A = bug2.getAPar();
      bug2R = bug2.getRPar();
    } else {
      bug2A = bug1.getAPar();
      bug2R = bug1.getRPar();
      bug1A = bug2.getAPar();
      bug1R = bug2.getRPar();
    }
    float[] newAPar = new float[NUM_PAR];
    float[] newRPar = new float[NUM_PAR];
    
    arrayCopy(bug1A, 0, newAPar, 0, NUM_PAR/2);
    arrayCopy(bug2A, NUM_PAR/2, newAPar, NUM_PAR/2, NUM_PAR/2);
    arrayCopy(bug1R, 0, newRPar, 0, NUM_PAR/2);
    arrayCopy(bug2R, NUM_PAR/2, newRPar, NUM_PAR/2, NUM_PAR/2);
    
    for (int i = 0; i < NUM_PAR; i ++) {
      newAPar[i] += newAPar[i]*random(-ACC_MUTATION, ACC_MUTATION);
      newRPar[i] += newRPar[i]*random(-ROT_MUTATION, ROT_MUTATION);
    }
    
    int newALThreshold;
    int newRLThreshold;
    
    if ( bug1.getLife() > bug2.getLife() ) {
      newALThreshold = bug1.getAThr() + PApplet.parseInt(bug1.getAThr() * random(-THR_MUTATION, THR_MUTATION));
      newRLThreshold = bug1.getRThr() + PApplet.parseInt(bug1.getRThr() * random(-THR_MUTATION, THR_MUTATION));
    } else {
      newALThreshold = bug2.getAThr() + PApplet.parseInt(bug2.getAThr() * random(-THR_MUTATION, THR_MUTATION));
      newRLThreshold = bug2.getRThr() + PApplet.parseInt(bug2.getRThr() * random(-THR_MUTATION, THR_MUTATION));
    }
    
    return new bugImplementation(newAPar, newRPar, newALThreshold, newRLThreshold);
  }
  
  public Bug init() {
    return new bugImplementation();
  }
}

class bugImplementation implements Bug {
  float[] aPar;
  int aLThreshold; // the threshold at which to cut off the life parameter
  float[] rPar;
  int rLThreshold; // the threshold at which to cut off the life parameter
  
  int life;
  
  // random parameters generated for new bug
  bugImplementation() {
    aPar = new float[NUM_PAR];
    rPar = new float[NUM_PAR];
    for (int i = 0; i < (NUM_PAR); i++){
      aPar[i] = random(START_PAR_MIN_ACC, START_PAR_MAX_ACC);
      rPar[i] = random(START_PAR_MIN_ROT, START_PAR_MAX_ROT);
    }
    aLThreshold = PApplet.parseInt(random(START_THR_ACC));
    rLThreshold = PApplet.parseInt(random(START_THR_ROT));
    //println(getValues());
    life = 0;
  }
  
  // input parameters
  bugImplementation (float[] acceleration, float[] rotation, int accLifeThreshold, int rotLifeThreshold) {
    aPar = acceleration;
    rPar = rotation;
    aLThreshold = accLifeThreshold;
    rLThreshold = rotLifeThreshold;
    
    life = 0;
  }
  
  // Every turn, this function will be called, and the bug will
  //  need to return a move that it will make to the World
  public Move tick(Packet turn) {
    
    life ++;
    
    ArrayList<Sensor> sensors = turn.sensors;
    Sensor s;
    int food, bug, mate, fire;
    float rotation = 0;
    float acceleration = 0;
    //String sensor = "";
    
    for (int i = 0; i < (NUM_PAR-2)/4; i++){
      //println(i);
      s = sensors.get(i);
      food = (s.food) ? 1 : 0;
      bug  = (s.bug)  ? 1 : 0;
      mate = (s.mate) ? 1 : 0;
      fire = (s.fire) ? 1 : 0;
      
      //sensor = sensor + food + ", " + bug + ", " + mate + ", " + fire + ", ";
      
      rotation = rotation + (rPar[i*4] * food) + (rPar[i*4+1] * bug) + (rPar[i*4+2] * mate) + (rPar[i*4+3] * fire);
      acceleration = acceleration + (aPar[i*4] * food) + (aPar[i*4+1] * bug) + (aPar[i*4+2] * mate) + (aPar[i*4+3] * fire);
    }
    
    if (life > aLThreshold) { acceleration = acceleration + aPar[NUM_PAR-2]; /*sensor = "1, 0, " + sensor;*/ } 
    else                    { acceleration = acceleration + aPar[NUM_PAR-1]; /*sensor = "0, 1, " + sensor;*/ }
    if (life > rLThreshold) { rotation = rotation + rPar[NUM_PAR-2]; /*sensor = "1, 0, " + sensor;*/ } 
    else                    { rotation = rotation + rPar[NUM_PAR-1]; /*sensor = "0, 1, " + sensor;*/ }
    
    //println(sensor);
    //println(rotation + ", " + acceleration);
    
    return new Move(rotation, acceleration);
  }
    
    
  // returns a string which contains a comma delimetered list
  //  of the current values used by the bugs.
  public String getValues() {
    String values;
    // the values string is laid out accordingly:
    // rLThreshold, aLThreshold, rot-s.food(0), rot-s.bug(0), rot-s.mate(0), rot-s.fire(0), acc-s.food(0), acc-s.bug(0), acc-s.mate(0), acc-s.fire(0)...
    values = rLThreshold + ", " + aLThreshold + ", " + rPar[NUM_PAR-2] + ", " + rPar[NUM_PAR-1] + ", " + aPar[NUM_PAR-2] + ", " + aPar[NUM_PAR-1] + ", ";
    for (int i = 0; i < (NUM_PAR-2); i+= 4){
      values = values + rPar[i] + ", " + rPar[i+1] + ", " + rPar[i+2] + ", " + rPar[i+3] + ", " + aPar[i] + ", " + aPar[i+1] + ", " + aPar[i+2] + ", " + aPar[i+3] + ", ";
    }
    return values;
  } 
  
  public float[] getAPar() {
    return aPar;
  }
  
  public float[] getRPar() {
    return rPar;
  }
  
  public int getAThr() {
    return aLThreshold;
  }
  
  public int getRThr() {
    return rLThreshold;
  }
  
  public int getLife() {
    return life;
  }
}
// class that handles time spent mating
class Mating {
  private WorldBug bug1;
  private WorldBug bug2;
  private int time;
  
  Mating(WorldBug bug1, WorldBug bug2){
    this.bug1 = bug1;
    this.bug2 = bug2;
    this.time = 0;
  }
  
  // increments time spent mating.
  //  returns true if bugs have mated long enough.
  public Boolean tick(WorldBug bug){
    time++;
    
    if (bug == bug1) {
      stroke(map(time/REPRODUCTION_TIME, 0, 1, 200, 0));
      line(bug1.x, bug1.y, bug2.x, bug2.y);
    }
    
    if ( time > REPRODUCTION_TIME*2 ) { // has to be times 2 because both bugs are going to tick it
      bug1.reproduced();
      bug2.reproduced();
      return true;
    } else {
      return false;
    }
  }
  
  // returns true if the given bug is in the pair.
  public Boolean inPair(WorldBug bug){
    if (bug1 == bug || bug2 == bug) {
      return true;
    } else {
      return false;
    }
  }
}
/* ==========
 * INTERFACES
 * ========== */
abstract class Master {
  Bug championBug;
  
  // Function, that given two "Parents" will return an "offspring"
  public abstract Bug reproduce(Bug bug1, Bug bug2);
  
  // Function, that will return a new random bug
  public abstract Bug init();
  
  // Function, that will return an offspring of the championBug mated with itself (slight mutation)
  //  if the championBug doesn't exist, will return just the init.
  public final Bug initChampion(){
    if (championBug == null) {
      return init();
    } else {
      return reproduce(championBug, championBug);
    }
  }
  
  // Function informing master that given bug has died
  public final void died(Bug deadBug){
    if (championBug == null) {
      championBug = deadBug;
    } else if (deadBug.getLife() > championBug.getLife()) {
      championBug = deadBug;
    }
  }
}

interface Bug {
  // Every turn, this function will be called, and the bug will
  //  need to return a move that it will make to the World
  public Move tick(Packet turn);
  
  // returns a string which contains a comma delimetered list
  //  of the current values used by the bugs.
  public String getValues(); 
  
  public float[] getAPar();
  public float[] getRPar();
  public int getAThr();
  public int getRThr();
  
  public int getLife();
}

/* =======
 * CLASSES
 * ======= */
class Move {
  // A float between -5 and 5 of how much the bug will rotate
  float rotation;
  // A float between -5 and 5 stating how much the bug will accelerate
  float acceleration; 
  
  Move(float rot, float acc){
    if (rot > MAX_ROT) {
      this.rotation = MAX_ROT;
    } else if (rot < -MAX_ROT) {
      this.rotation = -MAX_ROT;
    } else {
      this.rotation = rot;
    }
    
    if (acc > MAX_ACC) {
      this.acceleration = MAX_ACC;
    } else if (acc < -MAX_ACC) {
      this.acceleration = -MAX_ACC;
    } else {
      this.acceleration = acc;
    }
  }
}

// The packet is the information sent to each bug for it to use
//  to decide how to move
class Packet {
  // an integer counting the number of seconds the bug has till it starves
  int life;
  // An array list of 12 sensor objects, representing the 12 "Hours" of 15 degrees
  //  surrounding the object
  ArrayList<Sensor> sensors;
  
  Packet (int lifeLeft, ArrayList<Sensor> sensors){
    this.life = lifeLeft;
    if ( sensors.size() == 12 ) {
      this.sensors = sensors;
    } else {
      throw new RuntimeException("ArrayList of Sensors must have exactly 12 sensors");
    }
  }
  
  public int getLife() {
    return life;
  }

  public Sensor getSensor(int index){
    return this.sensors.get(index);
  }
}

class Sensor {
  Boolean food;
  Boolean bug;
  Boolean mate;
  Boolean fire;
  
  Sensor(Boolean food, Boolean bug, Boolean mate, Boolean fire){
    this.food = food;
    this.bug = bug;
    this.mate = mate;
    this.fire = fire;
  }
  
  Sensor(){
    this.food = false;
    this.bug = false;
    this.mate = false;
    this.fire = false;
  }
}


// The world bug class represents the bugs position in the world
class WorldBug {
  Bug bug;    // the bug object itself
  
  int life;   // the amout of time the bug has left to live
  
  
  float x;      // the x coordinate of the bug
  float y;      // the y coordinate of the bug
  float rot;    // the rotation of the bug
  private float vel;    // the velocity of the bug
  
  private int timesFed;     // the number of times the bug has eaten
  int totalLife;    // the total time the bug has been alive
  private int timeSinceRep; // the time since the bug last reproduced (or was born)
  int familyLine; // time up to this bug the family has been alive
  
  Mating mate; // the mating object that defines this bug.
  
  PShape currentShape;
  
  Boolean alive;

  // initialisation of the bug with its coordinates on screen 
  WorldBug(Bug bug, int startX, int startY, int rotation, int familyLine){
    initializer(bug, startX, startY, rotation, familyLine);
  }
  
  // initialisation of the bug with random coordinates and rotation.
  WorldBug(Bug bug){
    initializer(bug, PApplet.parseInt(random(SPAWN_BORDER, SCREEN_WIDTH - SPAWN_BORDER)), PApplet.parseInt(random(SPAWN_BORDER, SCREEN_HEIGHT - SPAWN_BORDER)), PApplet.parseInt(random(DEG)), 0);
  }
  
  public void initializer(Bug bug, int startX, int startY, int rotation, int familyLine){
    this.bug = bug;
    this.x = startX;
    this.y = startY;
    this.rot = rotation;
    
    this.life = LIFE_BUGS;   
    
    this.familyLine = familyLine;
    
    this.vel = 0;
    this.timesFed = 0;
    this.totalLife = 0;
    this.timeSinceRep = 0;
    
    this.currentShape = bugShape;
    
    this.alive = true;
    
  }
  
  // moves the bug, and re-renders it on screen and
  //  then returns true if the bug is still alive, false if
  //  it died.
  public Boolean tick(ArrayList<Sensor> sensorResults){
    life--;
    totalLife++;
    timeSinceRep++;
    familyLine++;
    
    Move result = bug.tick(new Packet(life, sensorResults));
    
    // get the newest velocity.
    //  If it's outside the range, set it to max/min
    this.vel = this.vel + result.acceleration;
    if (this.vel > MAX_VEL) {
      this.vel = MAX_VEL;
    } else if (this.vel < MIN_VEL) {
      this.vel = MIN_VEL;
    }
    
    // update the rotation
    this.rot = ( this.rot + result.rotation )% DEG;
    
    // get the angle and direction we're heading in
    float theta = this.rot;
    int xSign = 1;
    int ySign = -1;
    
    if (theta >= 270) {
      theta = 360 - theta;
      xSign = -1;
    } else if (theta >= 180) {
      theta = theta - 180;
      ySign = 1;
      xSign = -1;
    } else if (theta >= 90) {
      theta = 180 - theta;
      ySign = 1;
    }
    
    // calculate the change in X and Y
    float deltaX = (sin(radians(theta)) * this.vel * xSign);
    float deltaY = (cos(radians(theta)) * this.vel * ySign);
    
    // add the change to the values
    this.x = this.x + deltaX;
    this.y = this.y + deltaY;
    
    // make sure that x and y are within the bounds of the world
    if (this.x < 0) {
      this.x = 0;
    } else if (this.x > SCREEN_WIDTH) {
      this.x = SCREEN_WIDTH;
    }
    if (this.y < 0) {
      this.y = 0;
    } else if (this.y > SCREEN_HEIGHT) {
      this.y = SCREEN_HEIGHT;
    }
    
    pushMatrix();
    translate(this.x, this.y);
    rotate(radians(this.rot));
    shape(currentShape, 0, 0);
    popMatrix();
    if (SHOW_LIFE) {
      textAlign(LEFT, TOP);
      text(floor(this.life/TICKS_PER_SEC), this.x+10, this.y+10); 
    }
    if (SHOW_REP) {
      textAlign(LEFT, BOTTOM);
      if (canReproduce()) {
        text("1", this.x+10, this.y - 10);
      } else if (mate != null) {
        text("2", this.x+10, this.y - 10);
      } else {
        if (SHOW_TREP && timeSinceRep < REPRODUCTION_CYCLE) {
          text("(" + ceil((REPRODUCTION_CYCLE - timeSinceRep)/TICKS_PER_SEC) + ")", this.x + 10, this.y - 10);
        } else {
          text("0", this.x+10, this.y - 10);
        }
      }
    }
    
    if (this.life > 0) {
      return ALIVE;
    } else {
      death();
      return DEAD; 
    }
  }
  
  // set's this bug's shape to the oldest
  public void oldest(){
    if (isAdult()){
      currentShape = oldestBugShape;
    }
  }
  
  // prints out the values regarding the bug
  public void death(){
    alive = false;
    println (bug.getValues() + ", " + totalLife);
  }
  
  // function called every time the bug eats. Adds a little bit of extra life.
  public void fed(){
    life +=  (( pow(FOOD_SCORE_EXP,PApplet.parseFloat( timesFed + FOOD_SCORE_DISP )) ) * FOOD_SCORE_MULT )+ FOOD_SCORE_ADD;
    timesFed++;
    currentShape = adultBugShape;
  }
  
  // function called whenever the bug reproduces. Resets timeSinceRep.
  public void reproduced() {
    timeSinceRep = 0;
  }
  
  // returns true if the bug is able to reproduce.
  public Boolean canReproduce() {
    return timesFed > 0 && timeSinceRep >= REPRODUCTION_CYCLE && mate == null;
  }
  
  public Boolean isAdult() {
    return timesFed > 0;
  }
  
  // returns the distance to the given point
  public float getDistance(float pX, float pY) {
    return sqrt(pow(pX-this.x, 2) + pow(pY-this.y, 2));
  }
  
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Evolutionary_Bugs_Program" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
