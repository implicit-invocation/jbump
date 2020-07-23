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

import static java.lang.Math.*;

/**
 * grid_traverse* methods are based on "A Fast Voxel Traversal Algorithm for Ray Tracing",
 * by John Amanides and Andrew Woo - http://www.cse.yorku.ca/~amana/research/grid.pdf
 * It has been modified to include both cells when the ray "touches a grid corner",
 * and with a different exit condition
 * @author tao
 */
public class Grid {

  public static void grid_toWorld(float cellSize, float cx, float cy, Point point) {
    point.set((cx - 1) * cellSize, (cy - 1) * cellSize);
  }

  public static void grid_toCell(float cellSize, float x, float y, Point point) {
    point.set((float) floor(x / cellSize) + 1, (float) floor(y / cellSize) + 1);
  }

  public static int grid_traverse_initStep(float cellSize, float ct, float t1, float t2, Point point) {
    float v = t2 - t1;
    if (v > 0) {
      point.set(cellSize / v, ((ct + v) * cellSize - t1) / v);
      return 1;
    } else if (v < 0) {
      point.set(-cellSize / v, ((ct + v - 1) * cellSize - t1) / v);
      return -1;
    } else {
      point.set(Float.MAX_VALUE, Float.MAX_VALUE);
      return 0;
    }
  }

  public interface TraverseCallback {

    boolean onTraverse(float cx, float cy, int stepX, int stepY);
  }

  private final Point grid_traverse_c1 = new Point();
  private final Point grid_traverse_c2 = new Point();
  private final Point grid_traverse_initStepX = new Point();
  private final Point grid_traverse_initStepY = new Point();

  public void grid_traverse(float cellSize, float x1, float y1, float x2, float y2, TraverseCallback f) {
    grid_toCell(cellSize, x1, y1, grid_traverse_c1);
    float cx1 = grid_traverse_c1.x;
    float cy1 = grid_traverse_c1.y;
    grid_toCell(cellSize, x2, y2, grid_traverse_c2);
    float cx2 = grid_traverse_c2.x;
    float cy2 = grid_traverse_c2.y;
    int stepX = grid_traverse_initStep(cellSize, cx1, x1, x2, grid_traverse_initStepX);
    int stepY = grid_traverse_initStep(cellSize, cy1, y1, y2, grid_traverse_initStepY);
    float dx = grid_traverse_initStepX.x;
    float tx = grid_traverse_initStepX.y;
    float dy = grid_traverse_initStepY.x;
    float ty = grid_traverse_initStepY.y;
    float cx = cx1, cy = cy1;

    f.onTraverse(cx, cy, stepX, stepY);

    /*The default implementation had an infinite loop problem when
    approaching the last cell in some occasions. We finish iterating
    when we are *next* to the last cell*/
    boolean cont = true;
    while (abs(cx - cx2) + abs(cy - cy2) > 1 && cont) {
      if (tx < ty) {
        tx = tx + dx;
        cx = cx + stepX;
        cont = f.onTraverse(cx, cy, stepX, stepY);
      } else {
        //Addition: include both cells when going through corners
        if (tx == ty) {
          f.onTraverse(cx + stepX, cy, stepX, stepY);
        }
        ty = ty + dy;
        cy = cy + stepY;
        cont = f.onTraverse(cx, cy, stepX, stepY);
      }
    }

    //If we have not arrived to the last cell, use it
    if (cx != cx2 || cy != cy2) {
      f.onTraverse(cx2, cy2, stepX, stepY);
    }
  }
  
  public void grid_traverseRay(float cellSize, float x1, float y1, float dirX, float dirY, TraverseCallback f) {
    grid_toCell(cellSize, x1, y1, grid_traverse_c1);
    float cx1 = grid_traverse_c1.x;
    float cy1 = grid_traverse_c1.y;
    int stepX = grid_traverse_initStep(cellSize, cx1, x1, x1 + dirX, grid_traverse_initStepX);
    int stepY = grid_traverse_initStep(cellSize, cy1, y1, y1 + dirY, grid_traverse_initStepY);
    float dx = grid_traverse_initStepX.x;
    float tx = grid_traverse_initStepX.y;
    float dy = grid_traverse_initStepY.x;
    float ty = grid_traverse_initStepY.y;
    float cx = cx1, cy = cy1;
    
    f.onTraverse(cx, cy, stepX, stepY);
    
    boolean cont = true;
    while (cont) {
      if (tx < ty) {
        cx = cx + stepX;
        cont = f.onTraverse(cx, cy, stepX, stepY);
        tx = tx + dx;
      } else {
        //Addition: include both cells when going through corners
        if (tx == ty) {
          f.onTraverse(cx + stepX, cy, stepX, stepY);
        }
        cy = cy + stepY;
        cont = f.onTraverse(cx, cy, stepX, stepY);
        ty = ty + dy;
      }
    }
  }

  private final Point grid_toCellRect_cxy = new Point();

  public Rect grid_toCellRect(float cellSize, float x, float y, float w, float h, Rect rect) {
    grid_toCell(cellSize, x, y, grid_toCellRect_cxy);
    float cx = grid_toCellRect_cxy.x;
    float cy = grid_toCellRect_cxy.y;

    float cr = (float) ceil((x + w) / cellSize);
    float cb = (float) ceil((y + h) / cellSize);
    
    rect.set(cx, cy, cr - cx + 1, cb - cy + 1);
    return rect;
  }
}
