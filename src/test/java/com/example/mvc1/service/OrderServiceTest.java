package com.example.mvc1.service;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.services.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

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

    private User saveTestUser() {
        User user = User.builder()
                .email("mail1@gmail.com")
                .userName("user1")
                .color("purple")
                .build();
        entityManager.persistAndFlush(user);
        return user;
    }

    private User saveDeletedTestUser() {
        User user = User.builder()
                .email("mail1@gmail.com")
                .userName("user1")
                .color("purple")
                .deletedAt(Instant.now())
                .build();
        entityManager.persistAndFlush(user);
        return user;
    }

    @Nested
    @DisplayName("Create tests")
    class CreateTests {
        @Test
        void shouldReturnCorrectlyMappedDTOWhenProvidedCorrectRequestAndExistingUser() {
            OrderRequest request = new OrderRequest("order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            OrderResponse expectedResponse = new OrderResponse(1L, "order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING, null);

            Long userId = saveTestUser().getId();
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
            Long userId = saveTestUser().getId();

            OrderResponse response = orderService.create(userId, request);
            Order saved = orderRepository.findById(response.getId()).orElseThrow();

            assertThat(saved.getUser().getId()).isEqualTo(userId);
        }

        @Test
        void shouldNotSaveOrderToDBWhenProvidedUserIsInactive() {
            OrderRequest request = new OrderRequest("order3", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            Long inactiveUserId = saveDeletedTestUser().getId();

            try {
                orderService.create(inactiveUserId, request);
            } catch (RuntimeException ignored) {
            }

            assertThat(orderRepository.count()).isZero();
        }
    }

    @Nested
    @DisplayName("Get one tests")
    class GetOneTests {
        @Test
        void getOne_shouldReturnOrderWhenExistsAndNotDeleted() {
            User user = saveTestUser();
            Order order = saveTestOrder(user);

            OrderResponse response = orderService.getOne(user.getId(), order.getId());

            OrderResponse expectedResponse = new OrderResponse(
                    order.getId(),
                    order.getTitle(),
                    order.getPrice(),
                    order.getStatus(),
                    null);

            assertThat(response)
                    .usingRecursiveComparison()
                    .isEqualTo(expectedResponse);
        }

        @Test
        void getOne_shouldThrowExceptionWhenOrderIsDeleted() {
            User user = saveTestUser();
            Order deletedOrder = saveDeletedTestOrder(user);

            assertThatThrownBy(() -> orderService.getOne(user.getId(), deletedOrder.getId()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void getOne_shouldThrowExceptionWhenUserIsDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order order = saveTestOrder(deletedUser);

            assertThatThrownBy(() -> orderService.getOne(deletedUser.getId(), order.getId()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void getOne_shouldThrowExceptionWhenBothUserAndOrderAreDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order deletedOrder = saveDeletedTestOrder(deletedUser);

            assertThatThrownBy(() -> orderService.getOne(deletedUser.getId(), deletedOrder.getId()))
                    .isInstanceOf(RuntimeException.class);
        }

        private Order saveTestOrder(User user) {
            Order order = Order.builder()
                    .title("Order 1")
                    .price(BigDecimal.valueOf(3.45))
                    .status(OrderStatus.PENDING)
                    .user(user)
                    .deletedAt(null)
                    .build();
            entityManager.persistAndFlush(order);
            return order;
        }

        private Order saveDeletedTestOrder(User user) {
            Order order = Order.builder()
                    .title("Order 1")
                    .price(BigDecimal.valueOf(3.45))
                    .status(OrderStatus.PENDING)
                    .user(user)
                    .deletedAt(Instant.now())
                    .build();
            entityManager.persistAndFlush(order);
            return order;
        }
    }
}
