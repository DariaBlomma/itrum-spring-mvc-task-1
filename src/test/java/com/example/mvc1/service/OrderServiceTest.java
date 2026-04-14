package com.example.mvc1.service;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.services.OrderService;
import org.aspectj.weaver.ast.Or;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@DataJpaTest
@Import({OrderService.class, OrderMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Nested
    @DisplayName("Create tests")
    class CreateTests {
        @Test
        void shouldReturnCorrectlyMappedDTOWhenProvidedCorrectRequestAndExistingUser() {
            OrderRequest request = new OrderRequest("order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            OrderResponse expectedResponse = new OrderResponse(1L, "order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING, null);

            Long userId = saveTestUser();
            OrderResponse response = orderService.create(userId, request);

            assertThat(response)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expectedResponse);

            assertThat(response.getId()).isNotNull();
            assertThat(response.getId()).isPositive();
        }

        @Test
        void shouldBindOrderToUserWhenProvidedCorrectRequestAndExistingUser() {
            OrderRequest request = new OrderRequest("order2", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            Long userId = saveTestUser();

            OrderResponse response = orderService.create(userId, request);
            Order saved = orderRepository.findById(response.getId()).orElseThrow();

            assertThat(saved.getUser().getId()).isEqualTo(userId);
        }

        @Test
        void shouldNotSaveOrderToDBWhenProvidedUserIsInactive() {
            OrderRequest request = new OrderRequest("order3", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            Long inactiveUserId = saveInactiveTestUser();

            try {
                orderService.create(inactiveUserId, request);
            } catch (RuntimeException ignored) {
            }

            assertThat(orderRepository.count()).isZero();
        }

        private Long saveTestUser() {
            User user = User.builder()
                    .email("mail1@gmail.com")
                    .userName("user1")
                    .color("purple")
                    .build();
            entityManager.persistAndFlush(user);
            return user.getId();
        }

        private Long saveInactiveTestUser() {
            User user = User.builder()
                    .email("mail1@gmail.com")
                    .userName("user1")
                    .color("purple")
                    .deletedAt(Instant.now())
                    .build();
            entityManager.persistAndFlush(user);
            return user.getId();
        }
    }
}
