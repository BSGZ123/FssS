package cn.BsKPLu.FssS.web.controller;

import cn.BsKPLu.FssS.enums.InterceptorLevel;
import cn.BsKPLu.FssS.service.IDownloadedService;
import cn.BsKPLu.FssS.annotation.AuthInterceptor;
import com.zhazhapan.util.Formatter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author pantao
 * @since 2018/2/9
 */
@RestController
@RequestMapping(value = "/downloaded")
@Api(value = "/downloaded", description = "下载记录相关操作")
public class DownloadedController {

    private final IDownloadedService downloadService;

    @Autowired
    public DownloadedController(IDownloadedService downloadService) {
        this.downloadService = downloadService;
    }

    @ApiOperation(value = "获取文件下载记录")
    @ApiImplicitParams({@ApiImplicitParam(name = "user", value = "指定用户（默认所有用户）"), @ApiImplicitParam(name =
            "指定文件（默认所有文件）"), @ApiImplicitParam(name = "category", value = "指定分类（默认所有分类）"), @ApiImplicitParam(name =
            "offset", value = "偏移量", required = true)})
    @AuthInterceptor(InterceptorLevel.ADMIN)
    @RequestMapping(value = "all", method = RequestMethod.GET)
    public String getAll(String user, String file, String category, int offset) {
        List list=downloadService.list(user, file, category, offset);//根据用户名获取下载记录
        return Formatter.listToJson(list);
    }
}
