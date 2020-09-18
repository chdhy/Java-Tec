package com.hunter.generic;

import com.hunter.generic.bean.Apple;
import com.hunter.generic.bean.Banana;
import com.hunter.generic.bean.Goods;
import com.hunter.generic.bean.Poison;

public class Princess {

    public void eat(Goods goods) {
        if (goods instanceof Poison) {
            throw new RuntimeException("die");
        }
        System.out.println("eat " + goods.toString());
    }

    public void eatApple(Apple apple){
        System.out.println("eat " + apple);
    }

    public void eatBanana(Banana banana){
        System.out.println("eat " + banana);
    }

}
