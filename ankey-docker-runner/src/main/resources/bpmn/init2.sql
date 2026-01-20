create user ankey_report with password 'ankey_report';
create database ankey_report encoding 'utf8' owner ankey_report;
grant all privileges on database ankey_report to ankey_report;

create user ankey_bpmn with password 'ankey_bpmn';
create database ankey_bpmn encoding 'utf8' owner ankey_bpmn;
grant all privileges on database ankey_bpmn to ankey_bpmn;

\c ankey_report;

DO $$
BEGIN
IF (select Substr(setting, 1, strpos(setting, '.')-1) from pg_settings where name = 'server_version') = '12' THEN
    create extension pgcrypto;
    raise notice 'For database "ankey_report" created "pgcrypto" extension';
END IF;
END $$;

\c ankey_bpmn;

DO $$
BEGIN
IF (select Substr(setting, 1, strpos(setting, '.')-1) from pg_settings where name = 'server_version') = '12' THEN
    create extension pgcrypto;
    raise notice 'For database "ankey_bpmn" created "pgcrypto" extension';
END IF;
END $$;