import java.util.Iterator;
import org.gwoptics.graphics.graph2D.Graph2D;
import org.gwoptics.graphics.graph2D.traces.ILine2DEquation;
import org.gwoptics.graphics.graph2D.traces.RollingLine2DTrace;

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
void setup(){
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

void draw(){
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
    if (int(random(SPAWN_CHANCE_FOOD)) == 1) {
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
    if (int(random(SPAWN_CHANCE_FIRE)) == 1) {
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
      bugs.add(new WorldBug(bugMaster.reproduce(mate.bug1.bug, mate.bug2.bug), int((mate.bug1.x + mate.bug2.x)/2), int((mate.bug1.y + mate.bug2.y)/2), int(random(DEG)), max(mate.bug1.familyLine, mate.bug2.familyLine)));
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
PShape createBugShape(int type){
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

PShape createFireShape(){
  PShape s;
  s = createShape(ELLIPSE, 0, 0, FIRE_S, FIRE_S);
  s.setFill(color(225, 0, 0));
  s.setStroke(false);
  return s;
}

PShape createFoodShape(){
  PShape s;
  s = createShape(ELLIPSE, 0, 0, FOOD_S, FOOD_S);
  s.setFill(color(0, 225, 0));
  s.setStroke(false);
  return s;
}

/* =====================
 * GEOMETRICAL FUNCTIONS
 * ===================== */
int sensorDir(float bX, float bY, float pX, float pY, float rot){
    return (ceil((degrees(atan2(bX - pX, pY - bY))+(-(rot-75)))/(DEG/12))+15) % 12;
}

/* ========================
 * BUG MANAGEMENT FUNCTIONS
 * ======================== */
void killAll(){
  WorldBug bug;
  for (Iterator<WorldBug> bugIt = bugs.iterator(); bugIt.hasNext(); ) {
    bug = bugIt.next();
    bug.death();
    
    bugIt.remove();
  }
}
