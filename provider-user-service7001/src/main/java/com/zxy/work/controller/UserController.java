package com.zxy.work.controller;


import com.zxy.work.entities.User;
import com.zxy.work.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> registerUser(@RequestBody User user){
        log.info( "********用户注册服务7001：*********" );
        //参数校验

        return ResponseEntity.ok( userService.create(user) );
    }


    /**
     * 注销用户，逻辑删除
     * @param user 传来的用户信息json
     * @return  注销结果
     */
    @DeleteMapping("/update/delete")
    public ResponseEntity<Object>  deleteUser(@RequestBody User user){
        //参数校验
        log.info( "********注销服务7001：*********" );

        return ResponseEntity.ok( userService.delete(user) );
    }


    /**
     * 根据手机号获取用户信息，对密码信息脱敏
     * @param mobile    传来的用户手机号
     * @return  获取的结果以及数据
     */
    @GetMapping("/get/{mobile}")
    public ResponseEntity<Object>  getUserByMobile(@PathVariable("mobile")String mobile){
        log.info( "********查询服务7001：*********" );
        //参数校验

        return ResponseEntity.ok( userService.selectByMobile(mobile) );
    }


    /**
     * 用户登录
     * @param user 传来的用于登录的用户json
     * @return 登录结果：登录成功时将用户信息返回
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User user){
        log.info( "********登录服务7001：*********" );
        //参数校验

        return ResponseEntity.ok( userService.login(user) );
    }


    /**
     * 退出登录
     * @param user  用户对象
     */
    @PostMapping("/logout")
    public void logout(@RequestBody User user){
        log.info( "********退出登录服务7001：*********" );
        //退出登录需要的处理逻辑
    }


    /**
     * 更新用户信息（不包括密码）
     * @param user 传来的用户信息json
     * @return  更新的用户信息结果
     */
    @PutMapping("/update/message")
    public ResponseEntity<Object> updateUser(@RequestBody User user){
        log.info( "********更新信息服务7001：*********" );
        //参数校验

         return ResponseEntity.ok( userService.update(user) );
    }


    /**
     * 更新用户密码
     * @param requestMapper 传来的json
     * @return  更新结果
     */
    @PutMapping("/update/password")
    public ResponseEntity<Object> updatePassword(@RequestBody Map<String,Object> requestMapper){
        Integer id = (Integer) requestMapper.get("id");
        String password = (String) requestMapper.get("password");
        String newPassword = (String) requestMapper.get("newPassword");
        User user = new User(id,password);
        //检验

        log.info( "********更新密码服务7001：*********" );
        return ResponseEntity.ok( userService.updatePassword(user ,newPassword) );
    }


    /**
     * 通过ID查找用户
     * @param id 传来的ID
     * @return  查询结果
     */
    @GetMapping("/getById/{id}")
    public ResponseEntity<Object> getById(@PathVariable("id")Integer id)  {
        return ResponseEntity.ok( userService.selectById(id) );
    }

}
