# SpringCloud常用组件

## 学习步骤：

1. 搭建 Provider 和 Consumer 服务。
2. 使用 RestTemplate 完成远程调用。
3. ==搭建 Eureka Server 服务。==
4. 改造 Provider 和 Consumer 称为 Eureka Client。
5. Consumer 服务 通过从 Eureka Server 中抓取 Provider
   地址 完成 远程调用

## 基础环境搭建

### 创建父工程

创建module -父工程 Spring-cloud-parent![image-20220514221908950](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220514221908950.png)

删除src，目录结构如下![image-20220514222011830](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220514222011830.png)

父结构pom.xml文件

~~~xml
   <!--spring boot 环境 -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.0.RELEASE</version>
        <relativePath/>
    </parent>


    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>
~~~

### 创建服务提供者eureka-provider

#### 创建eureka-provider模块

#### eureka-provider  pom.xml

~~~xml
    <dependencies>

        <!--spring boot web-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>
~~~

#### 创建GoodsController

~~~java
/**
 * Goods Controller 服务提供方
 */

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/findOne/{id}")
    public Goods findOne(@PathVariable("id") int id){

        Goods goods = goodsService.findOne(id);

        return goods;
    }
}
~~~

#### 创建GoodsService

~~~java
/**
 * Goods 业务层
 */
@Service
public class GoodsService {

    @Autowired
    private GoodsDao goodsDao;


    /**
     * 根据id查询
     * @param id
     * @return
     */
    public Goods findOne(int id){
        return goodsDao.findOne(id);
    }
}
~~~

#### 创建GoodsDao

~~~java
/**
 * 商品Dao
 */

@Repository
public class GoodsDao {


    public Goods findOne(int id){
        return new Goods(1,"华为手机",3999,10000);
    }
}
~~~

#### 创建Goods

~~~java
/**
 * 商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Goods {

    private int id;
    private String title;//商品标题
    private double price;//商品价格
    private int count;//商品库存
}
~~~

#### 创建providerApplication

~~~java
/**
 * 启动类
 */

@SpringBootApplication
public class ProviderApp {


    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class,args);
    }
}
~~~

#### application.yml

~~~
server:
  port: 8000
~~~

### 创建服务消费者eureka-consumer

#### 创建eureka-consumer模块

#### eureka-consumer  pom.xml

~~~xml
    <dependencies>

        <!--spring boot web-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

    </dependencies>
~~~

#### 创建OrderController

~~~
/**
 * 服务的调用方
 */

@RestController
@RequestMapping("/order")
public class OrderController {

    @GetMapping("/goods/{id}")
    public Goods findGoodsById(@PathVariable("id") int id){
        System.out.println("findGoodsById..."+id);


		//远程调用Goods服务中的findOne接口
        return null;
    }
}
~~~

#### 创建Goods

#### 创建providerApplication

#### application.yml

~~~
server:
  port: 9000
~~~

## RestTemplate进行远程调用

Spring提供的一种简单便捷的模板类，用于在 java 代码里访问 restful 服务。
其功能与 HttpClient 类似，但是 RestTemplate 实现更优雅，使用更方便。

### 创建RestTemplateConfig类

~~~java
@Configuration
public class RestTemplateConfig {


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
~~~

### 修改OrderController

~~~java
/**
 * 服务的调用方
 */

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/goods/{id}")
    public Goods findGoodsById(@PathVariable("id") int id){
        System.out.println("findGoodsById..."+id);
        /*
            //远程调用Goods服务中的findOne接口
            使用RestTemplate
            1. 定义Bean  restTemplate
            2. 注入Bean
            3. 调用方法
         */

        String url = "http://localhost:8000/goods/findOne/"+id;
        // 3. 调用方法
        Goods goods = restTemplate.getForObject(url, Goods.class);
        return goods;
    }
}
~~~

## eureka进行远程调用

### 创建eureka-server模块

#### 引入 SpringCloud 和 euraka-server 相关依赖

~~~xml
<!-- eureka-server -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
~~~

