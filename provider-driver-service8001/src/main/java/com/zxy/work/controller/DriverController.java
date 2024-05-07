package com.zxy.work.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
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
    public ApiResponse<String> deleteDriver(@RequestParam("mobile") String mobile) throws MyException {
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
                : ApiResponse.error(600, "司机查询失败");
    }


    /**
     * 根据手机号获取司机信息
     * @param id    传来的司机id
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById")
    public ApiResponse<Object> getById(@RequestParam("id") Long id) throws MyException {
        log.info("通过电话获取司机服务提供者：" + id);
        //参数校验
        Driver driver = driverService.selectById(id);
        return driver != null
                ? ApiResponse.success(driver)
                : ApiResponse.error(600, "司机查询失败");
    }


    /**
     * 用户登录
     * @param driver 传来的用于登录的司机json
     * @return 登录结果
     */
    @SaIgnore
    @PostMapping("/login")
    public ApiResponse<String> login(@RequestBody Driver driver) throws MyException {
        log.info( "用户登录服务提供者：mobile={}", driver.getMobile() );
        if (!driverService.login(driver)){
            log.warn("手机号={}登录失败，原因密码输入错误", driver.getMobile());
            return ApiResponse.error(600, "登录失败，密码输入错误");
        }
        //进行必要操作
        StpUtil.setStpLogic(new StpLogic("driver"));
        StpUtil.login(driver.getMobile());
        String tokenValue = StpUtil.getTokenValue();
        return ApiResponse.success(tokenValue);
    }


    /**
     * 判断司机登录状态，直接调用即可
     */
    @SaIgnore
    @GetMapping("/checkLogin")
    public ApiResponse<String> checkLogin(){
        return StpUtil.isLogin()
                ? ApiResponse.success("已登录")
                : ApiResponse.error(600, "未登录");
    }

    /**
     * 退出登录
     * @param mobile 登录时使用的手机号
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestParam("mobile") String mobile){
        log.info( "司机退出登录服务提供者mobile={}", mobile );
        //退出登录需要的处理逻辑
        //1.使token失效
        StpUtil.logout(mobile);
        //2.从redis中移除用户所有信息(消息队列)

        return ApiResponse.success("退出登录成功");
    }


    /**
     * 更新司机密码
     * @param mobile 传来的手机号
     * @param oldPassword 传来的旧密码
     * @param newPassword 传来的新密码
     * @return 更新结果
     */
    @SaCheckRole(value = "driver", mode = SaMode.OR)
    @PutMapping("/update/password")
    public ApiResponse<String> updatePassword(
            @RequestParam("mobile") String mobile,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword
    ) throws MyException {
        return driverService.updatePassword(mobile, oldPassword, newPassword) == 1
                ? ApiResponse.success("密码更新成功")
                : ApiResponse.error(600, "密码更新失败");
    }


}
