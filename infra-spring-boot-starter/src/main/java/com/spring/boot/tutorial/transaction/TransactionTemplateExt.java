package com.spring.boot.tutorial.transaction;

import com.spring.boot.tutorial.SpringContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>以编程的方式控制事务</p>
 * <pre>
 * public void doWithinTransaction() {
 *     // some business logically
 *     new TransactionTemplateExt().execute(new TransactionInvokerWithinResult() {
 *         {@code @Override}
 *         public List<Person> invoker() {
 *             int suc = userByXmlMapper.updateById(uuid);
 *             if (fail) {
 *                 // rollback if you want
 *                 throw new RuntimeException();
 *             }
 *             return userByXmlMapper.loadById(uuid);
 *          }
 *    });
 *    // set propagation if you want
 *    // TransactionTemplateExt transactionTemplateExt = new TransactionTemplateExt();
 *    // transactionTemplateExt.setPropagation(TransactionDefinition.PROPAGATION_NESTED);
 *    // transactionTemplateExt.execute(invokerWithoutResult)
 * }
 * </pre>
 * @see TransactionInvokerWithinResult
 * @see TransactionInvokerWithoutResult
 * @see TransactionTemplate
 * @author chen
 * 2017/11/29 9:31
 */
public class TransactionTemplateExt {
    private TransactionTemplate transactionTemplate;
    private static final String DEFAULT_CLASS_NAME = TransactionTemplateExt.class.getSimpleName();
    public TransactionTemplateExt() {
        PlatformTransactionManager transactionManager =
                SpringContextHolder.getBean(AbstractPlatformTransactionManager.class);
        transactionTemplate = new TransactionTemplate(transactionManager);
        setTimeout(1800);
        setPropagation(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    }

    private void setTransactionName(String name) {
        transactionTemplate.setName(DEFAULT_CLASS_NAME+":"+name);
    }

    /**
     * 在事务内执行业务，并且带有返回值
     * 如果未抛出异常，默认执行commit操作
     * 如果业务需要做逻辑判断，则需要自己抛出业务异常，防止事务不提交导致程序bug
     * @see #execute(TransactionInvokerWithoutResult)
     * @throws TransactionException
     */
    public <T>T execute(final TransactionInvokerWithinResult<T> invoker) throws TransactionException {
        setTransactionName(invoker.getClass().getName());
        return transactionTemplate.execute(new TransactionCallback<T>() {
            @Override
            public T doInTransaction(TransactionStatus status) {
                return invoker.invoker();
            }
        });
    }

    /**
     * @see #execute(TransactionInvokerWithinResult)
     */
    public void execute(final TransactionInvokerWithoutResult invoker) throws TransactionException {
        setTransactionName(invoker.getClass().getName());
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                invoker.invoker();
                return null;
            }
        });
    }

    /**
     * 设置事务超时时间(以秒为单位)
     * @param second
     */
    public void setTimeout(int second) {
        transactionTemplate.setTimeout(second);
    }

    /**
     * <p>设置事务隔离界别</p>
     * 默认的隔离级别为：TransactionDefinition#PROPAGATION_REQUIRES_NEW
     * @see TransactionDefinition
     * @param propagation
     */
    public void setPropagation(int propagation) {
        transactionTemplate.setPropagationBehavior(propagation);
    }
}

