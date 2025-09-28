CREATE TABLE IF NOT EXISTS ITEM
(
    id        SERIAL PRIMARY KEY,
    item_name TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS UNIT
(
    id        SERIAL PRIMARY KEY,
    unit_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS PACKAGE
(
    id                     SERIAL PRIMARY KEY,
    package_name           TEXT    NOT NULL,
    package_weight         DECIMAL NOT NULL,
    package_weight_unit_id INTEGER REFERENCES UNIT (id) ON DELETE CASCADE,
    UNIQUE (package_name, package_weight, package_weight_unit_id)
);

CREATE TABLE IF NOT EXISTS STORAGE
(
    id              SERIAL PRIMARY KEY,
    storage_name    TEXT NOT NULL UNIQUE,
    storage_address TEXT
);

CREATE TABLE IF NOT EXISTS ITEM_IN_STORAGE
(
    id                         SERIAL PRIMARY KEY,
    item_id                    INTEGER NOT NULL REFERENCES ITEM (id) ON DELETE CASCADE,
    storage_id                 INTEGER REFERENCES STORAGE (id) ON DELETE CASCADE,
    nett_item_quantity         DECIMAL,
    nett_item_quantity_unit_id INTEGER REFERENCES UNIT (id) ON DELETE CASCADE,
    sell_price                 DECIMAL,
    sell_price_unit            INTEGER REFERENCES UNIT (id) ON DELETE CASCADE,
    UNIQUE (item_id, storage_id, nett_item_quantity_unit_id, sell_price, sell_price_unit)
);

CREATE TABLE IF NOT EXISTS ITEM_IN_STORAGE_IN_PACKAGE
(
    id                          SERIAL PRIMARY KEY,
    item_in_storage_id          BIGINT NOT NULL REFERENCES ITEM_IN_STORAGE (id) ON DELETE CASCADE,
    package_id                  BIGINT NOT NULL REFERENCES PACKAGE (id) ON DELETE CASCADE
);