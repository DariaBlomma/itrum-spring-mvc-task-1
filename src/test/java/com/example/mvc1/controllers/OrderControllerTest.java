package com.example.mvc1.controllers;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.repositories.UserRepository;
import com.example.mvc1.services.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.mockito.Mockito.when;

@WebMvcTest(OrderController.class)
@Import({OrderService.class, OrderMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class OrderControllerTest {
    @Autowired
    private MockMvcTester mockMvcTester;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    private final Long userId = 1L;
    private final Long orderId = 1L;

    private User getTestUser(List<Order> orders) {
        return new User(userId, "testUser", "test@gmail.com", "#FFF", orders, null);
    }

    private Order getTestOrder() {
        return Order.builder()
                .id(orderId)
                .title("Order 1")
                .price(BigDecimal.valueOf(3.45))
                .status(OrderStatus.PENDING)
                .user(getTestUser(List.of()))
                .deletedAt(null)
                .build();
    }

    private void mockNotDeletedUserFound() {
        when(userRepository.findActiveById(userId)).thenReturn(Optional.of(getTestUser(List.of())));
    }

    private void mockNotDeletedUserNotFound() {
        when(userRepository.findActiveById(userId)).thenReturn(Optional.empty());
    }

    private void mockNotDeletedOrderFound() {
        when(orderRepository.findActiveByIdForUser(orderId, userId)).thenReturn(Optional.of(getTestOrder()));
    }

    private void mockNotDeletedOrderNotFound() {
        when(orderRepository.findActiveByIdForUser(orderId, userId)).thenReturn(Optional.empty());
    }

    @Nested
    @DisplayName("Create tests")
    class CreateTests {
        @Test
        void shouldReturn201WhenRequestIsCorrect() throws Exception {
            mockNotDeletedUserFound();
            OrderRequest request = new OrderRequest("order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING);

            performCreate(userId, request)
                    .assertThat()
                    .hasStatus(HttpStatus.CREATED);
        }

        @Test
        void shouldReturn404IfUserDoesNotExistOrDeleted() throws Exception {
            OrderRequest request = new OrderRequest("order1", BigDecimal.valueOf(34.03), OrderStatus.PENDING);
            mockNotDeletedUserNotFound();

            performCreate(userId, request).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }


        private MvcTestResult performCreate(Long userId, OrderRequest request) throws Exception {
            return mockMvcTester
                    .post()
                    .uri("/orders")
                    .param("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Get one tests")
    class GetOneTests {
        @Test
        void shouldReturn200WhenRequestIsValidAndUserExists() throws Exception{
            mockNotDeletedOrderFound();

            performGet(userId, orderId).assertThat().hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExistOrDeleted() throws Exception {
            mockNotDeletedOrderNotFound();

            performGet(userId, orderId).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }
    }

    private MvcTestResult performGet(Long userId, Long orderId) throws Exception {
        return mockMvcTester
                .get()
                .uri("/orders/{id}", orderId)
                .param("userId", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .exchange();
    }
}
