import { generateDemandSearchURL, generateGetBillURL } from "../utils";
import { httpRequest } from "../utils/api";

export const generateDemand = async (
  requestInfo,
  tenantId,
  calculations,
  config
) => {
  let consumercodeList = calculations.map(calculation => {
    return calculation.applicationNumber;
  });

  let createcalculations = [];
  let updatecalculations = [];
  let demandsSearch = await searchDemand(
    requestInfo,
    tenantId,
    consumercodeList
  );
  let foundConsumerCode = demandsSearch.Demands.map(demand => {
    return demand.consumerCode;
  });
  calculations.map(calculation => {
    if (foundConsumerCode.includes(calculation.applicationNumber))
      updatecalculations.push(calculation);
    else createcalculations.push(calculation);
  });
  if (createcalculations > 0)
    createDemand(requestInfo, createcalculations, config);
  if (updatecalculations > 0)
    updateDemand(requestInfo, updatecalculations, config, demandsSearch);
  return "uri";
};

const createDemand = async (requestInfo, calculations, config) => {
  let demands = [];
  calculations.map(calculation => {
    let tenantId = calculation.tenantId;
    let consumerCode = calculation.applicationNumber;
    let fireNOC = calculation.fireNOC;
    let demand = {
      tenantId,
      consumerCode,
      consumerType: "FIRENOC",
      businessService: process.env.BUSINESSSERVICE,
      demandDetails: []
    };
    calculation.taxHeadEstimates.map(taxHeadEstimate => {
      let demandDetail = {
        taxHeadMasterCode: taxHeadEstimate.taxHeadCode,
        taxAmount: taxHeadEstimate.estimateAmount,
        collectionAmount: 0,
        tenantId
      };
      demand.demandDetails.push(demandDetail);
    });
    demands.push(demand);
  });

  let DemandRequest = {
    RequestInfo,
    Demands: demands
  };
  var demandCreateResponse = await httpRequest({
    hostURL: process.env.EGOV_BILLINGSERVICE_HOST,
    endPoint: process.env.EGOV_DEMAND_CREATE_ENDPOINT,
    requestBody: DemandRequest
  });
};

const updateDemand = async (
  requestInfo,
  calculations,
  config,
  demandsSearch
) => {
  let demandMap = {};
  demandsSearch.map(demand => {
    demandMap = { ...demandMap, [demand.consumerCode]: demand };
  });
  let demands = [];
  calculations.map(calculation => {
    let tenantId = calculation.tenantId;
    let consumerCode = calculation.applicationNumber;
    let fireNOC = calculation.fireNOC;
    let demand = demandMap[consumerCode];
    demand.demandDetails = [];

    calculation.taxHeadEstimates.map(taxHeadEstimate => {
      let demandDetail = {
        taxHeadMasterCode: taxHeadEstimate.taxHeadCode,
        taxAmount: taxHeadEstimate.estimateAmount,
        collectionAmount: 0,
        tenantId
      };
      demand.demandDetails.push(demandDetail);
    });
    demands.push(demand);
  });

  let DemandRequest = {
    RequestInfo,
    Demands: demands
  };
  var demandUpdateResponse = await httpRequest({
    hostURL: process.env.EGOV_BILLINGSERVICE_HOST,
    endPoint: process.env.EGOV_DEMAND_UPDATE_ENDPOINT,
    requestBody: DemandRequest
  });
};

const searchDemand = async (requestInfo, tenantId, consumercodeList) => {
  let uri = generateDemandSearchURL();
  uri = uri.replace("{1}", tenantId);
  uri = uri.replace("{2", process.env.BUSINESSSERVICE);
  uri = uri.replace("{3}", consumercodeList.join());
  let requestBody = { RequestInfo: requestInfo };
  var demandsSearch = await httpRequest({
    hostURL: process.env.EGOV_BILLINGSERVICE_HOST,
    endPoint: uri,
    requestBody
  });

  // console.log("error from api utils:", errorReponse);
  return demandsSearch;
};

export const generateBill = async (requestInfo, billCriteria) => {
  const consumerCode = billCriteria.consumerCode;
  const tenantId = billCriteria.tenantId;
  let demandsSearch = await searchDemand(requestInfo, tenantId, consumerCode);

  if (demandsSearch.Demands && demandsSearch.Demands.length > 0) {
    let uri = generateGetBillURL(tenantId, consumerCode);
    let requestBody = { RequestInfo: requestInfo };
    var billResponse = await httpRequest({
      hostURL: process.env.EGOV_BILLINGSERVICE_HOST,
      endPoint: uri,
      requestBody
    });
  } else {
    throw "Invalid Consumer Code ";
  }
  return billResponse;
};
