package com.example.mvc1.service;

import com.example.mvc1.dtos.order.OrderRequest;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.entities.User;
import com.example.mvc1.enums.OrderStatus;
import com.example.mvc1.mappers.OrderMapperImpl;
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
import java.math.BigDecimal;

@DataJpaTest
@Import({OrderService.class, OrderMapperImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

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
    }
}
