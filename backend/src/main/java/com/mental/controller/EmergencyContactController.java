package com.mental.controller;

import com.mental.common.Result;
import com.mental.entity.EmergencyContact;
import com.mental.service.EmergencyContactService;
import com.mental.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyContactController {

    @Resource
    private EmergencyContactService emergencyContactService;

    @Resource
    private JwtUtil jwtUtil;  // 👈 注入 JwtUtil，而不是静态调用

    @GetMapping("/list")
    public Result<List<EmergencyContact>> getList(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);  // 👈 改为 jwtUtil.xxx()
        if (userId == null) {
            return Result.error("未登录");
        }
        List<EmergencyContact> contacts = emergencyContactService.getUserContacts(userId);
        return Result.success(contacts);
    }

    @GetMapping("/primary")
    public Result<EmergencyContact> getPrimary(HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        EmergencyContact contact = emergencyContactService.getPrimaryContact(userId);
        return Result.success(contact);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody EmergencyContact contact, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        contact.setUserId(userId);
        if (emergencyContactService.addContact(contact)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody EmergencyContact contact) {
        if (emergencyContactService.updateContact(contact)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (emergencyContactService.deleteContact(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }

    @PutMapping("/setPrimary/{id}")
    public Result<String> setPrimary(@PathVariable Long id, HttpServletRequest request) {
        Long userId = jwtUtil.getUserIdFromRequest(request);
        if (userId == null) {
            return Result.error("未登录");
        }
        if (emergencyContactService.setPrimaryContact(id, userId)) {
            return Result.success("设置成功");
        }
        return Result.error("设置失败");
    }
}