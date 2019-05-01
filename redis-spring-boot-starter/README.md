#### redis使用方式
  + 第一种默认方式(推荐)
  + 
                // 缓存key自动带上前缀的操作，默认是spring.application.name，可以通过spring.application.redis.key.prefix修改
                @Autowired
                private AbstractPrefixOperation<String> prefixOperation;
                // 值为integer类型的操作，比如使用inc操作
                @Autowired
                private AbstractPrefixOperation<Integer> prefixOperationForInt;
                // 值为对象类型的操作
                @Autowired
                private AbstractPrefixOperation<RedisUser> userPrefixOperation;
                @Autowired
                private AbstractDefaultOperation<String> defaultOperation;
                @Autowired
                private AbstractDefaultOperation<Integer> defaultOperationForInt;
                @Autowired
                private AbstractDefaultOperation<RedisUser> userDefaultOperation;
                // 类似AbstractPrefixOperation等操作，但是key会加上一个槽位标致，槽位一致的则会落在同一个redis服务端，比如希望业务key都落在一个服务端
                @Autowired
                private AbstractHashTagOperation<String> hashTagOperation;
                @Autowired
                private AbstractHashTagOperation<Integer> hashTagOperationForInt;
                @Autowired
                private AbstractHashTagOperation<RedisUser> userhashTagOperation;
                private static final String KEY = "namespace:key:test";
                private static final String VALUE = "namespace:key:test";
                
                @Test
                public void testDefaultStringOper() throws InterruptedException {
                    fundamentalOperationTest(defaultOperation);
                    intOperationTest(defaultOperationForInt);
                    userOperationTest(userDefaultOperation);
                    testClusterScan(defaultOperation, "cluster:default:scan", "cluster:default:scan*");
                }
                
                private void intOperationTest(NamespaceOperation<Integer> operation) {
                    operation.set(KEY, 1);
                    assertEquals((int)operation.get(KEY), 1);
                    assertEquals((long)operation.increment(KEY, 1), 2);
                    assertEquals(operation.increment(KEY, 2.00), 4.00, 1);
                    assertTrue(operation.delete(KEY));
                }
            
                private void userOperationTest(NamespaceOperation<RedisUser> operation) {
                    // operation obj
                    assertTrue(operation.setIfAbsent(KEY, RedisUser.builder().name("hello").age(1).build()));
                    assertEquals("hello", operation.get(KEY).getName());
                    assertTrue(operation.delete(KEY));
                    assertNull(operation.get(KEY));
                }
                    
                private void fundamentalOperationTest(NamespaceOperation<String> operation) {
                    // fundamental oper
                    operation.set(KEY, VALUE);
                    assertEquals(operation.get(KEY), VALUE);
                    assertFalse(operation.setIfAbsent(KEY, VALUE));
                    assertEquals((long)operation.getExpire(KEY), -1);
                    assertTrue(operation.delete(KEY));
                    // batch delete
                    operation.set(KEY+1, VALUE);
                    operation.set(KEY+2, VALUE);
                    operation.set(KEY+3, VALUE);
                    assertEquals((long)operation.delete(Arrays.asList(KEY+1, KEY+2, KEY+3)), 3);
            
                    // timeout oper
                    operation.set(KEY, VALUE, 3, TimeUnit.SECONDS);
                    assertEquals(operation.get(KEY), VALUE);
                    long expired = operation.getExpire(KEY, TimeUnit.SECONDS);
                    assertTrue(expired>=2 || expired<=1);
                    assertTrue(operation.expire(KEY, 5, TimeUnit.SECONDS));
                    expired = operation.getExpire(KEY, TimeUnit.SECONDS);
                    assertTrue(expired>=3 || expired<=5);
                    assertTrue(operation.expireAt(KEY, new Date(System.currentTimeMillis() + 10_1000)));
                    expired = operation.getExpire(KEY, TimeUnit.SECONDS);
                    assertTrue(expired>=7 || expired<=10);
                    // append oper 因为序列化的原因，所以不能直接判定到底增加了多少内容
                    assertTrue((long)operation.append(KEY, "a") > VALUE.length());
                    try {
                        // replace oper
                        // 因为序列化原因，实际存储的内容并非与原始内容保持一致，所以无法通过字符串上下界限操作内容
                        // 如果需要替换操作，请直接全值替换
                        operation.set(KEY, "vvvv", 0);
                        assertEquals(operation.get(KEY, 0, 3), "vvvv");
                        assertFalse(true);
                    } catch (UnsupportedOperationException e) {
                        assertTrue(true);
                    }
                }
                
                private void testClusterScan(NamespaceOperation<String> operation, String key, String pattern) {
                    for (int i=0;i<100;i++) {
                        operation.set(key+i, key);
                    }
                    ClusterScanOperation.AggressionResult scanResult = operation.scan(pattern, 1000);
                    assertEquals(100, scanResult.getResult().size());
            
                    scanResult = operation.scan(pattern, 10);
                    // 如果是cluster模式下，可能是多个服务端组合出来的数据，所以可能大于10
                    assertTrue(scanResult.getResult().size() < 100);
                    List<String> keys = new LinkedList<>();
                    keys.addAll(scanResult.getResult());
                    while (scanResult.hasNext()) {
                        scanResult = operation.scan(pattern, 10, scanResult);
                        keys.addAll(scanResult.getResult());
                    }
                    assertEquals(100, keys.size());
                    keys.forEach(operation::delete);
            
                    scanResult = operation.scan(pattern, 1000);
                    assertEquals(0, scanResult.getResult().size());
                }
                
                
  + 第二种按照`ReactiveRedisTemplate`方式(`reactive`使用)
    + 在properties中添加`spring.redis.reactive.enable=true`配置
    +  或者yml文件
        
                spring:
                  redis:
                    reactive:
                      enable: true
    
    + 使用方式：
    
                @Autowired
                 private ReactiveRedisTemplate<String, String> template;
                 @Test
                 public void testReactiveRedis() {
                     String k = "k";
                     String v = "v";
                     template.opsForValue().set(k, v, Duration.ofSeconds(10)).block();
                     Assert.assertTrue(v.equals(template.opsForValue().get(k).block()));
                 }
                 
