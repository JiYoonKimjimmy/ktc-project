-- KTCA.MEMBERS
CREATE TABLE IF NOT EXISTS ${schema}.MEMBERS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    team VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    last_login_at TIMESTAMP NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP
);

-- KTCA.MEMBER_ZONE_LOG
CREATE TABLE IF NOT EXISTS ${schema}.MEMBER_ZONE_LOG (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    zone_id VARCHAR(255) NOT NULL,
    zone_alias VARCHAR(255) NOT NULL,
    threshold BIGINT NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    activation_time TIMESTAMP NOT NULL,
    zone_created TIMESTAMP NOT NULL,
    zone_updated TIMESTAMP NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP
);

-- KTCA.TRAFFIC_ZONE_GROUPS
CREATE TABLE IF NOT EXISTS ${schema}.TRAFFIC_ZONE_GROUPS (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_order INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    CONSTRAINT uk_zone_group_order UNIQUE (group_order)
);

-- KTCA.TRAFFIC_ZONES
CREATE TABLE IF NOT EXISTS ${schema}.TRAFFIC_ZONES (
    id VARCHAR(255) PRIMARY KEY,
    alias VARCHAR(255) NOT NULL,
    threshold BIGINT NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    activation_time TIMESTAMP NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    CONSTRAINT fk_traffic_zone_group FOREIGN KEY (group_id) REFERENCES ${schema}.TRAFFIC_ZONE_GROUPS(id)
);

-- KTCA.TRAFFIC_ZONE_MONITORING
CREATE TABLE IF NOT EXISTS ${schema}.TRAFFIC_ZONE_MONITORING (
    id VARCHAR(255) PRIMARY KEY,
    zone_id VARCHAR(255) NOT NULL,
    zone_alias VARCHAR(255) NOT NULL,
    threshold BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    activation_time TIMESTAMP NOT NULL,
    entry_count BIGINT NOT NULL,
    waiting_count BIGINT NOT NULL,
    estimated_clear_time BIGINT NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP
);

-- MEMBERS
INSERT INTO ${schema}.MEMBERS (login_id, password, name, email, team, role, status, last_login_at, created, updated)
VALUES ('admin', 'admin', '관리자', 'admin@konai.com', '관리자', 'ADMINISTRATOR', 'ACTIVE', NOW(), NOW(), NOW());

-- TRAFFIC_ZONE_GROUPS
INSERT INTO ${schema}.TRAFFIC_ZONE_GROUPS (id, name, group_order, status, created, updated)
VALUES ('KG0000000000000000000', '기본 그룹', 1, 'ACTIVE', NOW(), NOW());