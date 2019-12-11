package cn.BsKPLu.FssS.interceptor;

import cn.BsKPLu.FssS.entity.User;
import cn.BsKPLu.FssS.enums.InterceptorLevel;
import cn.BsKPLu.FssS.modules.constant.DefaultValues;
import cn.BsKPLu.FssS.service.impl.UserServiceImpl;
import cn.BsKPLu.FssS.FssSApplication;
import cn.BsKPLu.FssS.annotation.AuthInterceptor;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author pantao
 * @since 2018/1/25
 */
public class WebInterceptor implements HandlerInterceptor {

    @Autowired
    UserServiceImpl userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
            Exception {
        String url = request.getServletPath();
        InterceptorLevel level = InterceptorLevel.NONE;
        if (handler instanceof HandlerMethod) {
            AuthInterceptor interceptor = ((HandlerMethod) handler).getMethodAnnotation(AuthInterceptor.class);
            //注解到类上面的注解，无法直接获取，只能通过扫描
            if (Checker.isNull(interceptor)) {
                for (Class<?> type : FssSApplication.controllers) {
                    RequestMapping mapping = type.getAnnotation(RequestMapping.class);
                    if (Checker.isNotNull(mapping)) {
                        for (String path : mapping.value()) {
                            if (url.startsWith(path)) {
                                interceptor = type.getAnnotation(AuthInterceptor.class);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            if (Checker.isNotNull(interceptor)) {
                level = interceptor.value();
            }
        }
        User user = (User) request.getSession().getAttribute(ValueConsts.USER_STRING);
        if (Checker.isNull(user)) {
            //读取token，自动登录
            Cookie cookie = HttpUtils.getCookie(ValueConsts.TOKEN_STRING, request.getCookies());
            if (Checker.isNotNull(cookie)) {
                user = userService.login(ValueConsts.EMPTY_STRING, ValueConsts.EMPTY_STRING, cookie.getValue(),
                        response);
                if (Checker.isNotNull(user)) {
                    request.getSession().setAttribute(ValueConsts.USER_STRING, user);
                }
            }
        }
        if (level != InterceptorLevel.NONE) {
            boolean isRedirect = Checker.isNull(user) || (level == InterceptorLevel.ADMIN && user.getPermission() <
                    2) || (level == InterceptorLevel.SYSTEM && user.getPermission() < 3);
            if (isRedirect) {
                response.sendRedirect(DefaultValues.SIGNIN_PAGE);
                return false;
            }
        }
        return true;
    }
}
