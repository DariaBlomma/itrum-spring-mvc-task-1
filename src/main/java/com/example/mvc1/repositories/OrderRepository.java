package com.example.mvc1.repositories;

import com.example.mvc1.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.deletedAt IS NULL AND o.user.id = :userId AND o.user.deletedAt IS NULL")
    Optional<Order> findActiveByIdForUser(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.id = :orderId AND o.user.id = :userId AND o.user.deletedAt IS NULL")
    Optional<Order> findByIdForUser(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.deletedAt IS NULL AND o.user.id = :userId AND o.user.deletedAt IS NULL")
    List<Order> findAllActiveForUser(@Param("userId") Long userId);
}
