
CREATE TABLE IF NOT EXISTS DEVICE_WEARER(
    ID UUID primary key,
    TITLE varchar(200),
    USER_NAME varchar(240) NOT NULL,
    STATUS varchar(15)
);