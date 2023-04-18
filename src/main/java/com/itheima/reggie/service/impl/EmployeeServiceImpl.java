package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/10 22:41
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements com.itheima.reggie.service.EmployeeService {
}
