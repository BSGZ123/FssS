package cn.BsKPLu.FssS.config;

import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import cn.BsKPLu.FssS.EfoApplication;
import cn.BsKPLu.FssS.EfoApplicationTest;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * @author pantao
 * @since 2018/1/26
 */
public class SettingConfigTest {

    @Test
    public void testFileSuffixPattern() {
        EfoApplicationTest.setSettings();
        assert Pattern.compile(EfoApplication.settings.getStringUseEval(ConfigConsts.FILE_SUFFIX_MATCH_OF_SETTING)).matcher("jpg").matches();
    }

    @Test
    public void testGetStoragePath() {
        EfoApplicationTest.setSettings();
        System.out.println(SettingConfig.getStoragePath(ConfigConsts.TOKEN_OF_SETTINGS));
    }
}
