package cn.tinsur.mall.service.impl;

import cn.tinsur.mall.pojo.entity.OperLog;
import cn.tinsur.mall.mapper.OperLogMapper;
import cn.tinsur.mall.service.IOperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author Tinsur
 * @since 2026-09-15
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

}