#### 创建EurekaApp启动类

~~~java
@SpringBootApplication
// 启用EurekaServer
@EnableEurekaServer
public class EurekaApp {

    public static void main(String[] args) {
        SpringApplication.run(EurekaApp.class,args);
    }
}
~~~

#### application.yml文件配置eureka信息

~~~yaml
server:
  port: 8761

# eureka 配置
# eureka 一共有4部分 配置
# 1. dashboard:eureka的web控制台配置
# 2. server:eureka的服务端配置
# 3. client:eureka的客户端配置
# 4. instance:eureka的实例配置


eureka:
  instance:
    hostname: localhost # 主机名
  client:
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka # eureka服务端地址，将来客户端使用该地址和eureka进行通信

    register-with-eureka: false # 是否将自己的路径 注册到eureka上。eureka server 不需要的，eureka provider client 需要
    fetch-registry: false # 是否需要从eureka中抓取路径。eureka server 不需要的，eureka consumer client 需要

~~~

### consumer和provider设置eureka-client

pom.xml

~~~xml
<dependencies>

        <!--spring boot web-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- eureka-client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

    </dependencies>
~~~

yml配置文件：

~~~
server:
  port: 8001


eureka:
  instance:
    hostname: localhost # 主机名
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka # eureka服务端地址，将来客户端使用该地址和eureka进行通信
spring:
  application:
    name: eureka-provider # 设置当前应用的名称。将来会在eureka中Application显示。将来需要使用该名称来获取路径，consumer则改成eureka-consumer
~~~

启动类：

添加@EnableEurekaClient //该注解 在新版本中可以省略

#### 测试是否注入成功

#### 动态获取路径

Consumer启动类添加@EnableDiscoveryClient注解

OrderController修改代码动态获取路径：

~~~
/**
 * 服务的调用方
 */

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/goods/{id}")
    public Goods findGoodsById(@PathVariable("id") int id){
        System.out.println("findGoodsById..."+id);


        /*
            //远程调用Goods服务中的findOne接口
            使用RestTemplate
            1. 定义Bean  restTemplate
            2. 注入Bean
            3. 调用方法
         */

        /*
            动态从Eureka Server 中获取 provider 的 ip 和端口
             1. 注入 DiscoveryClient 对象.激活
             2. 调用方法
         */

        //演示discoveryClient 使用
       List<ServiceInstance> instances = discoveryClient.getInstances("EUREKA-PROVIDER");

        //判断集合是否有数据
        if(instances == null || instances.size() == 0){
            //集合没有数据
            return null;
        }

        ServiceInstance instance = instances.get(0);
        String host = instance.getHost();//获取ip
        int port = instance.getPort();//获取端口

        System.out.println(host);
        System.out.println(port);

        String url = "http://"+host+":"+port+"/goods/findOne/"+id;
        // 3. 调用方法
        Goods goods = restTemplate.getForObject(url, Goods.class);


        return goods;
    }
}
~~~

#### consumer调用provider测试

#### Eureka示例流程步骤：

~~~
1. 搭建 Provider 和 Consumer 服务。
	provider：按照restful风格定义controller，定义service业务	层和dao层。
	consumer：按照restful风格定义controller
2. 使用 RestTemplate 完成远程调用。
	定义RestTemplate配置类；
	在consumer模块的controller中注入RestTemplate，然后调用其		getForObject（url，Pojo.class）:其中url是provider的访问
	路径。
3. 搭建 Eureka Server 服务。（单独的注册中心，需要我们自己来配置）
	pom文件中导入eureka坐标，在启动类上使用注解
    EnableEurekaServer开启服务；
    配置文件中配置：server.port、eureka.instance.hostname、
    eureka.service-url.defaultZone、 register-with-eureka
    和fetch-rigister。
4. 改造 Provider 和 Consumer 称为 Eureka Client。
	在Provider和Consumer的启动类使用注解@EnableEurekaClient
	声明该应用是Eureka的使用者，并且在配置文件中进行eureka相关信
	息的配置，还要配置spring.application.name；
