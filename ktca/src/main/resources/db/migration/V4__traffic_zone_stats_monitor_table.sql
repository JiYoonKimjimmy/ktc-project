-- ktca.traffic_zone_stats_monitor
CREATE TABLE IF NOT EXISTS ${schema}.traffic_zone_stats_monitor (
    zone_id VARCHAR(255) NOT NULL,
    stats_date VARCHAR(255) NOT NULL,
    zone_alias VARCHAR(255) NOT NULL,
    stats_type VARCHAR(50) NOT NULL,
    max_threshold BIGINT NOT NULL,
    total_entry_count BIGINT NOT NULL,
    max_waiting_count BIGINT NOT NULL,
    max_estimated_clear_time BIGINT NOT NULL,
    created TIMESTAMP,
    updated TIMESTAMP,
    PRIMARY KEY (zone_id, stats_date)
);