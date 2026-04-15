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
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Import({OrderService.class, OrderMapperImpl.class})
public class OrderServiceTest extends BaseServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

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
        void shouldReturnOrderWhenExistsAndNotDeleted() {
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
        void shouldThrowExceptionWhenOrderIsDeleted() {
            User user = saveTestUser();
            Order deletedOrder = saveDeletedTestOrder(user);

            assertThatThrownBy(() -> orderService.getOne(user.getId(), deletedOrder.getId()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldThrowExceptionWhenUserIsDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order order = saveTestOrder(deletedUser);

            assertThatThrownBy(() -> orderService.getOne(deletedUser.getId(), order.getId()))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        void shouldThrowExceptionWhenBothUserAndOrderAreDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order deletedOrder = saveDeletedTestOrder(deletedUser);

            assertThatThrownBy(() -> orderService.getOne(deletedUser.getId(), deletedOrder.getId()))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Get list tests")
    class GetListTests {
        @Test
        void shouldReturnListOfNotDeletedOrdersForUserWhenUserIsNptDeleted() {
            User user = saveTestUser();

            Order order1 = Order.builder()
                    .title("Order 1")
                    .price(BigDecimal.valueOf(1.45))
                    .status(OrderStatus.PENDING)
                    .user(user)
                    .deletedAt(null)
                    .build();
            Order order2 = Order.builder()
                    .title("Order 2")
                    .price(BigDecimal.valueOf(2.45))
                    .status(OrderStatus.CANCELED)
                    .user(user)
                    .deletedAt(null)
                    .build();
            Order order3 = Order.builder()
                    .title("Order 3")
                    .price(BigDecimal.valueOf(3.45))
                    .status(OrderStatus.CANCELED)
                    .user(user)
                    .deletedAt(null)
                    .build();
            Order order4 = Order.builder()
                    .title("Order 4")
                    .price(BigDecimal.valueOf(4.45))
                    .status(OrderStatus.CANCELED)
                    .user(user)
                    .deletedAt(Instant.now())
                    .build();
            saveListOfTestOrders(new Order[]{order1, order2, order3, order4});

            OrderResponse response1 = new OrderResponse(order1.getId(), order1.getTitle(), order1.getPrice(), order1.getStatus(), null);
            OrderResponse response2 = new OrderResponse(order2.getId(), order2.getTitle(), order2.getPrice(), order2.getStatus(), null);
            OrderResponse response3 = new OrderResponse(order3.getId(), order3.getTitle(), order3.getPrice(), order3.getStatus(), null);

            List<OrderResponse> expected = List.of(response1, response2, response3);
            List<OrderResponse> result = orderService.getList(user.getId());

            assertThat(result).size().isEqualTo(3);
            assertThat(result)
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(expected);
        }

        @Test
        void shouldReturnEmptyListWhenNoOrdersForUserExist() {
            User user1 = saveTestUser();
            User user2 = saveAnotherTestUser();
            Order order1 = saveTestOrder(user1);
            Order order2 = saveTestOrder(user1);
            saveListOfTestOrders(new Order[]{order1, order2});

            List<OrderResponse> responses = orderService.getList(user2.getId());

            assertThat(responses.size()).isEqualTo(0);
        }

        @Test
        void shouldReturnEmptyListWhenOnlyDeletedOrdersForUserExist() {
            User user = saveTestUser();
            Order deletedOrder1 = saveDeletedTestOrder(user);
            Order deletedOrder2 = saveDeletedTestOrder(user);
            saveListOfTestOrders(new Order[]{deletedOrder1, deletedOrder2});

            List<OrderResponse> responses = orderService.getList(user.getId());

            assertThat(responses.size()).isEqualTo(0);
        }

        @Test
        void shouldReturnEmptyListWhenUserIsDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order order1 = saveTestOrder(deletedUser);
            Order order2 = saveTestOrder(deletedUser);
            saveListOfTestOrders(new Order[]{order1, order2});

            List<OrderResponse> responses = orderService.getList(deletedUser.getId());

            assertThat(responses.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Update tests")
    class UpdateTests {
        @Test
        void shouldUpdateOrderWhenNotDeletedAndBelongsToNotDeletedUser()  {
            User user = saveTestUser();
            Order order = saveTestOrder(user);
            OrderRequest request = new OrderRequest("upd1", BigDecimal.ONE, OrderStatus.PAID);

            OrderResponse response = orderService.update(user.getId(), order.getId(), request);

            OrderResponse expected = new OrderResponse(1L, request.getTitle(), request.getPrice(), request.getStatus(), null);

            assertThat(response)
                     .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(expected);
        }

        @Test
        void shouldNotUpdateOrderWhenUserIsDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order order = saveTestOrder(deletedUser);
            Order originalOrder = cloneOrder(order);

            OrderRequest request = new OrderRequest("upd1", BigDecimal.ONE, OrderStatus.PAID);

            try {
                orderService.update(deletedUser.getId(), order.getId(), request);
            } catch (RuntimeException ignored) {

            }

            Order orderAfter = orderRepository.findById(order.getId()).orElseThrow();

            assertThat(orderAfter)
                    .usingRecursiveComparison()
                    .isEqualTo(originalOrder);
        }

        @Test
        void shouldNotUpdateOrderWhenOrderIsDeleted() {
            User user = saveTestUser();
            Order deletedOrder = saveDeletedTestOrder(user);
            Order originalOrder = cloneOrder(deletedOrder);

            OrderRequest request = new OrderRequest("upd1", BigDecimal.ONE, OrderStatus.PAID);

            try {
                orderService.update(user.getId(), deletedOrder.getId(), request);
            } catch (RuntimeException ignored) {

            }

            Order orderAfter = orderRepository.findById(deletedOrder.getId()).orElseThrow();

            assertThat(orderAfter)
                    .usingRecursiveComparison()
                    .isEqualTo(originalOrder);
        }

        @Test
        void shouldNotUpdateOrderWhenBothOrderAndUserAreDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order deletedOrder = saveDeletedTestOrder(deletedUser);
            Order originalOrder = cloneOrder(deletedOrder);

            OrderRequest request = new OrderRequest("upd1", BigDecimal.ONE, OrderStatus.PAID);

            try {
                orderService.update(deletedUser.getId(), deletedOrder.getId(), request);
            } catch (RuntimeException ignored) {

            }

            Order orderAfter = orderRepository.findById(deletedOrder.getId()).orElseThrow();

            assertThat(orderAfter)
                    .usingRecursiveComparison()
                    .isEqualTo(originalOrder);
        }

        private Order cloneOrder(Order order) {
            return Order.builder()
                    .id(order.getId())
                    .title(order.getTitle())
                    .price(order.getPrice())
                    .status(order.getStatus())
                    .user(order.getUser())
                    .deletedAt(order.getDeletedAt())
                    .build();
        }
    }

    @Nested
    @DisplayName("Delete soft tests")
    class DeleteSoftTests {
        @Test
        void shouldMarkOrderDeletedWhenItExistsAndUserIsNotDeleted() {
            User user = saveTestUser();
            Order order = saveTestOrder(user);

            orderService.deleteSoft(user.getId(), order.getId());

            assertThat(order.isDeleted()).isTrue();
        }

        @Test
        void shouldNotMarkOrderDeletedWhenUserIsDeleted() {
            User deletedUser = saveDeletedTestUser();
            Order order = saveTestOrder(deletedUser);

            try {
                orderService.deleteSoft(deletedUser.getId(), order.getId());
            } catch (RuntimeException ignored) {

            }

            assertThat(order.isDeleted()).isFalse();
        }
    }
}
