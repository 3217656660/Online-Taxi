package com.zxy.work.util.cache;


import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 抽象缓存接口，可以替换框架
 */
public interface CacheUtil {

//=======================================基础========================================

    /**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间（秒）
     */
    boolean expire(String key,long time);


    /**
     * 根据key 获取过期时间
     * @param key 键，不能为null
     * @return  时间（秒）当为0的时候代表永久有效
     */
     long getExpire(String key);


    /**
     * 判断key是否存在
     * @param key 键
     * @return true存在，false不存在
     */
     boolean hasKey(String key);


    /**
     * 删除缓存
     * @param key 可以传递一个或者多个值
     */
     void del(String ...key);



    //===========================================String======================================
    /**
     * 普通缓存获取
     * @param key   键
     * @return  值
     */
     Object get(String key);


    /**
     * 普通放入缓存
     * @param key   键
     * @param value 值
     * @return  成功或者失败
     */
     boolean set(String key, Object value);


    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time  时间，time要大于零，如果小于等于0，则设置无限期
     * @return  是否成功设置
     */
     boolean set(String key,Object value,long time);


    /**
     * 递增
     * @param key 键
     * @param delta 步长（大于0）
     */
     long incr(String key,long delta);


    /**
     * 递减
     * @param key   键
     * @param delta 步长（大于0）
     */
     long decr(String key,long delta);


    // ===============================List 列表=================================
    /**
     * 获取list缓存的内容
     *
     * @param key   键
     * @param start 开始
     * @param end   结束 0 到 -1代表所有值
     */
     List<Object> lGet(String key, long start, long end);


    /**
     * 获取list缓存的长度
     *
     * @param key 键
     */
     long lGetListSize(String key);


    /**
     * 通过索引 获取list中的值
     *
     * @param key   键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0
     *              时，-1，表尾，-2倒数第二个元素，依次类推
     */
     Object lGetIndex(String key, long index);


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     */
     boolean lSet(String key, Object value);


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     */
     boolean lSet(String key, Object value, long time);


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @return 赋值结果
     */
     boolean lSet(String key, List<Object> value);


    /**
     * 将list放入缓存
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒)
     * @return 赋值结果
     */
     boolean lSet(String key, List<Object> value, long time);


    /**
     * 根据索引修改list中的某条数据
     *
     * @param key   键
     * @param index 索引
     * @param value 值
     * @return 赋值结果
     */
     boolean lUpdateIndex(String key, long index, Object value);


    /**
     * 移除N个值为value
     *
     * @param key   键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
     long lRemove(String key, long count, Object value);



    //=========================================Map========================================


    /**
     * HashGet
     * @param key   不能为null
     * @param item  项 不能为null
     */
     Object hget(String key,String item);


    /**
     * 获取hashKey对应的所有键值
     * @param key   键
     * @return  对应的多个键值对
     */
     Map<Object,Object> hmget(String key);


    /**
     * 向一张hash表放数据，不存在则创建
     * @param key   键
     * @param item  项
     * @param value 值
     * @return  成功与否
     */
     boolean hset(String key,String item,Object value);


    /**
     * 添加的时候带时间
     * @param key   键
     * @param item  项的键
     * @param value 值
     * @param time  时间（秒），如果已经存在hash表的时间，这里会替换原有的时间
     */
     boolean hset(String key,String item,Object value,long time);


    /**
     * HashSet
     * @param key 键
     * @param map 多个键值对
     */
     boolean hmset(String key,Map<String,Object> map);


    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map   键值对
     * @param time  时间（秒）
     */
     boolean hmset(String key,Map<String,Object> map,long time);


    /**
     * 删除hash中的值
     * @param key 键
     * @param item 项，可以为多个，不可为null
     */
     void hdel(String key,Object ...item);


    /**
     * 判断hash中是否有该项的值
     * @param key 键
     * @param item 项
     */
     boolean hHasKey(String key,String item);


    /**
     * 递增，不存在的话，会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 步长>0
     */
     double hincr(String key,String item,double by);


    /**
     * 递减，不存在的话，会创建一个 并把新增后的值返回
     * @param key 键
     * @param item 项
     * @param by 步长>0
     */
     double hdecr(String key,String item,double by);



//=========================================Set========================================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     */
     Set<Object> sGet(String key);


    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key   键
     * @param value 值
     * @return true 存在 false不存在
     */
     boolean sHasKey(String key, Object value);


    /**
     * 将数据放入set缓存
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 成功个数
     */
     long sSet(String key, Object... values);


    /**
     * 将set数据放入缓存
     *
     * @param key    键
     * @param time   时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
     long sSetAndTime(String key, long time, Object... values);


    /**
     * 获取set缓存的长度
     *
     * @param key 键
     */
     long sGetSetSize(String key);


    /**
     * 移除值为value的
     *
     * @param key    键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
     long setRemove(String key, Object... values);


//=======================地理位置geo==============================

    /**
     * 添加一个地理位置
     * @param key 地理位置集合键值
     * @param longitude 经度
     * @param latitude 纬度
     * @param member 具体位置的键值
     */
    void geoadd(String key, double longitude, double latitude, String member);


    /**
     * 查找
     *
     * @param key       地理位置集合键值
     * @param longitude 经度
     * @param latitude  纬度
     * @param radius    以上述经纬度的地理位置为中心，相距radius（米）
     * @return 相距radius（米）的地理位置列表
     */
    List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> georadius(String key, double longitude, double latitude, double radius);


    /**
     * 删除对应key地理位置集合的相应位置
     * @param key 地理位置集合键值
     * @param member 具体位置的键值
     */
    void geodelete(String key, String member);


}
