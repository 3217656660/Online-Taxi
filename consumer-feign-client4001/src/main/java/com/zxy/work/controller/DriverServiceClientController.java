package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zxy.work.entities.Driver;
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
    public ResponseEntity<String> register(@Valid @RequestBody Driver driver){
        log.info("注册成为司机：" + driver.getMobile());
       return driverServiceClient.register(driver);
    }


    /**
     * 逻辑删除司机信息，一般用于账号注销时将对应司机信息也删除
     * @param driver 传来的司机json
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@Valid @RequestBody Driver driver){
        log.info("注销司机：" + driver.getId());
        return driverServiceClient.delete(driver);
    }


    /**
     * 通过司机电话获取司机专属信息
     * @param mobile 传来的司机电话
     * @return  查询结果
     */
    @GetMapping("/getByMobile/{mobile}")
    public ResponseEntity<String> getByMobile(@PathVariable("mobile")String mobile){
        log.info("通过电话获取司机：" + mobile);
        return driverServiceClient.getByMobile(mobile);
    }


    /**
     * 更新司机信息
     * @param driver 传来的司机信息
     * @return 更新结果：成功时会将更新的司机对象也返回
     */
    @PutMapping("/message")
    public ResponseEntity<String> updateMessage(@RequestBody Driver driver){
        log.info("更新司机信息：" + driver);
        return driverServiceClient.update(driver);
    }


    /**
     * 通过id查找司机
     * @param id 司机id
     * @return  查找结果
     */
    @GetMapping("/getById/{id}")
    ResponseEntity<String> getById(@PathVariable Integer id){
        log.info("通过id获取司机：" + id);
        return driverServiceClient.getById(id);
    }


}
