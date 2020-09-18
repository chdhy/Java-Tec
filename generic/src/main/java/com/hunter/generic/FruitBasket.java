package com.hunter.generic;

import com.hunter.generic.bean.Fruit;

import java.util.ArrayList;
import java.util.List;

public class FruitBasket {

    private List fruitBasket = new ArrayList();

    public void addFruit(Fruit fruit) {
        fruitBasket.add(fruit);
    }

    public Fruit getFruit() {
        if (fruitBasket.size() > 0) {
            Object o = fruitBasket.get(0);
            fruitBasket.remove(0);
            return (Fruit) o;
        } else {
            return null;
        }
    }

    public boolean canGetFruit() {
        return !fruitBasket.isEmpty();
    }

}
