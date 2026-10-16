INSERT INTO rental_terms (
    car_id,
    price_per_day,
    pickup_location,
    pickup_time_start,
    pickup_time_end,
    return_location,
    return_time_start,
    return_time_end
) VALUES
(1, 89.00, 'Stationsplein 1, Gorinchem', '08:00:00', '18:00:00', 'Parkeergarage Kazerneplein, Gorinchem', '08:00:00', '20:00:00'),
(2, 125.50, 'Markt 12, Leerdam', '09:00:00', '17:00:00', NULL, NULL, NULL),
(3, 110.00, 'Havenstraat 5, Sliedrecht', NULL, NULL, NULL, NULL, NULL);
