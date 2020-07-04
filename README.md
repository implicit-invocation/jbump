# jbump

[![](https://jitpack.io/v/implicit-invocation/jbump.svg)](https://jitpack.io/#implicit-invocation/jbump)  
jbump is Java port for bump.lua, a 2D AABB collision detection and response library.

## Features
- Can be used for Android, iOS (robovm) and normal Java applications
- Multiple instances can run on different threads (for server side simulation)
- Safe to reposition `Item` (must use `world.update`)

![Tile](images/tile.gif?raw=true "tile")

## Installation

You can download jar file from https://jitpack.io/com/github/implicit-invocation/jbump/17737e7/jbump-17737e7.jar

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

```Java
World<Entity> world = new World<Entity>();
Item<Entity> item = world.add(new Item<Entity>(entity), x, y, w, h);
for(Entity obstacle: obstacles) {
  world.add(new Item<Entity>(obstacle), obstacle.x, obstacle.y, obstacle.w, obstacle.h);
}
```

If you want to move an `Item`, call `world.move()`:

```Java
world.move(item, newX, newY, CollisionFilter.defaultFilter);
```

World is in `tileMode` by default. jbump will do additional sorting logic to avoid `Item` getting stuck between tiles.
You can disable `tileMode` if you are not using tiles for walls to save some circles.

You can write a custom `CollisionFilter`:

```Java
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
world.move(bulletItem, newX, newY, unitCollisionFilter);
```

![Bullet](images/shoot.gif?raw=true "bullet")

Update `Item` position and size:
```Java
world.update(item, newX, newY, newWidth, newHeight);
world.update(item, newX, newY); // not resize
```

Available Response: `slide`, `cross` and `touch`.

Get collided items:

```Java
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Array<Item> touched = new Array<Item>();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  touched.add(col.other);
}
```

Store collisions:

```
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Collisions collisions = new Collisions();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  collisions.add(col);
}
```
