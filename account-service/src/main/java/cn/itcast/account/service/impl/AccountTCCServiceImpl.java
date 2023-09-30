package cn.itcast.account.service.impl;

import cn.itcast.account.entity.AccountFreeze;
import cn.itcast.account.mapper.AccountFreezeMapper;
import cn.itcast.account.mapper.AccountMapper;
import cn.itcast.account.service.AccountTCCService;
import io.seata.core.context.RootContext;
import io.seata.rm.tcc.api.BusinessActionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Description: AccountTCCServiceImpl
 * <br></br>
 * className: AccountTCCServiceImpl
 * <br></br>
 * packageName: cn.itcast.account.service.impl
 *
 * @author jinhui-huang
 * @version 1.0
 * @email 2634692718@qq.com
 * @Date: 2023/9/29 15:27
 */
@Slf4j
@Service
public class AccountTCCServiceImpl implements AccountTCCService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AccountFreezeMapper freezeMapper;

    @Override
    @Transactional
    public void deduct(String userId, int money) {
        /*0. 获取事务id*/
        String xid = RootContext.getXID();
        /*解决业务悬挂问题*/
        /*判断freeze中是否有冻结记录, 如果有, 一定是CANCEL执行过, 拒绝业务*/
        AccountFreeze oldFreeze = freezeMapper.selectById(xid);
        if (oldFreeze != null) {
            /*CANCEL执行过, 拒绝业务*/
            return;
        }
        /*1. 扣减可用余额*/
        accountMapper.deduct(userId, money);
        /*2. 记录冻结金额, */
        AccountFreeze freeze = new AccountFreeze();
        freeze.setUserId(userId);
        freeze.setFreezeMoney(money);
        freeze.setState(AccountFreeze.State.TRY);
        freeze.setXid(xid);
        freezeMapper.insert(freeze);
    }

    @Override
    public boolean confirm(BusinessActionContext context) {
        /*1. 获取事务id*/
        String xid = context.getXid();
        /*2. 根据id删除冻结记录*/
        int count = freezeMapper.deleteById(xid);
        return count == 1;
    }

    @Override
    public boolean cancel(BusinessActionContext context) {
        String xid = context.getXid();
        AccountFreeze freeze = freezeMapper.selectById(xid);
        String userId = Objects.requireNonNull(context.getActionContext("userId")).toString();

        /*0. 空回滚判断, 判断freeze是否为null, 为null证明try没执行, 需要空回滚*/
        if (freeze == null) {
            /*保存空回滚的记录*/
            freeze = new AccountFreeze();
            freeze.setUserId(userId);
            freeze.setFreezeMoney(0);
            freeze.setState(AccountFreeze.State.CANCEL);
            freeze.setXid(xid);
            freezeMapper.insert(freeze);
            return true;
        }
        /*判断幂等, 一个业务在事务中只能执行一次*/
        if (freeze.getState() == AccountFreeze.State.CANCEL) {
            /*已经处理过一次, 无需再次执行*/
            return true;
        }
        /*1. 恢复可用余额*/
        accountMapper.refund(freeze.getUserId(), freeze.getFreezeMoney());
        /*2. 将冻结金额清零, 状态改为CANCEL*/
        freeze.setFreezeMoney(0);
        freeze.setState(AccountFreeze.State.CANCEL);
        /*3. 更新*/
        int count = freezeMapper.updateById(freeze);
        return count == 1;
    }
}
