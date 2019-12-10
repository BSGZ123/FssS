package cn.BsKPLu.FssS;

import cn.BsKPLu.FssS.config.TokenConfig;
import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import cn.BsKPLu.FssS.modules.constant.DefaultValues;
import com.spring4all.swagger.EnableSwagger2Doc;
import com.zhazhapan.config.JsonParser;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.MailSender;
import com.zhazhapan.util.ReflectUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

/**
 * @author pantao
 */
@SpringBootApplication
@EnableSwagger2Doc
@MapperScan("cn.BsKPLu.FssS.dao")
@EnableTransactionManagement
public class EfoApplication {

    public static JsonParser settings = new JsonParser();

    public static List<Class<?>> controllers;

    public static Hashtable<String, Integer> tokens;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        settings.setJsonObject(FileExecutor.read(EfoApplication.class.getResourceAsStream(DefaultValues.SETTING_PATH)));
        MailSender.config(settings.getObjectUseEval(ConfigConsts.EMAIL_CONFIG_OF_SETTINGS));
        controllers = ReflectUtils.getClasses(DefaultValues.CONTROLLER_PACKAGE);
        tokens = TokenConfig.loadToken();
        SpringApplication.run(EfoApplication.class, args);
    }
}
