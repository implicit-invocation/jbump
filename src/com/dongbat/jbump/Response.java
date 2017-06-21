/*
 * Copyright 2017 tao.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dongbat.jbump;

import java.util.ArrayList;

/**
 *
 * @author tao
 */
public interface Response {

  public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result);

  public static class Result {

    public float goalX;
    public float goalY;
    public ArrayList<Collision> projectedCollisions;

    public void set(float goalX, float goalY, ArrayList<Collision> projectedCollisions) {
      this.goalX = goalX;
      this.goalY = goalY;
      this.projectedCollisions = projectedCollisions;
    }
  }
  
  public static final ArrayList<Collision> collisions = new ArrayList<Collision>();

  public static Response slide = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      Point tch = collision.touch;
      Point move = collision.move;
      float sx = tch.x, sy = tch.y;
      if (move.x != 0 || move.y != 0) {
        if (collision.normal.x == 0) {
          sx = goalX;
        } else {
          sy = goalY;
        }
      }
      
      x = tch.x;
      y = tch.y;
      goalX = sx;
      goalY = sy;
      
      world.project(collision.item, x, y, w, h, goalX, goalY, filter, collisions);
      result.set(goalX, goalY, collisions);
      return result;
    }
  };

  public static Response touch = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      collisions.clear();
      result.set(collision.touch.x, collision.touch.y, collisions);
      return result;
    }
  };

  public static Response cross = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      world.project(collision.item, x, y, w, h, goalX, goalY, filter, collisions);
      result.set(goalX, goalY, collisions);
      return result;
    }
  };
}
