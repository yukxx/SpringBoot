package com.yukx.spring_boot.controller;

import com.yukx.spring_boot.model.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

/**
 * @Author yukx
 * @Date 2021/6/23
 **/
@Slf4j
@Api(tags = "用户接口")
@RestController
@RequestMapping("/users")
public class UserController {

    @ApiOperation(value = "查询用户", notes = "查询用户信息。")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "查询的用户姓名", required = true),
            @ApiImplicitParam(name = "age", value = "查询的用户岁数", required = false)
    })
    @GetMapping(value = "/{name}")
    public ResponseEntity<User> findUser(@PathVariable(value = "name", required = true) String name,
                                         @RequestParam(value = "age", required = false) Integer age) {
        // 创建模拟数据，然后返回数据
        return ResponseEntity.ok(new User(name, "男", age, new Date()));
    }

    @PostMapping(value = "/")
    @ApiOperation(value = "创建用户", notes = "创建新的用户信息。")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        return ResponseEntity.ok(user.getName() + " created");
    }

    @PutMapping(value = "/")
    @ApiOperation(value = "更新用户", notes = "更新用户信息。")
    public ResponseEntity<User> modifyUser(@RequestBody User user) {
        return ResponseEntity.ok(user);
    }

    @DeleteMapping(value = "/{name}")
    @ApiOperation(value = "删除用户", notes = "删除用户信息。")
    public ResponseEntity<String> deleteUser(@PathVariable(value = "name") String name) {
        return ResponseEntity.ok(name + " deleted");
    }

    @GetMapping(value = "/login/{name}/{value}")
    @ApiOperation(value = "登录", notes = "用户登录。")
    public ResponseEntity<String> login(HttpServletRequest request, @PathVariable(value = "name") String name
            , @PathVariable(value = "value") String value) {
        HttpSession session = request.getSession();
        session.setAttribute(name, value);
        return ResponseEntity.ok("sessionId:" + session.getId() + " name:" + name);
    }

    @GetMapping("/get/{name}")
    @ApiOperation(value = "查看sessioin", notes = "查看session")
    public String getSesseion(HttpServletRequest request, @PathVariable("name") String name) {
        HttpSession session = request.getSession();
        String value = (String) session.getAttribute(name);
        return "sessionId:" + session.getId() + " value:" + value;
    }

    @GetMapping("/testErrorLog")
    @ApiOperation(value = "测试错误日志", notes = "测试错误日志")
    public void testErrorLog() {
        log.info("test");
        log.debug("debug_test");
        int i = 1;
        int b = 0;
        log.error("错误测试",new RuntimeException("错误测试"));
        i = i / b;
    }
}
