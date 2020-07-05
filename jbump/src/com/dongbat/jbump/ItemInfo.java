package com.dongbat.jbump;

import java.util.Comparator;

public class ItemInfo {
    public Item item;
    public float x1, y1, x2, y2, ti1, ti2;
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
