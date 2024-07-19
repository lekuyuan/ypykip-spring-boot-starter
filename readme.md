## 使用说明
1. 在启动类上配置@EnableGBase8s注解
2. 并在@EnableGBase8s注解属性plugins设置需要的插件进去

> 可以配置多个不同的插件

```java
import com.ypy.flexiplug.annotation.EnableGBase8s;
import com.ypy.flexiplug.plugin.split.impl.SplitService;

@SpringBootApplication
@EnableGBase8s(plugins = {SplitService.class})
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
```

### 开启插件后，即可使用插件中的方法
```java
@Controller
public class TestController {

    @Resource
    private ISplitPlugin iSplitPlugin;

    @GetMapping("test")
    @ResponseBody
    public String test() {
        List<String> split = iSplitPlugin.split("aaa,bb,cc,dd");
        System.out.println(split);
        return "hello";
    }
}
```

### 自定义插件

```java
// 在plugin包下创建插件包，定义插件的接口
// *** i.e.: 如下示例(TestPlugin)
// mkdir test
// cd test
// touch ITestPlugin.java

import com.ypy.flexiplug.mark.Plugin;

import javax.annotation.Resource;

public interface ITestPlugin extends Plugin {
    // 定义插件的功能
    String test();
}

// mkdir impl

// cd impl

// touch TestPlugin.java 

public class TestPlugin implements ITestPlugin {
    public String test() {
        return "test Plugin! ";
    }
}
// 打包到本地仓库或远程maven仓库

// 拉取jar包代码,配置到pom.xml文件中

// 第三方导入此starter 使用
public class ServiceImpl {
    @Resource
    private ITestPlugin testPlugin;


    public void testUse() {
        System.out.println(testPlugin.test());
        // 输出: test Plugin!
    }
}
```

### 插件列表