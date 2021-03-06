CREATE TABLE RESOURCE_CONNECTIONS_TMP AS SELECT * FROM RESOURCE_CONNECTIONS;
DROP TABLE RESOURCE_CONNECTIONS;
CREATE TABLE RESOURCE_CONNECTIONS
(
    UUID CHAR(36) NOT NULL,
    NODE_NAME_SRC VARCHAR(255) NOT NULL,
    NODE_NAME_DST VARCHAR(255) NOT NULL,
    RESOURCE_NAME VARCHAR(48) NOT NULL,
    FLAGS BIGINT NOT NULL,
    CONSTRAINT PK_RC PRIMARY KEY (NODE_NAME_SRC, NODE_NAME_DST, RESOURCE_NAME),
    CONSTRAINT FK_RC_RSCS_SRC  FOREIGN KEY (NODE_NAME_SRC, RESOURCE_NAME) REFERENCES
        RESOURCES(NODE_NAME, RESOURCE_NAME) ON DELETE CASCADE,
    CONSTRAINT FK_RC_RSCS_DST FOREIGN KEY (NODE_NAME_DST, RESOURCE_NAME) REFERENCES
        RESOURCES(NODE_NAME, RESOURCE_NAME) ON DELETE CASCADE,
    CONSTRAINT UNQ_RC_UUID UNIQUE (UUID)
);

INSERT INTO RESOURCE_CONNECTIONS SELECT UUID, NODE_NAME_SRC, NODE_NAME_DST, RESOURCE_NAME, 0 FROM RESOURCE_CONNECTIONS_TMP;
DROP TABLE RESOURCE_CONNECTIONS_TMP;
