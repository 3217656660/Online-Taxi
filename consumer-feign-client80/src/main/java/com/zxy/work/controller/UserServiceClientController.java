package com.zxy.work.controller;


import com.zxy.work.entities.CommonResult;
import com.zxy.work.entities.StatusCode;
import com.zxy.work.entities.User;
import com.zxy.work.service.UserServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping("/consumer/user/")
public class UserServiceClientController {

    @Resource
    private UserServiceClient userServiceClient;


    @PostMapping("/update/register")
    public CommonResult register(@RequestBody User user){
        Map<String,Object> map = userServiceClient.register(user);
        Object data = map.get("data");

        if ( data == null ) return new CommonResult<>( StatusCode.FAILURE, "您当前手机号已经注册过了"  );

        return new CommonResult<>( StatusCode.SUCCESS, data );
    }


    @DeleteMapping("/update/delete")
    public CommonResult delete(@RequestBody User user){
        Map<String,Object> map = userServiceClient.delete(user);
        Object data = map.get("data");

        if ( data == null ) return new CommonResult<>( StatusCode.FAILURE, "注销失败"  );

        return new CommonResult<>( StatusCode.SUCCESS, data + "注销成功" );
    }


    @GetMapping("/get/{mobile}")
    public CommonResult get(@PathVariable("mobile")String mobile){
        Map<String,Object> map = userServiceClient.get(mobile);
        Object data = map.get("data");

        if ( data == null ) return new CommonResult<>( StatusCode.FAILURE, "查找失败" );

        return new CommonResult<>( StatusCode.SUCCESS, data );
    }


    @PostMapping("/login")
    public CommonResult login(@RequestBody User user){
        Map<String,Object> map = userServiceClient.delete(user);
        Object data = map.get("data");
        log.info("data" + data);

        if ( Objects.equals(data,"账号不存在") )
            return new CommonResult<>( StatusCode.FAILURE, "账号不存在"  );
        else if ( Objects.equals(data,"密码不正确") )
            return new CommonResult<>( StatusCode.FAILURE, "密码不正确"  );
        else
            return new CommonResult<>( StatusCode.SUCCESS, data );
    }


    @PutMapping("/update/message")
    public CommonResult updateMessage(@RequestBody User user){
        Map<String,Object> map = userServiceClient.updateMessage(user);
        Object data = map.get("data");

        if ( data == null ) return new CommonResult<>( StatusCode.FAILURE, "信息更新失败" );

        return new CommonResult<>( StatusCode.SUCCESS, data );
    }


    @PutMapping("/update/password")
    public CommonResult updatePassword(@RequestBody Map<String,Object> requestMapper){
        Map<String,Object> map = userServiceClient.updatePassword(requestMapper);
        Object data = map.get("data");

        if ( data == null ) return new CommonResult<>( StatusCode.FAILURE, "修改失败，原密码错误" );

        return new CommonResult<>( StatusCode.SUCCESS, data );
    }



}
