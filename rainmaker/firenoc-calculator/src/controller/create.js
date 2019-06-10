import producer from "../kafka/producer";
import { requestInfoToResponseInfo, upadteForAuditDetails } from "../utils";
import uuid4 from "uuid/v4";

const create = (req, res) => {
  console.log("create");
  let createResponse = {};
  createResponse.ResponseInfo = requestInfoToResponseInfo(
    req.body.RequestInfo,
    true
  );
  createResponse.BillingSlabs = enrichCreateData(req.body);

  const payloads = [
    {
      topic: process.env.KAFKA_TOPICS_SAVE_SERVICE,
      messages: JSON.stringify(createResponse)
    }
  ];

  producer.send(payloads, function(err, data) {});
  console.log("createResponse", createResponse);
  res.send(createResponse);
};

const enrichCreateData = reqBody => {
  reqBody.BillingSlabs.map(billingSlab => {
    billingSlab.id = uuid4();
    billingSlab.auditDetails = {};
    upadteForAuditDetails(billingSlab.auditDetails, reqBody.RequestInfo);
  });
  return reqBody.BillingSlabs;
};

export default create;
