package com.example.mvc1.services;

import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.example.mvc1.mappers.UserMapperImpl;
import com.example.mvc1.mappers.OrderMapperImpl;
import org.springframework.data.domain.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({UserService.class, UserMapperImpl.class, OrderMapperImpl.class})
public class UserServiceTest extends BaseServiceTest {
    @Autowired
    private UserService userService;

    @MockitoBean
    private OrderService orderService;

    @Nested
    @DisplayName("Create tests")
    class CreateTests {
        @Test
        void shouldReturnCorrectlyMappedDTOWhenProvidedCorrectRequest() {
            UserRequest request = new UserRequest("user1", "mail1@gmail.com", "#FFF");

            UserResponse response = userService.create(request);

            UserResponse expected = new UserResponse(1L, request.getUserName(), request.getEmail(), new ArrayList<>(), request.getColor(), null);

            assertThat(response).usingRecursiveComparison().ignoringFields("id").isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Get one with orders")
    class GetOneWithOrdersTests {
        @Test
        void shouldReturnUserWithOrdersWhenUserIsNotDeletedAndOrdersExist() {
            User user = saveTestUser();
            List<OrderResponse> mockOrders = List.of(
                    new OrderResponse(1L, "Order1", BigDecimal.valueOf(100.50), OrderStatus.PENDING, null),
                    new OrderResponse(2L, "Order2", BigDecimal.valueOf(250.00), OrderStatus.PAID, null),
                    new OrderResponse(3L, "Order3", BigDecimal.valueOf(75.99), OrderStatus.CANCELED, Instant.now())
            );
            when(orderService.getList(user.getId()))
                    .thenReturn(mockOrders);

            UserResponse response = userService.getOneWithOrders(user.getId());

            UserResponse expected = new UserResponse(user.getId(), user.getUserName(), user.getEmail(), mockOrders, user.getColor(), null);

            assertThat(response).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        void shouldReturnUserWithEmptyOrdersWhenUserIsNotDeletedAndHasNoOrders() {
            User user = saveTestUser();
            List<OrderResponse> mockOrders = List.of();
            when(orderService.getList(user.getId()))
                    .thenReturn(mockOrders);

            UserResponse response = userService.getOneWithOrders(user.getId());

            UserResponse expected = new UserResponse(user.getId(), user.getUserName(), user.getEmail(), mockOrders, user.getColor(), null);

            assertThat(response).usingRecursiveComparison().isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Get list with pagination")
    class GetListWithPagination {
        @Test
        void shouldReturnNotDeletedUsersWithOrdersWithPaginationWhenRequestIsCorrect() {
            User user1 = saveTestUser();
            User user2 = saveAnotherTestUser();
            Order order1 = Order.builder()
                    .title("Order1")
                    .price(BigDecimal.valueOf(100))
                    .status(OrderStatus.PENDING)
                    .deletedAt(null)
                    .user(user1)
                    .build();

            Order order2 = Order.builder()
                    .title("Order2")
                    .price(BigDecimal.valueOf(101))
                    .status(OrderStatus.PENDING)
                    .deletedAt(null)
                    .user(user1)
                    .build();
            saveListOfTestOrders(new Order[]{order1, order2});
            user1.setOrders(List.of(order2, order1));

            List<OrderResponse> ordersForUser1 = List.of(
                    new OrderResponse(order1.getId(), order1.getTitle(), order1.getPrice(), order1.getStatus(), null),
                    new OrderResponse(order2.getId(), order2.getTitle(), order2.getPrice(), order2.getStatus(), null)
            );
            List<OrderResponse> ordersForUser2 = List.of();

            Pageable pageable = PageRequest.of(0, 10, Sort.by("userName").ascending());

            Page<UserResponse> result = userService.getListWithPagination(pageable);

            List<UserResponse> expectedContent = List.of(
                    new UserResponse(user1.getId(), user1.getUserName(), user1.getEmail(), ordersForUser1, user1.getColor(), null),
                    new UserResponse(user2.getId(), user2.getUserName(), user2.getEmail(), ordersForUser2, user2.getColor(), null)
            );

            assertThat(result.getContent())
                    .usingRecursiveComparison()
                    .ignoringCollectionOrder()
                    .isEqualTo(expectedContent);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isZero();
            assertThat(result.getSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Update tests")
    class UpdateTests {
        @Test
        void shouldReturnUpdatedUserWhenNotDeleted() {
            User user = saveTestUser();
            UserRequest request = new UserRequest("upd1", "upd2@gmail.com", "#CCC");

            UserResponse response = userService.update(user.getId(), request);

            UserResponse expected = new UserResponse(user.getId(), request.getUserName(), request.getEmail(), List.of(), request.getColor(), null);

            assertThat(response).usingRecursiveComparison().isEqualTo(expected);
        }

        @Test
        void shouldNotUpdateUserWhenDeleted() {
            User deletedUser = saveDeletedTestUser();
            UserResponse expected = new UserResponse(deletedUser.getId(), deletedUser.getUserName(), deletedUser.getEmail(), List.of(), deletedUser.getColor(), deletedUser.getDeletedAt());
            UserRequest request = new UserRequest("upd1", "upd2@gmail.com", "#CCC");

            try {
                userService.update(deletedUser.getId(), request);
            } catch (RuntimeException ignored) {
            }

            assertThat(deletedUser).usingRecursiveComparison().ignoringFields("orders").isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Soft delete tests")
    class SoftDeleteTests {
        @Test
        void shouldMarkUserDeletedWhenExists() {
            User user = saveTestUser();

            userService.softDelete(user.getId());

            assertThat(user.isDeleted()).isTrue();
        }
    }
}
