package com.example.mvc1.controllers;

import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
import com.example.mvc1.mappers.UserMapperImpl;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.services.OrderService;
import com.example.mvc1.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
@Import({UserService.class, UserMapperImpl.class, OrderMapperImpl.class})
public class UserControllerTests extends BaseControllerTest {
    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderService orderService;

    @MockitoSpyBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void mockOrderServiceListFound(List<OrderResponse> orders) {
        when(orderService.getList(userId)).thenReturn(orders);
    }

    private List<OrderResponse> getOrderResponseList() {
        OrderResponse response1 = new OrderResponse(1L, "Order 1", BigDecimal.valueOf(100.50), OrderStatus.PENDING, null);
        OrderResponse response2 = new OrderResponse(2L, "Order 2", BigDecimal.valueOf(250.00), OrderStatus.PAID, null);
        OrderResponse response3 = new OrderResponse(3L, "Order 3", BigDecimal.valueOf(75.99), OrderStatus.CANCELED, null);
        return  List.of(response1, response2, response3);
    }

    @Nested
    @DisplayName("Create tests")
    class CreateTests {
        @Test
        void shouldReturn201WhenRequestIsValid() throws Exception {
            UserRequest request = new UserRequest("user1", "mail1@gmail.com", "#FFF");

            performCreate(request).assertThat().hasStatus(HttpStatus.CREATED);
        }

        private MvcTestResult performCreate(UserRequest request) throws Exception {
            return mockMvcTester
                    .post()
                    .uri("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Get one with orders")
    class GetOneWithOrdersTests {
        @Test
        void shouldReturn200WhenRequestIsValidAndUserNotDeleted() throws Exception {
            mockNotDeletedUserFound();
            mockOrderServiceListFound(getOrderResponseList());
            performGet(userId).assertThat().hasStatus(HttpStatus.OK);
        }
        
        @Test
        void shouldReturnFullResponseFromServerWhenUserAndOrdersExist() throws Exception {
            UserResponse user = getTestUserResponse();
            List<OrderResponse> orders = getOrderResponseList();
            user.setOrders(orders);

            doReturn(user).when(userService).getOneWithOrders(userId);

            String expectedJson = objectMapper.writeValueAsString(user);

            performGet(userId).assertThat().bodyJson().isEqualTo(expectedJson);
        }
        
        @Test
        void shouldReturn404WhenUserNotFoundOrDeleted() throws Exception {
            mockNotDeletedUserNotFound();
            performGet(userId).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        private MvcTestResult performGet(Long userId) throws Exception {
            return mockMvcTester
                    .get()
                    .uri("/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }
}
