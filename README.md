# jbump

[![](https://jitpack.io/v/tommyettinger/jbump.svg)](https://jitpack.io/#tommyettinger/jbump)  
jbump is a Java port for bump.lua, a 2D AABB collision detection and response library.  
Please see the [bump.lua README](https://github.com/kikito/bump.lua/blob/master/README.md) for the original
documentation.

## Features
- Simple, fast, and accurate collisions in a lightweight package
- Avoids tunneling with fast AABB calculation 
- User has complete control over the movement and physics of entities
- Can be used for Android, iOS (robovm), HTML5 (GWT), and desktop Java applications
- Multiple instances can run on different threads (for server side simulation)
- Entities can be repositioned and resized during the simulation without errors

![Tile](images/tile.gif?raw=true "tile")

### Use jbump for...
* Tile based games
* Games that entities can be mostly represented by axis-aligned rectangles
* Top-Down Adventures, Shoot 'Em Ups, Tournament Fighters, and Platformers

### Do not use jbump for...
* Games that require polygon collision detection
* Realistic physics simulations and multiple fast moving objects colliding against each other
* Simulations where the order in which the collisions are resolved isn't known.

## Installation

You can directly [download the jar file from JitPack](https://jitpack.io/com/github/tommyettinger/jbump/659fea75c3/jbump-659fea75c3.jar),
or you can use a project management tool like Maven or Gradle.

Using Gradle

```gradle
// NOTE: this is not the "repositories" section inside "buildscript", so make sure to check where you're putting the repository!
allprojects {
  repositories {
    // ...
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
  // you may need to use "api" instead of "implementation" in a multi-module project, like most libGDX projects
  implementation 'com.github.tommyettinger:jbump:659fea75c3' // check JitPack for other versions if you want a newer one
}
```  

Using Maven

```xml
  <repositories>
    <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
    </repository>
  </repositories>
 ...
  <dependencies>
    <dependency>
      <groupId>com.github.tommyettinger</groupId>
      <artifactId>jbump</artifactId>
      <version>659fea75c3</version> <!-- check JitPack for other versions if you want a newer one -->
    </dependency>
  </dependencies>
```

HTML5(GWT) is supported by adding the sources dependency to the project:

```gradle
project(":html") {
  ...

  dependencies {
    ...
    // you may need to use "api" instead of "implementation" in a multi-module project, like most libGDX projects
    implementation 'com.github.tommyettinger:jbump:659fea75c3:sources'
  }
}
``` 

You must also add the inherits line to the GdxDefinition.gwt.xml file of your HTML project:

```xml
<module rename-to="html">
  ...
  <inherits name="com.dongbat.jbump" />
</module>
```

The latest commit version can be looked up [here, on JitPack's page for jbump](https://jitpack.io/#tommyettinger/jbump),
under the Commits tab. You can copy any of those 10-hex-digit identifiers for a commit and replace `659fea75c3` with your
commit of choice to change version. You can also just look at the green JitPack bar at the top of this README.md .

## Usage

You must create a `World` instance and add `Item` instances to it:

```java
World<Entity> world = new World<Entity>();
Item<Entity> item = world.add(new Item<Entity>(entity), x, y, w, h);
for(Entity obstacle: obstacles) {
  world.add(new Item<Entity>(obstacle), obstacle.x, obstacle.y, obstacle.w, obstacle.h);
}
```

To move an `Item` through the World, call `world.move()`:

```java
world.move(item, newX, newY, CollisionFilter.defaultFilter);
```

The above code will simulate movement of the item through the world and generate collisions. The item will be stopped 
if it collides with another item. To "teleport" an `Item` to a new position without collisions: 

```java
world.update(item, newX, newY);
```

To also update the size of the `Item`:

```java
world.update(item, newX, newY, newWidth, newHeight);
```

You must also remember to remove an entity's associated `Item` when you remove it from the game:

```java
world.remove(item)
```

To determine what may generate collisions and how they interact with other items, write a custom `CollisionFilter`:

```java
CollisionFilter bulletCollisionFilter = new CollisionFilter() {
  @Override
  public Response filter(Item item, Item other) {
    if (EntityUtil.isOwner(other.userData, item.userData)) {
      return Response.cross;
    } else {
      return Response.touch;
    }
  }
};
...
world.move(bulletItem, newX, newY, bulletCollisionFilter);
```

`CollisionFilter` may return `Response.slide`, `Response.cross`, `Response.bounce`, `Response.touch`, and `null`.

* A value of `null` indicates that `other` will not block movement and will not trigger a `Collision`. Use this for
entities that do not interact with each other.
* A value of `Response.touch` will stop all movement of `item` and trigger a `Collision`. Use this for entities like 
arrows that get stuck in the entities they hit.
* A value of `Response.slide` will trigger a `Collision` and stop movement in the direction that `item` hits `other`, but 
will allow it to slide across its surface. This is the typical interaction you would see in a platformer game and is the 
default `Response`.
* A value of `Response.bounce` will trigger a `Collision` and bounce `item` back against the side that it hits `other`.
This is typically used in games like Breakout where balls bounce against walls and tiles.
* A value of `Response.cross` will trigger a `Collision` but will not stop `item` from intersecting `other` and passing 
through it. This is useful for penetrating bullets and area triggers that are turned on when a player passes through
them.

![Bullet](images/shoot.gif?raw=true "bullet")

Get collided items:

```java
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Array<Item> touched = new Array<Item>();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  touched.add(col.other);
}
```

Store collisions:

```java
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Collisions collisions = new Collisions();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  collisions.add(col);
}
```

Each `Collision` reports various data on the contact between the items:
* item = the item being moved / checked.
* other = an item colliding with the item being moved.
* type = the result of `filter(other)`. It's usually "touch", "cross", "slide", or "bounce".
* overlaps = boolean. True if item "was overlapping" other when the collision started.  
  False if it didn't but "tunneled" through other.
* ti = float between 0 and 1. How far along the movement to the goal did the collision occur.
* move = The difference between the original coordinates and the actual ones in x and y values.
* normal = The collision normal indicating the side the item hit other; integer -1, 0 or 1 in `x` and `y` 
  Useful in detecting if the player hit the ground or is pushing against the side of a wall.
* touch = The coordinates where item started touching other
* itemRect = The rectangle item occupied when the touch happened
* otherRect = The rectangle other occupied when the touch happened

## Advanced Settings

World is in `tileMode` by default. jbump will do additional sorting logic to avoid `Item` getting stuck between tiles.
You can disable `tileMode` if you are not using tiles to increase performance under certain circumstances.

Otherwise, you can fine tune the `cellSize` of each cell used internally: the world is broken up into a grid, which
holds the different objects in cells to reduce the number of collision checks necessary every frame. Use the 
following World constructor:

```java
World<Entity> world = new World<Entity>(32f);
```

`cellSize` represents the size of the sides of the squared cells that will be used internally to provide the data.
This value defaults to 64f, which is fine for most use. However, it should be set to a multiple of your tile size in 
world units for tile-based games. For example, if you're using pixel units and your tiles are 32x32 pixels, cellSize 
could be 32f, 64f, 128f, etc. If you're using meters and your tiles are 1x1 meters, cellSize could be 1f, 2f, 4f. Set 
this value lower/higher to tweak performance. Note that you do not have to have a Tile based game to use jbump, but the
cell rules still apply to the world.

## Querying the World

Instead of checking for collisions with direct bounding box to bounding box tests, you can also use `World` query methods. 
Arbitrarily positioned point, rectangle, segment, and ray queries are available:
* Point: `world.queryPoint(x, y, filter, items);`
* Rectangle: `world.queryRect(x, y, width, height, filter, items);`
* Segment: `world.querySegment(x1, y1, x2, y2, filter, items);`
* Ray: `world.queryRay(originX, originY, dirX, dirY, filter, items);`

`items` is an empty list that will be filled with all the items that collide with the given shape. `filter` is the 
`CollisionFilter` that defines what items would be tested. Use `CollisionFilter.defaultFilter` to test all items. If 
you create a custom filter, return null to not accept an item. To accept, return any kind of `Response`. `item` is the 
item that would be tested. `other` is always null in this case.

More detailed segment and ray queries are available:
* Segment with Coords: `world.querySegmentWithCoords(x1, y1, x2, y2, filter, infos);`
* Ray with Coords: `world.querySegmentWithCoords(originX, originY, dirX, dirY, filter, infos);`

`infos` is a more detailed list of collision information. The (x1, y1) coordinates of `ItemInfo` define where the 
segment/ray intersects the `Item`. This is helpful for drawing particle effects where a bullet enters a body, for example. 
The (x2, y2) coordinates define where the segment/ray exits the item, which is great for drawing an exit wound. (ti1, ti2) 
are values between 0 and 1 that define how far from the starting point the impact happened. This can be used to 
describe an effect that weakens with distance.

## jbump Overview Tutorial

Watch the following video on YouTube that summarizes the use of jbump in the context of a platformer game:
[Jbump AABB Collision Detection and Physics](https://youtu.be/IeU06Vzz2hA)

Also, review these two example games to learn how jbump can be implemented in platformers and shooters:
[jbumpexample by raeleus](https://github.com/raeleus/jbumpexample)

The test class demonstrates the use the query methods, among other important examples:
[TestBump.java](https://github.com/tommyettinger/jbump/blob/master/test/src/com/dongbat/jbump/test/TestBump.java)
