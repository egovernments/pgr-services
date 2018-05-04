CREATE TABLE wcms_connection_v2 (

  uuid character varying(64) NOT NULL,
  tenantId character varying(256) NOT NULL,
  type character varying(16) NOT NULL,
  status character varying(16) NOT NULL,
  oldConnectionNumber character varying(128),
  acknowledgmentNumber character varying(128) NOT NULL,
  connectionNumber character varying(128) NOT NULL,
  applicationType character varying(64) ,
  billingType character varying(64) NOT NULL,
  pipesize character varying(64),
  sourceType  character varying(64) NOT NULL,
  numberOfTaps bigint,
  numberOfPersons bigint,
  parentConnection character varying(128),
  documents character varying[],
  property character varying(64),
  address character varying(64), --> reference for address object in wcms connection
  meter JSONB,
  owner character varying[],
  additionalDetails JSONB,
  createdBy	character varying(64),
  lastModifiedBy	character varying(64),
  createdTime	bigint,
  lastModifiedTime bigint,

  CONSTRAINT pk_wcms_connection_v2 PRIMARY KEY (connectionNumber,tenantid),
  CONSTRAINT uk_wcms_connection_v2 UNIQUE (uuid,tenantid)
);

CREATE TABLE wcms_connection_address_v2 (

  tenantid character varying(256) NOT NULL,
  uuid character varying(64) NOT NULL,
  buildingName character varying(1024),
  roadName character varying(2056),
  billingAddress character varying(2056),
  gisNumber character varying(128),
  revenueBoundary character varying(64),
  locationBoundary character varying(64),
  adminBoundary character varying(64),
  createdBy	character varying(64),
  lastModifiedBy	character varying(64),
  createdTime	bigint,
  lastModifiedTime bigint,

  CONSTRAINT pk_wcms_connection_address_v2 PRIMARY KEY(uuid,tenantid)
);
