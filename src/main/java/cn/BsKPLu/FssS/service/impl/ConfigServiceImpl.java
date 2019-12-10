package cn.BsKPLu.FssS.service.impl;

import cn.BsKPLu.FssS.service.IConfigService;
import com.alibaba.fastjson.JSONObject;
import cn.BsKPLu.FssS.EfoApplication;
import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import org.springframework.stereotype.Service;

/**
 * @author pantao
 * @since 2018/1/22
 */
@Service
public class ConfigServiceImpl implements IConfigService {

    @Override
    public String getGlobalConfig() {
        JSONObject jsonObject = (JSONObject) EfoApplication.settings.getObjectUseEval(ConfigConsts
                .GLOBAL_OF_SETTINGS).clone();
        jsonObject.remove(ConfigConsts.UPLOAD_PATH_OF_GLOBAL);
        jsonObject.remove(ConfigConsts.TOKEN_PATH_OF_GLOBAL);
        jsonObject.remove(ConfigConsts.UPLOAD_FORM_OF_SETTING);
        return jsonObject.toString();
    }

    @Override
    public String getUserConfig() {
        JSONObject jsonObject = (JSONObject) EfoApplication.settings.getObjectUseEval(ConfigConsts.USER_OF_SETTINGS)
                .clone();
        jsonObject.remove(ConfigConsts.EMAIL_CONFIG_OF_USER);
        return jsonObject.toString();
    }
}
