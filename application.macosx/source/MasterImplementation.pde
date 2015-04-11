class MasterImplementation1 extends Master {
   
  Bug reproduce(Bug bug1, Bug bug2){
    
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
      newALThreshold = bug1.getAThr() + int(bug1.getAThr() * random(-THR_MUTATION, THR_MUTATION));
      newRLThreshold = bug1.getRThr() + int(bug1.getRThr() * random(-THR_MUTATION, THR_MUTATION));
    } else {
      newALThreshold = bug2.getAThr() + int(bug2.getAThr() * random(-THR_MUTATION, THR_MUTATION));
      newRLThreshold = bug2.getRThr() + int(bug2.getRThr() * random(-THR_MUTATION, THR_MUTATION));
    }
    
    return new bugImplementation(newAPar, newRPar, newALThreshold, newRLThreshold);
  }
  
  Bug init() {
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
    aLThreshold = int(random(START_THR_ACC));
    rLThreshold = int(random(START_THR_ROT));
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
  Move tick(Packet turn) {
    
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
  String getValues() {
    String values;
    // the values string is laid out accordingly:
    // rLThreshold, aLThreshold, rot-s.food(0), rot-s.bug(0), rot-s.mate(0), rot-s.fire(0), acc-s.food(0), acc-s.bug(0), acc-s.mate(0), acc-s.fire(0)...
    values = rLThreshold + ", " + aLThreshold + ", " + rPar[NUM_PAR-2] + ", " + rPar[NUM_PAR-1] + ", " + aPar[NUM_PAR-2] + ", " + aPar[NUM_PAR-1] + ", ";
    for (int i = 0; i < (NUM_PAR-2); i+= 4){
      values = values + rPar[i] + ", " + rPar[i+1] + ", " + rPar[i+2] + ", " + rPar[i+3] + ", " + aPar[i] + ", " + aPar[i+1] + ", " + aPar[i+2] + ", " + aPar[i+3] + ", ";
    }
    return values;
  } 
  
  float[] getAPar() {
    return aPar;
  }
  
  float[] getRPar() {
    return rPar;
  }
  
  int getAThr() {
    return aLThreshold;
  }
  
  int getRThr() {
    return rLThreshold;
  }
  
  int getLife() {
    return life;
  }
}
