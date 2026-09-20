package org.example.vvpd.task.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import org.example.vvpd.task.model.TaskPriority;
import org.junit.jupiter.api.Test;

class QueuedTaskTest {

    @Test
    void ordersByPriorityBeforeCreationTime() {
        Instant now = Instant.parse("2026-09-20T08:00:00Z");
        QueuedTask low = new QueuedTask(UUID.randomUUID(), TaskPriority.LOW, now.minusSeconds(60), 1);
        QueuedTask high = new QueuedTask(UUID.randomUUID(), TaskPriority.HIGH, now, 2);
        PriorityBlockingQueue<QueuedTask> queue = new PriorityBlockingQueue<>();

        queue.offer(low);
        queue.offer(high);

        assertThat(queue.poll()).isEqualTo(high);
        assertThat(queue.poll()).isEqualTo(low);
    }

    @Test
    void ordersSamePriorityByCreationTime() {
        Instant now = Instant.parse("2026-09-20T08:00:00Z");
        QueuedTask older = new QueuedTask(UUID.randomUUID(), TaskPriority.MEDIUM, now.minusSeconds(60), 1);
        QueuedTask newer = new QueuedTask(UUID.randomUUID(), TaskPriority.MEDIUM, now, 2);
        PriorityBlockingQueue<QueuedTask> queue = new PriorityBlockingQueue<>();

        queue.offer(newer);
        queue.offer(older);

        assertThat(queue.poll()).isEqualTo(older);
        assertThat(queue.poll()).isEqualTo(newer);
    }
}
