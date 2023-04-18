package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/16 11:08
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    //涉及到两张表，表之间用id联系，，同时注意前端需要的参数，需要补充dto
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //直接返回dish只得到了菜品id，需要的菜品名称
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询...........选择器选择筛选的是dish数据。需要的是带有菜品名称的dishdto数据
        dishService.page(pageInfo,queryWrapper);

        ////需要返回的是dto的数据，但是现在只有dishsever的方法进行处理，刷选，使用dishname进行删选。
        //对象拷贝
        //dishDtoPage复制pageInfo的内容，除了records。两个都是page类型的
        //records里面存的就是要修改的内容
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");//除了page.records全拷贝

        List<Dish> records = pageInfo.getRecords();//拿到旧的records

        //传递的records需要自己去处理。。。注意是列表形式
        List<DishDto> list = records.stream().map((item) -> {
            //每一条disdto都要复制旧的数据，再把菜品的名称放进disdto中，封装
            DishDto dishDto = new DishDto();

            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//找到了分类id
            //目的是根据菜品分类id查询分类对象名称
            Category category = categoryService.getById(categoryId);

            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    //修改套餐，，根据id访问跳转到新增页面，再更新保存修改后的RequestBody实体
    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return R.success("新增菜品成功");

    }

    //停售起售菜品
    @PostMapping("/status/{status}")
    public R<String> sale(@PathVariable int status,
                          String[] ids){
        //根据ids找到每个对应菜品id的Dish实体类，修改状态再更新保存即可
        for(String id: ids){
            Dish dish = dishService.getById(id);
            dish.setStatus(status);
            dishService.updateById(dish);
        }
        return R.success("修改成功");
    }

    //删除菜品
    @DeleteMapping
    public R<String> delete(String[] ids){
        for (String id:ids) {
            dishService.removeById(id);
        }
        return R.success("删除成功");
    }

    //新增套餐页面添加菜品时展示多种菜品。。request List<DishDto>
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        log.info("dish:{}",dish);
        //条件构造器
        //
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()), Dish::getName, dish.getName());//根据名字查找
        queryWrapper.eq(null != dish.getCategoryId(), Dish::getCategoryId, dish.getCategoryId());//为什么还有这一条?
        //

        //添加条件，查询状态为1（起售状态）的菜品
        queryWrapper.eq(Dish::getStatus,1);

        queryWrapper.orderByDesc(Dish::getUpdateTime);

        List<Dish> dishs = dishService.list(queryWrapper);//为什么用dishDto返回？？？？？！@@@

        List<DishDto> dishDtos = dishs.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);//拷贝

            //dishDto的categoryName，List<DishFlavor> flavors都需要根据dish加入
            Category category = categoryService.getById(item.getCategoryId());//下面这一段干什么用？？？@@@@@@@
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }

            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, item.getId());

            dishDto.setFlavors(dishFlavorService.list(wrapper));
            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtos);

    }
    /*@GetMapping("/list")
        public R<List<Dish>> list(Dish dish){
            //构造查询条件
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(dish.getCategoryId() != null ,Dish::getCategoryId,dish.getCategoryId());
            //添加条件，查询状态为1（起售状态）的菜品
            queryWrapper.eq(Dish::getStatus,1);

            //添加排序条件
            queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

            List<Dish> list = dishService.list(queryWrapper);//

            return R.success(list);
        }*/




}
