package runner;

import format.MySQLOutputFormat;
import kv.impl.ComDimension;
import kv.impl.CountDurationValue;
import mapper.CountDurationMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import reducer.CountDurationReducer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;

/**
 * @ Autheor:ldl
 * @Description:
 * @Date: 2018/3/24 9:04
 * @Modified By:
 */

public class CountDurationRunner implements Tool {
    private Configuration configuration = null;
    @Override
    public int run(String[] strings) throws Exception {

        addJarToClassPath();

        //得到conf对象
        Configuration conf = this.getConf();
        //创建Job
        Job job = Job.getInstance(conf, "CALL_LOG_ANALYSIS");
        job.setJarByClass(CountDurationRunner.class);

        //添加多个第三方依赖
        job.addFileToClassPath(new Path("/jobs/mysql-connector-java-5.1.27-bin.jar"));
//        job.addFileToClassPath(new Path("/jobs/fastjson-1.2.36.jar"));

        //为Job设置Mapper
        this.setHBaseInputConfig(job);
        //为Job设置Reducer
        job.setReducerClass(CountDurationReducer.class);
        job.setOutputKeyClass(ComDimension.class);
        job.setOutputValueClass(CountDurationValue.class);
        //为Job设置OutputFormat
        job.setOutputFormatClass(MySQLOutputFormat.class);
        return job.waitForCompletion(true) ? 0 : 1;
    }

    private void setHBaseInputConfig(Job job) {

        Configuration conf = job.getConfiguration();
        HBaseAdmin admin = null;
        try {
            admin = new HBaseAdmin(conf);
            //如果表不存在则直接返回，抛个异常也挺好
            if(!admin.tableExists("ns_ct:db_telecom")) throw new RuntimeException("Unable to find the specified table.");

            Scan scan = new Scan();
            scan.setAttribute(Scan.SCAN_ATTRIBUTES_TABLE_NAME, Bytes.toBytes("ns_ct:db_telecom"));
            TableMapReduceUtil.initTableMapperJob("ns_ct:db_telecom", scan,
                    CountDurationMapper.class, ComDimension.class, Text.class,
                    job, true);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(admin != null) try {
                admin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void setConf(Configuration configuration) {
        this.configuration = HBaseConfiguration.create(configuration);
    }

    @Override
    public Configuration getConf() {
        return configuration;
    }

    /**
     * 加载当前 Class 中jar包到当前虚拟机
     * @throws NoSuchMethodException
     * @throws MalformedURLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private void addJarToClassPath() throws NoSuchMethodException, MalformedURLException, IllegalAccessException, InvocationTargetException {
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
        addURL.setAccessible(true);
        URLClassLoader classloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        String url = findContainingJar(getClass());
        //        String url = "file:///home/hadoop/calllog/jobs/lib/ct_count_duration-1.0-SNAPSHOT.jar"; // 包路径位置
        URL classUrl = new URL(url);
        addURL.invoke(classloader, new Object[]{classUrl});
    }
    /**
     * 根据 Class 找出jar包位置
     * @param clazz
     * @return
     */
    public static String findContainingJar(Class<?> clazz) {
        ClassLoader loader = clazz.getClassLoader();
        String classFile = clazz.getName().replaceAll("\\.", "/") + ".class";
        try {
            Enumeration itr = loader.getResources(classFile);
            URL url;
            do {
                if(!itr.hasMoreElements()) {
                    return null;
                }
                url = (URL)itr.nextElement();
            } while(!"jar".equals(url.getProtocol()));
            String toReturn = url.getPath();
//            if(toReturn.startsWith("file:")) {
//                toReturn = toReturn.substring("file:".length());
//            }
            toReturn = URLDecoder.decode(toReturn, "UTF-8");
            return toReturn.replaceAll("!.*$", "");
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        }
    }

    public static void main(String[] args) {
        try {
            int status = ToolRunner.run(new CountDurationRunner(), args);
            System.exit(status);
            if(status == 0){
                System.out.println("运行成功");
            }else {
                System.out.println("运行失败");
            }
        } catch (Exception e) {
            System.out.println("运行失败");
            e.printStackTrace();
        }
    }


    
}
