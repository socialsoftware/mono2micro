/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.communication;

import cn.springcloud.gray.communication.InformationClient;
import cn.springcloud.gray.communication.InformationClientDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableInformationClient
extends InformationClientDecorator {
    private static final Logger log = LoggerFactory.getLogger(RetryableInformationClient.class);
    public static final int DEFAULT_NUMBER_OF_RETRIES = 3;
    private final int numberOfRetries;
    private InformationClient delegate;

    public RetryableInformationClient(int numberOfRetries, InformationClient delegate) {
        this.numberOfRetries = numberOfRetries;
        this.delegate = delegate;
    }

    @Override
    protected <R> R execute(InformationClientDecorator.RequestExecutor<R> requestExecutor) {
        for (int retry = 0; retry < this.numberOfRetries; ++retry) {
            try {
                R retval = requestExecutor.execute(this.delegate);
                if (retry > 0) {
                    log.info("Request execution succeeded on retry #{}", (Object)retry);
                }
                return retval;
            }
            catch (Exception e) {
                log.warn("Request execution failed with message: {}", (Object)e.getMessage());
                continue;
            }
        }
        throw new RuntimeException("Retry limit reached; giving up on completing the request");
    }
}

