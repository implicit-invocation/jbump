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

import java.util.*;

import static com.dongbat.jbump.Grid.*;
import static com.dongbat.jbump.ItemInfo.weightComparator;
import static com.dongbat.jbump.Rect.rect_getSegmentIntersectionIndices;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 *
 * @author tao
 */
public class World<E> {

//  private final HashMap<Float, HashMap<Float, Cell>> rows = new HashMap<Float, HashMap<Float, Cell>>();
  private final HashMap<Point, Cell> cellMap = new HashMap<Point, Cell>();
  private final HashSet<Cell> nonEmptyCells = new HashSet<Cell>();
  private float cellMinX, cellMinY, cellMaxX, cellMaxY;
  private final Grid grid = new Grid();
  private final RectHelper rectHelper = new RectHelper();
  private boolean tileMode = true;
  private final float cellSize;
  
  public World() {
    this(64f);
  }
  
  public World(float cellSize) {
    this.cellSize = cellSize;
  }
  
  public void setTileMode(boolean tileMode) {
    this.tileMode = tileMode;
  }

  public boolean isTileMode() {
    return tileMode;
  }

  private void addItemToCell(Item<E> item, float cx, float cy) {
    Point pt = new Point(cx, cy);
    Cell cell = cellMap.get(pt);
    if(cell == null) {
      cell = new Cell();
      cellMap.put(pt, cell);
      if (cx < cellMinX) cellMinX = cx;
      if (cy < cellMinY) cellMinY = cy;
      if (cx > cellMaxX) cellMaxX = cx;
      if (cy > cellMaxY) cellMaxY = cy;
    }
    nonEmptyCells.add(cell);
    if (!cell.items.contains(item)) {
      cell.items.add(item);
      cell.itemCount = cell.itemCount + 1;
    }
  }

  private boolean removeItemFromCell(Item item, float cx, float cy) {
    Point pt = new Point(cx, cy);
    Cell cell = cellMap.get(pt);
    if(cell == null) {
      return false;
    }
    if (!cell.items.contains(item)) {
      return false;
    }
    cell.items.remove(item);
    cell.itemCount = cell.itemCount - 1;
    if (cell.itemCount == 0) {
      nonEmptyCells.remove(cell);
    }
    return true;
  }

  private LinkedHashSet<Item> getDictItemsInCellRect(float cl, float ct, float cw, float ch, LinkedHashSet<Item> result) {
    result.clear();
    Point pt = new Point(cl, ct);
    for (float cy = ct; cy < ct + ch; cy++, pt.y++) {
      for (float cx = cl; cx < cl + cw; cx++, pt.x++) {
        Cell cell = cellMap.get(pt);
        if (cell != null && cell.itemCount > 0) { //no cell.itemCount > 1 because tunneling
          result.addAll(cell.items);
        }
      }
      pt.x = cl;
    }
    return result;
  }

  private final ArrayList<Cell> getCellsTouchedBySegment_visited = new ArrayList<Cell>();

  public ArrayList<Cell> getCellsTouchedBySegment(float x1, float y1, float x2, float y2, final ArrayList<Cell> result) {
    result.clear();
    getCellsTouchedBySegment_visited.clear();
    // use set
    final ArrayList<Cell> visited = getCellsTouchedBySegment_visited;
    final Point pt = new Point(x1, y1);
    grid.grid_traverse(cellSize, x1, y1, x2, y2, new TraverseCallback() {
      @Override
      public boolean onTraverse(float cx, float cy, int stepX, int stepY) {
        //stop if cell coordinates are outside of the world.
        if (stepX == -1 && cx < cellMinX || stepX == 1 && cx > cellMaxX
                || stepY == -1 && cy < cellMinY || stepY == 1 && cy > cellMaxY) return false;
        pt.x = cx;
        pt.y = cy;
        Cell cell = cellMap.get(pt);
        if (cell == null || visited.contains(cell)) {
          return true;
        }
        visited.add(cell);
        result.add(cell);
        return true;
      }
    });

    return result;
  }
  
