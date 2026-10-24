INSERT INTO car (
    id,
    user_id,
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
        'PASSENGER_CAR', 1925, 0.76, NULL
    ),
    (
        4, 1, 'P519LV', 'VOLKSWAGEN', 'GOLF', 2021,
        'Life 1.0 eTSI', 'GRIJS', 5, 5,
        'PASSENGER_CAR', 1255, NULL, 121
    ),
    (
        5, 2, 'ZG283T', 'SKODA', 'OCTAVIA COMBI', 2020,
        '2.0 TDI Style', 'ZWART', 5, 5,
        'PASSENGER_CAR', 1450, 4.4, 116
    ),
    (
        6, 3, 'L702RX', 'DACIA', 'SANDERO', 2022,
        'TCe 100 Bi-Fuel', 'ORANJE', 5, 5,
        'PASSENGER_CAR', 1150, 7.3, 118
    ),
    (
        7, 2, 'N455GK', 'TESLA', 'MODEL 3', 2023,
        'Long Range', 'WIT', 5, 4,
        'PASSENGER_CAR', 1844, 14.5, NULL
    ),
    (
        8, 1, 'J712ST', 'VOLKSWAGEN', 'PASSAT', 2019,
        '1.5 TSI 150pk', 'GRIJS', 5, 5,
        'PASSENGER_CAR', 1465, 5.5, 126
    );

INSERT INTO ice_car (
    car_id,
    fuel_type,
    tank_capacity_l,
    automatic_transmission
)
VALUES (
           1, 'PETROL', 55.00, false
       ),
       (
           4, 'PETROL', 50.00, true
       ),
       (
           5, 'DIESEL', 45.00, false
       ),
       (
           6, 'LPG', 40.00, false
       ),
       (
           8, 'PETROL', 66.00, false
       );

INSERT INTO bev_car (
    car_id,
    battery_capacity_kwh
)
VALUES (
           2, 54.00
       ),
       (
           7, 75.00
       );

INSERT INTO fcev_car (
    car_id,
    tank_capacity_kg_h2
)
VALUES (
           3, 20.00
       );