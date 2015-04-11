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
public final float FOOD_SCORE_EXP = 0.78;
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
public final float ROT_MUTATION = 0.02;         // The maximum amount to mutate the rotation of each ofspring by
public final float ACC_MUTATION = 0.001;        // The maximum amount to mutate the acceleration of each ofspring by
public final float THR_MUTATION = 0.1;           // The maximum amount to mutate the threshold of each ofspring by

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
public final float GRAPH_X_SPEED = 0.01;
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
