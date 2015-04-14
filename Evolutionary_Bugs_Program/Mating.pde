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
  Boolean tick(WorldBug bug){
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
  Boolean inPair(WorldBug bug){
    if (bug1 == bug || bug2 == bug) {
      return true;
    } else {
      return false;
    }
  }
}
