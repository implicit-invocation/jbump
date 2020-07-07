package com.dongbat.jbump;

import java.util.Comparator;

public class ItemInfo {
    public Item item;
    
    /**
     * The x coordinate where the line segment intersects the {@link Rect} of the {@link Item}.
     */
    public float x1;
    
    /**
     * The y coordinate where the line segment intersects the {@link Rect} of the {@link Item}.
     */
    public float y1;
    
    /**
     * The x coordinate where the line segment exits the {@link Rect} of the {@link Item}. In the case that the segment
     * does not exit the Rect, this simply returns the coordinate of the segment's end point.
     */
    public float x2;
    
    /**
     * The y coordinate where the line segment exits the {@link Rect} of the {@link Item}. In the case that the segment
     * does not exit the Rect, this simply returns the coordinate of the segment's end point.
     */
    public float y2;
    
    /**
     * A value from 0 to 1 indicating how far from the starting point of the segment did the impact happen horizontally.
     */
    public float ti1;
    
    /**
     * A value from 0 to 1 indicating how far from the starting point of the segment did the impact happen vertically.
     */
    public float ti2;
    public float weight;
    
    public ItemInfo(Item item,  float ti1, float ti2, float weight) {
        this.item = item;
        this.ti1 = ti1;
        this.ti2 = ti2;
        this.weight = weight;
    }
    
    public static final Comparator<ItemInfo> weightComparator = new Comparator<ItemInfo>() {
        @Override
        public int compare(ItemInfo o1, ItemInfo o2) {
            return Float.compare(o1.weight, o2.weight);
        }
    };
}
