package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/16 11:08
 */
@Mapper///dao层注解注意
public interface DishFlavorMapper extends BaseMapper<DishFlavor> {
}
