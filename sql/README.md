# SQL Scripts

This folder contains MySQL 8 initialization scripts for the current project baseline.

## Execution order
1. 00_init_database.sql
2. 01_iam.sql
3. 02_radar.sql
4. 03_rule_engine.sql
5. 04_notification.sql
6. 05_habit.sql
7. 99_seed_dev.sql

## Notes
- Charset: utf8mb4
- Engine: InnoDB
- All scripts are idempotent with IF NOT EXISTS where possible.
