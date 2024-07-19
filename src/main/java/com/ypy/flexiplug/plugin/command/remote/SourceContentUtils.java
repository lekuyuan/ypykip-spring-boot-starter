package com.ypy.flexiplug.plugin.command.remote;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Slf4j
public class SourceContentUtils {


    public static String findInstallDirOfInstance(String instanceName){
        return String.format(" for line in `tac /GBASEDBTTMP/.infxdirs`; do if [ -f $line/etc/.conf.%s ];then echo $line;break; fi; done",instanceName);
    }


    public static List<String> getProfileInfo(InputStream in, String installDir) {
        log.info("解析.conf.实例文件开始");
        List<String> content = new ArrayList<>();
        // 读.conf文件内容
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.add(line);
            }
        } catch (IOException e) {
            log.error("读取.conf文件内容异常 [ {} ]", e.getMessage(), e);
            throw new RuntimeException();
        }
        String contentStr = content.stream().collect(Collectors.joining());
        // 按\u0000拆分
        String[] tags = contentStr.split(String.valueOf((char) 0));
        List<String> tmp = new ArrayList<>();
        for (int i= 0 ; i< tags.length; i++) {
            if(StringUtils.hasLength(tags[i]) && tags[i].startsWith(installDir)) {
                tmp.add(tags[i]);
            }
        }
        log.info("解析.conf.实例文件, 参数: [ {} ]", tmp);
        return tmp;
    }


    public static String getSourceContent(String instanceName, String installDir, String sqlHostsDir) {
        log.info("组装source内容开始");
        StringJoiner command = new StringJoiner(" && ");
        command.add("export GBASEDBTSERVER=" + instanceName);
        command.add("export GBASEDBTDIR=" + installDir);
        command.add("export GBASEDBTSQLHOSTS=" + sqlHostsDir);
        command.add("export ONCONFIG=onconfig." + instanceName);
        command.add("export PATH=" + installDir + "/bin:${PATH}");
        // TODO 暂只支持字符集为 中文UTF-8 的数据库建立ER任务
        command.add("export DB_LOCALE=zh_CN.57372");
        return command.toString();
    }
}
