package com.epam.java.specialization.gym_crm.dao;

import com.epam.java.specialization.gym_crm.dao.implementations.AbstractMapDao;
import com.epam.java.specialization.gym_crm.model.AbstractEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractMapDaoTest<T extends AbstractEntity<Long>> {

    protected AbstractMapDao<T> dao;
    protected Map<Long, T> testStorage;

    protected abstract AbstractMapDao<T> getDaoInstance();
    protected abstract T createSampleEntity();
    protected abstract void updateSampleEntity(T entity);

    @BeforeEach
    void setUpAbstract() {
        testStorage = new ConcurrentHashMap<>();
        dao = getDaoInstance();
        dao.setStorage(testStorage);
    }

    @Test
    void testCreate_ShouldGenerateId_WhenIdIsNull() {
        T entity = createSampleEntity();
        assertNull(entity.getId());

        T saved = dao.create(entity);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId(), "First ID generated should be 1");
        assertTrue(testStorage.containsKey(1L));
    }

    @Test
    void testUpdate_ShouldModifyExistingEntity() {
        T entity = createSampleEntity();
        T saved = dao.create(entity);

        updateSampleEntity(saved);
        T updated = dao.update(saved);

        assertNotNull(updated);
        assertEquals(saved.getId(), updated.getId());
        assertSame(updated, testStorage.get(saved.getId()));
    }

    @Test
    void testFindById_ShouldReturnPresentOptional() {
        T entity = createSampleEntity();
        T saved = dao.create(entity);

        Optional<T> found = dao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void testDelete_ShouldRemoveFromStorage() {
        T entity = createSampleEntity();
        T saved = dao.create(entity);

        dao.delete(saved.getId());

        assertFalse(testStorage.containsKey(saved.getId()));
        assertTrue(dao.findById(saved.getId()).isEmpty());
    }

    @Test
    void testCreate_ShouldBeThreadSafe_WhenMultipleThreadsInsertSimultaneously() throws InterruptedException {
        int threadCount = 10;
        int insertsPerThread = 100;
        int totalExpectedEntities = threadCount * insertsPerThread;

        java.util.concurrent.ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(threadCount);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    latch.await();
                    for (int j = 0; j < insertsPerThread; j++) {
                        T entity = createSampleEntity();
                        dao.create(entity);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        latch.countDown();
        executorService.shutdown();
        boolean finished = executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        assertTrue(finished, "All threads should finish within timeout");
        assertEquals(totalExpectedEntities, testStorage.size(),
                "Storage size must exactly match total inserted entities count without any overwrites or ID collisions");
    }
}