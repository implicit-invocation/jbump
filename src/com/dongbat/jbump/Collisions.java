/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dongbat.jbump;

import static com.dongbat.jbump.Rect.rect_getSquareDistance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tao
 */
public class Collisions {

  private final ArrayList<Boolean> overlaps = new ArrayList<Boolean>();
  private final ArrayList<Float> tis = new ArrayList<Float>();
  private final ArrayList<Float> moveXs = new ArrayList<Float>();
  private final ArrayList<Float> moveYs = new ArrayList<Float>();
  private final ArrayList<Float> normalXs = new ArrayList<Float>();
  private final ArrayList<Float> normalYs = new ArrayList<Float>();
  private final ArrayList<Float> touchXs = new ArrayList<Float>();
  private final ArrayList<Float> touchYs = new ArrayList<Float>();
  private final ArrayList<Float> x1s = new ArrayList<Float>();
  private final ArrayList<Float> y1s = new ArrayList<Float>();
  private final ArrayList<Float> w1s = new ArrayList<Float>();
  private final ArrayList<Float> h1s = new ArrayList<Float>();
  private final ArrayList<Float> x2s = new ArrayList<Float>();
  private final ArrayList<Float> y2s = new ArrayList<Float>();
  private final ArrayList<Float> w2s = new ArrayList<Float>();
  private final ArrayList<Float> h2s = new ArrayList<Float>();
  public ArrayList<Item> items = new ArrayList<Item>();
  public ArrayList<Item> others = new ArrayList<Item>();
  public ArrayList<Response> types = new ArrayList<Response>();
  private int size = 0;
  
  public void add(Collision col) {
    add(col.overlaps, col.ti, col.move.x, col.move.y, col.normal.x, col.normal.y, col.touch.x, col.touch.y, col.itemRect.x, col.itemRect.y, col.itemRect.w, col.itemRect.h, col.otherRect.x, col.otherRect.y, col.otherRect.w, col.otherRect.h, col.item, col.other, col.type);
  }

  public void add(boolean overlap, float ti, float moveX, float moveY, float normalX, float normalY, float touchX, float touchY,
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

  private static final Collision COLLISION = new Collision();

  public Collision get(int index) {
    if (index >= size) {
      return null;
    }
    COLLISION.set(overlaps.get(index), tis.get(index), moveXs.get(index), moveYs.get(index), normalXs.get(index), normalYs.get(index), touchXs.get(index), touchYs.get(index), x1s.get(index), y1s.get(index), w1s.get(index), h1s.get(index), x2s.get(index), y2s.get(index), w2s.get(index), h2s.get(index));
    COLLISION.item = items.get(index);
    COLLISION.other = others.get(index);
    COLLISION.type = types.get(index);
    return COLLISION;
  }

  public void remove(int index) {
    if (index < size) {
      size--;
      overlaps.remove(index);
      tis.remove(index);
      moveXs.remove(index);
      moveYs.remove(index);
      normalXs.remove(index);
      normalYs.remove(index);
      touchXs.remove(index);
      touchYs.remove(index);
      x1s.remove(index);
      y1s.remove(index);
      w1s.remove(index);
      h1s.remove(index);
      x2s.remove(index);
      y2s.remove(index);
      w2s.remove(index);
      h2s.remove(index);
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

  private static final ArrayList<Integer> order = new ArrayList<Integer>();
  private final Comparator<Integer> orderComparator = new Comparator<Integer>() {
    @Override
    public int compare(Integer a, Integer b) {
      if (sortByTiAndDistance(a, b)) {
        return -1;
      } else {
        return 1;
      }
    }
  };

  public static <T extends Comparable<T>> void keySort(
    final List<Integer> indices, List<?>... lists) {
    Map<Integer, Integer> swapMap = new HashMap<Integer, Integer>(indices.size());

    for (int i = 0; i < indices.size(); i++) {
      int k = indices.get(i);
      while (swapMap.containsKey(k)) {
        k = swapMap.get(k);
      }

      swapMap.put(i, k);
    }

    for (Map.Entry<Integer, Integer> e : swapMap.entrySet()) {
      for (List<?> list : lists) {
        Collections.swap(list, e.getKey(), e.getValue());
      }
    }
  }

  public void sort() {
    order.clear();
    for (int i = 0; i < size; i++) {
      order.add(i);
    }
    order.sort(orderComparator);
    keySort(order, overlaps,
      tis,
      moveXs,
      moveYs,
      normalXs,
      normalYs,
      touchXs,
      touchYs,
      x1s,
      y1s,
      w1s,
      h1s,
      x2s,
      y2s,
      w2s,
      h2s,
      items,
      others,
      types);
  }

  private boolean sortByTiAndDistance(int a, int b) {
    if (tis.get(a).equals(tis.get(b))) {

      float ad = rect_getSquareDistance(x1s.get(a), y1s.get(a), w1s.get(a), h1s.get(a), x2s.get(a), y2s.get(a), w2s.get(a), h2s.get(a));
      float bd = rect_getSquareDistance(x1s.get(a), y1s.get(a), w1s.get(a), h1s.get(a), x2s.get(b), y2s.get(b), w2s.get(b), h2s.get(b));

      return ad < bd;
    }
    return tis.get(a) < tis.get(b);
  }
}
