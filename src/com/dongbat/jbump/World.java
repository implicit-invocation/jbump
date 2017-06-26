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

import static com.dongbat.jbump.Grid.*;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author tao
 */
public class World<E> {

  private HashMap<Float, HashMap<Float, Cell>> rows = new HashMap<Float, HashMap<Float, Cell>>();
  private HashMap<Cell, Boolean> nonEmptyCells = new HashMap<Cell, Boolean>();
  private Grid grid = new Grid();
  private RectHelper rectHelper = new RectHelper();
  private boolean tileMode = true;

  public void setTileMode(boolean tileMode) {
    this.tileMode = tileMode;
  }

  public boolean isTileMode() {
    return tileMode;
  }

  private void addItemToCell(Item<E> item, float cx, float cy) {
    if (!rows.containsKey(cy)) {
      rows.put(cy, new HashMap<Float, Cell>());
    }
    HashMap<Float, Cell> row = rows.get(cy);
    if (!row.containsKey(cx)) {
      row.put(cx, new Cell());
    }
    Cell cell = row.get(cx);

    nonEmptyCells.put(cell, true);
    if (!cell.items.containsKey(item)) {
      cell.items.put(item, true);
      cell.itemCount = cell.itemCount + 1;
    }
  }

  private boolean removeItemFromCell(Item item, float cx, float cy) {
    if (!rows.containsKey(cy)) {
      return false;
    }
    HashMap<Float, Cell> row = rows.get(cy);
    if (!row.containsKey(cx)) {
      return false;
    }
    Cell cell = row.get(cx);
    if (!cell.items.containsKey(item)) {
      return false;
    }
    cell.items.remove(item);
    cell.itemCount = cell.itemCount - 1;
    if (cell.itemCount == 0) {
      nonEmptyCells.remove(cell);
    }
    return true;
  }

  private HashMap<Item, Boolean> getDictItemsInCellRect(float cl, float ct, float cw, float ch, HashMap<Item, Boolean> result) {
    result.clear();
    for (float cy = ct; cy < ct + ch; cy++) {
      if (rows.containsKey(cy)) {
        HashMap<Float, Cell> row = rows.get(cy);
        for (float cx = cl; cx < cl + cw; cx++) {
          if (row.containsKey(cx)) {
            Cell cell = row.get(cx);
            if (cell.itemCount > 0) {
              for (Item item : cell.items.keySet()) {
                result.put(item, true);
              }
            }
          }
        }
      }
    }
    return result;
  }

  private float cellSize = 64;

  private final ArrayList<Cell> getCellsTouchedBySegment_visited = new ArrayList<Cell>();

  private ArrayList<Cell> getCellsTouchedBySegment(float x1, float y1, float x2, float y2, final ArrayList<Cell> result) {
    result.clear();
    getCellsTouchedBySegment_visited.clear();
    // use set
    final ArrayList<Cell> visited = getCellsTouchedBySegment_visited;
    grid.grid_traverse(cellSize, x1, y1, x2, y2, new TraverseCallback() {
      @Override
      public void onTraverse(float cx, float cy) {
        if (!rows.containsKey(cy)) {
          return;
        }
        HashMap<Float, Cell> row = rows.get(cy);
        if (!row.containsKey(cx)) {
          return;
        }
        Cell cell = row.get(cx);
        if (visited.contains(cell)) {
          return;
        }
        visited.add(cell);
        result.add(cell);
      }
    });

    return result;
  }

  public Collisions project(Item item, float x, float y, float w, float h, float goalX, float goalY, Collisions collisions) {
    return project(item, x, y, w, h, goalX, goalY, CollisionFilter.defaultFilter, collisions);
  }

  private final ArrayList<Item> project_visited = new ArrayList<Item>();
  private final Rect project_c = new Rect();
  private final HashMap<Item, Boolean> project_dictItemsInCellRect = new HashMap<Item, Boolean>();

