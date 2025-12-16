package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.domain.PayFlow;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author fyt
 * @since 2025-12-13
 */
public interface PayFlowMapper extends BaseMapper<PayFlow> {

    int updateStatus(@Param("orderNo") String orderNo, @Param("status")Integer status, @Param("oldStatus")Integer oldStatus);

}
