CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(64) UNIQUE                      NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS users
(
    id    BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name  VARCHAR(250)                            NOT NULL,
    email VARCHAR(254) UNIQUE                     NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation         VARCHAR(2000)                           NOT NULL,
    category_id        BIGINT                                  NOT NULL,
    description        VARCHAR(7000)                           NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    lat                FLOAT                                   NOT NULL,
    lon                FLOAT                                   NOT NULL,
    paid               BOOLEAN                                 NOT NULL,
    participant_limit  INT                                     NOT NULL,
    request_moderation BOOLEAN                                 NOT NULL,
    title              VARCHAR(120)                            NOT NULL,
    confirmed_requests BIGINT                                  NOT NULL,
    state              VARCHAR(20)                             NOT NULL,
    created_on         TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    initiator_id       BIGINT                                  NOT NULL,
    CONSTRAINT pk_events PRIMARY KEY (id),
    CONSTRAINT fk_events_to_categories FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id)
        REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN                                 NOT NULL,
    title  VARCHAR(50)                             NOT NULL,
    CONSTRAINT pk_compilations PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS participation_requests
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    event_id     BIGINT                                  NOT NULL,
    requester_id BIGINT                                  NOT NULL,
    status       VARCHAR                                 NOT NULL,
    created_on   TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    CONSTRAINT pk_participation_requests PRIMARY KEY (id),
    CONSTRAINT fk_participation_requests_to_events FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_participation_requests_to_users FOREIGN KEY (requester_id)
        REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_participation_requests UNIQUE (event_id, requester_id)
);

CREATE TABLE IF NOT EXISTS events_compilations
(
    event_id       BIGINT NOT NULL,
    compilation_id BIGINT NOT NULL,
    CONSTRAINT pk_events_compilations PRIMARY KEY (event_id, compilation_id),
    CONSTRAINT fk_events_compilations_events FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_events_compilations_compilations FOREIGN KEY (compilation_id)
        REFERENCES compilations (id)
);

CREATE TABLE IF NOT EXISTS comments
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    comment_text VARCHAR(500)                            NOT NULL,
    event_id     BIGINT                                  NOT NULL,
    author_id    BIGINT                                  NOT NULL,
    created_on   TIMESTAMP WITHOUT TIME ZONE             NOT NULL,
    modified_on  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (id),
    CONSTRAINT fk_comments_to_events FOREIGN KEY (event_id)
        REFERENCES events (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id)
        REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
)
