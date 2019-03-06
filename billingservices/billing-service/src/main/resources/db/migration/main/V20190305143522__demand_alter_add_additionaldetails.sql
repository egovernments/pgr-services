--> demand

ALTER TABLE egbs_demand RENAME column owner TO payer;

ALTER TABLE egbs_demand ADD COLUMN additionaldetails json;

--> demand detail

ALTER TABLE egbs_demanddetail ADD COLUMN additionaldetails json;

--> BILL 

ALTER TABLE egbs_bill RENAME COLUMN payeename TO payername;

ALTER TABLE egbs_bill RENAME COLUMN payeeaddress TO payeraddress;

ALTER TABLE egbs_bill RENAME COLUMN payeeemail TO payeremail;


--> bill detail

ALTER TABLE egbs_billdetail ADD COLUMN fromperiod bigint,
							ADD COLUMN toperiod bigint,
							ADD COLUMN demandid character varying(64);



--> billACCountDetail

ALTER TABLE egbs_billaccountdetail ADD COLUMN taxheadcode character varying(256),
								   ADD COLUMN amount numeric(10,2),
								   ADD COLUMN adjustedamount numeric(10,2),
								   ADD COLUMN demanddetailid character varying(64);
								   
								   