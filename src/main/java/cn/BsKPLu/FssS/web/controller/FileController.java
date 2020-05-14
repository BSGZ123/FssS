package cn.BsKPLu.FssS.web.controller;

import cn.BsKPLu.FssS.entity.User;
import cn.BsKPLu.FssS.enums.InterceptorLevel;
import cn.BsKPLu.FssS.modules.constant.ConfigConsts;
import cn.BsKPLu.FssS.service.IFileService;
import cn.BsKPLu.FssS.util.Constants;
import cn.BsKPLu.FssS.util.ControllerUtils;
import cn.BsKPLu.FssS.util.StringConstant;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.BsKPLu.FssS.FssSApplication;
import cn.BsKPLu.FssS.annotation.AuthInterceptor;
import cn.BsKPLu.FssS.util.BeanUtils;
import com.zhazhapan.modules.constant.ValueConsts;
import com.zhazhapan.util.Checker;
import com.zhazhapan.util.FileExecutor;
import com.zhazhapan.util.Formatter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author BsKPLu
 * @since 2019/11/29
 */
@RestController
@RequestMapping("/file")
@Api(value = "/file", description = "文件相关操作")
public class FileController {

    private final IFileService fileService;

    private final HttpServletRequest request;

    private final JSONObject jsonObject;

    @Autowired
    public FileController(IFileService fileService, HttpServletRequest request, JSONObject jsonObject) {
        this.fileService = fileService;
        this.request = request;
        this.jsonObject = jsonObject;
    }

