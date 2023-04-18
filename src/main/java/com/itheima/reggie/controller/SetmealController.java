package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description：套餐分类
 * @author： jinji
 * @create： 2023/4/17 13:49
 */
@RequestMapping("/setmeal")
@RestController
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    //在SetmealServiceImpl实现saveWithDish方法：新增套餐，同时要保持与菜品的关联关系
    /**
     * 新增套餐
     * @param setmealDto
     * @return
     *
     * Request URL: http://localhost:8080/setmeal
     * Request Method: POST
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){//套餐需要保存多条对应菜品信息，使用dto

        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * Setmeal SetmealDto(有categoryName)
     *
     * 根据name查询对应的Setmeal，，而需要的是SetmealDto
     *
     * SetmealDto拷贝Setmeal，不同的信息在records中额外处理
     *
     * records 需要的内容是 List<SetmealDto>。。而此时有List<Setmeal>
     *
     * 每个遍历，SetmealDto再拷贝Setmeal，同时把categoryName放进去（根据categoryService）
     *
     * dtoPage再保存records（ List<SetmealDto>）
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //用Setmeal接收，返回SetmealDto,这样可以在页面展示菜品名称、categoryName
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //按照名字先查询再复制
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件，根据name进行like模糊查询
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo,queryWrapper);//根据查询条件进行查询

        //拷贝、、、
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");


        //处理新的records信息，，
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {
            //需要得到的对象是List<SetmealDto>，而SetmealDto也需要拷贝一份旧数据。
            //同时别忘了将   setmealDto.setCategoryName(categoryName);
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                ///菜品餐厨的名字
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     * http://localhost:8080/setmeal?ids=1647871674302676993
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("套餐数据删除成功");
    }

    //停售该套餐
    @PostMapping("/status/{status}")
    public R<String> sale(@PathVariable int status,
                          String[] ids){
        //根据ids找到每个对应套餐id的实体类，修改状态再更新保存即可
        for(String id: ids){

            Setmeal setmeal=setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }

    //编辑修改套餐功能
    //Request URL: http://localhost:8080/setmeal/1415580119015145474
    //根据Id查询套餐信息
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){

        SetmealDto setmealDto=setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }


    //修改套餐
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }


    ///list 套餐查找@@。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        log.info("setmeal:{}", setmeal);
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(setmeal.getName()), Setmeal::getName, setmeal.getName());
        queryWrapper.eq(null != setmeal.getCategoryId(), Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(null != setmeal.getStatus(), Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        return R.success(setmealService.list(queryWrapper));
    }

}
