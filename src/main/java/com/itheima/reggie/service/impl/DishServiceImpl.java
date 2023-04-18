package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/12 20:19
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //1.保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //dishFlavorService.saveBatch(dishDto.getFlavors());

        //根据菜品id更新保存对应的dish_flavor：一个菜品对应多个口味
        Long dishId = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        ///在dish_flavor中添加时，dishFlavorService.saveBatch(dishDto.getFlavors())
        // 就只是放进去了name和value，而在之前是没有dishId的，在this.save(dishDto);之后才有了对应的dishid
        //所以需要把dishid与口味建立联系
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    //@Transactional
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);//dish表中没有口味信息，需要从dishflavor中查看口味再复制到dto，传递dto数据。。。
        //拷贝，查找，赋值再上传
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //条件查找
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        //根据查询id条件，查找口味..dishflavor表
        //注意一个id对应有多条口味信息，list收集
        List<DishFlavor> flavors=dishFlavorService.list(queryWrapper);
        //赋值到dto中
        dishDto.setFlavors(flavors);

        return dishDto;

    }

    @Override
    @Transactional//需要更新两张表信息
    public void updateWithFlavor(DishDto dishDto) {
        //1.根据dishdto更新dish表的基本信息
        //2.还要更新dishflavor对应的菜品口味信息    使用删除再赋值的方式进行更新操作
        this.updateById(dishDto);//1.

        //mv根据dish.id筛选dishflavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        //删除
        dishFlavorService.remove(queryWrapper);

        //赋值
        List<DishFlavor> flavors = dishDto.getFlavors();//修改后的口味信息
        //dishFlavorService.saveBatch(flavors);//直接这样赋值，在表中缺少口味对应的菜品id
        //flavors中每一条的DishFlavor信息都要set对应的DishId.
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);

    }
}
