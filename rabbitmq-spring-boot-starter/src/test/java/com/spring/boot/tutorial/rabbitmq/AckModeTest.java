package com.spring.boot.tutorial.rabbitmq;

import org.junit.Test;
import org.springframework.amqp.core.AcknowledgeMode;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.amqp.core.AcknowledgeMode.AUTO;
import static org.springframework.amqp.core.AcknowledgeMode.NONE;

/**
 * @author cheny.huang
 * @date 2019-01-12 17:55.
 */
public class AckModeTest {
    @Test
    public void testAckMode() {
        assertEquals(NONE, mode("manual"));
        assertEquals(NONE, mode("mauff"));
        assertEquals(AUTO, mode("auto"));
        assertEquals(NONE, mode("none"));
    }

    private AcknowledgeMode mode(String mode) {
        return Optional.ofNullable(mode)
                .filter(s->AUTO.name().equalsIgnoreCase(s))
                .map(s-> AcknowledgeMode.valueOf(s.toUpperCase())).orElse(NONE);
    }
}
