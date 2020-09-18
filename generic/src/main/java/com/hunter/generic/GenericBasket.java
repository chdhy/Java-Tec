package com.hunter.generic;

import java.util.ArrayList;
import java.util.List;

public class GenericBasket<T> {

    private List fruitBasket = new ArrayList();

    public void add(T fruit) {
        fruitBasket.add(fruit);
    }

    public T get() {
        if (fruitBasket.size() > 0) {
            Object o = fruitBasket.get(0);
            fruitBasket.remove(0);
            return (T) o;
        } else {
            return null;
        }
    }

    public boolean canGetMore() {
        return !fruitBasket.isEmpty();
    }

}