#### cluster scan 功能
  + redis cluster中的keys、scan功能都是仅仅针对单个服务端有效，集群环境下，都不支持，而redisson有支持，但是只提供把所有的
  数据都load进来，还是没有提供limit功能，所以单独实现一个集群下的scan功能(key 功能已经被服务端屏蔽了)
  + 使用方式
  
            @Autowired
            private ClusterScanOperation scanOperation;
            
            public void testClusterScan(){
                String pattern = "cluster:scan:operation:*";// 注意有*
                ClusterScanOperation.AggressionResult scanResult = scanOperation.scan(pattern, 200);
                assertEquals(100, scanResult.getResult().size());
                scanResult = scanOperation.scan(pattern, 100);
                // 如果是cluster模式下，可能是多个服务端组合出来的数据，所以可能大于100
                assertTrue(scanResult.getResult().size() < 400);
                List<String> keys = new LinkedList<>();
                keys.addAll(scanResult.getResult());
                while (scanResult.hasNext()) {
                    scanResult = scanOperation.scan(pattern, 100, scanResult);
                    keys.addAll(scanResult.getResult());
                }
                assertEquals(200, keys.size());
            }
                      
#### 引入分布式锁
   + 项目已经是`spring boot`项目，支持自动配置等功能
   + 实现了所有`java.util.concurrent.Lock`锁的语义并且增加了超时机制
   + [具体实现](https://github.com/redisson/redisson/wiki)
   + 添加依赖
 
           <dependency>
               <groupId>com.infras.sc</groupId>
               <artifactId>redisson-spring-boot-starter</artifactId>
           </dependency>  
                                             
#### 分布式锁使用方式
            
            @Autowired
            private DisLockAllocator allocator;
            private final String prefix = "project:dis:lock:test:";
            @Test
            public void testLockWithLeaseTime() throws Exception {
                // redis key
                String key = prefix + "3";
                DistributedLock lock = allocator.generator(key);
                // 获取时间段为2s的锁
                lock.lock(2, TimeUnit.SECONDS);
                // 其它线程获取锁肯定失败
                new Thread(()-> Assert.assertFalse(allocator.generator(key).tryLock())).start();
                // 等待2s释放锁后重新获取锁
                TimeUnit.MILLISECONDS.sleep(2200);
                // 其它线程重新获取锁
                new Thread(()-> allocator.generator(key).lock(2, TimeUnit.SECONDS)).start();
                TimeUnit.MILLISECONDS.sleep(10);
                Assert.assertFalse(lock.tryLock());
                TimeUnit.SECONDS.sleep(3);
                // 等待3s又可以获取到锁
                Assert.assertTrue(lock.tryLock());
                lock.unlock();
            }
        
            @Test
            public void testLockInterruptedAndWithLeaseTime() throws Exception {
                String key = prefix + "4";
                // 尝试获取可中断锁
                DistributedLock lock = allocator.generator(key);
                lock.lockInterruptibly(2, TimeUnit.SECONDS);
                // 其它线程获取锁肯定失败
                new Thread(()-> Assert.assertFalse(allocator.generator(key).tryLock())).start();
                // 可中断获再次获取锁
                Thread t = new Thread(()->{
                    try {
                        lock.lockInterruptibly(2, TimeUnit.SECONDS);
                        Assert.fail();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Assert.assertTrue(Thread.currentThread().isInterrupted());
                });
                t.start();
                // 中断获取锁的线程
                t.interrupt();
                TimeUnit.MILLISECONDS.sleep(2200);
                // 锁已经释放了，可以由下一个线程获取锁
                mustAllocated(key);
                TimeUnit.MILLISECONDS.sleep(100);
                Assert.assertFalse(lock.tryLock());
            }         
                                       
  + 具体API可以查看`com.infras.sc.component.redisson.DistributedLock`

                                    
#### [引入redis cluster](https://redis.io/topics/cluster-tutorial)
   + 优点：
     + 出现分区时，自动进行服务降级，如果有大多数服务不可用，则会引起整个`cluster`不可用，保证数据的一致性
     + 自动将数据拆分到不同的节点，自动实现负载功能(天然的自动分片功能)
     + 某个服务不可用时，不会引起整体服务不可用(不会出现单点问题)
     + `redis`提供了一主多从的模式，当某个主与大多数的从无法通信时，自动会将从晋升为主，保证高可用
     + `redis`提供了`slave`自动迁移到其它`master`的机制，也就是说，如果某个`master`没有`slave`了，
        那么其它的`master`会将多余的`slave`送给孤儿`master`
     + 如果存在多个`slave`，当出现选举的时候，默认会按照同步数据最多的`slave`作为主，尽最大努力保证数据一致性
     + 如果存在多个`slave`，当出现选举的时候，可以设置`slave`与`master`断链了多久后的`slave`不参与选举，排除数据少的`slave`选举为`master`
     + 可以执行全量、增量同步
     
   + 缺陷：
     + `cluster`并未保证强一致性，在大家不要期望`redis`会做到强一致性(分布式`脑裂`问题)，在极端情况会出现一定时间的数据丢失
       + `cluster`操作是同步返回给客户端，但是异步将数据复制到`slave`节点，所以当`master`写成功，并且返回成功时，
           在复制到`slave`的时候`crash`了，然后一个新的`slave`晋升为`master`，那么刚刚写入的数据就丢失了
       + 数据刚刚写入到`cluster`时，正在同步到`slave`节点时，出现了网络分区，然后`slave`节点晋升为`master`，也会导致数据不一致
       + `redis`设置了一个最大时间窗口(`maximum window`或者叫做`node timeout`)，如果`master`在这个时间段内一直无法通信，
       那么会选举出一个新的`master`，并且会停止当前这个`master`接收写请求,那么这段时间`master`接收的数据也会丢失
     + `key`批量操作支持有限。如：`MSET`/`MGET`，目前只支持具有相同`slot`值的`key`执行批量操作。
     + `key`事务操作支持有限。支持多`key`在同一节点上的事务操作，不支持分布在多个节点的事务功能。
     + `key`作为数据分区的最小粒度，因此不能将一个大的键值对象映射到不同的节点。如：`hash`、`list`
       
   + 持久化数据策略(默认情况，可以配置)：
     + 如果`60`秒内，有超过`10000`个`key`改变，则持久化
     + 如果`300`秒内，有超过`10`个`key`改变，则持久化
     + 如果`900`秒内，有超过`1`个`key`改变，则持久化
     + `redis`可以使用`rdb+aof`两种方式进行数据持久化，`rdb`的方式就是上面所说的全量持久化，`aof`属于增量持久化数据，
         默认是每秒持久化一次，也就是说，在机器宕机时，理论情况只有最后一秒钟的数据会丢失。但是在持久化`rdb`的时候，
         可能会禁止`aof`的持久化(不执行`fsync`操作，强行刷盘)，如果是这种情况，也有可能导致这个时间点的数据丢失，
         最大时间为`30s`，由系统`os`的页缓存时间控制如果系统宕机，可以使用`rdb`文件来恢复快照数据，然后再配合`aof`恢复增量数据          
                                  
#### 基本配置(所有的配置都是来自redis.conf # version 4.0)
  + `./redis-server /path/to/redis.conf` # `redis`启动方式
  + `include /path/to/local.conf` # `redis`依赖文件，`redis`是按照从上往下读取配置的方式，如果`include`的内容需要覆盖原始模板则
        放置在配置文件最后一行，否则放置在最前面，`redis`做集群的时候，会需要一个标准的模板配置然后增加一些个性的配置
  + `bind 192.168.1.100 127.0.0.1 ::1` # 绑定`redis`只接收哪些网卡的请求，比如`192.168.1.100`表示的是内网地址，那么就可以接收
      来自内网的请求，如果是外网地址那么则表示可以接收外网地址。如果不配置，那么表示接收所有的网络请求。
      `redis`默认仅接收来自本机的IPv4网络请求
  + `protected-mode yes` # 是否开启保护模式，默认开启
      + 如果开启保护模式，而且`server`未绑定`ip(bind)`，也未设置连接密码，那么`server`只接收`IPv4`、`IPv6`
         和`Unix Domain Sockets`发过来的连接请求
      + 如果开启保护模式，而`server`有绑定`ip`，那么会接收来自绑定`ip`的网络请求
      + 如果开启保护模式，而`server`未绑定`ip`，但是有设置连接密码，那么客户端可以连接到`redis`服务器，但是提交请求的时候需要输入密码
      + 如果需要接收其它ip的连接请求，那么可以选择关闭保护模式(并且不绑定任何`ip`)，或者绑定`ip`
      + 总结，如果有绑定`IP`，那么以绑定`IP`为优先级，如果未绑定IP，那么再采取保护模式以确定可以接收哪些主机的连接请求  
  + `port 6379` # 设置`server`监听的端口(如果设置为`0`，那么不会接收`tcp`请求，比如`Unix Domain Sockets`)
  + `tcp-backlog 511` # `tcp`全连接数量(还需要配置系统参数`/proc/sys/net/core/somaxconn`大小)
  + `unixsocket /tmp/redis.sock` # `unix domain socket` 监听地址，默认关闭
  + `timeout 0` # 当客户端连接空闲`N`秒后，关闭连接，`0`表示禁止关闭，由客户端控制连接是否关闭
  + `tcp-keepalive 300` # `tcp`服务端发送监控给客户端的时间间隔，以检测客户端是否关闭或者继续保持连接     
  + `daemonize no` # 是否以`daemon`的方式启动服务，如果以`daemon`启动，则`pid`会写入到`${piffile}`，如果未指定则放置`/var/run/redis.pid`
  + `pidfile redis.pid` # 如果`redis`以`daemon`方式启动，那么会将`pid`文件写入到指定位置      
  + `loglevel notice` # `redis`日志等级，包括(`debug`、`verbose`、`notice`、`warning`)
  + `logfile ""` # 日志文件名称，默认为空，即写入到标准控制台，如果是以`daemon`方式则输入到`/dev/null`
  + `databases 16` # 设置数据库数量，默认是将数据存放在`0`数据库，如果用了`redis cluster`则不需要了
    (如果需要将多个`key`存储到同一个节点，那么可以使用`hash tag`，比如：`aaa{username}bbb`，`xxx{username}yyy`，
     这两个`key`可以按照`username`这个`tag`进行`hash`，然后放入到同一个节点)
  + `always-show-logo yes` # `redis`启动时显示`logo`
  + 数据持久化配置，可以组合多个配置
    + `save 900 1` # 如果`900`秒内，有超过`1`个`key`改变，则持久化 
    + `save 300 10` # 如果`300`秒内，有超过`10`个`key`改变，则持久化
    + `save 60 10000` # 如果`60`秒内，有超过`10000`个`key`改变，则持久化
    + `save ""` # 禁止保存到`rdb`数据库
  + `stop-writes-on-bgsave-error yes` # 默认情况下，当开启保存`rdb`快照，并且最近的一次快照保存失败，那么`redis`会禁止写入，
    这样能让业务方感知到数据未持久化到硬盘，防止发生未知灾难。一旦`bgsave`可以正常运行时，`redis`也会开始正常写入，如果有对
    `redis`持久化硬盘监控，或者不需要`redis`持久化的功能，也可以禁止掉这个特性
  + `rdbcompression yes` # 默认开启使用`LZF`算法压缩`string`对象，以减少`dump.rdb`大小，但是也会损耗`cpu`
  + `rdbchecksum yes` # 默认开启`rdb checksum`，防止出现数据损坏
  + `dbfilename dump.rdb` # 数据库 文件名称
  + `dir ./` # `rdb、aof`文件写入的目录地址，默认是当前目录
  + `slaveof 192.168.2.55 7000` # `slaveof <masterip> <masterport>` ，指定当前`redis`为某个`redis`的`slave`(`cluster`集群不需要配置)
  + `masterauth 123455` # `masterauth <master-password>`，当master 配置`requirepass`，那么`slave`需要配置认证密码
  + `slave-serve-stale-data yes` # 当`slave`与`master`失去连接，或者`slave`正在同步`master`的数据时
    + 如果设置为`yes`，当客户端请求到`slave`时，继续响应请求，只是数据可能不是最新的
    + 如果设置为`false`，当客户端响应请求时，那么会返回`SYNC with master in progress`错误
    + `redis`是建议`slave`仅仅作为备份而已，不接收请求，所以就不会产生这种问题
  + `slave-read-only yes` #  默认开启`slave`只读模式，如果需要对`slave`写临时数据，也可以关闭此开关(此参数在集群模式下无效，
     在集群模式下，直接使用连接级命令`readonly`即可，或者使用`readwrite`命令管理只读命令)；如果为`yes`时，将数据写入到`slave`
     会提示错误(`(error) READONLY You can't write against a read only slave.`)
  + `slave` 同步策略
    + `disk-backed: redis master`启用新的线程把`rdb`写入到磁盘，随后父进程增量将文件同步到`slave`，如果有多个`slave`，那么会将
      同步的文件按照队列的形式一一同步
    + `diskless`: `redis master` 创建一个新的`rdb`文件到`slave sockets`中，而不是操作`rdb`文件，如果有多个`slave`，可以按照指定时间
      预热后并行的同步到`slave`，如果服务器的带宽非常大，采用此方式会更加好
    + `repl-diskless-sync no` # 默认关闭`diskless`
    + `repl-diskless-sync-delay 5`: # 默认预热`5`秒后才是同步数据到`slave`(比如在一主多从的场景，可能`server`突然重启等，
      如果按照常规，那么是一个一个的服务。`redis`提供这个参数，可以让`server`等待一定的时间，然后一次性同步给所有的`slave`)
  + `repl-ping-slave-period 10` # `slave`向`master`发送心跳时间间隔，默认`10s`(在`cluster`模式下,如果设置了`slave-validity-factor=0`
      参数，则这个参数无效)
  + 同步超时情况：
    + 繁重的`IO`数据同步导致的超时
    + `slave`检测到`master`超时
    + `master`检测到`slave`超时
    + `repl-timeout 60` # 同步超时，默认`60s`
  + `repl-timeout` # `slave`与`master`连接超时时间，如果`master`数据量非常大，在进行全量同步时，如果超过这个时间还未同步完毕，
     那么`slave`会关闭此连接，然后再清空数据，导致无法完成同步，如果数据量比较大，增加这个值。(比如一个`6G`的数据，在`1G`的带宽
     下，每秒最高同步`100MB`，将会超过`60S`)
  + `repl-disable-tcp-nodelay no` # `redis`默认开启`tcp-nodelay`，即禁用`nagle`算法，如果网络带宽拥塞，或者网络的`hop`数过多，那么可以将开关改为`yes`
  + `repl-backlog-size 1mb` # `slave`与`master`断连后，`master`会把增量的数据写入一个临时文件，用于下次`slave`重连后同步增量数据
  + `repl-backlog-ttl 3600` # 超过这个时间后，`slave`还未连接上，那么释放`backlog`    
  + `slave-priority 100` # 晋升`master`的优先级，数值越低优先级越高，如果设置为`0`则表示不参数`master`晋升
  + `min-slaves-to-write 3` # 至少存在3个`slave`才可以写入数据，低于三个`slave`则`master`停止写入，如果设置为`0`则表示禁止此特性，
      `redis`默认是禁止
  + `min-slaves-max-lag 10` # 与`min-slaves-to-write`配合使用，设置当一个`master`端的可用`slave`少于`N`个，
      延迟时间大于`M`秒时，不接收写操作(需要两个参数同时配合才会生效)
  + `slave-announce-ip 192.168.2.22` # `slave`向`master`上报自己的`ip`，`slave`连上`master`时候，`master`其实已经知道了
      `slave`的`ip`和`port`但是由于存在端口跳转或者`NAT`技术，所以获取的`ip`不一定是真实的，`redis`提供了这个配置让`slave`
      自动上报自己真实的`ip`，这个在使用`docker`部署`redis cluster`的时候必须指定，否则部署`cluster`失败
  + `slave-announce-port 6379` # `slave`向`master`上报自己的`port`
  + `requirepass test` # 设置连接`redis`服务器密码
  + `rename-command shutdown server-close` # 重命名命令，比如像关闭服务这种特殊的命令，不应该暴露给外部调用，但是可以提供给
      管理员使用。或者直接禁止某个命令，则改为 `rename-command shutdown ""`
  + `maxclients 10000` # 客户端最大的连接数，默认是`10000`(如果系统限制了最大连接符数量，那么默认是最大连接符减去`32`，
      `redis`内部需要使用一些文件描述符)，如果超过了这个数，`redis`会返 `max number of clients reached`，客户端也是需要设置
      最大连接数，防止出现连接泄漏问题
  + `maxmemory 10gb` # 最大内存，超过内存后会按照清理策略清理缓存
  + `maxmemory-policy volatile-lru` # 以`lru`的方式清理缓存
  + `maxmemory-samples 5` # 清理缓存时采集的样本数量，值越大那么越精确，但是消耗`cpu`越高，值越小，速度越快，但是精确度降低
      `redis`默认是`5`(`redis`是使用采样的方式移除缓存，并不是真正的`lru`，而是[近似lru](https://redis.io/topics/lru-cache))
  + `redis`删除`key`有两种方式：
    + 第一种是传统的阻塞型(命令为`DEL`)，如果对象非常小的话，删除速度非常快，时间复杂度为`O(1)`或者`O(log_N)`，但是如果`key`
    关联的对象非常多，比如`set`、`list`等，那么需要花费的时间就会非常多
    + 由于阻塞导致时间变长，所以`redis`提供了以非阻塞的方式删除`key`(命令为`UNLINK`)，客户端以异步的方式提交命令后，
    会立刻返回给客户端，`redis`会有一个后台线程进行删除`key`和重新整理内存
  + `redis` 删除`key`或者刷新缓存的场景有下面几种：
    + `redis`内存达到最大值，需要按照清理缓存策略清理`key`
    + 缓存`key`已经到了过期时间，开始清理`key`
    + 客户端命令操作而引起的删除`key`操作，比如`set`一个已经存在的`key`，那么会导致先删除旧的内容，然后重新替换新的内容；或者是
      执行`rename`操作，那么会先删除旧的`key`，然后将新的`key`加入`redis`
    + 在`slave`同步`master`的时候，`slave`执行了全量的重复制
  + 在性能和数据一致性上，`redis`提供了几个参数，让我们控制
    + `lazyfree-lazy-eviction no` # 异步释放由于内存达到最高值而需要清理的对象空间
    + `lazyfree-lazy-expire no` # 异步释放过期`key`对象空间
    + `lazyfree-lazy-server-del no` # 异步释放`del`操作`key`的对象空间
    + `slave-lazy-flush no` #  异步刷新`slave`数据库
  + `appendonly no` # 是否开启`aof`，如果开启则以`aof`的方式加载文件，否则以`rdb`的方式加载文件(`aof`文件就是客户端操作命令
      的集合日志，`redis`重启后直接对日志进行重做以达到恢复功能)  
  + `appendfilename "appendonly.aof"`
  + `redis` 数据落盘策略
    + `appendfsync always` # 每次执行操作完，都会执行一次`fsync`刷盘操作，这样会保证数据不会丢失，但是性能会非常低
    + `appendfsync everysec` # 每隔一秒钟执行一次`fsync`刷盘操作，这样最多也就是`1`秒钟出现数据丢失，`redis`默认的方式
    + `appendfsync no` # 不进行`fsync`刷盘操作，这样最大的数据丢失依赖于系统缓存刷盘策略，默认是`30s`(这个时间是系统
       级别的，不提供配置，系统只是保证最长`30s`能落盘，系统尽量保证快速的落盘，其实理论上是写入马上就会落盘的)
    + 上面说的数据丢失，针对的是服务器突然宕机而言，而不是`redis`服务`crash`，因为每次操作都是`write`进系统，真正落盘由系统
    决定，而执行`fsync`只是强制系统落到磁盘。所以如果想要高性能，而且最数据不是非常敏感，其实可以考虑设置`appendfync no`
  + `no-appendfsync-on-rewrite no` # 在执行`BGSAVE`或者`BGREWRITEAOP`时候，虽然是异步线程操作，但是因为使用了大量的`IO`操作，
    也会影响到主业务操作`redis`，导致延迟增大，通过这个参数可以避免这个问题，如果设置为`yes`，则表示在`BGSAVE`等操作的时候，
    不进行`fsync`刷盘操作，这样最坏的情况，也会出现最大`30s`的数据丢失(这个配置会使`appendfsync`在`BGSAVE`情况下配置失效)
  + `redis rewrite`: `redis`的`aof`文件是对`redis`命令的一个`redo`日志，所以会存在大量的重复日志(比如一个`set`操作了`100`次，
      只有最后一次才有效，但是`aof`日志记录了`100`次)，`redis` 提供了日志重写的特性
    + `auto-aof-rewrite-percentage 100` # 设置`aof`文件达到指定大小的百分比后开始重写`aof`文件，`redis`会记录上一次重写后的
      `aof`文件大小，然后以上一次`aof`文件大小为基数，如果当前的`aof`文件超过上次重写后的`100%`，那么开始再次触发重写操作(比如第一次
      `aof`文件为`1G`，那么当`aof`文件达到`2G`的时候则执行重写，重写完毕后可能是`1.5G`，那么下次再触发重写则是`3G`)
    + `auto-aof-rewrite-min-size 64mb` # 设置`aof`文件最小值达到多少才开始进行重写`aof`文件，虽然有上面的百分比基数，但是
      在初始时，一般文件很小，所以触发重写的频率会非常高，所以设置当`aof`后续的增长至少达到指定值才开始执行`aof`重写，也就是说
      `aof`重写必须要满足这两个条件才真正的触发
  + `aof-load-truncated yes` # 由于服务器宕机或者文件系统损坏等原因导致`aof`文件损坏，`redis`启动时候检查到`aof`文件已经损坏，
      那么会截断文件末尾损坏的部分，加载未损坏的部分，这样就会导致一些数据丢失。如果设置为`no`，那么启动的时候`redis`就会
      报错，由人工进行修复
  + `aof-use-rdb-preamble no` # 如果配置了`appendonly yes`，那么表示`redis`每次都是加载`aof`文件，而不会加载`rdb`文件，而`aof`文件
      仅仅是一些操作集合的命令，如果数据量非常大的情况，那么加载的时间会变得非常长。如果开启了`aof-use-rdb-preamble`配置
      后，那么每次重写`aof`文件的时候，都会把`rdb`文件写入到`aof`文件前面，然后再追加真正的`aof`增量数据，这样就将`rdb`文件的快速
      和`aof`增量文件结合在一起，大大提高性能(如果开启`aof`方式，那么可以关闭`rdb`生成快照了)
  + `cluster-enabled yes` # 开启`redis cluster`特性
  + `cluster-config-file nodes.conf` # `cluster`集群配置文件，当集群内节点发生信息变化时，如添加节点、节点下线、故障转移等。
      节点会自动保存集群的状态到配置文件中。该配置文件由`redis`自行维护，不要手动修改，防止节点重启时产生集群信息错乱
  + `cluster-node-timeout 15000` # `cluster`节点超时时间，单位`ms`，`slave`检查到`master`超过时间还未获取到心跳，则认为`master`下线
  + `slave`晋升成`master`时候有多个匹配条件
    + 每个`slave`之间会检查自己同步数据的`offset`，值最大的表示同步的数据最大，最有机会晋升为`master`
    + 如果`slave`与`master`断连了超过一定的时间，那么当前`slave`是不会参与晋升`master`，超过 
    `(node-timeout * slave-validity-factor) + repl-ping-slave-period` 时间的`slave`不会参与晋升`master`
  + 一个节点下线分为两个阶段：
    + 第一个阶段为主观下线(`redis`日志中显示的`pfail`)，也就是当达到`cluster-node-timeout`时间后，会判定当前节点主观下线
    + 第二个阶段为客观下载(`redis`日志中显示`fail`)，当某个节点主观下线后，相应的节点状态会在集群中传播，当半数以上的主节点
    都标记某个节点主观下线后，触发客观下线流程，也就是当前节点真正下线了
    + 为什么要分为两个阶段呢？因为有可能存在网络分区的请求，导致误判节点
    + 为什么必须是负责槽的主节点参与故障发现决策？因为集群模式下，只有处理槽的主节点才负责读写请求和集群槽等关键信息的维护，
    而从节点只进行主节点数据和状态信息的复制
    + 为什么必须是半数以上处理槽的主节点？因为是为了应对网络分区等原因造成的集群分割请求，所以`cluster`集群也必须要求`3`个以上的节点
  + `slave`晋升为`master`耗时：
    + 主观下线时间=`cluster-node-timeout`
    + 主观下线消息传播时间<=`cluster-node-timeout/2`：`cluster`与`cluster`/`slave`之间是通过`ping/pong`进行心跳检测的，
    集群中的服务都会在每秒随机选择另外一个服务进行心跳检测，然后更新本地信息(每个节点一定会主动`ping`那些自己在
    `NODE_TIMEOUT/2`时间内没有发送过`ping`或从之接收过`pong`的节点)
    + 从节点转移时间<=`1000ms`，由于存在延迟发起选举机制(根据节点同步数据的偏移量来确定延迟时间)，偏移量最大的从节点会最多延迟`1s`发起选举
    + `failover-time <= cluster-node-timeout + cluster-node-timeout/2 + 1000`  
  + `cluster-slave-validity-factor 10` # 验证`slave`合法性的因子，如果这个值越大，`slave`越有可能参与`master`晋升，那么新的`master`
    存在数据丢失的情况就越高，设置为`0`表示所有的`slave`都参数晋升`master`
  + `cluster-migration-barrier 1` # 如果某个`master`下没有`slave`，那么称这个`master`为孤儿，`redis`为了保证可用性，
    会把其它`master`下多余的`slave`迁移到孤儿`master`中，成为孤儿`master`的`slave`。`cluster-migration-barrier`
    表示`master`下最少有多少个`slave`，超过这个数的`slave`可以把多余的`slave`迁移给孤儿`master`。
    这也就是为什么`redis`建议`slave`的数量一定要比预计`slave`数量多`N`个的原因(如果某个`master`和`slave`同时`crash`，
    或者`slave`还未晋升为`master`之前也挂了，那么多余的`slave`是不会迁移成为旧的`master`的`slave`，出现某个`master`
    有多个`slave`，而某个`slot`直接不存在任何的服务)
  + `cluster-require-full-coverage yes` # 如果其中某些`hash slot`未覆盖到(`slot`未分配正确)`redis`默认会停止接收请求，
     如果希望其它的节点继续接收请求则改为`no`(如果某个`master`和`slave`都`crash`，虽然也是出现某个`hash slot`未提供服务，但是
     不此配置不影响)
  + `cluster-slave-no-failover no` # 设置当前`slave`不参与`failover`
  + `slowlog-log-slower-than 10000` # 超过这个时间的命令都记录日志(单位微妙)，设置负数表示禁止日志，设置`0`表示每个命令都记录日志
     这个时间是不包括`IO`时间或者网络请求时间，而是命令真正执行的时间
  + `slowlog-max-len 128` # `slowlog`记录数量(因为`slowlog`是不存储在磁盘，所以防止慢日志非常多，所以设置数量)
  + `aof-rewrite-incremental-fsync yes` # 在执行重写`aof`文件时，每次生成`32MB`的增量数据后就强制刷盘到磁盘，而不是一次性
     把整个文件写入到磁盘，防止出现过高的`IO`导致性能抖动
  + `redis`主从复制可以根据是否是全量分为全量同步和增量同步(线上不要重启服务器，防止出现全量同步大量的数据时导致`IO`抖动)
    + 全量同步,`redis`全量复制一般发生在`slave`初始化阶段，这时`slave`需要将`master`上的所有数据都复制一份。具体步骤如下： 
      1. 从服务器连接主服务器，发送`sync`命令； 
      2. 主服务器接收到`SYNC`命名后，开始执行`bgsave`命令生成`rdb`文件并使用缓冲区记录此后执行的所有写命令； 
      3. 主服务器`bgsave`执行完后，向所有从服务器发送快照文件，并在发送期间继续记录被执行的写命令； 
      4. 从服务器收到快照文件后丢弃所有旧数据，载入收到的快照； 
      5. 主服务器快照发送完毕后开始向从服务器发送缓冲区中的写命令； 
      6. 从服务器完成对快照的载入，开始接收命令请求，并执行来自主服务器缓冲区的写命令；
    + 增量同步,`redis`增量复制是指`slave`初始化后开始正常工作时主服务器发生的写操作同步到从服务器的过程。 
        增量复制的过程主要是主服务器每执行一个写命令就会向从服务器发送相同的写命令，从服务器接收并执行收到的写命令
      + 增量同步的概念
        + 内存缓存队列(`in-memory backlog`): 记录连接断开时`master`收到的写操作
        + 复制偏移量(`replication offset`): `master`,`slave`都有一个偏移，记录当前同步记录的位置 
        + `master`服务器`id(master run id)`: `master`服务器唯一标识
      + 增量同步条件
        + `slave`记录的`master`服务器`id`和当前要连接的`master`服务器`id`相同 
        + `slave`的复制偏移量比`master`的偏移量靠前。比如`slave`是`1000`， `master`是`1100`
        + `slave`的复制偏移量所指定的数据仍然保存在主服务器的内存缓存队列中(设置`repl-backlog-size`大小)      
    + 主从刚刚连接的时候，进行全量同步；全同步结束后，进行增量同步。当然，如果有需要,`slave`在任何时候都可以发起全量同步。
        `redis`策略是，无论如何，首先会尝试进行增量同步，如不成功，要求从机进行全量同步(如果配置了`diskless`则无需生成)

