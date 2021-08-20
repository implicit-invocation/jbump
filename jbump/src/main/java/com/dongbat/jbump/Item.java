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
 * Wraps an {@code E} value in {@link #userData}, so it is compared by the identity
 * of the Item, not the E value. You can freely change the E value; it is not used
 * for comparison with other Items. The userData can be null.
 * @author tao
 */
public class Item<E> {

  /**
   * This is not ever read by JBump, so this can be anything user code needs it to be.
   * It isn't considered by {@link #equals(Object)} or by {@link #hashCode()}. This
   * may be null.
   */
  public E userData;
  protected final int identityHash;

  /**
   * Constructs an Item with no userData (it will be null).
   * <br>
   * If you subclass Item, you should call {@code super()} so the cached identity hash code is stored correctly.
   */
  public Item() {
    identityHash = System.identityHashCode(this);
  }

  /**
   * Constructs an Item with the given {@code E} userData; the userData can change after construction.
   * <br>
   * If you subclass Item, you should call {@code super(E)} so the cached identity hash code is stored correctly.
   * @param userData whatever {@code E} item this should hold; may be null.
   */
  public Item(E userData) {
    identityHash = System.identityHashCode(this);
    this.userData = userData;
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
