import { Router } from "express";
import producer from "../kafka/producer";
import envVariables from "../envVariables";
const asyncHandler = require("express-async-handler");
import {
  requestInfoToResponseInfo,
  addUUIDAndAuditDetails,
  createWorkFlow
} from "../utils";

export default ({ config, db }) => {
  let api = Router();
  api.post(
    "/_update",
    asyncHandler(async ({ body }, res) => {
      let payloads = [];
      payloads.push({
        topic: envVariables.KAFKA_TOPICS_FIRENOC_UPDATE,
        messages: JSON.stringify(body)
      });
      producer.send(payloads, function(err, data) {
        let response = {
          ResponseInfo: requestInfoToResponseInfo(body.RequestInfo, true),
          FireNOCs: body.FireNOCs
        };
        res.json(response);
      });
    })
  );
  return api;
};