#### 搭建cluster
  + [通过官方提供的`redis-trib.rb`脚本搭建](https://redis.io/topics/cluster-tutorial),我写的非常简单，仅仅是作为记录
    使用而已，具体的每个步骤的作用需要去官网查看详细信息
    1. 创建`6`个目录
            
            mkdir cluster-test
            cd cluster-test
            mkdir 700{0..5}
            
    2. 在每个目录中新增一份名为`redis.conf`文件，内容如下(记得修改端口号)
        
            port 7000
            cluster-enabled yes
            cluster-config-file nodes.conf
            cluster-node-timeout 5000
            appendonly yes
                
    3. 依次进入每个目录中启动`redis`
    
            cd 7000
            ../redis-server ./redis.conf
            
    4. 启动后，你会看见如下日志
        
            [82462] 26 Nov 11:56:55.329 * No cluster configuration found, I'm 97a3a64667477371c4479320d683e4c8db5858b1
            
    5. `redis`依赖`ruby`，[需要去下载源码包安装](http://ask.xmodulo.com/upgrade-ruby-centos.html)，
        然后依次执行下面的命令(我安装的是`4.x`版本，所以需要最新的`ruby`，如果是旧版的话，可能不需要，则直接按照官网执行就好)
      
              sudo yum remove ruby ruby-devel
              sudo yum groupinstall "Development Tools"
              sudo yum install openssl-devel
              wget http://cache.ruby-lang.org/pub/ruby/2.5/ruby-2.5.1.tar.gz
              tar xvfvz ruby-2.5.1.tar.gz
              cd ruby-2.5.1
              ./configure
              make
              sudo make install
    
    6. 安装`redis`依赖工具
    
            gem install redis
            
    7. 构建`cluster`
            
            ./redis-trib.rb create --replicas 1 127.0.0.1:7000 127.0.0.1:7001 \
            127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005
    
    8. 构建过程中会提示是否按照配置进行构建，输入`yes`，则构建成功，成功后会有下面的内容
            
            [OK] All 16384 slots covered
    
  + [通过命令行搭建(来自`redis`运维与实战),个人推荐这个](https://blog.csdn.net/men_wen/article/details/72853078)
    1. 准备节点
      1.1. `redis`集群一般由多个节点组成，节点数量为`6`个才能保证组成完整高可用的集群。下面给出一个节点的配置，其他的节点和该节点只是端口不同
            
              port 6382                               
              cluster-enabled yes                     
              cluster-config-file nodes.conf 
              cluster-node-timeout 15000 
              
      2.1. 进入各个节点目录，然后启动所有节点(最好是写一个启动脚本，但是千万别做成如果节点存在则`kill`再启动)
              
              sudo ../src/redis-server ./redis-6384.conf
              sudo ../src/redis-server ./redis-6383.conf
              sudo ../src/redis-server ./redis-6382.conf
              sudo ../src/redis-server ./redis-6381.conf
              sudo ../src/redis-server ./redis-6380.conf
              sudo ../src/redis-server ./redis-6379.conf
              
      3.1. 查看启动日志      
        
            9507:M 28 Aug 19:02:28.789 * DB loaded from append only file: 0.090 seconds
            9507:M 28 Aug 19:02:28.789 * Ready to accept connections
       
      3.2. 通过`nodes.conf`文件查看集群节点信息
      
            cat nodes.conf 
            dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535453483000 1 connected
      
      3.3. 通过`cluster nodes`命令查看集群节点间的信息                       
                 
              127.0.0.1:6382> CLUSTER NODES
              dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535457329000 1 connected
                       
    2. 节点握手,节点握手是指一批运行在集群模式的节点通过`gossip`协议彼此通信，达到感知对方的过程。节点握手是集群彼此通信的第一步
      2.1. 由客户端发起命令：`cluster meet <ip> <port>`
            
            127.0.0.1:6382> CLUSTER MEET 127.0.0.1 6380
            OK
            // 发送CLUSTER NODES可以查看到已经感知到 6380 端口的节点了。
            127.0.0.1:6382> CLUSTER NODES
            dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535457329000 1 connected
            8f285670923d4f1c599ecc93367c95a30fb8bf34 127.0.0.1:6380 master - 0 1496129041442 0 connected
            
      2.2. 让所有的节点互通(如果是需要外网访问，那么需要写外网的地址，否则redis客户端会以127端口连接远程)
      
            127.0.0.1:6382> CLUSTER MEET 127.0.0.1 6380
            OK
            127.0.0.1:6382> CLUSTER MEET 127.0.0.1 6381
            OK
            127.0.0.1:6382> CLUSTER MEET 127.0.0.1 6383
            OK
            127.0.0.1:6382> CLUSTER MEET 127.0.0.1 6384
            OK
            // 已经全部感知到所有的节点
            127.0.0.1:6382> CLUSTER NODES
            274e449a29557f9835ded08c35e36712fc810e43 127.0.0.1:6384@16384 master - 0 1535457331000 12 connected
            54b9e67ba0fdf2619e27ebf23550bfe75ea1b746 127.0.0.1:6383@16383 master - 0 1535457333000 7 connected
            dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535457329000 0 connected 
            6518b5f73b9d4137d64352cd8e771e07817abe14 127.0.0.1:6381@16381 master - 0 1535457333000 12 connected
            b3bd8f5d30d185d6fb6eaf1f4b091738a0c6543d 127.0.0.1:6380@16380 master - 0 1535457332000 7 connected
            615f59cd9b6af4f376ada79e6c5c5cbded51ed1d 127.0.0.1:6385@16385 master - 0 1535457333334 5 connected
            
      2.3. 当前已经使这六个节点组成集群，但是现在还无法工作，因为集群节点还没有分配槽(`slot`)     
       
    3. 分配槽
      3.1. 可以看一下`6382`端口的槽个数
      
            127.0.0.1:6382> CLUSTER INFO
            cluster_state:fail
            cluster_slots_assigned:0            // 被分配槽的个数为0
            cluster_slots_ok:0
            cluster_slots_pfail:0
            cluster_slots_fail:0
            cluster_known_nodes:6
            cluster_size:0
            cluster_current_epoch:5
            cluster_my_epoch:1
            cluster_stats_messages_sent:479
            cluster_stats_messages_received:479
            
      3.2. 通过`cluster addslots`命令分配槽
           
           redis-cli -h 127.0.0.1 -p 6380 cluster addslots {0..5461}
           OK
           redis-cli -h 127.0.0.1 -p 6381 cluster addslots {5462..10922}
           OK
           redis-cli -h 127.0.0.1 -p 6382 cluster addslots {10923..16383}
           OK
      
      3.3. 我们将`16383`个槽平均分配给`6379、6380、6381`端口的节点。再次执行`CLUSTER INFO`查看一下集群的状态：
      
            127.0.0.1:6382> CLUSTER INFO
            cluster_state:ok                // 集群状态OK
            cluster_slots_assigned:16384    // 已经分配了所有的槽
            cluster_slots_ok:16384
            cluster_slots_pfail:0
            cluster_slots_fail:0
            cluster_known_nodes:6
            cluster_size:3
            cluster_current_epoch:5
            cluster_my_epoch:1
            cluster_stats_messages_sent:1212
            cluster_stats_messages_received:1212
       
      3.4 可以通过`CLUSTER NODES`来查看分配情况： 
      
            127.0.0.1:6382> CLUSTER NODES
            274e449a29557f9835ded08c35e36712fc810e43 127.0.0.1:6384@16384 master - 0 1535457331000 12 connected 5462-10922
            54b9e67ba0fdf2619e27ebf23550bfe75ea1b746 127.0.0.1:6383@16383 master - 0  0 1535457333000 7 connected
            dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535457329000 0 connected 10923-16383
            6518b5f73b9d4137d64352cd8e771e07817abe14 127.0.0.1:6381@16381 master - 0  0 1535457333000 12 connected
            b3bd8f5d30d185d6fb6eaf1f4b091738a0c6543d 127.0.0.1:6380@16380 master - 0 1535457332000 7 connected 0-5461
            615f59cd9b6af4f376ada79e6c5c5cbded51ed1d 127.0.0.1:6385@16385 master - 0  0 1535457333334 5 connected
       
      3.5. 目前还有三个节点没有使用，作为一个完整的集群，每个负责处理槽的节点应该具有从节点，保证当主节点出现故障时，
        可以自动进行故障转移。集群模式下，首次启动的节点和被分配槽的节点都是主节点，从节点负责复制主节点槽的信息和相关数据
        使用`cluster replicate <nodeid>`在从节点上执行。
         
            redis-cli -h 127.0.0.1 -p 6383 cluster replicate b3bd8f5d30d185d6fb6eaf1f4b091738a0c6543d
            OK
            redis-cli -h 127.0.0.1 -p 6384 cluster replicate 6518b5f73b9d4137d64352cd8e771e07817abe14
            OK
            redis-cli -h 127.0.0.1 -p 6385 cluster replicate dab02bf322e5aa9ce802579a075bd8b5afe7e946
            OK
      
      3.6. 通过`CLUSTER NODES`可以查看集群节点的状态,这样就完成了一个`3`主`3`从的`redis`集群搭建   
            
            274e449a29557f9835ded08c35e36712fc810e43 127.0.0.1:6384@16384 master - 0 1535457331000 12 connected 5462-10922
            54b9e67ba0fdf2619e27ebf23550bfe75ea1b746 127.0.0.1:6383@16383 slave b3bd8f5d30d185d6fb6eaf1f4b091738a0c6543d 0 1535457333000 7 connected
            dab02bf322e5aa9ce802579a075bd8b5afe7e946 127.0.0.1:6382@16382 myself,master - 0 1535457329000 0 connected 10923-16383
            6518b5f73b9d4137d64352cd8e771e07817abe14 127.0.0.1:6381@16381 slave 274e449a29557f9835ded08c35e36712fc810e43 0 1535457333000 12 connected
            b3bd8f5d30d185d6fb6eaf1f4b091738a0c6543d 127.0.0.1:6380@16380 master - 0 1535457332000 7 connected 0-5461
            615f59cd9b6af4f376ada79e6c5c5cbded51ed1d 127.0.0.1:6385@16385 slave dab02bf322e5aa9ce802579a075bd8b5afe7e946 0 1535457333334 5 connected

#### redis参考配置

  + 禁止客户端使用的命令 
    + `rename-command flushdb cflushdb`
    + `rename-command flushall cflushall`
    + `rename-command keys ckeys`
    + `rename-command shutdown cshutdown`
    + `rename-command config cconfig`
    + `rename-command slaveof cslaveof`
    + `rename-command sync csync`
    + `rename-command monitor cmonitor`
    + `rename-command save csave`
    + `rename-command bgsave cbgsave`
    + `rename-command bgrewriteaof cbgrewriteaof`
    
  + 需要修改的配置(单机)
    + 禁止绑定`ip(bind)`
    + 关闭`protected-mode`
    + `pidfile /var/run/redis.pid`
    + `logfile /var/log/redis.log`
    + `daemonize yes`
    + `mxmemory 10gb`
    + `maxmemory-policy volatile-lru`
    + `appendonly yes`
    + `appendfsync no`
    + `no-appendfsync-on-rewrite yes`
    + `auto-aof-rewrite-min-size 1gb` 
    + `aof-use-rdb-preamble yes`
    + `save ""`
    + `repl-backlog-size 100mb`
    + `slowlog-log-slower-than 10000`
    + `slowlog-max-len 10000`
    + `timeout 300`
    + `repl-timeout 120`
  + 集群配置
    + `cluster-enabled yes`
    + `cluster-config-file nodes.conf`
    + `cluster-node-timeout 15000`
    + `cluster-slave-validity-factor 0`
    + `cluster-migration-barrier 1`
    + `cluster-require-full-coverage no`
  
  + 修改系统配置(引自redis实战与运维)
    + `echo 511 > /proc/sys/net/core/somaxconn`
      + `tcp`三次握手后，会将接受的连接放入队列中，`tcp-backlog`就是这个队列大小，在`redis`中默认是`511`，但是这个参数又
      受到`linux`内核`/proc/sys/net/core/somaxconn`影响
    + `sysctl vm.overcommit_memory=1`
      + `Linux`对大部分的申请内存的请求回复`yes`，以便能够运行更多的程序。申请内存后，并不会马上使用内存，这种技术叫做`overcommit`
        + `0`：内核检查是否有足够的内存，如果有则内存申请通过，否则失败，错误返回应用进程
        + `1`：表示内核运行超量使用内存知道用完为止
        + `2`：表示内核绝不过量使用内存，即系统整个内存地址空间不能超过`swap+50%`的`RAM`值，`50%`是`overcommit_ratio`默认值
      + 日志中`Background save`代表就是`bgsave`和`bgrewriteaof`，如果当前可用内存不足，操作系统应该如何处理`fork`操作。
      如果`vm.overcommit_memory=0`，代表没有可用内存，就申请内存失败，对应到`Redis`就是执行`fork`失败，在`Redis`日志出现`Cannot allocate memory`
      + 设置合理的`maxmemory`参数，保证机器有`20%-30%`的闲置内存
    + `echo never > /sys/kernel/mm/transparent_hugepage/enabled`
      + `linux kernel`在`2.6.38`增加了`Transparent Huge Pages(THP)`,支持`huge page(2MB)`的页分配大小，默认开启。当开启时
      可以降低`fork`创建进程的速度，单执行`fork`之后，如果开启`THP`，复制页单位由原来的`4KB`变为`2MB`，会大幅增加重写期间父进程内存消耗。
      同时每次写命令引起的复制内存页单位放大了`512`倍，会拖慢写操作的执行时间，导致大量写操作慢查询


#### 引用
  + [working with objects through RedisTemplate](https://docs.spring.io/spring-data/redis/docs/2.1.0.RC1/reference/html/#redis:template)
  + [redis-persistence-from-antirez](http://oldblog.antirez.com/post/redis-persistence-demystified.html)
  + [cluster-tutorial](https://redis.io/topics/cluster-tutorial)
  + [spring data redis](https://docs.spring.io/spring-data/redis/docs/2.1.0.RC1/reference/html)
  + [redis persistence](http://redis.io/topics/persistence)
  + [redis aof rewrite](https://yq.aliyun.com/articles/177819?spm=a2c4e.11153940.blogcont193034.9.2c133524z7U34f)
  + [Meet and Gossip with your neighbors](https://cristian.regolo.cc/2015/09/05/life-in-a-redis-cluster.html)
  + [redis replication](https://redis.io/topics/replication)
  + [Redis Cluster介绍与搭建](https://blog.csdn.net/men_wen/article/details/72853078)