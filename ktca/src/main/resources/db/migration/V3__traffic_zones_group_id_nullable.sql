-- traffic_zones 테이블의 group_id 컬럼을 nullable로 변경 (Aurora MySQL 호환)
ALTER TABLE ${schema}.traffic_zones
    MODIFY COLUMN group_id VARCHAR(255) NULL; 