5. Consumer 服务 通过从 Eureka Server 中抓取 Provider
   地址 完成 远程调用
   在启动类上使用注解@EnableDidcoveryClient激活eureka的功能，
   然后直接在controller中注入DiscoveryClient对象，调用其
   getInstance（spring.application.name：eureka上的服务提供
   方在eureka上的名称）获取封装了Provider访问路径的集合。然后就可
   以进行操作了。
~~~

### Eureka属性：

#### instance相关属性

![image-20220524231739749](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220524231739749.png)

Eureka Instance的配置信息全部保存在org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean配置类里，实际上它是com.netflix.appinfo.EurekaInstanceConfig的实现类，替代了netflix的com.netflix.appinfo.CloudInstanceConfig的默认实现。

#### server相关属性

![image-20220524233302015](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220524233302015.png)

Eureka Server注册中心端的配置是对注册中心的特性配置。Eureka Server的配置全部在org.springframework.cloud.netflix.eureka.server.EurekaServerConfigBean里，实际上它是com.netflix.eureka.EurekaServerConfig的实现类，替代了netflix的默认实现。

### Eureka高可用

![image-20220524233439123](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220524233439123.png)

1. 准备两个Eureka Server

2. 分别进行配置，相互注册

3. Eureka Client 分别注册到这两个 Eureka Server中

4. 客户端测试

   

## Feign

 Feign 是一个声明式的 REST 客户端，它用了基于接口的注解方式。使调用远程接口和调用本地接口一样。

### Demo示例：

#### 在消费端引入 open-feign 依赖

~~~xml
   <!--feign-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
~~~

#### 在消费端编写Feign调用接口

~~~java
/**
 *
 * feign声明式接口。发起远程调用的。
 *
 String url = "http://FEIGN-PROVIDER/goods/findOne/"+id;
 Goods goods = restTemplate.getForObject(url, Goods.class);
 *
 * 1. 定义接口
 * 2. 接口上添加注解 @FeignClient,设置value属性为 服务提供者的 应用名称
 * 3. 编写调用接口，接口的声明规则 和 提供方接口保持一致。
 * 4. 注入该接口对象，调用接口方法完成远程调用
 */
@FeignClient(value = "FEIGN-PROVIDER")
public interface GoodsFeignClient {
    @GetMapping("/goods/findOne/{id}")
    public Goods findGoodsById(@PathVariable("id") int id);
}
~~~

#### 修改消费方orderController：

~~~java
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GoodsFeignClient goodsFeignClient;

    @GetMapping("/goods/{id}")
    public Goods findGoodsById(@PathVariable("id") int id){

        /*
        String url = "http://FEIGN-PROVIDER/goods/findOne/"+id;
        // 3. 调用方法
        Goods goods = restTemplate.getForObject(url, Goods.class);
        return goods;*/

        Goods goods = goodsFeignClient.findGoodsById(id);

        return goods;
    }
}
~~~

#### 在启动类添加@EnableFeignClients 注解，开启Feign功能

~~~java
@EnableDiscoveryClient // 激活DiscoveryClient
@EnableEurekaClient
@SpringBootApplication

@EnableFeignClients //开启Feign的功能
public class ConsumerApp {
    public static void main(String[] args) {
        SpringApplication.run(ConsumerApp.class,args);
    }
}
~~~

### Feign超时配置

• Feign 底层依赖于 Ribbon 实现负载均衡和远程调用。
• Ribbon默认1秒超时。
• 超时配置：

feign-consumer  application.yml

~~~yaml
# 设置Ribbon的超时时间
ribbon:
  ConnectTimeout: 1000 # 连接超时时间 默	认1s  默认单位毫秒
  ReadTimeout: 3000 # 逻辑处理的超时时间 默认1s 默认单位毫秒
~~~



## Hystrix

Hystix 是 Netflix 开源的一个延迟和容错库，用于隔离访问远程服务、第三方库，防止出现级联失败（雪崩：一个服务失败，导致整条链路的服务都失败的情形）

