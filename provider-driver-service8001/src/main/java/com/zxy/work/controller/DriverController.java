package com.zxy.work.controller;

import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.Driver;
import com.zxy.work.entities.MyException;
import com.zxy.work.service.DriverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("/driver")
public class DriverController {

    @Resource
    private DriverService driverService;


    /**
     * 注册司机（前提身份是用户）
     * @param driver 传来的用户信息json
     * @return 注册结果
     */
    @PostMapping("/update/register")
    public ApiResponse<String> registerDriver(@RequestBody Driver driver) throws MyException {
        log.info("注册成为司机服务提供者：" + driver.getMobile());
        //参数校验
        return driverService.create(driver) == 1
                ? ApiResponse.success("司机注册成功")
                : ApiResponse.error(600, "司机注册失败");
    }


    /**
     * 注销司机，逻辑删除
     * @param mobile 传来的用户手机号
     * @return  注销结果
     */
    @DeleteMapping("/update/delete")
    public ApiResponse<String> deleteDriver(@RequestParam String mobile) throws MyException {
        log.info("注销司机服务提供者：" + mobile);
        //参数校验

        return driverService.delete(mobile) == 1
                ? ApiResponse.success("司机注销成功")
                : ApiResponse.error(600, "司机注销失败");
    }


    /**
     * 更新司机信息
     * @param driver 传来的信息json
     * @return  更新的信息结果
     */
    @PutMapping("/update/message")
    public ApiResponse<String> updateDriver(@RequestBody Driver driver) throws MyException {
        log.info("更新司机信息服务提供者：" + driver);
        //参数校验
        return driverService.update(driver) == 1
                ? ApiResponse.success("司机更新信息成功")
                : ApiResponse.error(600, "司机更新信息失败");
    }


    /**
     * 根据手机号获取司机信息
     * @param mobile    传来的司机手机号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getByMobile")
    public ApiResponse<Object> getDriverByMobile(@RequestParam("mobile") String mobile) throws MyException {
        log.info("通过电话获取司机服务提供者：" + mobile);
        //参数校验
        Driver driver = driverService.selectByMobile(mobile);
        return driver != null
                ? ApiResponse.success(driver)
                : ApiResponse.error(600, "司机更新查询失败");
    }

}
