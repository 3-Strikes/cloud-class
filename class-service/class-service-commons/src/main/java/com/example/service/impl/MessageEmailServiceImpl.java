package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.domain.MessageEmail;
import com.example.mapper.MessageEmailMapper;
import com.example.service.MessageEmailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fyt
 * @since 2025-12-10
 */
@Service
public class MessageEmailServiceImpl extends ServiceImpl<MessageEmailMapper, MessageEmail> implements MessageEmailService {

}
