package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @description：补充dish信息，返回给页面
 * @author： jinji
 * @create： 2023/4/16 14:32
 */
@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();////多个菜品口味，这个在dish中没有

    private String categoryName;///

    private Integer copies;
}
