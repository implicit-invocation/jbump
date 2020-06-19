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

import static com.dongbat.jbump.Extra.DELTA;
import static com.dongbat.jbump.Extra.sign;
import static com.dongbat.jbump.Rect.rect_containsPoint;
import static com.dongbat.jbump.Rect.rect_getDiff;
import static com.dongbat.jbump.Rect.rect_getNearestCorner;
import static com.dongbat.jbump.Rect.rect_getSegmentIntersectionIndices;
import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 *
 * @author tao
 */
public class RectHelper {

  private final Rect rect_detectCollision_diff = new Rect();
  private final Point rect_detectCollision_nearestCorner = new Point();
  private final Point rect_detectCollision_getSegmentIntersectionIndices_ti = new Point();
  private final Point rect_detectCollision_getSegmentIntersectionIndices_n1 = new Point();
  private final Point rect_detectCollision_getSegmentIntersectionIndices_n2 = new Point();
  private final Collision rect_detectCollision_getSegmentIntersectionIndices_col = new Collision();

  public Collision rect_detectCollision(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2,
    float goalX, float goalY) {
    Collision col = rect_detectCollision_getSegmentIntersectionIndices_col;
    float dx = goalX - x1;
    float dy = goalY - y1;

    rect_getDiff(x1, y1, w1, h1, x2, y2, w2, h2, rect_detectCollision_diff);
    float x = rect_detectCollision_diff.x;
    float y = rect_detectCollision_diff.y;
    float w = rect_detectCollision_diff.w;
    float h = rect_detectCollision_diff.h;

    boolean overlaps = false;
    Float ti = null;
    float nx = 0, ny = 0;

    if (rect_containsPoint(x, y, w, h, 0, 0)) {
      rect_getNearestCorner(x, y, w, h, 0, 0, rect_detectCollision_nearestCorner);
      float px = rect_detectCollision_nearestCorner.x;
      float py = rect_detectCollision_nearestCorner.y;
      float wi = min(w1, abs(px));
      float hi = min(h1, abs(py));
      ti = -wi * h1;
      overlaps = true;
    } else {
      boolean intersect = rect_getSegmentIntersectionIndices(x, y, w, h, 0, 0, dx, dy, -Float.MAX_VALUE, Float.MAX_VALUE, rect_detectCollision_getSegmentIntersectionIndices_ti, rect_detectCollision_getSegmentIntersectionIndices_n1, rect_detectCollision_getSegmentIntersectionIndices_n2);
      float ti1 = rect_detectCollision_getSegmentIntersectionIndices_ti.x;
      float ti2 = rect_detectCollision_getSegmentIntersectionIndices_ti.y;
      float nx1 = rect_detectCollision_getSegmentIntersectionIndices_n1.x;
      float ny1 = rect_detectCollision_getSegmentIntersectionIndices_n1.y;

      if (intersect && ti1 < 1 && abs(ti1 - ti2) >= DELTA
        && (0 < ti1 + DELTA || 0 == ti1 && ti2 > 0)) {
        ti = ti1;
        nx = nx1;
        ny = ny1;
        overlaps = false;
      }
    }
    if (ti == null) {
      return null;
    }
    float tx, ty;

    if (overlaps) {
      if (dx == 0 && dy == 0) {
        rect_getNearestCorner(x, y, w, h, 0, 0, rect_detectCollision_nearestCorner);
        float px = rect_detectCollision_nearestCorner.x;
        float py = rect_detectCollision_nearestCorner.y;
        if (abs(px) < abs(py)) {
          py = 0;
        } else {
          px = 0;
        }
        nx = sign(px);
        ny = sign(py);
        tx = x1 + px;
        ty = y1 + py;
      } else {
        boolean intersect = rect_getSegmentIntersectionIndices(x, y, w, h, 0, 0, dx, dy, -Float.MAX_VALUE, 1, rect_detectCollision_getSegmentIntersectionIndices_ti, rect_detectCollision_getSegmentIntersectionIndices_n1, rect_detectCollision_getSegmentIntersectionIndices_n2);
        float ti1 = rect_detectCollision_getSegmentIntersectionIndices_ti.x;
        nx = rect_detectCollision_getSegmentIntersectionIndices_n1.x;
        ny = rect_detectCollision_getSegmentIntersectionIndices_n1.y;
        if (!intersect) {
          return null;
        }
        tx = x1 + dx * ti1;
        ty = y1 + dy * ti1;
      }
    } else {
      tx = x1 + dx * ti;
      ty = y1 + dy * ti;
    }
    col.set(overlaps, ti, dx, dy, nx, ny, tx, ty, x1, y1, w1, h1, x2, y2, w2, h2);
    return col;
  }

}
