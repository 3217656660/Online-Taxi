package com.zxy.work.util.cache;


import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 自定义RedisUtil,实现了缓存工具类接口
 */
@Component
public final class RedisUtil implements CacheUtil {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;


//=======================================基础========================================

    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间（秒）
     */
    public boolean expire(String key,long time){
        try{
            if (time > 0){
                redisTemplate.expire(key,time, TimeUnit.SECONDS);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 根据key 获取过期时间
     * @param key 键，不能为null
     * @return  时间（秒）当为0的时候代表永久有效
     */
    public long getExpire(String key){
        return redisTemplate.getExpire(key,TimeUnit.SECONDS);
    }


    /**
     * 判断key是否存在
     * @param key 键
     * @return true存在，false不存在
     */
    public boolean hasKey(String key){
        try{
            return redisTemplate.hasKey(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除缓存
     * @param key 可以传递一个或者多个值
     */
    @SuppressWarnings("unchecked")
    public void del(String ...key){
        if (key != null && key.length > 0){
            if (key.length == 1){
                redisTemplate.delete(key[0]);
            }else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }



    //===========================================String======================================
    /**
     * 普通缓存获取
     * @param key   键
     * @return  值
     */
    public Object get(String key){
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }


    /**
     * 普通放入缓存
     * @param key   键
     * @param value 值
     * @return  成功或者失败
     */
    public boolean set(String key, Object value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time  时间，time要大于零，如果小于等于0，则设置无限期
     * @return  是否成功设置
     */
    public boolean set(String key,Object value,long time){
        try{
            if (time > 0){
                redisTemplate.opsForValue().set(key, value, time,TimeUnit.SECONDS);
            }else {
                set(key, value);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 递增
     * @param key 键
     * @param delta 步长（大于0）
     */
    public long incr(String key,long delta){
        if (delta < 0){
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }


    /**
     * 递减
     * @param key   键
     * @param delta 步长（大于0）
     */
    public long decr(String key,long delta){
        if (delta < 0){
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }


    // ===============================List 列表=================================
    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
    public long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0
     *              时，-1，表尾，-2倒数第二个元素，依次类推
     */
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 赋值结果
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return 赋值结果
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return 赋值结果
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            return redisTemplate.opsForList().remove(key, count, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    //=========================================Map========================================


    /**
     * HashGet
     * @param key   不能为null
     * @param item  项 不能为null
     */
    public Object hget(String key,String item){
        return redisTemplate.opsForHash().get(key,item);
    }


    /**
     * 获取hashKey对应的所有键值
     * @param key   键
     * @return  对应的多个键值对
     */
    public Map<Object,Object> hmget(String key){
        return redisTemplate.opsForHash().entries(key);
    }


    /**
     * 向一张hash表放数据，不存在则创建
     * @param key   键
     * @param item  项
     * @param value 值
     * @return  成功与否
     */
    public boolean hset(String key,String item,Object value){
        try{
            redisTemplate.opsForHash().put(key,item,value);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 添加的时候带时间
     * @param key   键
     * @param item  项的键
     * @param value 值
     * @param time  时间（秒），如果已经存在hash表的时间，这里会替换原有的时间
     */
    public boolean hset(String key,String item,Object value,long time){
        try{
            redisTemplate.opsForHash().put(key,item,value);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * HashSet
     * @param key 键
     * @param map 多个键值对
     */
    public boolean hmset(String key,Map<String,Object> map){
        try{
            redisTemplate.opsForHash().putAll(key,map);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map   键值对
     * @param time  时间（秒）
     */
    public boolean hmset(String key,Map<String,Object> map,long time){
        try{
            redisTemplate.opsForHash().putAll(key,map);
            if (time > 0){
                expire(key, time);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }


    /**
     * 删除hash中的值
     * @param key 键
     * @param item 项，可以为多个，不可为null
     */
    public void hdel(String key,Object ...item){
        redisTemplate.opsForHash().delete(key, item);
    }


    /**
     * 判断hash中是否有该项的值
     * @param key 键
     * @param item 项
     */
    public boolean hHasKey(String key,String item){
        return redisTemplate.opsForHash().hasKey(key,item);
    }


    /**
     * 递增，不存在的话，会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 步长>0
     */
    public double hincr(String key,String item,double by){
        return redisTemplate.opsForHash().increment(key,item,by);
    }


    /**
     * 递减，不存在的话，会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 步长>0
     */
    public double hdecr(String key,String item,double by){
        return redisTemplate.opsForHash().increment(key,item,-by);
    }



//=========================================Set========================================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 获取set缓存的长度
     *
     * @param key 键
     */
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().remove(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    //=======================地理位置geo==============================

    /**
     * 添加一个地理位置
     * @param key 地理位置集合键值
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 具体位置的键值
     */
    @Override
    public void geoadd(String key, double longitude, double latitude, String member) {
        Point point = new Point(longitude, latitude);
        try {
            redisTemplate.opsForGeo().add(key, point, member);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 查找相距位置小于等于 radius（米）的地理位置列表, 其中附带距离以及按从短到长排序
     * @param key       地理位置集合键值
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    半径（米）
     * @return 相距位置小于等于 radius（米）的地理位置列表
     */
    @Override
    public List< GeoResult<RedisGeoCommands.GeoLocation<Object>> > georadius(String key, double longitude, double latitude, double radius) {
        Point point = new Point(longitude, latitude);
        Circle circle = new Circle(point, new Distance(radius));
        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults
                = redisTemplate.opsForGeo().radius(key, circle, RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().sortAscending());
        if (geoResults == null) return null;

        return geoResults.getContent();
    }


    /**
     * 删除对应key地理位置集合的相应位置
     * @param key 地理位置集合键值
     * @param member 具体位置的键值
     */
    @Override
    public void geodelete(String key, String member) {
        redisTemplate.opsForGeo().remove(key, member);
    }

}
