/*
 * Copyright 2017 DongBat.
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

import static com.dongbat.jbump.Rect.rect_getSquareDistance;
import com.dongbat.jbump.util.BooleanArray;
import com.dongbat.jbump.util.FloatArray;
import com.dongbat.jbump.util.IntArray;
import com.dongbat.jbump.util.IntIntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author tao
 */
public class Collisions implements Comparator<Integer> {

  private final BooleanArray overlaps = new BooleanArray();
  private final FloatArray tis = new FloatArray();
  private final FloatArray moveXs = new FloatArray();
  private final FloatArray moveYs = new FloatArray();
  private final IntArray normalXs = new IntArray();
  private final IntArray normalYs = new IntArray();
  private final FloatArray touchXs = new FloatArray();
  private final FloatArray touchYs = new FloatArray();
  private final FloatArray x1s = new FloatArray();
  private final FloatArray y1s = new FloatArray();
  private final FloatArray w1s = new FloatArray();
  private final FloatArray h1s = new FloatArray();
  private final FloatArray x2s = new FloatArray();
  private final FloatArray y2s = new FloatArray();
  private final FloatArray w2s = new FloatArray();
  private final FloatArray h2s = new FloatArray();
  public ArrayList<Item> items = new ArrayList<Item>();
  public ArrayList<Item> others = new ArrayList<Item>();
  public ArrayList<Response> types = new ArrayList<Response>();
  private int size = 0;
  
  public Collisions() {
    
  }
  
  public Collisions(Collisions other) {
    overlaps.addAll(other.overlaps);
    tis.addAll(other.tis);
    moveXs.addAll(other.moveXs);
    moveYs.addAll(other.moveYs);
    normalXs.addAll(other.normalXs);
    normalYs.addAll(other.normalYs);
    touchXs.addAll(other.touchXs);
    touchYs.addAll(other.touchYs);
    x1s.addAll(other.x1s);
    y1s.addAll(other.y1s);
    w1s.addAll(other.w1s);
    h1s.addAll(other.h1s);
    x2s.addAll(other.x2s);
    y2s.addAll(other.y2s);
    w2s.addAll(other.w2s);
    h2s.addAll(other.h2s);
    items.addAll(other.items);
    others.addAll(other.others);
    types.addAll(other.types);
    size = other.size;
  }
  
  public void add(Collision col) {
    add(col.overlaps, col.ti, col.move.x, col.move.y, col.normal.x, col.normal.y, col.touch.x, col.touch.y,
            col.itemRect.x, col.itemRect.y, col.itemRect.w, col.itemRect.h, col.otherRect.x, col.otherRect.y,
            col.otherRect.w, col.otherRect.h, col.item, col.other, col.type);
  }

  public void add(boolean overlap, float ti, float moveX, float moveY, int normalX, int normalY, float touchX, float touchY,
    float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2, Item item, Item other, Response type) {
    size++;
    overlaps.add(overlap);
    tis.add(ti);
    moveXs.add(moveX);
    moveYs.add(moveY);
    normalXs.add(normalX);
    normalYs.add(normalY);
    touchXs.add(touchX);
    touchYs.add(touchY);
    x1s.add(x1);
    y1s.add(y1);
    w1s.add(w1);
    h1s.add(h1);
    x2s.add(x2);
    y2s.add(y2);
    w2s.add(w2);
    h2s.add(h2);
    items.add(item);
    others.add(other);
    types.add(type);
  }

  private final Collision collision = new Collision();

  public Collision get(int index) {
    if (index >= size) {
      return null;
    }
    collision.set(overlaps.get(index), tis.get(index), moveXs.get(index), moveYs.get(index), normalXs.get(index), normalYs.get(index), touchXs.get(index), touchYs.get(index), x1s.get(index), y1s.get(index), w1s.get(index), h1s.get(index), x2s.get(index), y2s.get(index), w2s.get(index), h2s.get(index));
    collision.item = items.get(index);
    collision.other = others.get(index);
    collision.type = types.get(index);
    return collision;
  }

