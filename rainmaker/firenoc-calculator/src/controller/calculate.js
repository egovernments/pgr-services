import { requestInfoToResponseInfo, upadteForAuditDetails } from "../utils";
import { calculateService } from "../services/calculateService";

const calculalte = async (req, res, pool) => {
  console.log("calculalte");
  let calculalteResponse = {};
  calculalteResponse = await calculateService(req, pool);
  // console.log("calculalteResponse", calculalteResponse);
  res.send(calculalteResponse);
};

export default calculalte;
