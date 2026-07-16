-- Baseline schema, captured from the schema Hibernate produced via ddl-auto=update
-- (pg_dump --schema-only against a fresh local Postgres 15), so Flyway's managed history
-- starts from exactly what existing environments already have on disk.

CREATE TABLE application_history_of_relaunches (
    application_id bigint NOT NULL,
    history_of_relaunches timestamp(6) without time zone
);

CREATE TABLE applications (
    id bigint NOT NULL,
    date_relaunch timestamp(6) without time zone,
    initial_application_date timestamp(6) without time zone NOT NULL,
    job_offer_id bigint NOT NULL
);

CREATE SEQUENCE applications_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE applications_id_seq OWNED BY applications.id;

CREATE TABLE businesses (
    id bigint NOT NULL,
    description character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    recruitment_service_contact character varying(255) NOT NULL,
    user_id uuid NOT NULL
);

CREATE SEQUENCE businesses_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE businesses_id_seq OWNED BY businesses.id;

CREATE TABLE canvassing_history_of_relaunches (
    canvassing_id bigint NOT NULL,
    history_of_relaunches timestamp(6) without time zone
);

CREATE TABLE canvassings (
    id bigint NOT NULL,
    date_relaunch timestamp(6) without time zone,
    initial_application_date timestamp(6) without time zone NOT NULL
);

CREATE SEQUENCE canvassings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE canvassings_id_seq OWNED BY canvassings.id;

CREATE TABLE job_offers (
    id bigint NOT NULL,
    link character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    relaunch_frequency integer NOT NULL,
    business_id bigint NOT NULL
);

CREATE SEQUENCE job_offers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE job_offers_id_seq OWNED BY job_offers.id;

CREATE TABLE professionals (
    id bigint NOT NULL,
    contact character varying(255) NOT NULL,
    first_name character varying(255) NOT NULL,
    job character varying(255) NOT NULL,
    last_name character varying(255) NOT NULL,
    business_id bigint NOT NULL
);

CREATE SEQUENCE professionals_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

ALTER SEQUENCE professionals_id_seq OWNED BY professionals.id;

CREATE TABLE users (
    id uuid NOT NULL,
    email character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    pseudo character varying(255) NOT NULL,
    role character varying(255),
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ROLE_USER'::character varying, 'ROLE_ADMIN'::character varying])::text[])))
);

ALTER TABLE ONLY applications ALTER COLUMN id SET DEFAULT nextval('applications_id_seq'::regclass);
ALTER TABLE ONLY businesses ALTER COLUMN id SET DEFAULT nextval('businesses_id_seq'::regclass);
ALTER TABLE ONLY canvassings ALTER COLUMN id SET DEFAULT nextval('canvassings_id_seq'::regclass);
ALTER TABLE ONLY job_offers ALTER COLUMN id SET DEFAULT nextval('job_offers_id_seq'::regclass);
ALTER TABLE ONLY professionals ALTER COLUMN id SET DEFAULT nextval('professionals_id_seq'::regclass);

ALTER TABLE ONLY applications
    ADD CONSTRAINT applications_pkey PRIMARY KEY (id);

ALTER TABLE ONLY businesses
    ADD CONSTRAINT businesses_pkey PRIMARY KEY (id);

ALTER TABLE ONLY canvassings
    ADD CONSTRAINT canvassings_pkey PRIMARY KEY (id);

ALTER TABLE ONLY job_offers
    ADD CONSTRAINT job_offers_pkey PRIMARY KEY (id);

ALTER TABLE ONLY professionals
    ADD CONSTRAINT professionals_pkey PRIMARY KEY (id);

ALTER TABLE ONLY users
    ADD CONSTRAINT uk_6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email);

ALTER TABLE ONLY applications
    ADD CONSTRAINT uk_pf3r3eh14xy4tpfkdl8vsb5w2 UNIQUE (job_offer_id);

ALTER TABLE ONLY businesses
    ADD CONSTRAINT ukrlbivsomeem3hof1116hmq6o2 UNIQUE (user_id, name);

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE ONLY application_history_of_relaunches
    ADD CONSTRAINT fk8rr2pvqrm2fvrjs72bdrobugf FOREIGN KEY (application_id) REFERENCES applications(id);

ALTER TABLE ONLY canvassing_history_of_relaunches
    ADD CONSTRAINT fkagcsmllw995wi5be44flrprvq FOREIGN KEY (canvassing_id) REFERENCES canvassings(id);

ALTER TABLE ONLY applications
    ADD CONSTRAINT fkawuu01ou28l0903avu0nwx058 FOREIGN KEY (job_offer_id) REFERENCES job_offers(id);

ALTER TABLE ONLY businesses
    ADD CONSTRAINT fkg8wf081dyjc8mwodmg5mairv6 FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE ONLY professionals
    ADD CONSTRAINT fkh6gllgi619gpyarbw7rvsp7j4 FOREIGN KEY (business_id) REFERENCES businesses(id);

ALTER TABLE ONLY job_offers
    ADD CONSTRAINT fks2omlm1kqwgukvehgh0ft13go FOREIGN KEY (business_id) REFERENCES businesses(id);
