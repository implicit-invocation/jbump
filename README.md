# jbump

[![](https://jitpack.io/v/implicit-invocation/jbump.svg)](https://jitpack.io/#implicit-invocation/jbump)  
jbump is Java port for bump.lua, a 2D AABB collision detection and response library.  
Please see the [bump.lua README](https://github.com/kikito/bump.lua/blob/master/README.md) for the original
documentation.

## Features
- Simple, fast, and accurate collisions in a lightweight package
- Avoids tunneling with fast AABB calculation 
- User has complete control over the movement and physics of entities
- Can be used for Android, iOS (robovm), and desktop Java applications
- Multiple instances can run on different threads (for server side simulation)
- Entities can be repositioned and resized during the simulation without errors

![Tile](images/tile.gif?raw=true "tile")

### Use jbump for...
* Tile based games
* Games that entities can be mostly represented by axis-aligned rectangles
* Top-Down Adventures, Shoot 'Em Ups, Tournament Fighters, and Platfromers

### Do not use jbump for...
* Games that require polygon collision detection
* Realistic physics simulations and multiple fast moving objects colliding against each other
* Simulations where the order in which the collisions are resolved isn't known.

## Installation

You can download the jar file from https://jitpack.io/com/github/implicit-invocation/jbump/17737e7/jbump-17737e7.jar

Using Gradle

 ```gradle
 repositories { 
      jcenter()
      maven { url "https://jitpack.io" }
 }
 dependencies {
       compile 'com.github.implicit-invocation:jbump:master-SNAPSHOT'
 }
 ```  

Using Maven

```maven
  <repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
  </repositories>
 ...
  <dependency>
      <groupId>com.github.implicit-invocation</groupId>
      <artifactId>jbump</artifactId>
      <version>master-SNAPSHOT</version>
  </dependency>

```

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

World is in `tileMode` by default. jbump will do additional sorting logic to avoid `Item` getting stuck between tiles.
You can disable `tileMode` if you are not using tiles to increase performance under certain circumstances.