  public void remove(int index) {
    if (index < size) {
      size--;
      overlaps.removeIndex(index);
      tis.removeIndex(index);
      moveXs.removeIndex(index);
      moveYs.removeIndex(index);
      normalXs.removeIndex(index);
      normalYs.removeIndex(index);
      touchXs.removeIndex(index);
      touchYs.removeIndex(index);
      x1s.removeIndex(index);
      y1s.removeIndex(index);
      w1s.removeIndex(index);
      h1s.removeIndex(index);
      x2s.removeIndex(index);
      y2s.removeIndex(index);
      w2s.removeIndex(index);
      h2s.removeIndex(index);
      items.remove(index);
      others.remove(index);
      types.remove(index);
    }
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public void clear() {
    size = 0;
    overlaps.clear();
    tis.clear();
    moveXs.clear();
    moveYs.clear();
    normalXs.clear();
    normalYs.clear();
    touchXs.clear();
    touchYs.clear();
    x1s.clear();
    y1s.clear();
    w1s.clear();
    h1s.clear();
    x2s.clear();
    y2s.clear();
    w2s.clear();
    h2s.clear();
    items.clear();
    others.clear();
    types.clear();
  }

  private final ArrayList<Integer> order = new ArrayList<Integer>();
  private final IntIntMap swapMap = new IntIntMap();

  public <T extends Comparable<T>> void keySort(
    final List<Integer> indices, List<?> list) {
    swapMap.clear();
    for (int i = 0; i < indices.size(); i++) {
      int k = indices.get(i);
      while (swapMap.containsKey(k)) {
        k = swapMap.get(k, 0);
      }

      swapMap.put(i, k);
    }

    for (IntIntMap.Entry e : swapMap) {
      Collections.swap(list, e.key, e.value);
    }
  }

  public <T extends Comparable<T>> void keySort(
    final List<Integer> indices, FloatArray list) {
    swapMap.clear();
    for (int i = 0; i < indices.size(); i++) {
      int k = indices.get(i);
      while (swapMap.containsKey(k)) {
        k = swapMap.get(k, 0);
      }

      swapMap.put(i, k);
    }

    for (IntIntMap.Entry e : swapMap) {
      list.swap(e.key, e.value);
    }
  }
  
  public <T extends Comparable<T>> void keySort(
          final List<Integer> indices, IntArray list) {
    swapMap.clear();
    for (int i = 0; i < indices.size(); i++) {
      int k = indices.get(i);
      while (swapMap.containsKey(k)) {
        k = swapMap.get(k, 0);
      }
      
      swapMap.put(i, k);
    }
    
    for (IntIntMap.Entry e : swapMap) {
      list.swap(e.key, e.value);
    }
  }

  public <T extends Comparable<T>> void keySort(
    final List<Integer> indices, BooleanArray list) {
    swapMap.clear();
    for (int i = 0; i < indices.size(); i++) {
      int k = indices.get(i);
      while (swapMap.containsKey(k)) {
        k = swapMap.get(k, 0);
      }

      swapMap.put(i, k);
    }

    for (IntIntMap.Entry e : swapMap) {
      list.swap(e.key, e.value);
    }
  }

  public void sort() {
    order.clear();
    for (int i = 0; i < size; i++) {
      order.add(i);
    }
    Collections.sort(order, this);
    keySort(order, overlaps);
    keySort(order, tis);
    keySort(order, moveXs);
    keySort(order, moveYs);
    keySort(order, normalXs);
    keySort(order, normalYs);
    keySort(order, touchXs);
    keySort(order, touchYs);
    keySort(order, x1s);
    keySort(order, y1s);
    keySort(order, w1s);
    keySort(order, h1s);
    keySort(order, x2s);
    keySort(order, y2s);
    keySort(order, w2s);
    keySort(order, h2s);
    keySort(order, items);
    keySort(order, others);
    keySort(order, types);
  }

  @Override
  public int compare(Integer a, Integer b) {
    if (tis.get(a) == (tis.get(b))) {

      float ad = rect_getSquareDistance(x1s.get(a), y1s.get(a), w1s.get(a), h1s.get(a), x2s.get(a), y2s.get(a), w2s.get(a), h2s.get(a));
      float bd = rect_getSquareDistance(x1s.get(a), y1s.get(a), w1s.get(a), h1s.get(a), x2s.get(b), y2s.get(b), w2s.get(b), h2s.get(b));

      return Float.compare(ad, bd);
    }
    return Float.compare(tis.get(a), tis.get(b));
  }
}
