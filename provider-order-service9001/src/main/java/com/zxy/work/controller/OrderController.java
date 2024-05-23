package com.zxy.work.controller;

import com.github.pagehelper.PageInfo;
import com.zxy.work.entities.ApiResponse;
import com.zxy.work.entities.MyException;
import com.zxy.work.entities.Order;
import com.zxy.work.entities.PageResult;
import com.zxy.work.service.OrderService;
import com.zxy.work.util.MyNotify;
import com.zxy.work.util.cache.CacheUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @Resource
    private KafkaTemplate<String ,String> kafkaTemplate;

    @Resource
    private CacheUtil redisUtil;


    /**
     * kafka topic name
     */
    private static final String TOPIC_NAME = "main";

    /**
     * 创建订单后处理消息key
     */
    private static final String MQ_CREATE_ORDER_KEY = "createOrder";

    /**
     * 取消订单后处理消息key
     */
    private static final String MQ_CANCEL_ORDER_KEY = "cancelOrder";

    /**
     * 司机接单后处理消息key
     */
    private static final String MQ_ACCEPT_ORDER_KEY = "acceptOrder";

    /**
     * 司机到达指定开始地点后处理消息key
     */
    private static final String MQ_ARRIVE_START_ADDRESS_KEY = "arriverStartAddress";

    /**
     * 开始驾驶到终点后处理消息key
     */
    private static final String MQ_TO_END_ADDRESS_KEY = "toEndAddress";

    /**
     * 到终点后处理消息key
     */
    private static final String MQ_ARRIVE_END_ADDRESS_KEY = "arriveEndAddress";


    /**
     * 用于不需要指定顺序的消息随机分区
     */
    private static final Random random = new Random();


    /**
     * 创建订单
     * @param order 传来的用户信息json
     * @return 创建结果
     */
    @Deprecated
    @PostMapping("/update/create")
    public ApiResponse<String> createOrder(@RequestBody Order order) throws MyException {
        log.info("创建订单服务提供者:" + order);
        return orderService.create(order) == 1
                ? ApiResponse.success("订单创建成功")
                : ApiResponse.error(600, "订单创建失败");
    }


    /**
     * 用户删除订单，逻辑删除
     * @param id 传来的订单id
     * @return  删除结果
     */
    @DeleteMapping("/update/deleteByUser")
    public ApiResponse<String> deleteByUser(@RequestParam("id") long id) throws MyException {
        log.info("取消订单服务提供者:" + "id=" + id);
        return orderService.deleteByUser(id) == 1
                ? ApiResponse.success("订单删除成功")
                : ApiResponse.error(600, "订单删除失败");
    }


    /**
     * 司机删除订单，逻辑删除
     * @param id 传来的订单id
     * @return  删除结果
     */
    @DeleteMapping("/update/deleteByDriver")
    public ApiResponse<String> deleteByDriver(@RequestParam("id") long id) throws MyException {
        log.info("取消订单服务提供者:" + "id=" + id);
        return orderService.deleteByDriver(id) == 1
                ? ApiResponse.success("订单删除成功")
                : ApiResponse.error(600, "订单删除失败");
    }


    /**
     * 更新订单信息
     * @param order 传来的订单信息json
     * @return  更新的订单信息结果
     */
    @PutMapping("/update/message")
    public ApiResponse<String> update(@RequestBody Order order) throws MyException {
        log.info("更新订单服务提供者:" + order);
        return orderService.update(order) == 1
                ? ApiResponse.success("订单更新成功")
                : ApiResponse.error(600, "订单更新失败");
    }


    /**
     * 根据订单号获取订单信息
     * @param id    传来的订单号
     * @return  获取的结果以及数据
     */
    @GetMapping("/getById")
    public ApiResponse<Object> getOrderById(@RequestParam("id") long id) throws MyException {
        log.info("通过id获取订单服务提供者:" + id);
        Order order = orderService.selectByOrderId(id);
        return order != null
                ? ApiResponse.success(order)
                : ApiResponse.error(600, "订单未查询到");
    }


    /**
     * 根据用户Id分页获取历史订单信息
     * @param userId    传来的用户Id
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 查询结果
     */
    @GetMapping("/get/user/history")
    public ApiResponse< PageResult<Order> > getOrderByUserId(
            @RequestParam("userId")long userId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ) throws MyException {
        log.info("根据用户Id获取历史订单服务提供者:" + userId);
        PageInfo<Order> pageInfo = orderService.selectByUserId(userId, pageNum, pageSize);
        PageResult<Order> pageResult = new PageResult<>(
                pageInfo.getTotal(),
                pageSize,
                pageNum,
                pageInfo.getList()
        );
        return ApiResponse.success(pageResult);
    }


    /**
     * 根据司机Id分页获取历史订单信息
     * @param driverId   传来的司机Id
     * @param pageNum 页号
     * @param pageSize 页大小
     * @return 查询结果
     */
    @GetMapping("/get/driver/history")
    public ApiResponse< PageResult<Order> > getOrderByDriverId(
            @RequestParam("driverId")long driverId,
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize
    ) throws MyException {
        log.info("根据司机Id获取历史订单服务提供者:" + driverId);
        PageInfo<Order> pageInfo = orderService.selectByDriverId(driverId, pageNum, pageSize);
        PageResult<Order> pageResult = new PageResult<>(
                pageInfo.getTotal(),
                pageSize,
                pageNum,
                pageInfo.getList()
        );
        return ApiResponse.success(pageResult);
    }


    /**
     * 用户通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderIdWithUser")
    public ApiResponse<Order> selectByOrderIdWithUser(@RequestParam("id") long id) throws MyException{
        return ApiResponse.success(orderService.selectByOrderIdWithUser(id));
    }


    /**
     * 司机通过订单id查询订单
     * @param id 订单id
     * @return 查询结果
     */
    @GetMapping("/getByOrderIdWithDriver")
    public ApiResponse<Order> selectByOrderIdWithDriver(@RequestParam("id") long id) throws MyException{
        return ApiResponse.success(orderService.selectByOrderIdWithDriver(id));
    }


    /**
     * 检查乘客未解决的订单
     * @param userId 传来的乘客id
     * @return 处理结果
     */
    @GetMapping("/checkOrder")
    public ApiResponse<Order> checkOrder(@RequestParam("userId") long userId) throws MyException {
        log.info("检查用户是否有未解决的订单userId={}", userId);
        return ApiResponse.success(orderService.selectNotSolve(userId));
    }


    /**
     * 查询司机未解决的订单
     * @param driverId 司机id
     */
    @GetMapping("/checkDriverOrder")
    public ApiResponse<Order> checkDriverOrder(@RequestParam("driverId") long driverId) throws MyException{
        log.info("检查司机是否有未解决的订单userId={}", driverId);
        return ApiResponse.success(orderService.selectNotSolveByDriver(driverId));
    }


    //-------------------------------------------复杂业务-------------------------------------------
    //用户下单：先创建订单、同时带着开始地经纬度和结束地经纬度放入缓存，并实现10分钟倒计数，倒计数结束未接单自动关闭订单并移除缓存，被接单时通知用户并停止倒计时
    @PostMapping("/createOrderByUser")
    public ApiResponse<String> createOrderByUser(@RequestBody Order order) throws MyException {
        //1.创建订单
        int creatResult = orderService.create(order);
        if (creatResult == 0){
            return ApiResponse.error(600, "订单创建失败");
        }
        //2.开始地经纬度放入缓存，并实现10分钟倒计数
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_CREATE_ORDER_KEY, String.valueOf(order.getUserId()));
        return ApiResponse.success("订单创建成功");
    }


    //获取订单倒计时所剩余时间
    @GetMapping("/getOrderTimeOut")
    public ApiResponse<String> getOrderTimeOut(@RequestParam("id") Long id){
        long expire = redisUtil.getExpire("expire:order:id:" + id);
        if (expire == -2)
            return ApiResponse.error(600, "订单已经过期");
         else
            return ApiResponse.success(expire+"");
    }


    //用户取消status小于2的订单：传订单id，从缓存中或数据库查询订单信息，如果status小于2则，更新订单并取消，同时推送给司机。如果status大于2，订单不可取消
    @PostMapping("/cancelOrderByUser")
    public ApiResponse<String> cancelOrderByUser(@RequestParam("id") Long id) throws MyException{
        Order order = orderService.selectByOrderIdWithUser(id);
        if (order == null)
            return ApiResponse.error(600, "订单取消失败，订单不存在");
        else if (order.getStatus() >= 2 && order.getStatus() < 5)
            return ApiResponse.error(600, "订单已经无法取消");
        else if (order.getStatus() == 5)
            return ApiResponse.error(600, "订单已经取消过了");

        //未被接单时取消
        if (order.getDriverId() == 0){
            int cancelOrder = orderService.cancelOrder(id);
            if (cancelOrder == 0)
                return ApiResponse.error(600, "订单取消失败，请稍后重试");
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_CANCEL_ORDER_KEY, String.valueOf(id));
            return ApiResponse.success("订单取消成功");
        }

        //接单后被取消
        int update = orderService.update(order.setStatus(5));
        if (update == 0)
            return ApiResponse.error(600, "订单取消失败，请稍后重试");
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_CANCEL_ORDER_KEY, String.valueOf(id));
        return ApiResponse.success("订单取消成功");
    }


    //司机分页查询可接单列表：传来司机当前位置经纬度放入缓存，从缓存中查出订单开始位置距离司机位置小于20km的订单列表返回给司机
    @MyNotify("查询时间较慢，需要增加效率")
    @GetMapping("/getAcceptList")
    public ApiResponse<Object> getAcceptList(
            @RequestParam("longitude")Double longitude,
            @RequestParam("latitude")Double latitude,
            @RequestParam("pageNum")int pageNum,
            @RequestParam("pageSize")int pageSize
    )throws MyException {
        //1.从redis中查询出起始点距离司机当前位置小于5km的订单
        List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResults = redisUtil.georadius(
                "position",
                longitude,
                latitude,
                5 * 1000,
                (long) pageNum * pageSize  //限制返回的订单个数
        );
        if (geoResults == null) return ApiResponse.error(600, "当前没有可接订单");
        List<Order> orderList = new ArrayList<>();
        //计算起点页位置，终点页位置
        int start = Math.min(geoResults.size(), (pageNum-1) * pageSize);
        //范围限制
        int end = Math.min(geoResults.size(), pageNum * pageSize);
        //判断参数正确性
        if (start == end) return ApiResponse.success("over");
        //截断列表
        geoResults = geoResults.subList(start, end);
        //遍历
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>>  geoResult: geoResults) {
            System.out.println(geoResult);
            String name = (String) geoResult.getContent().getName();
            String idStr = name.split(":")[2];
            //拿出未被接单的订单
            Object temp = redisUtil.get("order:id:" + idStr);
            //过滤
            if (temp == null || redisUtil.getExpire("expire:order:id:" + idStr) == -2) continue;
            orderList.add((Order) temp);
        }
        return orderList.size() == 0 ? ApiResponse.success("over"): ApiResponse.success(orderList);
    }


    //司机接单：传来要接的订单信息，更新订单，如果更新失败则被别人接单或者订单被取消，如果成功则推送消息给用户，同时返回司机接单成功
    @PutMapping("/acceptOrder")
    public ApiResponse<String> acceptOrder(@RequestBody Order order) throws MyException{
        String key = "order:id:" + order.getId();
        Object result = redisUtil.get(key);
        long expire = redisUtil.getExpire("expire:order:id:" + order.getId());
        if (result == null || expire == -2){
            return ApiResponse.error(600, "订单已失效");
        }
        Order redisOrder = (Order) result;
        if (redisOrder.getDriverId() != 0)
            return ApiResponse.error(600, "非常抱歉订单已被他人接单");
        else if (redisOrder.getStatus() == 5) {
            return ApiResponse.error(600, "订单已被取消");
        }
        Float price = (float) (
                order.getDistance() <= 3000
                        ? 8
                        : ( 8 + 2.4 * ( order.getDistance() - 3000 ) )
        );
        int update = orderService.update(
                order.setStatus(1)
                        .setDriverId(order.getDriverId())
                        .setPrice(price)
        );
        if (update == 0){
            return ApiResponse.error(600, "非常抱歉,接单失败");
        }
        //锁定订单
        redisUtil.del(key);
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_ACCEPT_ORDER_KEY, String.valueOf(order.getId()));
        return ApiResponse.success("接单成功");
    }


    //司机到达指定接单位置：传来订单信息，并推送给用户，同时司机需要输入系统提供给用户的四位随机数字才能开始行驶状态，输入成功后也推送给用户
    @PostMapping("/arriverStartAddress")
    public ApiResponse<String> arriverStartAddress(@RequestParam("id")Long id)throws MyException{
        Object resultOrder = redisUtil.get("order:action:id:" + id);
        Order order;
        if (resultOrder == null)
            order = orderService.selectByOrderId(id);
        else
            order = (Order) resultOrder;

        if (order == null)
            return ApiResponse.error(600, "订单不存在");

        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_ARRIVE_START_ADDRESS_KEY, String.valueOf(id));
        return ApiResponse.success("请等待乘客提供给你四位数字");
    }


    //司机验证四位验证码
    @PostMapping("/verityCode")
    public ApiResponse<String> verityCode(@RequestParam("id")Long id, @RequestParam("code")int code)throws MyException{
        String verityKey = "order:verity:id:" + id;
        Object codeInCache = redisUtil.get(verityKey);
        if (codeInCache == null){
            return ApiResponse.error(600, "重复验证或者验证码不正确");
        }
        int result = (int) codeInCache;
        //验证失败:
        if (result != code){
            return ApiResponse.error(600, "验证失败");
        }
        Order order;

        Object resultOrder = redisUtil.get("order:action:id:" + id);
        if (resultOrder == null)
            order = orderService.selectByOrderId(id);
        else
            order = (Order) resultOrder;

        int update = orderService.update(order.setStatus(2));
        if (update == 1)
            kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_TO_END_ADDRESS_KEY, String.valueOf(id));
        return update == 1
                ? ApiResponse.success("验证成功！开始出发")
                : ApiResponse.error(600, "重复验证或者验证码不正确");
    }


    //检查该订单是否已经到达起点
    @GetMapping("/checkArriveStartAddress")
    public ApiResponse<String> checkArriveStartAddress(@RequestParam("id")Long id)throws MyException{
        String verityKey = "order:verity:id:" + id;
        Object codeInCache = redisUtil.get(verityKey);
        return codeInCache != null
                ? ApiResponse.success("已经到达过起点了")
                : ApiResponse.error(600, "还未到达起点");
    }


    /**
     * 查找对应订单对应的用户端的验证码
     * @param id 订单id
     */
    @GetMapping("/getVerityCode")
    public ApiResponse<Object> getVerityCode(@RequestParam("id")Long id)throws MyException{
        String verityKey = "order:verity:id:" + id;
        Object codeInCache = redisUtil.get(verityKey);
        if (codeInCache == null){
            return ApiResponse.error(600, "验证码已经过期");
        }
        return ApiResponse.success(codeInCache);
    }


    //到达终点时：传来订单信息，更改订单状态，更新成功则创建支付并推送给用户行程结束
    @PostMapping("/arriveEndAddress")
    public ApiResponse<String> arriveEndAddress(@RequestParam("id") Long id)throws MyException{
        Object result = redisUtil.get("order:action:id:" + id);
        Order order;
        if (result == null)
            order = orderService.selectByOrderId(id);
        else
            order = (Order) result;

        int update = orderService.update(order.setStatus(3).setEndTime(new Date()));
        if (update == 0){
            return ApiResponse.error(600, "请勿重复点击已到达");
        }
        kafkaTemplate.send(TOPIC_NAME, random.nextInt(3), MQ_ARRIVE_END_ADDRESS_KEY, String.valueOf(id));
        return ApiResponse.success("到达终点成功");
    }

}
