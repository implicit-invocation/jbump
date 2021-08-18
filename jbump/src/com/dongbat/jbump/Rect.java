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

import static com.dongbat.jbump.Extra.*;

/**
 *
 * @author tao
 */
public class Rect {

  public float x, y, w, h;

  public Rect() {
  }

  public Rect(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  public void set(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  public static void rect_getNearestCorner(float x, float y, float w, float h, float px, float py, Point result) {
    result.set(nearest(px, x, x + w), nearest(y, y, y + h));
  }
  
  /**
   * This is a generalized implementation of the liang-barsky algorithm, which also returns
   * the normals of the sides where the segment intersects.
   * Notice that normals are only guaranteed to be accurate when initially ti1 == -Float.MAX_VALUE, ti2 == Float.MAX_VALUE
   * @return false if the segment never touches the rect
   */
  public static boolean rect_getSegmentIntersectionIndices(float x, float y, float w, float h, float x1, float y1, float x2, float y2, float ti1, float ti2, Point ti, IntPoint n1, IntPoint n2) {
    float dx = x2 - x1;
    float dy = y2 - y1;

    int nx = 0, ny = 0;
    int nx1 = 0, ny1 = 0, nx2 = 0, ny2 = 0;
    float p, q, r;

    for (int side = 1; side <= 4; side++) {
      switch (side) {
        case 1: //left
          nx = -1;
          ny = 0;
          p = -dx;
          q = x1 - x;
          break;
        case 2: //right
          nx = 1;
          ny = 0;
          p = dx;
          q = x + w - x1;
          break;
        case 3: //top
          nx = 0;
          ny = -1;
          p = -dy;
          q = y1 - y;
          break;
        default: //bottom
          nx = 0;
          ny = 1;
          p = dy;
          q = y + h - y1;
          break;
      }

      if (p == 0) {
        if (q <= 0) {
          return false;
        }
      } else {
        r = q / p;
        if (p < 0) {
          if (r > ti2) {
            return false;
          } else if (r > ti1) {
            ti1 = r;
            nx1 = nx;
            ny1 = ny;
          }
        } else {
          if (r < ti1) {
            return false;
          } else if (r < ti2) {
            ti2 = r;
            nx2 = nx;
            ny2 = ny;
          }
        }
      }
    }
    ti.set(ti1, ti2);
    n1.set(nx1, ny1);
    n2.set(nx2, ny2);
    return true;
  }
  
  /**
   * Calculates the minkowsky difference between 2 rects, which is another rect
   */
  public static void rect_getDiff(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2, Rect result) {
    result.set(x2 - x1 - w1, y2 - y1 - h1, w1 + w2, h1 + h2);
  }

  public static boolean rect_containsPoint(float x, float y, float w, float h, float px, float py) {
    return px - x > DELTA && py - y > DELTA
      && x + w - px > DELTA && y + h - py > DELTA;
  }

  public static boolean rect_isIntersecting(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
    return x1 < x2 + w2 && x2 < x1 + w1
      && y1 < y2 + h2 && y2 < y1 + h1;
  }

  public static float rect_getSquareDistance(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2) {
    float dx = x1 - x2 + (w1 - w2) / 2;
    float dy = y1 - y2 + (h1 - h2) / 2;
    return dx * dx + dy * dy;
  }

}
