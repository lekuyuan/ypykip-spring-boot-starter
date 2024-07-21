## 使用说明
1. 在启动类上配置@EnableFlexPlug注解
2. 并在@EnableFlexPlug注解属性plugins设置需要的插件进去

> 可以配置多个不同的插件

```java
import com.ypy.flexiplug.annotation.EnableFlexPlug;
import com.ypy.flexiplug.plugin.split.impl.SplitService;

@SpringBootApplication
@EnableFlexPlug(plugins = {SplitService.class})
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
- [x] SplitService -> ISplitPlugin
- [x] GlobalExceptionPlugin 全局异常插件
- [x] localePlugin 国际化插件
- [x] LocalFileStorePlugin 本地文件存储插件
- [x] DefaultSecretProcessor 默认加密插件
- [x] ApacheCommandExecPlugin 本地命令执行插件
- [x] DefaultSecretPlugin 默认对称加密插件

### 关于我
A Java developer, dream to make code <span style="color:red;font-weight:bold;">simple</span>!<br>
Also, try to learn and apply go language to programming!
Contact me using the following <span style="color:red;font-weight:bold;">wechat: LeKu_yuan</span>!<br>

<img src="./imgs/wechat.jpg" style="width: 200px;height:200px;border-radius: 55px;margin-top:20px;margin-left: 60px;display:flex;justify-content: center;align-items: center;">
