package com.sise.ccj.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.StringUtil;
import com.sise.ccj.config.redis.RedisUtil;
import com.sise.ccj.constant.RedisConstant;
import com.sise.ccj.exception.ServerException;
import com.sise.ccj.mapper.OrderMapper;
import com.sise.ccj.mapper.PSpaceMapper;
import com.sise.ccj.pojo.common.OrderPO;
import com.sise.ccj.pojo.common.PSpacePO;
import com.sise.ccj.request.OrderRequest;
import com.sise.ccj.service.OrderService;
import com.sise.ccj.utils.DateHelper;
import com.sise.ccj.utils.OrderSnUtils;
import com.sise.ccj.vo.BaseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @ClassName OrderServiceImpl
 * @Description
 * @Author CCJ
 * @Date 2020/2/5 15:29
 **/
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private PSpaceMapper pSpaceMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addOrder() {

    }

    @Override
    public BaseVO queryOrder(OrderRequest param, String dbPrefix) {
        param.setDbPrefix(dbPrefix);
        return BaseVO.builder(orderMapper.queryOrder(param));
    }

    @Override
    public void insertUpdate(OrderPO orderPO, String dbPrefix) {
        orderPO.setDbPrefix(dbPrefix);
        if (StringUtil.isEmpty(orderPO.getOrderSn())) {
            PSpacePO pSpacePO = pSpaceMapper.queryPSpaceById(dbPrefix, orderPO.getPsId());
            JSONArray oldInfo = JSON.parseArray(pSpacePO.getInfo());
            JSONArray newInfo = JSON.parseArray(orderPO.getInfo());
            for (int i = 0; i < newInfo.size(); i++) {
                JSONObject json = newInfo.getJSONObject(i);
                int x = json.getIntValue("x");
                int y = json.getIntValue("y");
                if (oldInfo.getJSONArray(x).getIntValue(y) == 1) {
                    throw new ServerException((x + 1) + "排" + (y + 1) + "位已被抢走");
                }
            }
            orderPO.setOrderSn(OrderSnUtils.createOrderSn());
        }
        Date date = DateHelper.parseYYYY_MM_DD_HH_MM_SS(DateHelper.getTodayEndTime());
        long times = (date.getTime()-System.currentTimeMillis()) /1000;
        // 订单统计
        redisUtil.hmIncrementAndGet(RedisConstant.ORDER_COUNT,
                orderPO.getYId()+"",
                orderPO.getNum(), times);
        redisUtil.hmIncrementAndGet(RedisConstant.ORDER_TOTAL,
                orderPO.getYId()+"",
                orderPO.getTotal(), times);
        orderMapper.insertUpdate(orderPO);
    }
}