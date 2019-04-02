CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE egcl_receiptheader_history AS SELECT * FROM egcl_receiptheader WHERE tenantId = 'somerandomstring';
ALTER TABLE egcl_receiptheader_history ADD COLUMN uuid varchar(256) NOT NULL;

CREATE TABLE egcl_receiptdetails_history AS SELECT * FROM egcl_receiptdetails WHERE tenantId = 'somerandomstring';
ALTER TABLE egcl_receiptdetails_history ADD COLUMN uuid varchar(256) NOT NULL;

CREATE TABLE egcl_instrumentheader_history AS SELECT * FROM egcl_instrumentheader WHERE tenantId = 'somerandomstring';
ALTER TABLE egcl_instrumentheader_history ADD COLUMN uuid varchar(256) NOT NULL;

ALTER TABLE egcl_receiptheader ADD COLUMN demandid varchar(256);
ALTER TABLE egcl_receiptheader ADD COLUMN demandFromDate BIGINT;
ALTER TABLE egcl_receiptheader ADD COLUMN demandToDate BIGINT;

ALTER TABLE egcl_receiptdetails ADD COLUMN demanddetailid varchar(256);
ALTER TABLE egcl_receiptdetails ADD COLUMN taxheadcode varchar(256);


ALTER TABLE egcl_receiptheader RENAME COLUMN payeename TO payername;
ALTER TABLE egcl_receiptheader RENAME COLUMN payeeaddress TO payeraddress;
ALTER TABLE egcl_receiptheader RENAME COLUMN payeeemail TO payeremail;
ALTER TABLE egcl_receiptheader RENAME COLUMN payeemobile TO payermobile;



