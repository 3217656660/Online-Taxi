package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;
import com.zxy.work.service.DriverServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


@RestController
@Slf4j
@RequestMapping("/taxi/driver")
@SaCheckLogin
public class DriverServiceClientController {

    @Resource
    private DriverServiceClient driverServiceClient;


    /**
     * 司机认证（一般在用户第一次点击接单界面完善）
     * @param driver 传来的司机json
     * @return 认证结果
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody Driver driver){
        log.info("注册成为司机：" + driver.getMobile());
        try{
            return driverServiceClient.register(driver);
        }catch (Exception e){
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 逻辑删除司机信息，一般用于账号注销时将对应司机信息也删除
     * @param mobile 传来的司机手机号
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam("mobile") String mobile){
        log.info("注销司机：" + mobile);
        try{
            return driverServiceClient.delete(mobile);
        }catch (Exception e){
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 更新司机信息
     * @param driver 传来的司机信息
     * @return 更新结果：成功时会将更新的司机对象也返回
     */
    @PutMapping("/message")
    public ApiResponse<String> updateMessage(@RequestBody Driver driver){
        log.info("更新司机信息：" + driver);
        try{
            return driverServiceClient.update(driver);
        }catch (Exception e){
            return ApiResponse.error(600, e.getMessage());
        }
    }


    /**
     * 通过司机电话获取司机专属信息
     * @param mobile 传来的司机电话
     * @return  查询结果
     */
    @GetMapping("/getByMobile")
    public ApiResponse<Object> getByMobile(@RequestParam("mobile")String mobile){
        log.info("通过电话获取司机：" + mobile);
        try{
            return driverServiceClient.getByMobile(mobile);
        }catch (Exception e){
            return ApiResponse.error(600, e.getMessage());
        }
    }
}
