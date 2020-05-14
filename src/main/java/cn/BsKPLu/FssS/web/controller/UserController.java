package cn.BsKPLu.FssS.web.controller;

import cn.BsKPLu.FssS.config.TokenConfig;
import cn.BsKPLu.FssS.entity.User;
import cn.BsKPLu.FssS.enums.InterceptorLevel;
import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import cn.BsKPLu.FssS.modules.constant.DefaultValues;
import cn.BsKPLu.FssS.service.IUserService;
import cn.BsKPLu.FssS.util.Constants;
import cn.BsKPLu.FssS.util.ControllerUtils;
import cn.BsKPLu.FssS.util.StringConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import cn.BsKPLu.FssS.FssSApplication;
import cn.BsKPLu.FssS.annotation.AuthInterceptor;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.Formatter;
import com.zhazhapan.util.encryption.JavaEncrypt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @author BsKPLu
 * @since 2020/1/22
 */
@RestController
@RequestMapping("/user")
@Api(value = "/user", description = "用户相关操作")
public class UserController {

    private final IUserService userService;

    private final HttpServletRequest request;

    private final JSONObject jsonObject;

    @Autowired
    public UserController(IUserService userService, HttpServletRequest request, JSONObject jsonObject) {
        this.userService = userService;
        this.request = request;
        this.jsonObject = jsonObject;
    }

