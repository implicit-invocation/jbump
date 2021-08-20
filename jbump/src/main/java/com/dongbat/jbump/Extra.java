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
 * Small helper methods for math tasks, as well as the configurable {@link #DELTA}.
 * @author tao
 */
public class Extra {

  /**
   * Used by {@link Rect} and {@link RectHelper} to determine if a float is close
   * enough to a threshold to count as within that threshold, to counter-act
   * floating-point imprecision. Defaults to {@code 1e-5f} .
   */
  public static float DELTA = 1e-5f;

  public static int sign(float x) {
    if (x > 0) {
      return 1;
    } else if (x < 0) {
      return -1;
    }
    return 0;
  }

  public static float nearest(float x, float a, float b) {
    if (Math.abs(a - x) < Math.abs(b - x)) {
      return a;
    }
    return b;
  }

}
