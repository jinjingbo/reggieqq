package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;

import java.util.List;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/17 14:54
 */
@Data//Dto需要加注解
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;//多条套餐菜品的关系

    private String categoryName;
}
