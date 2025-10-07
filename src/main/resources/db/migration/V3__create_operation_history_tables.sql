CREATE TABLE IF NOT EXISTS PURCHASE_OPERATION_HISTORY
(
    id                  SERIAL PRIMARY KEY,
    item_id             INTEGER   NOT NULL REFERENCES ITEM (id) ON DELETE CASCADE,
    item_quantity       DECIMAL   NOT NULL,
    item_unit           INTEGER   NOT NULL REFERENCES UNIT (id) ON DELETE CASCADE,
    package_amount      INTEGER   NOT NULL,
    package_id          INTEGER   NOT NULL REFERENCES PACKAGE (id) ON DELETE CASCADE,
    bought_price        DECIMAL   NOT NULL,
    bought_price_unit   INTEGER   NOT NULL REFERENCES UNIT (id) ON DELETE CASCADE,
    date                TEXT      NOT NULL,
    operation_timestamp BIGINT NOT NULL
);