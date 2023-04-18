package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/12 20:18
 */
public interface CategoryService extends IService<Category> {

    public void remove(Long id);

}
