# jbump

[![](https://jitpack.io/v/implicit-invocation/jbump.svg)](https://jitpack.io/#implicit-invocation/jbump)  
Java port for bump.lua

## Installation

 ```gradle
 repositories { 
      jcenter()
      maven { url "https://jitpack.io" }
 }
 dependencies {
       compile 'com.github.implicit-invocation:jbump:master-SNAPSHOT'
 }
 ```  

## Usage

jbump is a 2D AABB collision detection and response library.

```Java
World<Entity> world = new World<Entity>();
Item<Entity> item = world.add(new Item<Entity>(entity), x, y, w, h);
for(Entity obstacle: obstacles) {
  world.add(new Item<Entity>(obstacle), obstacle.x, obstacle.y, obstacle.w, obstacle.h);
}
...
world.move(item, newX, newY, CollisionFilter.defaultFilter);
```

You can write custom `CollisionFilter`

```Java
CollisionFilter unitCollisionFilter = new CollisionFilter() {
  @Override
  public Response filter(Item item, Item item1) {
    if (item1.userData.isWall()) {
      return Response.slide;
    } else {
      return Response.cross;
    }
  }
};
...
world.move(item, newX, newY, unitCollisionFilter);
```

Get collided items
```
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Array<Item> touched = new Array<Item>();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  touched.add(col.other);
}
```

Store collisions
```
Result result = world.move(item, newX, newY, unitCollisionFilter);
Collisions projectedCollisions = result.projectedCollisions;
Collisions collisions = new Collisions();
for (int i = 0; i < projectedCollisions.size(); i++) {
  Collision col = projectedCollisions.get(i);
  collisions.add(col);
}
```

## TODO

- world querying
