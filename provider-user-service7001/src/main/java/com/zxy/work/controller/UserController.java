package com.zxy.work.controller;

import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.entities.User;

import com.zxy.work.service.UserService;
import com.zxy.work.util.PasswordEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 注册用户
     * @param user 传来的用户信息json
     * @return 注册结果:注册成功时返回注册的账号
     */
    @PostMapping("/update/register")
    public CommonResult registerUser(@RequestBody User user){

        log.info( "********用户注册服务7001：*********" );

        //检查该手机号否已注册
        User registeredUser = userService.selectByMobile( user.getMobile() );
        if ( registeredUser != null ) return new CommonResult<>( StatusCode.FAILURE,"您当前手机号已经注册过了:" );


        //开始注册
        int result = userService.create( user );

        if (result > 0){
            log.info( user + "注册成功" );
            return new CommonResult<>( StatusCode.SUCCESS,"注册成功" );
        }

         log.info( user + "注册失败" );
         return new CommonResult<>( StatusCode.FAILURE,"注册失败" );
    }


    /**
     * 注销用户，逻辑删除
     * @param user 传来的用户信息json
     * @return  注销结果
     */
    @DeleteMapping("/update/delete")
    public CommonResult deleteUser(@RequestBody User user){

        log.info( "********注销服务7001：*********" );

        int result = userService.delete(user);

        if (result > 0){
            log.info( user + "注销成功" );
            return new CommonResult<>( StatusCode.SUCCESS,user.getMobile() );
        }

        log.info( user + "注销失败" );
        return new CommonResult<>( StatusCode.FAILURE,"注销失败" );
    }


    /**
     * 根据手机号获取用户信息，对密码信息脱敏
     * @param mobile    传来的用户手机号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{mobile}")
    public CommonResult getUserByMobile(@PathVariable("mobile")String mobile){

        log.info( "********查询服务7001：*********" );

        User user = userService.selectByMobile(mobile);
        if (user == null){
            log.info( "查找失败" );
            return new CommonResult<>(StatusCode.FAILURE,"查找失败");
        }

         log.info( user + "查找成功" );
         //用户信息脱敏
         user.setPassword("**********");
         return new CommonResult<>(StatusCode.SUCCESS,user);
    }


    /**
     * 用户登录
     * @param user 传来的用于登录的用户json
     * @return 登录结果：登录成功时将用户信息返回
     */
    @PostMapping("/login")
    public CommonResult login(@RequestBody User user){

        log.info( "********登录服务7001：*********" );

        //获取输入的密码
        String inputPassword = user.getPassword();

        //获取该账号的加密密码
        User resultUser = userService.selectByMobile( user.getMobile() );
        String encodedPassword = resultUser.getPassword();

        //匹配
        boolean matches = PasswordEncoder.matches(inputPassword, encodedPassword);
        if (!matches){
            log.info(user.getMobile() + "登录失败");
            return new CommonResult<>(StatusCode.FAILURE,"密码不正确");
        }

        log.info(user.getMobile() + "登录成功");
        resultUser.setPassword("**************");
        return new CommonResult<>(StatusCode.SUCCESS,resultUser);
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user 传来的用户信息json
     * @return  更新的用户信息结果
     */
    @PutMapping("/update/message")
    public CommonResult updateUser(@RequestBody User user){

        log.info( "********更新信息服务7001：*********" );

        int result = userService.update(user);

        if (result > 0){
            log.info(user + "信息更新成功");
            return new CommonResult(StatusCode.SUCCESS,user);
        }

         log.info(user + "信息更新失败");
         return new CommonResult(StatusCode.FAILURE,"修改失败");
    }


    /**
     * 更新用户密码
     * @param requestMapper 传来的json
     * @return  更新结果
     */
    @PutMapping("/update/password")
    public CommonResult updatePassword(@RequestBody Map<String,Object> requestMapper){
        String mobile = (String) requestMapper.get("mobile");
        String password = (String) requestMapper.get("password");
        String newPassword = (String) requestMapper.get("newPassword");

        log.info( "********更新密码服务7001：*********" );

        User user = new User(mobile,password);

        int result = userService.updatePassword(user,newPassword);

        if (result > 0){
            log.info(user + "密码更新成功");
            user.setPassword("**************");
            return new CommonResult(StatusCode.SUCCESS,user);
        }

        log.info(user + "密码更新失败");
        return new CommonResult(StatusCode.FAILURE,"修改失败，原密码错误");
    }



}
