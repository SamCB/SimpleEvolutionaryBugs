/* ==========
 * INTERFACES
 * ========== */
abstract class Master {
  Bug championBug;
  
  // Function, that given two "Parents" will return an "offspring"
  abstract Bug reproduce(Bug bug1, Bug bug2);
  
  // Function, that will return a new random bug
  abstract Bug init();
  
  // Function, that will return an offspring of the championBug mated with itself (slight mutation)
  //  if the championBug doesn't exist, will return just the init.
  final Bug initChampion(){
    if (championBug == null) {
      return init();
    } else {
      return reproduce(championBug, championBug);
    }
  }
  
  // Function informing master that given bug has died
  final void died(Bug deadBug){
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
  Move tick(Packet turn);
  
  // returns a string which contains a comma delimetered list
  //  of the current values used by the bugs.
  String getValues(); 
  
  float[] getAPar();
  float[] getRPar();
  int getAThr();
  int getRThr();
  
  int getLife();
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
  
  int getLife() {
    return life;
  }

  Sensor getSensor(int index){
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


