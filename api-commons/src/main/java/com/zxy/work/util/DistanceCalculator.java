package com.zxy.work.util;

/**
 *  使用经纬度计算两地的大致距离
 */
public final class DistanceCalculator {

    /**
     * 使用 Haversine 公式计算两地之间的距离
     *
     * @param lat1 第一个地点的纬度
     * @param lon1 第一个地点的经度
     * @param lat2 第二个地点的纬度
     * @param lon2 第二个地点的经度
     * @return 两个地点之间的距离，单位为千米
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // 地球半径，单位为千米

        // 将经度、纬度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 根据 Haversine 公式计算两地之间的距离
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadius * c;
    }

}