  public Collisions project(Item item, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Collisions collisions) {
    collisions.clear();
    ArrayList<Item> visited = project_visited;
    visited.clear();
    if (item != null) {
      visited.add(item);
    }
    float tl = min(goalX, x);
    float tt = min(goalY, y);
    float tr = max(goalX + w, x + w);
    float tb = max(goalY + h, y + h);

    float tw = tr - tl;
    float th = tb - tt;

    grid.grid_toCellRect(cellSize, tl, tt, tw, th, project_c);
    float cl = project_c.x, ct = project_c.y, cw = project_c.w, ch = project_c.h;
    HashMap<Item, Boolean> dictItemsInCellRect = getDictItemsInCellRect(cl, ct, cw, ch, project_dictItemsInCellRect);
    for (Item other : dictItemsInCellRect.keySet()) {
      if (!visited.contains(other)) {
        visited.add(other);
        Response response = filter.filter(item, other);
        if (response != null) {
          Rect o = getRect(other);
          float ox = o.x, oy = o.y, ow = o.w, oh = o.h;
          Collision col = rectHelper.rect_detectCollision(x, y, w, h, ox, oy, ow, oh, goalX, goalY);

          if (col != null) {
            collisions.add(col.overlaps, col.ti, col.move.x, col.move.y, col.normal.x, col.normal.y, col.touch.x, col.touch.y, col.itemRect.x, col.itemRect.y, col.itemRect.w, col.itemRect.h, col.otherRect.x, col.otherRect.y, col.otherRect.w, col.otherRect.h, item, other, response);
          }
        }
      }
    }
    if (tileMode) {
      collisions.sort();
    }
    return collisions;
  }

  private HashMap<Item, Rect> rects = new HashMap<Item, Rect>();

  public Rect getRect(Item item) {
    return rects.get(item);
  }

  public int countCells() {
    int count = 0;
    for (HashMap<Float, Cell> row : rows.values()) {
      for (Float x : row.keySet()) {
        count++;
      }
    }
    return count;
  }

  public boolean hasItem(Item item) {
    return rects.containsKey(item);
  }

  public int countItems() {
    return rects.keySet().size();
  }

  public Point toWorld(float cx, float cy, Point result) {
    grid_toWorld(cellSize, cx, cy, result);
    return result;
  }

  public Point toCell(float x, float y, Point result) {
    grid_toCell(cellSize, x, y, result);
    return result;
  }

  private final Rect add_c = new Rect();

  public Item<E> add(Item<E> item, float x, float y, float w, float h) {
    if (rects.containsKey(item)) {
      return item;
    }
    rects.put(item, new Rect(x, y, w, h));
    grid.grid_toCellRect(cellSize, x, y, w, h, add_c);
    float cl = add_c.x, ct = add_c.y, cw = add_c.w, ch = add_c.h;
    for (float cy = ct; cy < ct + ch; cy++) {
      for (float cx = cl; cx < cl + cw; cx++) {
        addItemToCell(item, cx, cy);
      }
    }
    return item;
  }

  private final Rect remove_c = new Rect();

  public void remove(Item item) {
    Rect rect = getRect(item);
    float x = rect.x, y = rect.y, w = rect.w, h = rect.h;

    rects.remove(item);
    grid.grid_toCellRect(cellSize, x, y, w, h, remove_c);
    float cl = remove_c.x, ct = remove_c.y, cw = remove_c.w, ch = remove_c.h;

    for (float cy = ct; cy < ct + ch; cy++) {
      for (float cx = cl; cx < cl + cw; cx++) {
        removeItemFromCell(item, cx, cy);
      }
    }
  }

  public void update(Item item, float x2, float y2) {
    Rect rect = getRect(item);
    float x = rect.x, y = rect.y, w = rect.w, h = rect.h;
    update(item, x2, y2, w, h);
  }

  private final Rect update_c1 = new Rect();
  private final Rect update_c2 = new Rect();

