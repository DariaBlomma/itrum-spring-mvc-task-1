package com.example.mvc1.service;

import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import java.time.Instant;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class BaseServiceTest {
    @Autowired
    protected TestEntityManager entityManager;

    protected User saveTestUser() {
        User user = User.builder()
                .email("mail1@gmail.com")
                .userName("user1")
                .color("purple")
                .build();
        entityManager.persistAndFlush(user);
        return user;
    }

    protected User saveAnotherTestUser() {
        User user = User.builder()
                .email("mailAnother1@gmail.com")
                .userName("userAnother")
                .color("blue")
                .build();
        entityManager.persistAndFlush(user);
        return user;
    }

    protected User saveDeletedTestUser() {
        User user = User.builder()
                .email("mail1@gmail.com")
                .userName("user1")
                .color("purple")
                .deletedAt(Instant.now())
                .build();
        entityManager.persistAndFlush(user);
        return user;
    }

    protected void saveListOfTestOrders(Order[] orders) {
        for (Order order : orders) {
            entityManager.persistAndFlush(order);
        }
    }
}
