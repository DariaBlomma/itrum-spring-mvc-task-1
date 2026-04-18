package com.example.mvc1.controllers;

import com.example.mvc1.dtos.Views;
import com.example.mvc1.dtos.order.OrderResponse;
import com.example.mvc1.dtos.user.UserRequest;
import com.example.mvc1.dtos.user.UserResponse;
import com.example.mvc1.entities.Order;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
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
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class UserControllerTests extends BaseControllerTest {
    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderService orderService;

    @MockitoSpyBean
    private UserService userService;

    @Autowired
    private UserController userController;

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

    @Nested
    @DisplayName("Get list with pagination tests")
    class GetListWithPaginationTests {
        @Test
        void shouldReturn200WhenRequestIsCorrect() throws Exception {
            Page<User> usersPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(userRepository.findAllActiveWithOrdersPaginated(any(Pageable.class)))
                    .thenReturn(usersPage);

            performGetPaginated(0, 10, "userName,asc")
                    .assertThat()
                    .hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturnAllUserFieldsWithoutOrderInfoWhenRequestIsCorrect() throws Exception {
            // mock service return result
            UserResponse user1 = new UserResponse(1L, "A_user1", "user1@mail.com", List.of(), "#FFF", null);
            UserResponse user2 = new UserResponse(2L, "B_user2", "user2@mail.com", List.of(), "#000", null);
            OrderResponse order1 = new OrderResponse(101L, "Order 1", BigDecimal.valueOf(100), OrderStatus.PENDING, null);
            OrderResponse order2 = new OrderResponse(102L, "Order 2", BigDecimal.valueOf(200), OrderStatus.PAID, null);
            OrderResponse order3 = new OrderResponse(103L, "Order 3", BigDecimal.valueOf(300), OrderStatus.PENDING, null);
            user1.setOrders(List.of(order1, order2));
            user2.setOrders(List.of(order3));
            List<UserResponse> users = List.of(user1, user2);

            // request
            int PAGE = 0;
            int SIZE = 10;
            String SORT = "userName";
            Pageable pageable = PageRequest.of(PAGE, SIZE, Sort.by(SORT).ascending());;
            Page<UserResponse> usersPage = new PageImpl<>(users, pageable, users.size());

            String mock = objectMapper.writeValueAsString(usersPage);
            // service mock
            doReturn(usersPage).when(userService).getListWithPagination(any(Pageable.class));

            // expected result
            UserResponse user1WithoutOrders = user1.toBuilder().orders(List.of()).build();
            UserResponse user2WithoutOrders = user2.toBuilder().orders(List.of()).build();
            List<UserResponse> expectedContent = List.of(user1WithoutOrders, user2WithoutOrders);
            Page<UserResponse> expectedPage = new PageImpl<>(expectedContent, pageable, expectedContent.size());
            String expectedString = objectMapper
                    .writerWithView(Views.UserFull.class)
                    .writeValueAsString(expectedPage);

            performGetPaginated(PAGE, SIZE, SORT + ",asc").assertThat()
                    .bodyJson()
                    .isEqualTo(expectedString);
        }

        private MvcTestResult performGetPaginated(int page, int size, String sort) throws Exception {
            return mockMvcTester
                    .get()
                    .uri("/users?page=" + page + "&size=" + size + "&sort=" + sort)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Update tests")
    class UpdateTests {
        @Test
        void shouldReturn200IfRequestIsCorrectAndUserIsNotDeleted() throws Exception {
            mockNotDeletedUserFound();
            UserRequest request = new UserRequest("upd1", "mail11@gmail.com", "#FFF");
            performUpdate(userId, request).assertThat().hasStatus(HttpStatus.OK);
        }

        @Test
        void shouldReturn404IfUserIsDeleted() throws Exception {
            mockNotDeletedUserNotFound();
            UserRequest request = new UserRequest("upd1", "mail11@gmail.com", "#FFF");
            performUpdate(userId, request).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        private MvcTestResult performUpdate(Long userId, UserRequest request) throws Exception {
            return mockMvcTester
                    .put()
                    .uri("/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .exchange();
        }
    }

    @Nested
    @DisplayName("Delete soft tests")
    class DeleteSoftTests {
        @Test
        void shouldReturn204IfUserExists() throws Exception {
            mockAnyUserFound();
            performDelete(userId).assertThat().hasStatus(HttpStatus.NO_CONTENT);
        }

        @Test
        void shouldReturn404IfUserDoesNotExist() throws Exception {
            mockAnyUserNotFound();
            performDelete(userId).assertThat().hasStatus(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldReturn409IfUserAlreadyDeleted() throws Exception {
            mockDeletedUserFound();
            performDelete(deletedUserId).assertThat().hasStatus(HttpStatus.CONFLICT);
        }

        private MvcTestResult performDelete(Long userId) throws Exception {
            return mockMvcTester
                    .delete()
                    .uri("/users/{id}", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange();
        }
    }
}
