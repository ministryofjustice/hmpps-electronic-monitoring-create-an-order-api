

CREATE TABLE IF NOT EXISTS ORDER_FORM(
    ID UUID primary key,
    TITLE varchar(200),
    USER_NAME varchar(240) NOT NULL,
    STATUS varchar(15)
);

