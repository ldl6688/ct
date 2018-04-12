package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/24 11:22
 * @Modified By:
 */

public class JDBCUtil {
    private static final Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
    private static final String MYSQL_DRIVER_CLASS = PropertiesUtil.getProperty("driverClass");
    private static final String MYSQL_URL = PropertiesUtil.getProperty("jdbcUrl");
    private static final String MYSQL_USERNAME = PropertiesUtil.getProperty("user");
    private static final String MYSQL_PASSWORD = PropertiesUtil.getProperty("password");

    /**
     * 通过反射方式获取数据库连接
     */

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(MYSQL_URL,MYSQL_USERNAME,MYSQL_PASSWORD);
    }

    public static void close(Connection conn, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null || !resultSet.isClosed()) {
                resultSet.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
