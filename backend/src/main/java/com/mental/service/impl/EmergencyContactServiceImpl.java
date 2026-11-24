package com.mental.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mental.entity.EmergencyContact;
import com.mental.mapper.EmergencyContactMapper;
import com.mental.service.EmergencyContactService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyContactServiceImpl extends ServiceImpl<EmergencyContactMapper, EmergencyContact>
        implements EmergencyContactService {

    @Override
    public List<EmergencyContact> getUserContacts(Long userId) {
        // 查询指定用户的所有紧急联系人
        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyContact::getUserId, userId);
        wrapper.orderByDesc(EmergencyContact::getIsPrimary);
        return this.list(wrapper);
    }

    @Override
    public EmergencyContact getPrimaryContact(Long userId) {
        // 查询默认联系人
        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyContact::getUserId, userId)
                .eq(EmergencyContact::getIsPrimary, 1);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean addContact(EmergencyContact contact) {
        // 添加联系人
        return this.save(contact);
    }

    @Override
    @Transactional
    public boolean updateContact(EmergencyContact contact) {
        // 更新联系人
        return this.updateById(contact);
    }

    @Override
    @Transactional
    public boolean deleteContact(Long id) {
        // 删除联系人
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean setPrimaryContact(Long id, Long userId) {
        // 设置默认联系人（先将该用户所有联系人设为非默认，再将指定联系人设为默认）
        LambdaQueryWrapper<EmergencyContact> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmergencyContact::getUserId, userId);

        EmergencyContact updateAll = new EmergencyContact();
        updateAll.setIsPrimary(0);
        this.update(updateAll, wrapper);

        EmergencyContact updateOne = new EmergencyContact();
        updateOne.setId(id);
        updateOne.setIsPrimary(1);
        return this.updateById(updateOne);
    }
}