### 主要功能：

• 隔离

​		线程池隔离
​		信号量隔离

![image-20220605185603245](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220605185603245.png)

• 降级:异常，超时

![image-20220605185636945](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220605185636945.png)

• 熔断
• 限流

### Hystrix-降级

#### 提供方降级：直接方法提供方，服务提供方降级

引入依赖

~~~xml
        <!-- hystrix -->
         <dependency>
             <groupId>org.springframework.cloud</groupId>
             <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
         </dependency>
~~~

定义降级方法：

~~~java
 /**
     * 定义降级方法：
     *  1. 方法的返回值需要和原方法一样
     *  2. 方法的参数需要和原方法一样
     */
    public Goods findOne_fallback(int id){
        Goods goods = new Goods();
        goods.setTitle("降级了~~~");

        return goods;
    }
~~~

在要配置降级方法的方法上使用 @HystrixCommand 注解配置降级方法

~~~java
/**
     * 降级：
     *  1. 出现异常
     *  2. 服务调用超时
     *      * 默认1s超时
     *
     *  @HystrixCommand(fallbackMethod = "findOne_fallback")
     *      fallbackMethod：指定降级后调用的方法名称
     */
    @GetMapping("/findOne/{id}")
    @HystrixCommand(fallbackMethod = "findOne_fallback",commandProperties = {
            //设置Hystrix的超时时间，默认1s
 @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "3000")
    })
    public Goods findOne(@PathVariable("id") int id){

        //1.造个异常
        int i = 3/0;
        try {
            //2. 休眠2秒
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Goods goods = goodsService.findOne(id);

        goods.setTitle(goods.getTitle() + ":" + port);//将端口号，设置到了 商品标题上
        return goods;
    }
~~~

在启动类上开启Hystrix功能：@EnableCircuitBreaker

~~~java
@EnableCircuitBreaker // 开启Hystrix功能
public class ProviderApp {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApp.class,args);
    }
}
~~~

#### 消费方降级：

feign 组件已经集成了 hystrix 组件；

定义feign 调用接口实现类，复写方法，即 降级方法GoodFeignClient

~~~java
/**
 * Feign 客户端的降级处理类
 * 1. 定义类 实现 Feign 客户端接口
 * 2. 使用@Component注解将该类的Bean加入SpringIOC容器
 */
@Component
public class GoodsFeignClientFallback implements GoodFeignClient {
    @Override
    public Goods findGoodsById(int id) {
        Goods goods = new Goods();
        goods.setTitle("又被降级了~~~");
        return goods;
    }
}
~~~

在 @FeignClient 注解中使用 fallback 属性设置降级处理类。

GoodFeignClient

~~~java
@FeignClient(value = "HYSTRIX-PROVIDER",fallback = GoodsFeignClientFallback.class)
public interface GoodsFeignClient {
    @GetMapping("/goods/findOne/{id}")
    public Goods findGoodsById(@PathVariable("id") int id);

}
~~~

yaml文件配置开启 feign.hystrix.enabled = true

~~~yaml
# 开启feign对hystrix的支持
feign:
  hystrix:
    enabled: true
~~~

### Hystrix-熔断

• Hystrix 熔断机制，用于监控微服务调用情况，当失败的情况达到预定的阈值（5秒失败20次），会打开断路器，拒绝所有请求，直到服务恢复正常为止。

断路器三种状态：打开、半开、关闭

![image-20220606225500199](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220606225500199.png)

代码演示：

• circuitBreaker.sleepWindowInMilliseconds：监控时间
• circuitBreaker.requestVolumeThreshold：失败次数
• circuitBreaker.errorThresholdPercentage：失败率

~~~java
/**
 * Goods Controller 服务提供方
 */

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Value("${server.port}")
    private int port;

    /**
     * 降级：
     *  1. 出现异常
     *  2. 服务调用超时
     *      * 默认1s超时
     *
     *  @HystrixCommand(fallbackMethod = "findOne_fallback")
     *      fallbackMethod：指定降级后调用的方法名称
     */

 @GetMapping("/findOne/{id}")
