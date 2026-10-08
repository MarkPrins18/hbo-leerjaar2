INSERT INTO car (
    id,
    owner_id,
    license_plate,
    brand,
    model,
    production_year,
    trim,
    color,
    seats,
    doors,
    vehicle_type,
    ready_to_drive_weight_kg,
    consumption_combined,
    co2_emission_combined
)
VALUES
    (
        1, 1, 'KS711F', 'BMW', 'M235I', 2014,
        'M235i High Executive', 'WIT', 4, 2,
        'PASSENGER_CAR', 1530, 8.1, 189
    ),
    (
        2, 2, 'R003BJ', 'BMW', 'I4 M50', 2022,
        'M50', 'GROEN', 5, 4,
        'PASSENGER_CAR', 2290, NULL, NULL
    ),
    (
        3, 3, 'GVT21R', 'TOYOTA', 'MIRAI', 2017,
        'FCV', 'BLAUW', 4, 4,
        'PASSENGER_CAR', 1925, NULL, NULL
    );

INSERT INTO ice_car (
    car_id,
    fuel_type,
    tank_capacity_l,
    automatic_transmission
)
VALUES (
           1, 'PETROL', 55.00, false
       );

INSERT INTO bev_car (
    car_id,
    battery_capacity_kwh
)
VALUES (
           2, 54.00
       );

INSERT INTO fcev_car (
    car_id,
    tank_capacity_kg_h2
)
VALUES (
           3, 20.00
       );