  public ArrayList<Cell> getCellsTouchedByRay(float originX, float originY, float dirX, float dirY, final ArrayList<Cell> result) {
    result.clear();
    getCellsTouchedBySegment_visited.clear();
    // use set
    final ArrayList<Cell> visited = getCellsTouchedBySegment_visited;
    final Point pt = new Point(originX, originY);
    grid.grid_traverseRay(cellSize, originX, originY, dirX, dirY, new TraverseCallback() {
      @Override
      public boolean onTraverse(float cx, float cy, int stepX, int stepY) {
        //stop if cell coordinates are outside of the world.
        if (stepX == -1 && cx < cellMinX || stepX == 1 && cx > cellMaxX
                || stepY == -1 && cy < cellMinY || stepY == 1 && cy > cellMaxY) return false;
        pt.x = cx;
        pt.y = cy;
        Cell cell = cellMap.get(pt);
        if (cell == null || visited.contains(cell)) {
          return true;
        }
        visited.add(cell);
        result.add(cell);
        return true;
      }
    });

    return result;
  }
  
  private final ArrayList<Cell> info_cells = new ArrayList<Cell>();
  private final Point info_ti = new Point();
  private final IntPoint info_normalX = new IntPoint();
  private final IntPoint info_normalY = new IntPoint();
  private final ArrayList<Item> info_visited = new ArrayList<Item>();
  
  private ArrayList<ItemInfo> getInfoAboutItemsTouchedBySegment(float x1, float y1, float x2, float y2, CollisionFilter filter, ArrayList<ItemInfo> infos) {
    info_visited.clear();
    infos.clear();
    getCellsTouchedBySegment(x1, y1, x2, y2, info_cells);
    
    for (Cell cell : info_cells) {
      for (Item item : cell.items) {
        if (!info_visited.contains(item)) {
          info_visited.add(item);
          if (filter == null || filter.filter(item, null) != null) {
            Rect rect = rects.get(item);
            float l = rect.x;
            float t = rect.y;
            float w = rect.w;
            float h = rect.h;
            
            if (rect_getSegmentIntersectionIndices(l, t, w, h, x1, y1, x2, y2, 0, 1, info_ti, info_normalX, info_normalY)) {
              float ti1 = info_ti.x;
              float ti2 = info_ti.y;
              if ((0 < ti1 && ti1 < 1) || (0 < ti2 && ti2 < 1)) {
                rect_getSegmentIntersectionIndices(l, t, w, h, x1, y1, x2, y2, -Float.MAX_VALUE, Float.MAX_VALUE, info_ti, info_normalX, info_normalY);
                float tii0 = info_ti.x;
                float tii1 = info_ti.y;
                infos.add(new ItemInfo(item, ti1, ti2, Math.min(tii0, tii1)));
              }
            }
          }
        }
      }
    }
    Collections.sort(infos, weightComparator);
    return infos;
  }
  
  private ArrayList<ItemInfo> getInfoAboutItemsTouchedByRay(float originX, float originY, float dirX, float dirY, CollisionFilter filter, ArrayList<ItemInfo> infos) {
    info_visited.clear();
    infos.clear();
    getCellsTouchedByRay(originX, originY, dirX, dirY, info_cells);
    
    for (Cell cell : info_cells) {
      for (Item item : cell.items) {
        if (!info_visited.contains(item)) {
          info_visited.add(item);
          if (filter == null || filter.filter(item, null) != null) {
            Rect rect = rects.get(item);
            float l = rect.x;
            float t = rect.y;
            float w = rect.w;
            float h = rect.h;
            
            if (rect_getSegmentIntersectionIndices(l, t, w, h, originX, originY, originX + dirX, originY + dirY, 0, Float.MAX_VALUE, info_ti, info_normalX, info_normalY)) {
              float ti1 = info_ti.x;
              float ti2 = info_ti.y;
              infos.add(new ItemInfo(item, ti1, ti2, Math.min(ti1, ti2)));
            }
          }
        }
      }
    }
    Collections.sort(infos, weightComparator);
    return infos;
  }

