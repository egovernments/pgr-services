import uniqBy from "lodash/uniqBy";
import uniq from "lodash/uniq";
import get from "lodash/get";
import findIndex from "lodash/findIndex";
import isEmpty from "lodash/isEmpty";
import { httpRequest } from "./api";
import envVariables from "../envVariables";

export const uuidv1 = () => {
  return require("uuid/v4")();
};

export const requestInfoToResponseInfo = (requestinfo, success) => {
  let ResponseInfo = {
    apiId: "",
    ver: "",
    ts: 0,
    resMsgId: "",
    msgId: "",
    status: ""
  };
  ResponseInfo.apiId =
    requestinfo && requestinfo.apiId ? requestinfo.apiId : "";
  ResponseInfo.ver = requestinfo && requestinfo.ver ? requestinfo.ver : "";
  ResponseInfo.ts = requestinfo && requestinfo.ts ? requestinfo.ts : null;
  ResponseInfo.resMsgId = "uief87324";
  ResponseInfo.msgId =
    requestinfo && requestinfo.msgId ? requestinfo.msgId : "";
  ResponseInfo.status = success ? "successful" : "failed";

  return ResponseInfo;
};

const transformById = (payload, id) => {
  return (
    payload &&
    payload.reduce((result, item) => {
      result[item[id]] = {
        ...item
      };

      return result;
    }, {})
  );
};

const getOwnerResponse = async uuids => {
  let requestBody = {
    uuid: uuids
  };
  const response = await httpRequest({
    hostURL: envVariables.EGOV_USER_HOST,
    endPoint: `${envVariables.EGOV_USER_CONTEXT_PATH}${
      envVariables.EGOV_USER_SEARCH_ENDPOINT
    }`,
    queryObject: [],
    requestBody,
    headers: []
  });

  return transformById(response.user, "uuid");
};

export const addIDGenId = async (requestInfo, idRequests) => {
  let requestBody = {
    RequestInfo: requestInfo,
    idRequests
  };
  // console.log(JSON.stringify(requestBody));
  let idGenResponse = await httpRequest({
    hostURL: envVariables.EGOV_IDGEN_HOST,
    endPoint: `${envVariables.EGOV_IDGEN_CONTEXT_PATH}${
      envVariables.EGOV_IDGEN_GENERATE_ENPOINT
    }`,
    requestBody
  });
  // console.log("idgenresponse",idGenResponse);
  return get(idGenResponse, "idResponses[0].id");
};

export const createWorkFlow = async body => {
  let processInstances = body.FireNOCs.map(fireNOC => {
    return {
      tenantId: fireNOC.tenantId,
      businessService: envVariables.BUSINESS_SERVICE,
      businessId: fireNOC.fireNOCDetails.applicationNumber,
      action: fireNOC.fireNOCDetails.action,
      comment: fireNOC.fireNOCDetails.comment,
      assignee: fireNOC.fireNOCDetails.assignee,
      sla: 0,
      previousStatus: null,
      moduleName: envVariables.BUSINESS_SERVICE
    };
  });
  let requestBody = {
    RequestInfo: body.RequestInfo,
    ProcessInstances: processInstances
  };
  let workflowResponse = await httpRequest({
    hostURL: envVariables.EGOV_WORKFLOW_HOST,
    endPoint: envVariables.EGOV_WORKFLOW_TRANSITION_ENDPOINT,
    requestBody
  });
  return workflowResponse;
};

export const addQueryArg = (url, queries = []) => {
  if (url && url.includes("?")) {
    const urlParts = url.split("?");
    const path = urlParts[0];
    let queryParts = urlParts.length > 1 ? urlParts[1].split("&") : [];
    queries.forEach(query => {
      const key = query.key;
      const value = query.value;
      const newQuery = `${key}=${value}`;
      queryParts.push(newQuery);
    });
    const newUrl = path + "?" + queryParts.join("&");
    return newUrl;
  } else {
    return url;
  }
};