@HystrixCommand(fallbackMethod = "findOne_fallback",commandProperties = {
         //设置Hystrix的超时时间，默认1s
@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds",value = "3000"),
            //监控时间 默认5000 毫秒
@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "5000"),
            //失败次数。默认20次
 @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "20"),
            //失败率 默认50%
 @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "50") })
    public Goods findOne(@PathVariable("id") int id){
        //如果id == 1 ，则出现异常，id != 1 则正常访问
        if(id == 1){
            //1.造个异常
            int i = 3/0;
        }
        /*try {
            //2. 休眠2秒
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Goods goods = goodsService.findOne(id);

        goods.setTitle(goods.getTitle() + ":" + port);//将端口号，设置到了 商品标题上
        return goods;
    }


    /**
     * 定义降级方法：
     *  1. 方法的返回值需要和原方法一样
     *  2. 方法的参数需要和原方法一样
     */
    public Goods findOne_fallback(int id){
        Goods goods = new Goods();
        goods.setTitle("降级了~~~");

        return goods;
    }

}
~~~

## Gateway

- 网关旨在为微服务架构提供一种简单而有效的统一的API路由管理方式。


- 在微服务架构中，不同的微服务可以有不同的网络地址，各个微服务之间通过互相调用完成用户请求，客户端可能通过调用N个微服务的接口完成一个用户请求。

- 网关就是系统的入口，封装了应用程序的内部结构，为客户端提供统一服务，一些与业务本身功能无关的公共逻辑可以在这里实现，诸如认证、鉴权、监控、缓存、负载均衡、流量管控、路由转发等

- 在目前的网关解决方案里，有Nginx+ Lua、Netflix Zuul 、Spring Cloud Gateway等等

- 存在的问题：

- ~~~
  	1.客户端多次请求不同的微服务，增加客户端的复杂性
    	2.认证复杂，每个服务都要进行认证
    	3.http请求不同服务次数增加，性能不高
  ~~~

  ![image-20220606235342206](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220606235342206.png)



### 代码示例：

#### 搭建网关模块

创建api-gateway-server模块

#### 引入依赖：starter-gateway

~~~xml
 <dependencies>
        <!--引入gateway 网关-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <!-- eureka-client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
    </dependencies>
~~~

#### 编写启动类：

~~~java
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApp.class,args);
    }

}
~~~

#### 编写配置文件application.yml

~~~yml
server:
  port: 80

