package cn.BsKPLu.FssS.web.controller;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/adminStatus")
@Api(value = "/adminStatus", description = "管理员用户状态")
public class AdminController {

    public String postAdminStatus(){
        return "666";
    }
}