    @ApiOperation(value = "获取我的下载记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "offset", value = "偏移量", required = true), @ApiImplicitParam(name =
            "search", value = "记录匹配（允许为空）")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/user/downloaded", method = RequestMethod.GET)
    public String getUserDownloaded(int offset, String search) {
        User user;//创建User
        user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);
        int userId=user.getId();//获取用户id
        List list=fileService.listUserDownloaded(userId, offset, search);//list集合获取下载数据
        return Formatter.listToJson(list);
    }

    @ApiOperation(value = "获取我的上传记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "offset", value = "偏移量", required = true), @ApiImplicitParam(name =
            "search", value = "记录匹配（允许为空）")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/user/uploaded", method = RequestMethod.GET)
    public String getUserUploaded(int offset, String search) {
        User user = (User) request.getSession().getAttribute(Constants.USER_STRING);
        return Formatter.listToJson(fileService.listUserUploaded(user.getId(), offset, search));
    }

    //私有文件查询
    @ApiOperation(value = "获取我的私有文件")
    @ApiImplicitParam(name = "offset",value = "偏移量",required = true)
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/user/private", method = RequestMethod.GET)
    public String getUserPrivate(int offset){

        return "666";
    }
    @ApiOperation(value = "文件上传")
    @ApiImplicitParams({@ApiImplicitParam(name = "categoryId", value = "分类ID", required = true), @ApiImplicitParam
            (name = "tag", value = "文件标签"), @ApiImplicitParam(name = "description", value = "文件描述"),
            @ApiImplicitParam(name = "prefix", value = "文件前缀（仅适用于管理员上传文件，普通用户无效）"),@ApiImplicitParam(
                    name= "private",value = "文件私有选项（拥有基本权限的用户都可以选中）")})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "", method = RequestMethod.POST)
    public String upload(int categoryId, String tag, String description, String prefix,String privates,@RequestParam("file")
            MultipartFile mFile) {
        User user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);
        //System.out.println(privates);新功能测试
        JSONObject jsonObject = new JSONObject();
        Boolean isYes=fileService.upload(categoryId, tag, description, prefix, mFile, user);
        if (isYes) {
            jsonObject.put(StringConstant.STRING_STATE,StringConstant.STRING_SUCCESS);
        } else {
            jsonObject.put(StringConstant.STRING_STATE,StringConstant.STRING_ERROR_STATE);
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "获取文件记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "offset", value = "偏移量", required = true), @ApiImplicitParam(name =
            "categoryId", value = "分类ID", required = true), @ApiImplicitParam(name = "orderBy", value = "排序方式",
            required = true, example = "id desc"), @ApiImplicitParam(name = "search", value = "记录匹配（允许为空）")})
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public String getAll(int offset, int categoryId, String orderBy, String search) {
        User user;
        user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);
        boolean canGet = FssSApplication.settings.getBooleanUseEval(StringConstant.ANONYMOUS_VISIBLE_SETTING) ||
                (Checker.isNotNull(user) && user.getIsVisible() == 1);//获取设置，判断用户存在以及对应权限
        if (canGet) {
            int userId = Checker.isNull(user) ? 0 : user.getId();
            List list=fileService.listAll(userId, offset, categoryId, orderBy, search);
            return Formatter.listToJson(list);
        } else {
            jsonObject.put(StringConstant.STRING_ERROR_STATE,StringConstant.STRING_MESSAGE_NOT_VISIBLE_FAILURE);
            return jsonObject.toString();
        }
    }

    @ApiOperation(value = "删除指定文件")
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String removeFile(@PathVariable("id") long id) {
        User user = (User) request.getSession().getAttribute(Constants.USER_STRING);
        jsonObject.put("status", "error");
        if (Checker.isNull(user)) {
            jsonObject.put("message", "请先登录");
        } else if (id < 1) {
            jsonObject.put("message", "格式不合法");
        } else if (fileService.removeFile(user, id)) {
            jsonObject.put("status", "success");
        } else {
            jsonObject.put("message", "删除失败，权限不够，请联系管理员");
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "更新文件属性")
    @ApiImplicitParams({@ApiImplicitParam(name = "name", value = "文件名", required = true), @ApiImplicitParam(name =
            "category", value = "分类名称", required = true), @ApiImplicitParam(name = "tag", value = "文件标签", required =
            true), @ApiImplicitParam(name = "description", value = "文件描述", required = true)})
    @AuthInterceptor(InterceptorLevel.USER)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String updateFileInfo(@PathVariable("id") long id, String name, String category, String tag, String
            description) {
        User user;
        user = (User) request.getSession().getAttribute(StringConstant.STRING_SESSION_USER);//获取Session中的字段User数据
        Boolean canUpdate=fileService.updateFileInfo(id, user, name, category, tag, description);//提交数据获得更新结果
        jsonObject.put(StringConstant.STRING_LOGIN_STATE,StringConstant.STRING_ERROR_STATE);
        if (canUpdate) {
            jsonObject.put(StringConstant.STRING_STATE,StringConstant.STRING_UPDATE_SUCCESS);
        } else {
            jsonObject.put(StringConstant.STRING_MESSAGE, StringConstant.STRING_MESSAGE_NOT_UPDATE_FAILURE);
        }
        return jsonObject.toString();
    }

    @ApiOperation(value = "获取所有文件的基本信息")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "指定用户（默认所有用户）"), @ApiImplicitParam(name = "file",
            value = "指定文件（默认所有文件）"), @ApiImplicitParam(name = "category", value = "指定分类（默认所有分类）"), @ApiImplicitParam
            (name = "offset", value = "偏移量", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/basic/all", method = RequestMethod.GET)
    public String getBasicAll(String user, String file, String category, int offset) {
        return Formatter.listToJson(fileService.listBasicAll(user, file, category, offset));
    }

    @ApiOperation(value = "通过文件路径获取服务器端的文件")
    @ApiImplicitParam(name = "path", value = "文件路径（默认根目录）")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/server", method = RequestMethod.GET)
    public String getServerFilesByPath(String path) {
        File[] files = FileExecutor.listFile(Checker.isEmpty(path) ? (Checker.isWindows() ? "C:\\" : "/") : path);
        JSONArray array = new JSONArray();
        if (Checker.isNotNull(files)) {
            for (File file : files) {
                array.add(BeanUtils.beanToJson(file));
            }
        }
        return array.toJSONString();
    }

    @ApiOperation("分享服务器端文件")
    @ApiImplicitParams({@ApiImplicitParam(name = "prefix", value = "自定义前缀（可空）"), @ApiImplicitParam(name = "files",
            value = "文件", required = true, example = "file1,file2,file3")})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/server/share", method = RequestMethod.POST)
    public String shareFile(String prefix, String files) {
        User user = (User) request.getSession().getAttribute(Constants.USER_STRING);
        return ControllerUtils.getResponse(fileService.shareFiles(Checker.checkNull(prefix), files, user));
    }

    @ApiOperation(value = "更新文件路径（包括本地路径，访问路径，如果新的本地路径和访问路径均为空，这什么也不会做）")
    @ApiImplicitParams({@ApiImplicitParam(name = "oldLocalUrl", value = "文件本地路径", required = true), @ApiImplicitParam
            (name = "localUrl", value = "新的本地路径（可空）"), @ApiImplicitParam(name = "visitUrl", value = "新的访问路径（可空）")})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/url", method = RequestMethod.PUT)//@PathVariable注解 用于将前端传来的数据绑定在连接地址中
    public String uploadFileUrl(@PathVariable("id") int id, String oldLocalUrl, String localUrl, String visitUrl) {
        boolean result [] = fileService.updateUrl(id, oldLocalUrl, localUrl, visitUrl);
        String responseJsonDate = "{"+StringConstant.STRING_STATE+":{localUrl:" + result[0] + ",visitUrl:" + result[1] + "}}";
        //拼凑返回给前端的结果字符串
        return Formatter.formatJson(responseJsonDate);
    }

    @ApiOperation(value = "批量删除文件")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/batch/{ids}", method = RequestMethod.DELETE)
    public String deleteFiles(@PathVariable("ids") String ids) {
        return ControllerUtils.getResponse(fileService.deleteFiles(ids));
    }

    @ApiOperation(value = "获取指定文件的权限记录")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.GET)
    public String getAuth(@PathVariable("id") long id) {
        return BeanUtils.toPrettyJson(fileService.getAuth(id));
    }

    @ApiOperation(value = "更新指定文件的权限")
    @ApiImplicitParam(name = "auth", value = "权限", required = true, example = "1,1,1,1")
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "/{id}/auth", method = RequestMethod.PUT)
    public String updateAuth(@PathVariable("id") long id, String auth) {
        return ControllerUtils.getResponse(fileService.updateAuth(id, auth));
    }

    /**
     * 资源下载
     *
     * @param response {@link HttpServletResponse}
     */
    @ApiOperation(value = "通过访问路径获取文件资源")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public void getResource(HttpServletResponse response) throws IOException {
        ControllerUtils.loadResource(response, fileService.getResource(request.getServletPath(), request),
                Constants.FALSE);
    }

    /**
    *
    *文件tag搜索
     *
     * @return
     */
    @ApiOperation(value ="通过tag搜索文件")
    @AuthInterceptor(InterceptorLevel.NONE)
    @RequestMapping(value = "/search",method = RequestMethod.GET)
    public String getTagFile(HttpServletRequest request) {

        String tag = request.getParameter("tag");
        return  Formatter.listToJson(fileService.listSearch(tag));
    }
}
