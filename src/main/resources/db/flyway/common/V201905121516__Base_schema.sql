CREATE SCHEMA prh;

CREATE TABLE prh.object
(
    id      BIGSERIAL NOT NULL PRIMARY KEY,
    hash    varchar(50),
    version INTEGER DEFAULT 1
);

CREATE TABLE prh.position
(
    object     BIGINT REFERENCES prh.object,
    position_x INTEGER,
    position_y INTEGER,
    date       date,
    version    INTEGER DEFAULT 1
)