package com.usian.service;

import com.sun.org.apache.bcel.internal.generic.NEW;
import com.usian.mapper.LocalMessageMapper;
import com.usian.pojo.LocalMessage;
import com.usian.pojo.LocalMessageExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LocalMessageServiceImpl implements LocalMessageService {

    @Autowired
    private LocalMessageMapper localMessageMapper;

    /**
     * 执行扫描本地消息表的任务状态为0的数据
     * @return
     */
    @Override
    public List<LocalMessage> selectLocalMessageByStatus() {
        LocalMessageExample localMessageExample = new LocalMessageExample();
        LocalMessageExample.Criteria criteria = localMessageExample.createCriteria();
        criteria.andStateEqualTo(0);
        return localMessageMapper.selectByExample(localMessageExample);
    }
}
