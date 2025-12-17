package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.domain.KillActivity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fyt
 * @since 2025-12-17
 */
public interface KillActivityService extends IService<KillActivity> {

    void publish(Long actId);

}
