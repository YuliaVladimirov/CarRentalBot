package org.example.carrentalbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.carrentalbot.model.enums.CarCategory;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cars")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", nullable = false)
    private UUID id;

    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CarCategory category;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_file_id", length = 200)
    private String imageFileId;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "available", nullable = false)
    @Builder.Default
    private Boolean available = true;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Booking> bookings = new HashSet<>();
}
