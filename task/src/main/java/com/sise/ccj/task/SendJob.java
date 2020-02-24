package com.sise.ccj.task;

import com.sise.ccj.mapper.OpenOrderMapper;
import com.sise.ccj.task.job.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SendJob implements Job {

    @Autowired
    private OpenOrderMapper openOrderMapper;

    @Override
    public void execute(String db) {
    }
}
