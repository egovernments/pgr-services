import { httpRequest } from "./api";
import envVariables from "../envVariables";

export default async (requestInfo = {}) => {
  var requestBody = {
    RequestInfo: requestInfo,
    MdmsCriteria: {
      tenantId: "pb.amritsar",
      moduleDetails: [
        {
          moduleName: "common-masters",
          masterDetails: [{ name: "OwnerType" }, { name: "OwnerShipCategory" }]
        },
        {
          moduleName: "firenoc",
          masterDetails: [{ name: "BuildingType" }, { name: "FireStations" }]
        },
        {
          moduleName: "egov-location",
          masterDetails: [{ name: "TenantBoundary" }]
        },
        { moduleName: "tenant", masterDetails: [{ name: "tenants" }] }
      ]
    }
  };
  var mdmsResponse = await httpRequest({
    hostURL: envVariables.EGOV_MDMS_HOST,
    endPoint: `${envVariables.EGOV_MDMS_CONTEXT_PATH}${
      envVariables.EGOV_MDMS_SEARCH_ENPOINT
    }`,
    requestBody
  });
  return mdmsResponse;
};
