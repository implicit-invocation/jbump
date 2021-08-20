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
 * A 2D point with integer x and y.
 * @author tao
 */
public class IntPoint {
  public int x, y;

  public IntPoint() {
  }

  public IntPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }
  
  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IntPoint intPoint = (IntPoint) o;

    return (x == intPoint.x && y == intPoint.y);
  }

  @Override
  public int hashCode() {
    return x * 11 + y * 47;
  }

  @Override
  public String toString() {
    return "(" +x + ", " + y +')';
  }
}
