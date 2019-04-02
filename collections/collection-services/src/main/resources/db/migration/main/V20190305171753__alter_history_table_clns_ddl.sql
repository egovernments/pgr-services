ALTER TABLE egcl_receiptheader_history ADD COLUMN demandid varchar(256);
ALTER TABLE egcl_receiptheader_history ADD COLUMN demandFromDate BIGINT;
ALTER TABLE egcl_receiptheader_history ADD COLUMN demandToDate BIGINT;

ALTER TABLE egcl_receiptdetails_history ADD COLUMN demanddetailid varchar(256);
ALTER TABLE egcl_receiptdetails_history ADD COLUMN taxheadcode varchar(256);


ALTER TABLE egcl_receiptheader_history RENAME COLUMN payeename TO payername;
ALTER TABLE egcl_receiptheader_history RENAME COLUMN payeeaddress TO payeraddress;
ALTER TABLE egcl_receiptheader_history RENAME COLUMN payeeemail TO payeremail;
ALTER TABLE egcl_receiptheader_history RENAME COLUMN payeemobile TO payermobile;