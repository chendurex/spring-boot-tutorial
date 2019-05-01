package com.spring.boot.tutorial.druid;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.util.Objects;
import java.util.Set;

/**
 * <p>基于spring实现多数据源功能</p>
 * <p>如果配置了两个数据源，默认属于主从数据源，通过{@link #slave()}切换为从库，执行完从操作后可以通过{@link #reset()}切换回默认数据源</p>
 * <p>如果配置超过两个以上数据源，需要切换数据源时，只能通过{@link #route(String)}切换，执行完毕后可以通过{@link #reset()}切换回默认数据源</p>
 * <p>由于事务原因,ORM框架会在框架内部绑定数据库连接{@link java.sql.Connection}，所以在同一个事务中，每次请求都是同一个连接，导致切换数据源失效</p>
 * @author tim.wei
 * @author cheny.huang
 * @date Created on 2018/11/3.
 */
@Slf4j
public class MultipleDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATA_SOURCE = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> MULTI = ThreadLocal.withInitial(()->Boolean.TRUE);
    private static String master;
    private static String slave;
    private static Set<String> names;
    private static boolean check;
    MultipleDataSource(String master, String slave, Set<String> names, boolean check) {
        Objects.requireNonNull(master);
        MultipleDataSource.master = master;
        MultipleDataSource.slave = slave;
        MultipleDataSource.names = names;
        MultipleDataSource.check = check;
    }
    @Override
    protected String determineCurrentLookupKey() {
        return DATA_SOURCE.get();
    }

    /**
     * <p>路由到指定的数据源
     * 如果仅仅是配置了主从数据库，可以通过{@link #slave()}切换到从库
     * 如果需要切回到主数据库，可以通过{@link #reset()}</p>
     * @see #routeAtOnce(String)
     * @see #routeAtMulti(String)
     * @param dataSource 数据源key
     */
    public static void route(String dataSource) {
        if (master == null) {
            throw new RuntimeException("当前环境并未开启多数据源，请检查是否配置正确");
        }
        if (check && !names.contains(dataSource)) {
            throw new IllegalStateException("当前数据源["+dataSource+"]不存在，请检查名字是否配置正确");
        }
        DATA_SOURCE.set(dataSource);
    }

    /**
     * <p>切换到从库
     * 如果配置了超过两个以上的数据源，请使用{@link #route(String)}方法
     * 如果需要切回到主数据库，可以通过{@link #reset()}</p>
     * @see #slaveAtOnce()
     * @see #slaveAtMulti()
     */
    public static void slave() {
        if (slave == null) {
            throw new RuntimeException("当前环境并未设置从数据库，请检查是否配置正确");
        }
        route(slave);
    }

    /**
     * <p>执行一次db操作后，默认切换到主库</p>
     */
    public static void slaveAtOnce() {
        MULTI.set(false);
        slave();
    }

    /**
     * <p>可执行多次db操作，默认不会切换到主库，需要客户端自行选择切回动作</p>
     */
    public static void slaveAtMulti() {
        MULTI.set(true);
        slave();
    }

    /**
     * <p>执行一次操作后，切换到默认数据库</p>
     * @param datasource
     */
    public static void routeAtOnce(String datasource) {
        MULTI.set(false);
        route(datasource);
    }

    /**
     * <p>可执行多次db操作，需要自己切换到默认数据库</p>
     * @param datasource
     */
    public static void routeAtMulti(String datasource) {
        MULTI.set(true);
        route(datasource);
    }

    /**
     * 重置到默认(主)的数据源
     * 如果需要切换到其它的数据源请使用{@link #route(String)}
     */
    public static void reset() {
        DATA_SOURCE.remove();
        MULTI.remove();
    }

    /**
     * 针对单个数据库操作做清理操作
     */
    static void cleanAtOnceCompletion() {
        if (!MULTI.get()) {
            reset();
        }
    }

    /**
     * 针对整个请求做清理操作
     */
    static void cleanAfterCompletion() {
        if (DATA_SOURCE.get() != null && !master.equals(DATA_SOURCE.get())) {
            log.error("为及时清理数据源，当前数据源为:["+DATA_SOURCE.get()+"]，系统已经将数据源恢复到默认数据源中");
            reset();
        }
    }
}