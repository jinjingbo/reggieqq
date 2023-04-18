package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/10 22:39
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
