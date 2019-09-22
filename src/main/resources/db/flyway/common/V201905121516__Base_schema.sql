CREATE SCHEMA IF NOT EXISTS prh;

DROP TABLE IF EXISTS prh.position;
DROP TABLE IF EXISTS prh.search_parameter;
DROP TABLE IF EXISTS prh.area;
DROP TABLE IF EXISTS prh.object;

CREATE TABLE prh.object
(
    id         BIGSERIAL          NOT NULL PRIMARY KEY,
    hash       varchar(50) UNIQUE NOT NULL,
    iterations INTEGER DEFAULT 0,
    version    INTEGER DEFAULT 1
);

CREATE TABLE prh.area
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    object_hash varchar(50),
    x_1         INTEGER,
    x_2         INTEGER,
    y_1         INTEGER,
    y_2         INTEGER,
    version     INTEGER DEFAULT 1,
    FOREIGN KEY (object_hash) REFERENCES prh.object (hash)
);

CREATE TABLE prh.position
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    object_hash varchar(50),
    position_x  INTEGER,
    position_y  INTEGER,
    date        date,
    version     INTEGER DEFAULT 1,
    FOREIGN KEY (object_hash) REFERENCES prh.object (hash)
);

CREATE TABLE prh.search_parameter
(
    id          BIGSERIAL NOT NULL PRIMARY KEY,
    object_hash varchar(50),
    precision   INTEGER DEFAULT 0,
    error_rate  DECIMAL DEFAULT 0.0,
    version     INTEGER DEFAULT 1,
    FOREIGN KEY (object_hash) REFERENCES prh.object (hash)
)