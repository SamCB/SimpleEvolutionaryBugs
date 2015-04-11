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
    this.x = int(random(SPAWN_BORDER, SCREEN_WIDTH - SPAWN_BORDER));
    this.y = int(random(SPAWN_BORDER, SCREEN_HEIGHT - SPAWN_BORDER));
    this.time = time;
    this.entShape = s;
  }
  
  // returns true if the entity remains alive false if it dies
  //  decreases time remaining by 1 each time, and displays the shape
  Boolean tick(){
    this.time--;
    if (this.time > 0) {
      shape(this.entShape, x, y);
      return true;
    } else {
      return false;
    }
  }
}
