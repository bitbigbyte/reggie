package com.xiaoke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoke.common.CustomException;
import com.xiaoke.common.R;
import com.xiaoke.entity.User;
import com.xiaoke.service.UserService;
import com.xiaoke.utils.EmailUtils;
import com.xiaoke.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailUtils emailUtils;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession  session){
        //获取手机号
        String phone=user.getPhone();
        if(phone.length()!=11) {
           throw new CustomException("手机号码不正确");
        }
        //生成验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("生成的验证码为：{}",code);
        //发送邮件
        //emailUtils.sendMessage("3458298931@qq.com","登录验证","你好，你收到的验证码为："+code+"，请妥善保管。");
        //发送短信
        //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
        //将生成的验证码保存
        //session.setAttribute(phone,code);

        //把生成的代码存储到Redis中，并且设置有效期为5分钟
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);

        return R.success("验证码发送成功");

    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map<String,String> map, HttpSession  session){
        log.info(map.toString());
        String userPhone=map.get("phone");
        String userCode=map.get("code");
        if(Objects.equals(redisTemplate.opsForValue().get(userPhone), userCode)){
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,userPhone);
            User user=userService.getOne(queryWrapper);
            if(user==null){
                user=new User();
                user.setPhone(userPhone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            redisTemplate.delete(userPhone);

            return R.success(user);
        }
        return R.error("验证码错误");
    }
}
