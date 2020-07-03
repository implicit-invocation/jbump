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
public class Point {
  public float x, y;

  public Point() {
  }

  public Point(float x, float y) {
    this.x = x;
    this.y = y;
  }
  
  public void set(float x, float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Point point = (Point) o;

    return Float.compare(point.x, x) == 0 && Float.compare(point.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return (int)(Float.floatToIntBits(x) * 0xC13FA9A902A6328FL
            + Float.floatToIntBits(y) * 0x91E10DA5C79E7B1DL >>> 32);
  }

  @Override
  public String toString() {
    return "(" +x +", " + y +')';
  }
}
