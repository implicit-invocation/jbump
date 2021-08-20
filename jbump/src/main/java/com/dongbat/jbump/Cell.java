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

import java.util.HashSet;

/**
 *
 * @author tao
 */
public class Cell {
  public float x;
  public float y;
  public HashSet<Item> items = new HashSet<Item>();
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Cell cell = (Cell) o;

    return Float.compare(cell.x, x) == 0 && Float.compare(cell.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return (int)(Float.floatToIntBits(x) * 0xC13FA9A902A6328FL
            + Float.floatToIntBits(y) * 0x91E10DA5C79E7B1DL >>> 32);
  }

}
