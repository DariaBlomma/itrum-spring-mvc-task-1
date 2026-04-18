package com.example.mvc1.controllers;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.entities.Order;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
import com.example.mvc1.repositories.OrderRepository;
import com.example.mvc1.repositories.UserRepository;
import com.example.mvc1.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(OrderController.class)
@Import({OrderService.class, OrderMapperImpl.class})
public class OrderControllerTest extends BaseControllerTest{
    @MockitoBean
    private OrderRepository orderRepository;

    private final Long orderId = 1L;
    private final Long anotherOrderId = 2L;
    private final Long deletedOrderId = 3L;

    @BeforeEach
    void setUp() {
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Order getTestOrder() {
        return Order.builder()
                .id(orderId)
                .title("Order 1")
                .price(BigDecimal.valueOf(3.45))
                .status(OrderStatus.PENDING)
                .user(getTestUser())
                .deletedAt(null)
                .build();
    }

    private Order getAnotherTestOrder() {
        return Order.builder()
                .id(anotherOrderId)
                .title("Order Another")
                .price(BigDecimal.valueOf(3.45))
                .status(OrderStatus.PENDING)
                .user(getTestUser())
                .deletedAt(null)
                .build();
    }

    private Order getDeletedTestOrder() {
        return Order.builder()
                .id(deletedOrderId)
                .title("Deleted order")
                .price(BigDecimal.valueOf(3.45))
                .status(OrderStatus.PENDING)
                .user(getTestUser())
                .deletedAt(Instant.now())
                .build();
    }

    private void mockNotDeletedOrderFound() {
        when(orderRepository.findActiveByIdForUser(orderId, userId)).thenReturn(Optional.of(getTestOrder()));
    }

    private void mockNotDeletedOrderNotFound() {
        when(orderRepository.findActiveByIdForUser(orderId, userId)).thenReturn(Optional.empty());
    }

    private void mockAnyOrderNotFound() {
        when(orderRepository.findByIdForUser(orderId, userId)).thenReturn(Optional.empty());
    }

    private void mockAnyOrderFound() {
        when(orderRepository.findByIdForUser(orderId, userId)).thenReturn(Optional.of(getTestOrder()));
    }

    private void mockDeletedOrderFound() {
        when(orderRepository.findByIdForUser(orderId, userId)).thenReturn(Optional.of(getDeletedTestOrder()));
    }

    private void mockListFound() {
        when(orderRepository.findAllActiveForUser(userId)).thenReturn(List.of(getTestOrder(), getAnotherTestOrder()));
    }

    private void mockListNotFound() {
        when(orderRepository.findAllActiveForUser(userId)).thenReturn(List.of());
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

        private MvcTestResult performGet(Long userId, Long orderId) throws Exception {
            return mockMvcTester
                    .get()
                    .uri("/orders/{id}", orderId)
                    .param("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Get List")
    class GetListTests {
        @Test
        void shouldReturn200WhenRequestIsValidAndUserIsNotDeleted() throws  Exception {
            mockNotDeletedUserFound();
            mockListFound();
            performGet(userId).assertThat().hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturn200WhenNoOrdersFound() throws Exception {
            mockNotDeletedUserFound();
            mockListNotFound();
            performGet(userId).assertThat().hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturn404WhenUserIsDeleted() throws Exception {
            mockNotDeletedUserNotFound();
            performGet(userId).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        private MvcTestResult performGet(Long userId) throws Exception {
            return mockMvcTester
                    .get()
                    .uri("/orders")
                    .param("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Update tests")
    class UpdateTests {
        @Test
        void shouldReturn200WhenBothOrderAndUserAreNotDeletedAndRequestIsValid() throws  Exception {
            mockNotDeletedOrderFound();
            OrderRequest request = new OrderRequest("upd", BigDecimal.valueOf(34.03), OrderStatus.PENDING);

            performPut(userId, orderId, request).assertThat().hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturn404WhenOrderOrUserIsDeleted() throws  Exception {
            mockNotDeletedOrderNotFound();
            OrderRequest request = new OrderRequest("upd", BigDecimal.valueOf(34.03), OrderStatus.PENDING);

            performPut(userId, orderId, request).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        private MvcTestResult performPut(Long userId, Long orderId, OrderRequest request) throws Exception {
            return mockMvcTester
                    .put()
                    .uri("/orders/{id}", orderId)
                    .param("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Delete soft tests")
    class DeleteSoftMethods {
        @Test
        void shouldReturn204WhenOrderExists() throws Exception {
            mockAnyOrderFound();
            performDelete(userId, orderId).assertThat().hasStatus(HttpStatus.NO_CONTENT);
        }

        @Test
        void shouldReturn404WhenOrderDoesNotExistOrUserIsDeleted() throws  Exception {
            mockAnyOrderNotFound();
            performDelete(userId, orderId).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldReturn409WhenOrderIsAlreadyDeleted() throws Exception {
            mockDeletedOrderFound();
            performDelete(userId, orderId).assertThat().hasStatus(HttpStatus.CONFLICT);
        }

        private MvcTestResult performDelete(Long userId, Long orderId) throws Exception {
            return mockMvcTester
                    .delete()
                    .uri("/orders/{id}", orderId)
                    .param("userId", String.valueOf(userId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }
}
