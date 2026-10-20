package clients

/** MRB (Dutch road tax) rules for 2026, filled in from the Belastingdienst. Sent to Gemini as context. */
val ROAD_TAX_RULES_2026 = """
    Motorrijtuigenbelasting (MRB) 2026 - personenauto, particuliere eigenaar, provincie Noord-Brabant
    Bron: Belastingdienst, rekenhulp "Motorrijtuigenbelasting berekenen" (https://www.belastingdienst.nl), peildatum: 15 oktober 2026

    Algemeen
    - Het tarief hangt af van de brandstofsoort en de massa rijklaar van de auto (in kg).
    - Sinds 1 juli 2026 rekent de Belastingdienst met de "massa rijklaar" (RDW). Staat alleen de "massa ledig voertuig" bekend, tel daar dan 100 kg bij op.
    - Zoek de gewichtsklasse waarin de massa rijklaar valt (bijv. 1800 kg valt in "1751 t/m 1850").
    - De bedragen hieronder zijn per kwartaal (3 maanden) en inclusief de provinciale opcenten van Noord-Brabant.

    Tarieven per gewichtsklasse (massa rijklaar, per kwartaal, incl. opcenten Noord-Brabant)
    Klasse (kg)    | Benzine | Diesel | Emissievrij (elektrisch/waterstof)
    1 t/m 650      | € 33    | € 118  | € 23
    651 t/m 750    | € 44    | € 143  | € 30
    751 t/m 850    | € 54    | € 169  | € 38
    851 t/m 950    | € 71    | € 202  | € 50
    951 t/m 1050   | € 93    | € 246  | € 65
    1051 t/m 1150  | € 121   | € 290  | € 84
    1151 t/m 1250  | € 148   | € 334  | € 104
    1251 t/m 1350  | € 176   | € 378  | € 123
    1351 t/m 1450  | € 203   | € 422  | € 142
    1451 t/m 1550  | € 231   | € 466  | € 161
    1551 t/m 1650  | € 258   | € 510  | € 180
    1651 t/m 1750  | € 285   | € 554  | € 200
    1751 t/m 1850  | € 313   | € 598  | € 219
    1851 t/m 1950  | € 340   | € 642  | € 238
    1951 t/m 2050  | € 368   | € 686  | € 257
    2051 t/m 2150  | € 395   | € 730  | € 276
    2151 t/m 2250  | € 423   | € 774  | € 296
    2251 t/m 2350  | € 450   | € 818  | € 315
    2351 t/m 2450  | € 477   | € 862  | € 334
    2451 t/m 2550  | € 505   | € 906  | € 353
    2551 t/m 2650  | € 532   | € 950  | € 372
    2651 t/m 2750  | € 560   | € 994  | € 392
    2751 t/m 2850  | € 587   | € 1038 | € 411
    2851 t/m 2950  | € 615   | € 1082 | € 430
    2951 t/m 3050  | € 642   | € 1126 | € 449
    3051 t/m 3150  | € 669   | € 1170 | € 468
    3151 t/m 3250  | € 697   | € 1214 | € 488
    3251 t/m 3350  | € 724   | € 1258 | € 507
    3351 t/m 3450  | € 747   | € 1298 | € 523

    Emissievrije auto's (elektrisch en waterstof)
    - Een personenauto die volledig en uitsluitend op elektriciteit of waterstof rijdt, geldt als emissievrij.
    - In 2026 betaalt een emissievrije personenauto 70% van het tarief voor een benzineauto met hetzelfde gewicht. De kolom "Emissievrij" hierboven is daar al op gebaseerd.
    - Er geldt in 2026 geen aparte gewichtscorrectie meer.

    Hybrides
    - (Plug-in) hybride personenauto's hebben geen apart tarief: gebruik het tarief van de brandstof (meestal benzine).
""".trimIndent()
