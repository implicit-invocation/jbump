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

/**
 *
 * @author tao
 */
public interface Response {

  public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result);

  public static class Result {

    public float goalX;
    public float goalY;
    public Collisions projectedCollisions = new Collisions();

    public void set(float goalX, float goalY) {
      this.goalX = goalX;
      this.goalY = goalY;
    }
  }

  public Response slide = new Response() {
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
      result.projectedCollisions.clear();
      world.project(collision.item, x, y, w, h, goalX, goalY, filter, result.projectedCollisions);
      result.set(goalX, goalY);
      return result;
    }
  };

  public Response touch = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      result.projectedCollisions.clear();
      result.set(collision.touch.x, collision.touch.y);
      return result;
    }
  };

  public Response cross = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      result.projectedCollisions.clear();
      world.project(collision.item, x, y, w, h, goalX, goalY, filter, result.projectedCollisions);
      result.set(goalX, goalY);
      return result;
    }
  };

  public Response bounce = new Response() {
    @Override
    public Result response(World world, Collision collision, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Result result) {
      Point tch = collision.touch;
      Point move = collision.move;
      float bx = tch.x, by = tch.y;
      if (move.x != 0 || move.y != 0) {
        float bnx = goalX - tch.x;
        float bny = goalY - tch.y;
        if (collision.normal.x == 0) {
          bny = -bny;
        } else {
          bnx = -bnx;
        }
        bx = tch.x + bnx;
        by = tch.y + bny;
      }

      x = tch.x;
      y = tch.y;
      goalX = bx;
      goalY = by;
      result.projectedCollisions.clear();
      world.project(collision.item, x, y, w, h, goalX, goalY, filter, result.projectedCollisions);
      result.set(goalX, goalY);
      return result;
    }
  };
}