  public Collisions project(Item item, float x, float y, float w, float h, float goalX, float goalY, Collisions collisions) {
    return project(item, x, y, w, h, goalX, goalY, CollisionFilter.defaultFilter, collisions);
  }

  private final ArrayList<Item> project_visited = new ArrayList<Item>();
  private final Rect project_c = new Rect();
  private final LinkedHashSet<Item> project_dictItemsInCellRect = new LinkedHashSet<Item>();

  public Collisions project(Item item, float x, float y, float w, float h, float goalX, float goalY, CollisionFilter filter, Collisions collisions) {
    collisions.clear();
    ArrayList<Item> visited = project_visited;
    visited.clear();
    if (item != null) {
      visited.add(item);
    }
    
    /*This could probably be done with less cells using a polygon raster over the cells instead of a
    bounding rect of the whole movement. Conditional to building a queryPolygon method*/
    float tl = min(goalX, x);
    float tt = min(goalY, y);
    float tr = max(goalX + w, x + w);
    float tb = max(goalY + h, y + h);

    float tw = tr - tl;
    float th = tb - tt;

    grid.grid_toCellRect(cellSize, tl, tt, tw, th, project_c);
    float cl = project_c.x, ct = project_c.y, cw = project_c.w, ch = project_c.h;
    LinkedHashSet<Item> dictItemsInCellRect = getDictItemsInCellRect(cl, ct, cw, ch, project_dictItemsInCellRect);
    for (Item other : dictItemsInCellRect) {
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

  private final HashMap<Item, Rect> rects = new HashMap<Item, Rect>();

  public Rect getRect(Item item) {
    return rects.get(item);
  }
  
  public Set<Item> getItems() {
    return rects.keySet();
  }
  
  public Collection<Rect> getRects() {
    return rects.values();
  }

  public Collection<Cell> getCells() {
    return cellMap.values(); 
  }
  
  public int countCells() { 
    return cellMap.size();
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
  
  public void reset() {
    rects.clear();
    cellMap.clear();
    nonEmptyCells.clear();
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
            if (cyOut || cx < cl1 || cx > cr1) {
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
  
  public float getCellSize() {
    return cellSize;
  }
  
  private final Rect query_c = new Rect();
  private final LinkedHashSet<Item> query_dictItemsInCellRect = new LinkedHashSet<Item>();
  
  /**
   * A collision check of items that intersect the given rectangle.
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null.
   * @param items An empty list that will be filled with the {@link Item} instances that collide with the rectangle.
   */
  public ArrayList<Item> queryRect(float x, float y, float w, float h, CollisionFilter filter, ArrayList<Item> items) {
    items.clear();
    grid.grid_toCellRect(cellSize, x, y, w, h, query_c);
    float cl = query_c.x, ct = query_c.y, cw = query_c.w, ch = query_c.h;
    LinkedHashSet<Item> dictItemsInCellRect = getDictItemsInCellRect(cl, ct, cw, ch, query_dictItemsInCellRect);
    
    for (Item item : dictItemsInCellRect) {
      Rect rect = rects.get(item);
      if ((filter == null || filter.filter(item, null) != null) && rect.rect_isIntersecting(x, y, w, h, rect.x, rect.y, rect.w,rect.h)) {
        items.add(item);
      }
    }
    
    return items;
  }
  
  private final Point query_point = new Point();
  
  /**
   * A collision check of items that intersect the given point.
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null.
   * @param items An empty list that will be filled with the {@link Item} instances that collide with the point.
   */
  public ArrayList<Item> queryPoint(float x, float y, CollisionFilter filter, ArrayList<Item> items) {
    items.clear();
    toCell(x, y, query_point);
    float cx = query_point.x;
    float cy = query_point.y;
    LinkedHashSet<Item> dictItemsInCellRect = getDictItemsInCellRect(cx, cy, 1, 1, query_dictItemsInCellRect);
    
    for (Item item : dictItemsInCellRect) {
      Rect rect = rects.get(item);
      if ((filter == null || filter.filter(item, null) != null) && rect.rect_containsPoint(rect.x, rect.y, rect.w, rect.h, x, y)) {
        items.add(item);
      }
    }
    return items;
  }
  
  /**
   * A collision check of items that intersect the given line segment.
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null.
   * @param items An empty list that will be filled with the {@link Item} instances that intersect the segment.
   */
  private final ArrayList<ItemInfo> query_infos = new ArrayList<ItemInfo>();
  public ArrayList<Item> querySegment(float x1, float y1, float x2, float y2, CollisionFilter filter, ArrayList<Item> items) {
    items.clear();
    ArrayList<ItemInfo> infos = getInfoAboutItemsTouchedBySegment(x1, y1, x2, y2, filter, query_infos);
    for (ItemInfo info : infos) {
      items.add(info.item);
    }
    
    return items;
  }
  
  /**
   * A collision check of items that intersect the given line segment. Returns more details about where the collision
   * occurs compared to {@link World#querySegment(float, float, float, float, CollisionFilter, ArrayList)}
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null
   * @param infos An empty list that will be filled with the collision information.
   */
  public ArrayList<ItemInfo> querySegmentWithCoords(float x1, float y1, float x2, float y2, CollisionFilter filter, ArrayList<ItemInfo> infos) {
    infos.clear();
    infos = getInfoAboutItemsTouchedBySegment(x1, y1, x2, y2, filter, infos);
    float dx = x2 - x1;
    float dy = y2 - y1;
    
    for (ItemInfo info : infos) {
      float ti1 = info.ti1;
      float ti2 = info.ti2;
      
      info.weight = 0;
      info.x1 = x1 + dx * ti1;
      info.y1 = y1 + dy * ti1;
      info.x2 = x1 + dx * ti2;
      info.y2 = y1 + dy * ti2;
    }
    
    return infos;
  }
  
  /**
   * A collision check of items that intersect the given ray.
   * @param originX The x-origin of the ray.
   * @param originY The y-origin of the ray.
   * @param dirX The x component of the vector that defines the angle of the ray.
   * @param dirY The y component of the vector that defines the angle of the ray.
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null.
   * @param items An empty list that will be filled with the {@link Item} instances that intersect the ray.
   */
  public ArrayList<Item> queryRay(float originX, float originY, float dirX, float dirY, CollisionFilter filter,  ArrayList<Item> items) {
    items.clear();
    ArrayList<ItemInfo> infos = getInfoAboutItemsTouchedByRay(originX, originY, dirX, dirY, filter, query_infos);
    for (ItemInfo info : infos) {
      items.add(info.item);
    }
    
    return items;
  }
  
  /**
   * A collision check of items that intersect the given ray. Returns more details about where the collision
   * occurs compared to {@link World#queryRay(float, float, float, float, CollisionFilter, ArrayList)}
   * @param originX The x-origin of the ray.
   * @param originY The y-origin of the ray.
   * @param dirX The x component of the vector that defines the angle of the ray.
   * @param dirY The y component of the vector that defines the angle of the ray.
   * @param filter Defines what items will be checked for collision. "item" is the {@link Item} checked for collision.
   *               "other" is null
   * @param infos An empty list that will be filled with the collision information.
   */
  public ArrayList<ItemInfo> queryRayWithCoords(float originX, float originY, float dirX, float dirY, CollisionFilter filter, ArrayList<ItemInfo> infos) {
    infos.clear();
    infos = getInfoAboutItemsTouchedByRay(originX, originY, dirX, dirY, filter, infos);
    
    for (ItemInfo info : infos) {
      float ti1 = info.ti1;
      float ti2 = info.ti2;
      
      info.weight = 0;
      info.x1 = originX + dirX * ti1;
      info.y1 = originY + dirY * ti1;
      info.x2 = originX + dirX * ti2;
      info.y2 = originY + dirY * ti2;
    }
    
    return infos;
  }
}
