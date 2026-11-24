package com.mental.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mental.entity.EmergencyContact;
import java.util.List;

public interface EmergencyContactService extends IService<EmergencyContact> {

    List<EmergencyContact> getUserContacts(Long userId);

    EmergencyContact getPrimaryContact(Long userId);

    boolean addContact(EmergencyContact contact);

    boolean updateContact(EmergencyContact contact);

    boolean deleteContact(Long id);

    boolean setPrimaryContact(Long id, Long userId);
}