spring:
  application:
    name: api-gateway-server

  cloud:
    # 网关配置
    gateway:
      # 路由配置：转发规则
      routes: #集合。
      # id: 唯一标识。默认是一个UUID
      # uri: 转发路径
      # predicates: 条件,用于请求网关路径的匹配规则


      - id: gateway-provider
        uri: http://localhost:8000/
        predicates:
        - Path=/Good/**
~~~

#### 启动测试

### 静态路由：application.yml  中的uri是写死的，就是静态路由

~~~yml
server:
  port: 9500

spring:
  application:
    name: api-gateway-server

  cloud:
    # 网关配置
    gateway:
      # 路由配置：转发规则
      routes: #集合。
      # id: 唯一标识。默认是一个UUID
      # uri: 转发路径
      # predicates: 条件,用于请求网关路径的匹配规则
      # filters：配置局部过滤器的
      - id: gateway-provider
        # 静态路由
        uri: http://localhost:8000/
        predicates:
        - Path=/Good/**
~~~

### 动态路由：启动类添加@EnableEurekaClient（新版本不加也可以）

~~~java
@SpringBootApplication
@EnableEurekaClient
public class ApiGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApp.class,args);
    }

}
~~~

#### 引入eureka-client配置

#### application.yml  中修改uri属性：uri: lb://服务名称

~~~java
server:
  port: 9500

spring:
  application:
    name: api-gateway-server

  cloud:
    # 网关配置
    gateway:
      # 路由配置：转发规则
      routes: #集合。
      # id: 唯一标识。默认是一个UUID
      # uri: 转发路径
      # predicates: 条件,用于请求网关路径的匹配规则
      # filters：配置局部过滤器的

      - id: eureka-provider
      	# 静态路由
        # uri: http://localhost:8000/
        # 动态路由
        uri: lb://EUREKA-PROVIDER
        predicates:
        - Path=/Good/**
~~~

## 4.5-Gateway-微服务名称配置



![1587545848975](C:/javaEE就业班/课程学习/springboot+springcloud/10.28课程学习/day02/讲义/img/1587545848975.png)



application.yml中配置微服务名称配置

```yaml
      # 微服务名称配置
      discovery:
        locator:
          enabled: true # 设置为true 请求路径前可以添加微服务名称
          lower-case-service-id: true # 允许为小写
```

## 4.6-Gateway-过滤器

### 4.6.1-过滤器-概述

- Gateway 支持过滤器功能，对请求或响应进行拦截，完成一些通用操作。

- Gateway 提供两种过滤器方式：“pre”和“post”

  ​      **pre 过滤器**，在转发之前执行，可以做参数校验、权限校验、流量监控、日志输出、协议转换等。
  ​      **post 过滤器**，在响应之前执行，可以做响应内容、响应头的修改，日志的输出，流量监控等。

  

-  Gateway 还提供了两种类型过滤器
       **GatewayFilter**：局部过滤器，针对单个路由
       **GlobalFilter** ：全局过滤器，针对所有路由

![image-20220612121409624](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220612121409624.png)

### 4.6.2-局部过滤器

-  GatewayFilter 局部过滤器，是针对单个路由的过滤器。
-  在Spring Cloud Gateway 组件中提供了大量内置的局部过滤器，对请求和响应做过滤操作。
-  遵循约定大于配置的思想，只需要在配置文件配置局部过滤器名称，并为其指定对应的值，就可以让其生效。



具体配置参见**gateway内置过滤器工厂.md**



测试配置

api-gateway-server   application.yml

```yaml
server:
  port: 80
spring:
  application:
    name: api-gateway-server
  cloud:
    # 网关配置
    gateway:
      # 路由配置：转发规则
      routes: #集合。
      # id: 唯一标识。默认是一个UUID
      # uri: 转发路径
      # predicates: 条件,用于请求网关路径的匹配规则
      # filters：配置局部过滤器的

      - id: gateway-provider
        # 静态路由
        # uri: http://localhost:8001/
        # 动态路由
        uri: lb://GATEWAY-PROVIDER
        predicates:
        - Path=/goods/**
        filters:
        - AddRequestParameter=username,zhangsan
```

gateway-provider模块中GoodsController中的findOne添加username参数

```java
 public Goods findOne(@PathVariable("id") int id,String username){

        System.out.println(username);

        //如果id == 1 ，则出现异常，id != 1 则正常访问
        if(id == 1){
            //1.造个异常
            int i = 3/0;
        }

        /*try {
            //2. 休眠2秒
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        Goods goods = goodsService.findOne(id);

        goods.setTitle(goods.getTitle() + ":" + port);//将端口号，设置到了 商品标题上
        return goods;
    }
```



### 4.6.3-全局过滤器

- GlobalFilter 全局过滤器，不需要在配置文件中配置，系统初始化时加载，并作用在每个路由上。
- Spring Cloud Gateway 核心的功能也是通过内置的全局过滤器来完成。
- 自定义全局过滤器步骤：

  1. 定义类实现 GlobalFilter 和 Ordered接口
  
  2. 复写方法
  
  3. 完成逻辑处理
  
     



![image-20220612121450266](C:\Users\15605\AppData\Roaming\Typora\typora-user-images\image-20220612121450266.png)



MyFilter

```java
package com.itheima.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class MyFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        System.out.println("自定义全局过滤器执行了~~~");

        return chain.filter(exchange);//放行
    }

    /**
     * 过滤器排序
     * @return 数值越小 越先执行
     */
    @Override
    public int getOrder() {
        return 0;
    }
}

```



