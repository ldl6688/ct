package converter.impl;

import converter.IConverter;
import kv.base.BaseDimension;
import kv.impl.ContactDimension;
import kv.impl.DateDimension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.JDBCCacheBean;
import utils.JDBCUtil;
import utils.LRUCache;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @ Autheor:ldl
 * @Description:把维度对象转换成对应的维度id
 * @Date: 2018/3/24 11:24
 * @Modified By:
 */

public class DimensionConverter implements IConverter {
    /**
     * 打印日志
     *
     * @param baseDimension
     * @return
     */
    private Logger logger = LoggerFactory.getLogger(DimensionConverter.class);

    /**
     * 从本地线程中获取连接对象,一个线程对应多个进程
     * @param baseDimension
     * @return
     */
    private ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    private LRUCache<String, Integer> lruCache = new LRUCache(300);

    public DimensionConverter() {
        /**
         * 关闭虚拟机时关闭数据库连接
         */
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping mysql connection...");
            JDBCUtil.close(threadLocalConnection.get(), null, null);
            logger.info("mysql connection is successfully closed");
        }));
    }

    public int getDimensionId(BaseDimension baseDimension) throws IOException {

        String cacheKey = genCacheKey(baseDimension);
        if (lruCache.containsKey(cacheKey)) {
            return lruCache.get(cacheKey);
        }
        String[] sqls = null;
        if (baseDimension instanceof DateDimension) {
            // 时间维度表tb_dimension_date
            sqls = genDateDimensionSQL();
        } else if (baseDimension instanceof ContactDimension) {
            //联系人表tb_contacts
            sqls = genContactSQL();
        } else {
            //抛出Checked异常，提醒调用者可以自行处理。
            throw new IOException("Cannot match the dimession, unknown dimension.");
        }

        try {
            Connection  conn = this.getConnection();
            int id = -1;
            synchronized (this) {
                id = execSQL(conn, sqls, baseDimension);
            }

            //将该id缓存到内存中
            lruCache.put(cacheKey, id);
            return id;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException(e);
        }



    }

    private int execSQL(Connection conn, String[] sqls, BaseDimension baseDimension) throws SQLException {

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            //1、假设数据库中有该条数据
            //封装查询sql语句
            preparedStatement = conn.prepareStatement(sqls[0]);
            setArguments(preparedStatement, baseDimension);

            //执行查询
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            //2、假设 数据库中没有该条数据
            //封装插入sql语句
            preparedStatement = conn.prepareStatement(sqls[1]);
            setArguments(preparedStatement, baseDimension);
            preparedStatement.executeUpdate();
            JDBCUtil.close(null, preparedStatement, resultSet);

            //重新获取id，调用自己即可。
            preparedStatement = conn.prepareStatement(sqls[0]);
            setArguments(preparedStatement, baseDimension);
            //执行查询
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } finally {
            JDBCUtil.close(null, preparedStatement, resultSet);
        }
        throw new RuntimeException("Failed to get id");

    }

    private void setArguments(PreparedStatement preparedStatement, BaseDimension baseDimension) throws SQLException {

        int i = 0;
        if (baseDimension instanceof DateDimension) {
            DateDimension dateDimension = (DateDimension) baseDimension;
            preparedStatement.setInt(++i, dateDimension.getYear());
            preparedStatement.setInt(++i, dateDimension.getMonth());
            preparedStatement.setInt(++i, dateDimension.getDay());
        } else if (baseDimension instanceof ContactDimension) {
            ContactDimension contactDimension = (ContactDimension) baseDimension;
            preparedStatement.setString(++i, contactDimension.getTelephone());
            preparedStatement.setString(++i, contactDimension.getName());
        }

    }

    private Connection getConnection() throws SQLException {

        Connection conn = null;
        synchronized (this) {
            conn = threadLocalConnection.get();
            if (conn == null || conn.isClosed() || conn.isValid(3)) {
                conn = JDBCCacheBean.getInstance();
            }
            threadLocalConnection.set(conn);
        }
        return conn;

    }

    private String[] genContactSQL() {
        String query = "SELECT `id` FROM `tb_contacts` WHERE `telephone` = ? AND `name` = ? order by `id`;";
        String insert = "INSERT INTO `tb_contacts`(`telephone`, `name`) VALUES(?, ?);";
        return new String[]{query, insert};
    }
    private String[] genDateDimensionSQL() {
        String query = "SELECT `id` FROM `tb_dimension_date` WHERE `year` = ? AND `month` = ? AND `day` = ? order by `id`;";
        String insert = "INSERT INTO `tb_dimension_date`(`year`, `month`, `day`) VALUES(?, ?, ?);";
        return new String[]{query, insert};
    }

    private String genCacheKey(BaseDimension baseDimension) {
        StringBuilder sb = new StringBuilder();
        if (baseDimension instanceof DateDimension) {
            DateDimension dateDimension = (DateDimension) baseDimension;
            //拼装缓存id对应的key
            sb.append("date_dimension");
            sb.append(dateDimension.getYear()).append(dateDimension.getMonth()).append(dateDimension.getDay());
        } else if (baseDimension instanceof ContactDimension) {
            ContactDimension contactDimension = (ContactDimension) baseDimension;
            //拼装缓存id对应的key
            sb.append("contact_dimension");
            sb.append(contactDimension.getTelephone()).append(contactDimension.getName());
        }

        if (sb.length() <= 0) throw new RuntimeException("Cannot create cachekey." + baseDimension);

        return sb.toString();
    }
}
