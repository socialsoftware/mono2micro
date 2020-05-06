/*
 * Decompiled with CFR 0.146.
 */
package cn.springcloud.gray.eureka.server.communicate;

import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClient;
import cn.springcloud.gray.eureka.server.communicate.GrayCommunicateClientDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableGrayCommunicateClient
extends GrayCommunicateClientDecorator {
    private static final Logger log = LoggerFactory.getLogger(RetryableGrayCommunicateClient.class);
    public static final int DEFAULT_NUMBER_OF_RETRIES = 3;
    private final int numberOfRetries;
    private GrayCommunicateClient delegate;

    public RetryableGrayCommunicateClient(int numberOfRetries, GrayCommunicateClient delegate) {
        this.numberOfRetries = numberOfRetries;
        this.delegate = delegate;
    }

    @Override
    protected <R> R execute(GrayCommunicateClientDecorator.RequestExecutor<R> requestExecutor) {
        for (int retry = 0; retry < this.numberOfRetries; ++retry) {
            try {
                R retval = requestExecutor.execute(this.delegate);
                if (retry > 0) {
                    log.info("Request execution succeeded on retry #{}", (Object)retry);
                }
                return retval;
            }
            catch (Exception e) {
                log.warn("{} Request execution failed with message: {}", (Object)requestExecutor.getRequestType(), (Object)e.getMessage());
                continue;
            }
        }
        throw new RuntimeException("Retry limit reached; giving up on completing the request");
    }
}