  public void update(Item item, float x2, float y2, float w2, float h2) {
    Rect rect = getRect(item);
    float x1 = rect.x, y1 = rect.y, w1 = rect.w, h1 = rect.h;
    if (x1 != x2 || y1 != y2 || w1 != w2 || h1 != h2) {

      Rect c1 = grid.grid_toCellRect(cellSize, x1, y1, w1, h1, update_c1);
      Rect c2 = grid.grid_toCellRect(cellSize, x2, y2, w2, h2, update_c2);

      float cl1 = c1.x, ct1 = c1.y, cw1 = c1.w, ch1 = c1.h;
      float cl2 = c2.x, ct2 = c2.y, cw2 = c2.w, ch2 = c2.h;

      if (cl1 != cl2 || ct1 != ct2 || cw1 != cw2 || ch1 != ch2) {
        float cr1 = cl1 + cw1 - 1, cb1 = ct1 + ch1 - 1;
        float cr2 = cl2 + cw2 - 1, cb2 = ct2 + ch2 - 1;
        boolean cyOut;

        for (float cy = ct1; cy <= cb1; cy++) {
          cyOut = cy < ct2 || cy > cb2;
          for (float cx = cl1; cx <= cr1; cx++) {
            if (cyOut || cx < cl2 || cx > cr2) {
              removeItemFromCell(item, cx, cy);
            }
          }
        }

        for (float cy = ct2; cy <= cb2; cy++) {
          cyOut = cy < ct1 || cy > cb1;
          for (float cx = cl2; cx <= cr2; cx++) {
            if (cyOut || cx < cl1 || cy > cr1) {
              addItemToCell(item, cx, cy);
            }
          }
        }
      }
      rect.set(x2, y2, w2, h2);
    }
  }

  private final ArrayList<Item> check_visited = new ArrayList<Item>();
  private final Collisions check_cols = new Collisions();
  private final Collisions check_projectedCols = new Collisions();
  private final Response.Result check_result = new Response.Result();

  public Response.Result check(Item item, float goalX, float goalY, final CollisionFilter filter) {
    final ArrayList<Item> visited = check_visited;
    visited.clear();
    visited.add(item);

    CollisionFilter visitedFilter = new CollisionFilter() {
      @Override
      public Response filter(Item item, Item other) {
        if (visited.contains(other)) {
          return null;
        }
        if (filter == null) {
          return defaultFilter.filter(item, other);
        }
        return filter.filter(item, other);
      }
    };

    Rect rect = getRect(item);
    float x = rect.x, y = rect.y, w = rect.w, h = rect.h;
    Collisions cols = check_cols;
    cols.clear();
    Collisions projectedCols = project(item, x, y, w, h, goalX, goalY, filter, check_projectedCols);
    Response.Result result = check_result;
    while (projectedCols != null && !projectedCols.isEmpty()) {
      Collision col = projectedCols.get(0);
      cols.add(col.overlaps, col.ti, col.move.x, col.move.y, col.normal.x, col.normal.y, col.touch.x, col.touch.y, col.itemRect.x, col.itemRect.y, col.itemRect.w, col.itemRect.h, col.otherRect.x, col.otherRect.y, col.otherRect.w, col.otherRect.h, col.item, col.other, col.type);

      visited.add(col.other);

      Response response = col.type;
      response.response(this, col, x, y, w, h, goalX, goalY, visitedFilter, result);
      goalX = result.goalX;
      goalY = result.goalY;
      projectedCols = result.projectedCollisions;
    }

    result.set(goalX, goalY);
    result.projectedCollisions.clear();
    for (int i = 0; i < cols.size(); i++) {
      result.projectedCollisions.add(cols.get(i));
    }
    return result;
  }

  public Response.Result move(Item item, float goalX, float goalY, CollisionFilter filter) {
    Response.Result result = check(item, goalX, goalY, filter);
    update(item, result.goalX, result.goalY);
    return result;
  }
}
