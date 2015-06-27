# SimpleEvolutionaryBugs
A simple project that I made a while ago over a few days, putting together a few things I learned from my AI Course at Uni.

## Installation
Executables (Must have Java 7 installed to run, not all versions have been tested):
* Mac: [Download](http://samwise.me/Bugs/application.macosx.zip)
* Windows: [Download 32-bit](http://samwise.me/Bugs/application.windows32.zip), [Download 64-bit](http://samwise.me/Bugs/application.windows64.zip)
* Linux: [Download 32-bit](http://samwise.me/Bugs/application.linux32.zip), [Download 64-bit](http://samwise.me/Bugs/application.linux64.zip)

Or you can use the code and run through processing. Processing can be downloaded [here](http://processing.org).

The program uses the LGPL library gwoptics for displaying graph information. It can either be downloaded and imported from [here](http://www.gwoptics.org/processing/gwoptics_p5lib/) or you can install it directly by opening Processing, and going to: Sketch -> Import Library -> Add Library and searching for "gwoptics".

Once everything required is installed, make sure that all the sketch files you've taken from here are in a folder called `Evolutionary_Bugs_Program` open one of the sketch files and click the play button in the top lefthand corner.

## What happens?
Each bug (blue) can see the fires (red), food (green) and other adult bugs (darker blue) in a short distance around them them and have a basic grasp of how long they have left to live. They use these inputs to make two decisions every frame.
1. How far to rotate in which direction
2. How much to accelerate/decelerate

This is calculation is performed every frame for every bug alive.

When a bug eats food, the amount of time they have left to live increases (displayed as the number in the bottom right corner of each bug), though the older they are, the less eating the food has an affect on prolonging their life. When a bug eats food for the first time, they advance from a “Juvenile” state (light blue) to an adult. They die either when they touch a fire, or their life counter runs out. The oldest living bug is coloured an even darker blue. 

Juvenile bugs are made when the following conditions are met:
1. Two adult bugs are in a close proximity,
2. They both can reproduce, (signified by a 1 in the top right corner for each bug)
At this point a line is drawn between the two bugs. Then, if they remain in close proximity for a set length of time, a new juvenile bug will be created between them. No points for guessing what’s happening here. This juvenile will inherit some of the decision making ability from both the parents (slightly more from the older of the two parents), though each “weighting” of the decisions will be mutated. The “reproduction counter” for all three of those bugs is reset, they must wait some time before being able to reproduce again.

If there are less than 3 bugs left alive, a new set of completely random juveniles will be created and any surviving bugs will be carried on.

## What to expect?
At first, expect very little. The bugs are totally random and there is little prompting them into any reasonable, logical behaviour. They'll jitter, run and turn for no good reason and are more likely to run into a fire than into food. On the off chance any do run into food, they're unlikely to be able to find a mate. It does take some time for the bugs to get beyond even two generations. However after this occurs, they start learning, moving towards food, avoiding fire, sticking in groups to increase the chances of finding a mate, and maintaining movement to find more food.

## How do the bugs work?
Each bug has 12 "Sensors" pointed in a circle around their reference point (the centre of their back, that is why when the collide with fire or food, the collision doesn't occur until their back side is in the circle). If a sensor detects fire, food another bug, or the bug it's mating with, that value is flagged. Each of these factors for each of these sensors (as well as the amount of life the bug has left) are given as arguments into 2 seperate linear combinations (where each argument is multiplied by an individual weight and then summed) which determine the bug's acceleration or rotation. I.e:

```java
acceleration = aw[1]*a[1] + aw[2]*a[2] + ... + aw[n]*a[n];
rotation = rw[1]*a[1] + rw[2]*a[2] + ... + rw[n]*a[n];
```

This calculation occurs once for every bug, every "tick".

When two bugs mate (if they stay in close enough proximitary for enough time) a juvenile bug will be produced, based off the genetics of both of its parents. The way this works, half the weights for each linear combination are taken from each bug and combined to form the new bug. Each of these weights are then multiplied by a small random number to "mutate" the results.

## Where to now?
While I was developing the simulator I did have ideas of where the project could go, however I never really continued implementing them. Now however, I am working on a similar project, except on a much larger scale. Of course, that means it's unlikely this code will be changed again (I may make an attempt at re-commenting it, but no guarantees). If you want to follow progress on my other project, you can check out my [blog](http://samcb.com/).

## FAQ?
Unsurprisingly, none.

## Not so FAQ?
<dl>
  <dt>What can I do with the code?</dt>
  <dd>
    Ehhh, I don't really care. Do whatever you like as long as you don't somehow figure out how to do something un-ethical with it. I probably won't accept any pull requests, but if you do make something or use this somehow, I'd love to hear about it and mention it here. And of course, any credit would be really appreciated.
  </dd>
</dl>
-----

<span>-</span> **Sam Collis-Bird**
