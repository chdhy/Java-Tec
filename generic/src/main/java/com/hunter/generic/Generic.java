package com.hunter.generic;

import com.hunter.generic.bean.*;

import java.util.ArrayList;
import java.util.List;

public class Generic {

    public static void main(String[] args) {
        Generic generic = new Generic();
        generic.genericBasket();
        generic.genericClass();
        generic.genericList();
    }

    public void genericBasket() {
        GenericBasket<Apple> apples = new GenericBasket<>();
        apples.add(new RedApple());
        apples.add(new GreenApple());
        apples.add(new Apple());
        // magic
//      apples.add(new Banana()); //  compile error - Required type:Apple Provided: Banana

        GenericBasket<Banana> bananas = new GenericBasket<>();
        bananas.add(new Banana());

        Princess princess = new Princess();
        while (apples.canGetMore()) {
            princess.eatApple(apples.get()); // elegant: no more force transfer,no more instance
        }
        while (bananas.canGetMore()) {
            princess.eatBanana(bananas.get());
        }
    }

    public void genericClass() {
        GenericBasket<Apple> apples = new GenericBasket<>();
        GenericBasket<Banana> bananas = new GenericBasket<>();
        System.out.println(apples.getClass());
        System.out.println(bananas.getClass());
    }

    public void genericList() {
        List<Apple> apples = new ArrayList<>();
        apples.add(new RedApple());
        apples.add(new GreenApple());
        apples.add(new Apple());

        List<Banana> bananas = new ArrayList<>();
        bananas.add(new Banana());

        Princess princess = new Princess();
        while (!apples.isEmpty()) {
            princess.eatApple(apples.get(0));
            apples.remove(0);
        }
        while (!bananas.isEmpty()) {
            princess.eatBanana(bananas.get(0));
            bananas.remove(0);
        }
    }

    public void genericDeepIn() {
//        List<Fruit> fruits = new ArrayList<Apple>();
//        List<Apple> fruits = new ArrayList<Fruit>();

        List<Fruit> fruits = new ArrayList<>();
        List<Apple> apples = new ArrayList<>();

//        noneGenericMethod(apples);
        genericMethod(apples);

//        noneGenericMethod2(fruits);
        genericMethod2(fruits);

        ArrayList listRawType = new ArrayList();
        listRawType.add(new Fruit());
        listRawType.add(new Apple());
        listRawType.add(new Object());


        ArrayList<?> listWildcard = new ArrayList<Fruit>();
//        listWildcard.add(new Fruit());
        listWildcard.add(null);
//        listWildcard.add(new Object());

        ArrayList<Fruit> fruitList = new ArrayList<>();
        fruitList.add(new Fruit());
        fruitList.add(new Banana());
        listRawType = fruitList;
        listRawType.add(new PoisonApple());
    }

    public void listObject(List<Object> list){
    }

    public void listRaw(List list){
    }

    public void listExtendsObject(List<? extends Object> list){
    }

    public void listWildCard(List<?> list){
    }

    public void noneGenericMethod(List<Fruit> fruits) {
        for (Fruit fruit : fruits) {
            // xxx
        }
    }

    public <T extends Fruit> void genericMethod(List<T> fruits) {
        // 添加的代码
//        fruits.add(new Fruit());

        for (Fruit fruit : fruits) {
            // xxx
        }
    }

    public List<Apple> noneGenericMethod2(List<Apple> apples) {
        ArrayList<Apple> otherApples = new ArrayList<>();
        // 省略代码
        apples.addAll(otherApples);
        return otherApples;
    }

    public List<Apple> genericMethod2(List<? super Apple> apples) {
        ArrayList<Apple> otherApples = new ArrayList<>();
        // 省略代码
        apples.addAll(otherApples);
        return otherApples;
    }

}
