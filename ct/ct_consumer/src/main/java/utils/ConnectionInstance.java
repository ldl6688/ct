package utils;

import contants.ContantsUtil;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;

import java.io.IOException;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/20 19:57
 * @Modified By:
 */

public class ConnectionInstance {

    private static Connection conn;

    public static synchronized Connection getConenction() throws IOException {
        if (conn == null || conn.isClosed()) {
            conn = ConnectionFactory.createConnection(ContantsUtil.conf);
        }
        return conn;
    }
}
