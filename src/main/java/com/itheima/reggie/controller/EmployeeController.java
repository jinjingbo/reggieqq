package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @description：TODO
 * @author： jinji
 * @create： 2023/4/10 22:46
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    //员工登录，1.是否符合登录密码等规则，2.员工信息id缓存
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){//json传入

        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库,mybatisPlus用法
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);//唯一标识

        //3、如果没有查询到则返回登录失败结果
        if(emp == null){
            return R.error("账号未存在");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)) return R.error("密码不正确");

        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()!=1) return R.error("状态禁用");

        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());


        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理Session中保存的当前登录员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee.toString());
        //在数据库添加员工
        //此时上传的employee缺少一些数据。。。补充 自定义
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());
        //
        ////获得当前登录用户的id,修改者的id
        //Long empId=(Long) request.getSession().getAttribute("employee");
        //
        //employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);
        //公共字段默认填充。。。。。。。。实体中申明，处理器中设置更新策略

        //数据库更新
        employeeService.save(employee);

        return R.success("新增员工成功");
    }


    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}" ,page,pageSize,name);

        //构造分页构造器
        Page pageInfo = new Page(page,pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //两个冒号是引用方法，可以理解为表的字段名
        //like 模糊查询 按照数据库中的name查询

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }


    /**
     * 根据id修改员工信息,禁用启用等
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        //
        //employee.setUpdateTime(LocalDateTime.now());
        //Long empId = (Long)request.getSession().getAttribute("employee");//此时登录修改者
        ////employee.setCreateUser(empId);
        //employee.setUpdateUser(empId);

        //更新数据库
        employeeService.updateById(employee);

        return R.success("员工信息修改成功");

    }

    //编辑员工信息时，回显，需要根据id返回之前的数据
    /**
     * 根据id查询员工信息....
     * http://localhost:8080/employee/1645615254588944386   id从url中获取，？连接可以使用
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){//url中读取id
        log.info("根据id查询员工信息...");
        Employee employee = employeeService.getById(id);
        if(employee != null){
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息");
    }


}
