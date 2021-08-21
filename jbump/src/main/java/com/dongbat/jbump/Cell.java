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

import com.dongbat.jbump.util.ObjectSet;

/**
 *
 * @author tao
 */
public class Cell {
  public float x;
  public float y;
  /**
   * This class is compared by identity, like {@link Item}, and it also caches its identityHashCode() result.
   */
  protected final int identityHash;
  /**
   * Stores the Item values in this Cell.
   * <br>
   * This uses a different implementation of Set than usual; {@link ObjectSet} reuses its iterators where HashSet does
   * not, and in general uses less memory. Avoiding GC pressure is important in games, especially those that target
   * mobile or web platforms, and JBump iterates over these items fairly often, so we don't want to create many
   * Iterators without needing to. ObjectSet doesn't support nested iteration over the same Set, so that is something to
   * be aware of. This was a HashSet, but ObjectSet implements about the same API.
   */
  public ObjectSet<Item> items = new ObjectSet<Item>(11);

  /**
   * Constructs a Cell with a position of 0,0 and no items (it will be empty).
   * <br>
   * If you subclass Cell, you should call {@code super()} so the cached identity hash code is stored correctly.
   */
  public Cell() {
    identityHash = System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    return (this == o);
  }

  @Override
  public int hashCode() {
    return identityHash;
  }

}
