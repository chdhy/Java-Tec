package com.hunter.generic;

import com.hunter.generic.bean.*;

import java.util.ArrayList;
import java.util.List;

public class NoneGeneric {

    public static void main(String[] args) {
        NoneGeneric noneGeneric = new NoneGeneric();
//        noneGeneric.commonList();
//        noneGeneric.commonListWithPoison();
//        noneGeneric.customList();
        noneGeneric.customListWithConcreteEat();
    }

    public void commonList() {
        List basket = new ArrayList();

        // add by seven dwarfs
        basket.add(new RedApple());
        basket.add(new GreenApple());
        basket.add(new Banana());

        // princess eat goods
        Princess princess = new Princess();
        for (Object o : basket) {
            princess.eat((Goods) o);  // force transfer
        }
    }

    public void commonListWithPoison() {
        List basket = new ArrayList();

        // add by seven dwarfs
        basket.add(new RedApple());
        basket.add(new GreenApple());
        basket.add(new Banana());

        // add by witch
        basket.add(new PoisonApple()); // die

        // princess eat goods
        Princess princess = new Princess();
        for (Object o : basket) {
            princess.eat((Goods) o);  // force transfer
        }
    }

    public void customList() {
        FruitBasket fruitBasket = new FruitBasket();

        fruitBasket.addFruit(new Apple());
        fruitBasket.addFruit(new Apple());
        fruitBasket.addFruit(new Banana());

        Princess princess = new Princess();
        while (fruitBasket.canGetFruit()) {
            // princess eat
            princess.eat(fruitBasket.getFruit());
        }
    }

    public void customListWithConcreteEat() {
        FruitBasket fruitBasket = new FruitBasket();

        fruitBasket.addFruit(new Apple());
        fruitBasket.addFruit(new Apple());
        fruitBasket.addFruit(new Banana());

        Princess princess = new Princess();
        while (fruitBasket.canGetFruit()) {
            // princess eat
            Fruit fruit = fruitBasket.getFruit();
            if (fruit instanceof Banana) {
                princess.eatBanana((Banana) fruit);
            }
            if (fruit instanceof Apple) {
                princess.eatApple((Apple) fruit);
            }
        }
    }

}
