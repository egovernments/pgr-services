import { requestInfoToResponseInfo, upadteForAuditDetails } from "../utils";
import { generateBill } from "../services/demandService";

const getbill = async (req, res) => {
  console.log("getbill");
  let getbillResponse = {};
  let requestInfo = req.body.RequestInfo;
  let billCriteria = req.query;
  getbillResponse = await generateBill(requestInfo, billCriteria);
  getbillResponse.ResponseInfo = requestInfoToResponseInfo(requestInfo, true);
  // console.log("calculalteResponse", calculalteResponse);
  res.send(getbillResponse);
};

export default getbill;
