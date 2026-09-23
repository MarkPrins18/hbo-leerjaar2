INSERT INTO car (
    id,
    owner_id,
    license_plate,
    brand,
    model,
    production_year,
    trim,
    color,
    seats
)
VALUES
    (1, 1, 'AA-123-BB', 'Toyota', 'Corolla', 2022, 'Comfort', 'Blue', 5),
    (2, 1, 'CC-456-DD', 'Tesla', 'Model 3', 2023, 'Long Range', 'White', 5),
    (3, 2, 'EE-789-FF', 'Hyundai', 'Nexo', 2021, 'Premium', 'Black', 5);

INSERT INTO ice_car (
    car_id,
    fuel_type,
    tank_capacity_l,
    automatic_transmission
)
VALUES (1, 'PETROL', 50.00, true);

INSERT INTO bev_car (
    car_id,
    battery_capacity_kwh
)
VALUES (2, 75.00);

INSERT INTO fcev_car (
    car_id,
    tank_capacity_kg_h2
)
VALUES (3, 6.30);