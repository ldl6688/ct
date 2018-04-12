package format;

import contant.Constants;
import converter.impl.DimensionConverter;
import kv.base.BaseDimension;
import kv.base.BaseValue;
import kv.impl.ComDimension;
import kv.impl.CountDurationValue;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import utils.JDBCCacheBean;
import utils.JDBCUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/24 9:05
 * @Modified By:
 */

public class MySQLOutputFormat extends OutputFormat<BaseDimension, BaseValue> {


    @Override
    public RecordWriter<BaseDimension, BaseValue> getRecordWriter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        //创建jdbc连接,用于MysqlRecordWriter 内部类操作数据库
        Connection conn = null;

        conn = JDBCCacheBean.getInstance();

        //关闭自动提交
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new MysqlRecordWriter(conn);
    }

    @Override
    public void checkOutputSpecs(JobContext jobContext) throws IOException, InterruptedException {
        //用于校验输出
    }

    @Override
    public OutputCommitter getOutputCommitter(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {
        String name = taskAttemptContext.getConfiguration().get(FileOutputFormat.OUTDIR);
        Path output = name == null ? null : new Path(name);
        return new FileOutputCommitter(output, taskAttemptContext);
    }


    /**
     * 向数据库插入数据
     */
    static class MysqlRecordWriter extends RecordWriter<BaseDimension, BaseValue> {

        private Connection conn = null;
        private DimensionConverter dimensionConverter = null;
        private PreparedStatement preparedStatement = null;
        private int batchNumber = 0;
        private int count = 0;

        public MysqlRecordWriter(Connection conn) {
            this.conn = conn;
            this.batchNumber = Constants.JDBC_DEFAULT_BATCH_NUMBER;
            this.dimensionConverter = new DimensionConverter();
        }

        @Override
        public void write(BaseDimension baseDimension, BaseValue baseValue) throws IOException, InterruptedException {
            //业务逻辑

            try {
                /**
                 * 向通话,信息表中插入通话信息记录,并返回对应的主键
                 */
                String sql = "INSERT INTO `tb_call`" +
                        "(`id_date_contact`," +
                        " `id_date_dimension`, " +
                        "`id_contact`, `call_sum`, " +
                        "`call_duration_sum`) " +
                        "VALUES" +
                        "(?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "`id_date_contact` = ? ;";

                /**
                 * 执行sql语句
                 */
                if (preparedStatement != null) {
                    preparedStatement.executeUpdate(sql);
                }

                /**
                 * 设置预编译参数
                 * 通过三个对象拿到参数值
                 * 通过对象转换器跟据方法传过来的对象获取对象对应的id
                 * 拿到插入数据库所需要的参数值
                 * 设置参数,同时向缓冲区中添加要执行的sql语句,需要设置sql语句计数器
                 * 需要设置计数器
                 * 当缓冲的sql语句达到一定的数量后提交数据执行sql语句
                 *每次批量提交后计数器清零
                 */
                ComDimension comDimension = (ComDimension) baseDimension;
                CountDurationValue countDurationValue = (CountDurationValue) baseValue;

                int id_date_dimension = dimensionConverter.getDimensionId(comDimension.getDateDimension());
                int id_contact = dimensionConverter.getDimensionId(comDimension.getContactDimension());
                int call_sum = countDurationValue.getCallSum();
                int call_duration_sum = countDurationValue.getCallDurationSum();

                String id_date_contact = id_date_dimension + "_" + id_contact;

                int i = 0;

                preparedStatement.setString(++i, id_date_contact);
                preparedStatement.setInt(++i, id_date_dimension);
                preparedStatement.setInt(++i, id_contact);
                preparedStatement.setInt(++i, call_sum);
                preparedStatement.setInt(++i, call_duration_sum);

                preparedStatement.setString(++i, id_date_contact);
                preparedStatement.addBatch();

                count++;
                if (count >= this.batchNumber) {
                    preparedStatement.executeBatch();
                    conn.commit();
                    count = 0;
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void close(TaskAttemptContext taskAttemptContext) throws IOException, InterruptedException {

            //业务收尾,提交剩余数据,关闭连接,释放资源

            try {
                preparedStatement.executeBatch();
                this.conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                JDBCUtil.close(conn, preparedStatement, null);
            }
        }
    }
}
