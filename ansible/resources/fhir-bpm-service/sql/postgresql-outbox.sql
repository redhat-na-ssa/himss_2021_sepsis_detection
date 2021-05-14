-- Table: public.process_service_outbox

-- DROP TABLE public.process_service_outbox;

CREATE TABLE public.process_service_outbox
(
    id uuid NOT NULL,
    aggregatetype character varying(255) COLLATE pg_catalog."default" NOT NULL,
    aggregateid character varying(255) COLLATE pg_catalog."default" NOT NULL,
    type character varying(255) COLLATE pg_catalog."default" NOT NULL,
    payload text COLLATE pg_catalog."default",
    ce_specversion character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ce_source character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ce_time character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ce_datacontenttype character varying(255) COLLATE pg_catalog."default" NOT NULL,
    ce_incidentid character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT process_service_outbox_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.process_service_outbox
    OWNER to rhpam;
