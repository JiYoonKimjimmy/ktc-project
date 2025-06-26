-- ktca.members
CREATE TABLE IF NOT EXISTS ${schema}.members (
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

-- ktca.member_zone_log
CREATE TABLE IF NOT EXISTS ${schema}.member_zone_log (
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

-- ktca.traffic_zone_groups
CREATE TABLE IF NOT EXISTS ${schema}.traffic_zone_groups (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    group_order INT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    CONSTRAINT uk_zone_group_order UNIQUE (group_order)
);

-- ktca.traffic_zones
CREATE TABLE IF NOT EXISTS ${schema}.traffic_zones (
    id VARCHAR(255) PRIMARY KEY,
    alias VARCHAR(255) NOT NULL,
    threshold BIGINT NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    activation_time TIMESTAMP NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    CONSTRAINT fk_traffic_zone_group FOREIGN KEY (group_id) REFERENCES ${schema}.traffic_zone_groups(id)
);

-- ktca.traffic_zone_monitoring
CREATE TABLE IF NOT EXISTS ${schema}.traffic_zone_monitoring (
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

-- members
INSERT INTO ${schema}.members (login_id, password, name, email, team, role, status, last_login_at, created, updated)
VALUES ('admin', '$2a$10$NLe3joUTFguOesWtQExQkuQg0Ba0rC7La0NNFqBuSWXY445tDoo7a', '관리자', 'admin@konai.com', '관리자', 'ADMINISTRATOR', 'ACTIVE', NOW(), NOW(), NOW());

-- traffic_zone_groups
INSERT INTO ${schema}.traffic_zone_groups (id, name, group_order, status, created, updated)
VALUES ('KG0000000000000000000', '기본 그룹', 1, 'ACTIVE', NOW(), NOW());