    @ApiOperation(value = "更新用户权限（注：不是文件权限）")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/{permission}", method = RequestMethod.PUT)
    public String updatePermission(@PathVariable("id") int id, @PathVariable("permission") int permission) {
        User user;//创建User
        user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);//获取session中字段user数据
        if (user.getPermission() < 3 && permission > 1) {//用户身份：0不允许登录，1普通用户，2管理员用户
            jsonObject.put(StringConstant.STRING_MESSAGE, "权限不够，设置失败");
        } else if (userService.updatePermission(id, permission)) {
            jsonObject.put(StringConstant.STRING_MESSAGE, "更新成功");
        } else {
            jsonObject.put(StringConstant.STRING_MESSAGE, "更新失败，请稍后重新尝试");
        }
        return jsonObject.toJSONString();
    }

    @ApiOperation("重置用户密码（管理员接口）")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/reset/{id}/{password}", method = RequestMethod.PUT)
    public String resetPassword(@PathVariable("id") int id, @PathVariable("password") String password) {
        return ControllerUtils.getResponse(userService.resetPassword(id, password));
    }

    @ApiOperation(value = "更新用户的默认文件权限")
    @ApiImplicitParam(name = "auth", value = "权限", example = "1,1,1,1", required = true)
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.PUT)
    public String updateFileAuth(@PathVariable("id") int id, String auth) {
        return ControllerUtils.getResponse(userService.updateFileAuth(id, auth));
    }

    @ApiOperation(value = "获取所有用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "指定用户（默认所有用户）"), @ApiImplicitParam(name = "offset",
            value = "偏移量", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getUser(String user, int offset) {
        User u = (User) request.getSession().getAttribute(Constants.USER_STRING);
        return Formatter.listToJson(userService.listUser(u.getPermission(), user, offset));
    }

    @ApiOperation(value = "更新我的基本信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "avatar", value = "头像（可空）"), @ApiImplicitParam(name = "realName",
            value = "真实姓名（可空）"), @ApiImplicitParam(name = "email", value = "邮箱（可空）"), @ApiImplicitParam(name =
            "code", value = "验证码（可空）")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/info", method = RequestMethod.PUT)
    public String updateBasicInfo(String avatar, String realName, String email, String code) {
        User user = (User) request.getSession().getAttribute(Constants.USER_STRING);
        jsonObject.put("message", "保存成功");
        boolean emilVerify = FssSApplication.settings.getBooleanUseEval(ConfigConsts.EMAIL_VERIFY_OF_SETTINGS);
        if (Checker.isNotEmpty(email) && !email.equals(user.getEmail())) {
            if (!emilVerify || isCodeValidate(code)) {
                if (userService.emailExists(email)) {
                    jsonObject.put("message", "邮箱更新失败，该邮箱已经存在");
                } else {
                    user.setEmail(email);
                }
            } else {
                jsonObject.put("message", "邮箱更新失败，验证码校验失败");
            }
        }
        if (userService.updateBasicInfoById(user.getId(), avatar, realName, user.getEmail())) {
            user.setAvatar(avatar);
            user.setRealName(realName);
            jsonObject.put("status", "success");
        } else {
            jsonObject.put("message", "服务器发生错误，请稍后重新尝试");
        }
        jsonObject.put("email", user.getEmail());
        return jsonObject.toString();
    }

    @ApiOperation(value = "更新我的密码")
    @ApiImplicitParams({@ApiImplicitParam(name = "oldPassword", value = "原密码", required = true), @ApiImplicitParam
            (name = "newPassword", value = "新密码", required = true)})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    public String updatePassword(String oldPassword, String newPassword) {
        User user = (User) request.getSession().getAttribute(Constants.USER_STRING);
        jsonObject.put("status", "error");
        try {
            if (user.getPassword().equals(JavaEncrypt.sha256(oldPassword))) {
                if (userService.updatePasswordById(newPassword, user.getId())) {
                    jsonObject.put("status", "success");
                    TokenConfig.removeTokenByValue(user.getId());
                } else {
                    jsonObject.put("message", "新密码格式不正确");
                }
            } else {
                jsonObject.put("message", "原密码不正确");
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            jsonObject.put("message", "服务器内部错误，请稍后重新尝试");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "获取我的基本信息")
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public String getInfo() {
        User user;//创建user对象
        user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);//获取Session中字段user的数据
        JSONObject obj = JSON.parseObject(user.toString());//将user数据转换String，存入map集合中，方便下面脱敏
        obj.remove(StringConstant.STRING_USER_ID);//map集合移除key值为用户id
        obj.remove(StringConstant.STRING_USER_PASSWORD);//map集合移除key值为用户password
        return obj.toString();
    }

    @ApiOperation(value = "登录（用户名密码和token必须有一个输入）")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名"), @ApiImplicitParam(name
            = "password", value = "密码"), @ApiImplicitParam(name = "auto", value = "是否自动登录", dataType = "Boolean"),
            @ApiImplicitParam(name = "token", value = "用于自动登录")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/login", method = RequestMethod.PUT)
    public String login(String username, String password, boolean aloin, String token) {
        //密码登录，停用token自动登录
        User user;//声明User
        user=userService.login(username, password, StringConstant.STRING_NULL,Constants.NULL_RESPONSE);//后端传递参数查询数据库
        Boolean isNull=Checker.isNull(user);//判断账户是否空
        Boolean isPermission=user.getPermission() < 1;//判断用户身份
        if (isNull || isPermission) {
            jsonObject.put(StringConstant.STRING_LOGIN_STATE,StringConstant.STRING_LOGIN_FAILURE);
        } else {
            request.getSession().setAttribute(StringConstant.STRING_SESSION_USER, user);//设定全局变量
            jsonObject.put(StringConstant.STRING_LOGIN_STATE, StringConstant.STRING_LOGIN_SUCCESS);//推入成功数据
            if (aloin) {//判断是否自动登录
                String tmpToken=TokenConfig.createToken(token, user.getId());//生成token
                jsonObject.put(StringConstant.STRING_LOGIN_TOKEN,tmpToken);//推入token数据
            } else {
                jsonObject.put(StringConstant.STRING_LOGIN_TOKEN,StringConstant.STRING_NULL);
                TokenConfig.removeTokenByValue(user.getId());
            }
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "用户注册（当不需要验证邮箱时，邮箱和验证码可空）")
    @ApiImplicitParams({@ApiImplicitParam(name = "username", value = "用户名", required = true), @ApiImplicitParam(name
            = "email", value = "邮箱"), @ApiImplicitParam(name = "password", value = "密码", required = true),
            @ApiImplicitParam(name = "code", value = "验证码")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(String username, String email, String password, String code) {
        boolean emilVerify = FssSApplication.settings.getBooleanUseEval(ConfigConsts.EMAIL_VERIFY_OF_SETTINGS);
        jsonObject.put("status", "error");
        if (!emilVerify || isCodeValidate(code)) {
            if (userService.usernameExists(username)) {
                jsonObject.put("message", "用户名已经存在");
            } else if (userService.emailExists(email)) {
                jsonObject.put("message", "该邮箱已经被注册啦");
            } else if (userService.register(username, email, password)) {
                jsonObject.put("status", "success");
            } else {
                jsonObject.put("message", "数据格式不合法");
            }
        } else {
            jsonObject.put("message", "验证码校验失败");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "重置我的密码")
    @ApiImplicitParams({@ApiImplicitParam(name = "email", value = "邮箱", required = true), @ApiImplicitParam(name =
            "code", value = "验证码", required = true), @ApiImplicitParam(name = "password", value = "密码", required =
            true)})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/password/reset", method = RequestMethod.PUT)
    public String resetPassword(String email, String code, String password) {
        jsonObject.put("status", "error");
        if (isCodeValidate(code)) {
            if (userService.resetPasswordByEmail(email, password)) {
                jsonObject.put("status", "success");
            } else {
                jsonObject.put("message", "格式不合法");
            }
        } else {
            jsonObject.put("message", "验证码校验失败");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "检测用户名是否已经注册")
    @ApiImplicitParam(name = "username", value = "用户名", required = true)
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/username/exists", method = RequestMethod.GET)
    public String usernameExists(String username) {
        jsonObject.put("exists", userService.usernameExists(username));
        return jsonObject.toString();
    }

    @ApiOperation(value = "检测邮箱是否已经注册")
    @ApiImplicitParam(name = "email", value = "邮箱", required = true)
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/email/exists", method = RequestMethod.GET)
    public String emailExists(String email) {
        jsonObject.put("exists", userService.emailExists(email));
        return jsonObject.toString();
    }

    private boolean isCodeValidate(String code) {
        return Checker.checkNull(code).equals(String.valueOf(request.getSession().getAttribute(DefaultValues
                .CODE_STRING)));
    }
}
