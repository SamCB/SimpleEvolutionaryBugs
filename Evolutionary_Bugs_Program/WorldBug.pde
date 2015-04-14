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
    initializer(bug, int(random(SPAWN_BORDER, SCREEN_WIDTH - SPAWN_BORDER)), int(random(SPAWN_BORDER, SCREEN_HEIGHT - SPAWN_BORDER)), int(random(DEG)), 0);
  }
  
  void initializer(Bug bug, int startX, int startY, int rotation, int familyLine){
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
  Boolean tick(ArrayList<Sensor> sensorResults){
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
  void oldest(){
    if (isAdult()){
      currentShape = oldestBugShape;
    }
  }
  
  // prints out the values regarding the bug
  void death(){
    alive = false;
    println (bug.getValues() + ", " + totalLife);
  }
  
  // function called every time the bug eats. Adds a little bit of extra life.
  void fed(){
    life +=  (( pow(FOOD_SCORE_EXP,float( timesFed + FOOD_SCORE_DISP )) ) * FOOD_SCORE_MULT )+ FOOD_SCORE_ADD;
    timesFed++;
    currentShape = adultBugShape;
  }
  
  // function called whenever the bug reproduces. Resets timeSinceRep.
  void reproduced() {
    timeSinceRep = 0;
  }
  
  // returns true if the bug is able to reproduce.
  Boolean canReproduce() {
    return timesFed > 0 && timeSinceRep >= REPRODUCTION_CYCLE && mate == null;
  }
  
  Boolean isAdult() {
    return timesFed > 0;
  }
  
  // returns the distance to the given point
  float getDistance(float pX, float pY) {
    return sqrt(pow(pX-this.x, 2) + pow(pY-this.y, 2));
  }
  
}
