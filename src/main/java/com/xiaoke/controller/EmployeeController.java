package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoke.common.R;
import com.xiaoke.entity.Employee;
import com.xiaoke.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * TODO:员工管理
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // TODO:员工登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的密码password进行md5加密处理
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        //2.根据页面提交的用户名username查询数据库
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //3.如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("登录失败");
        }
        //4.密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("登录失败");
        }
        //5.查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
            return R.error("账号已禁用");
        }
        //6.登陆成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    // TODO:员工退出
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除Session中保存的当前登录员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    // TODO:新增员工
    @PostMapping
    public R<String> save(@RequestBody Employee employee){
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        log.info("新增员工，员工信息：{}",employee);
        return R.success("新增员工成功");
    }

    // TODO:员工信息分页查询
    @GetMapping("/page")
    public R<Page<Employee>> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page<Employee> pageInfo = new Page<>(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByAsc(Employee::getUsername);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    // TODO:根据id修改员工信息
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        log.info("修改员工信息，员工信息：{}",employee.toString());
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    // TODO:根据id查询员工信息
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("查询员工信息");
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }
}