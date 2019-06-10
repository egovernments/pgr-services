import { Router } from "express";
import { requestInfoToResponseInfo } from "../utils";
import { mergeSearchResults } from "../utils/search";
import isEmpty from "lodash/isEmpty";

export default ({ config, db }) => {
  let api = Router();
  api.post("/_search", function(request, apiRes) {
    let response = {
      ResponseInfo: requestInfoToResponseInfo(request.body.RequestInfo, true),
      FireNOCs: []
    };
    const actions = {
      INITIATED: "INITIATE",
      APPROVED: "APPROVE"
    };

    const queryObj = JSON.parse(JSON.stringify(request.query));
    queryObj.action = actions[queryObj.status];

    const text = `SELECT FN.uuid as FID,FN.tenantid,FN.fireNOCNumber,FN.provisionfirenocnumber,FN.oldfirenocnumber,FN.dateofapplied,FN.createdBy,FN.createdTime,FN.lastModifiedBy,FN.lastModifiedTime,FD.uuid as firenocdetailsid,FD.action,FD.applicationnumber,FD.fireNOCType,FD.applicationdate,FD.financialYear,FD.issuedDate,FD.validFrom,FD.validTo,FD.action,FD.channel,FD.propertyid,FD.noofbuildings,FD.additionaldetail,FBA.uuid as puuid,FBA.doorno as pdoorno,FBA.latitude as platitude,FBA.longitude as plongitude,FBA.buildingName as pbuildingname,FBA.addressnumber as paddressnumber,FBA.pincode as ppincode,FBA.locality as plocality,FBA.city as pcity,FBA.street as pstreet,FB.uuid as buildingid ,FB.name as buildingname,FB.usagetype,FO.uuid as ownerid,FO.ownertype,FO.useruuid,FO.relationship,FUOM.uuid as uomuuid,FUOM.code,FUOM.value,FUOM.activeuom,FBD.uuid as documentuuid,FUOM.active,FBD.documentType,FBD.filestoreid,FBD.documentuid,FBD.createdby as documentCreatedBy,FBD.lastmodifiedby as documentLastModifiedBy,FBD.createdtime as documentCreatedTime,FBD.lastmodifiedtime as documentLastModifiedTime FROM eg_fn_firenoc FN JOIN eg_fn_firenocdetail FD ON (FN.uuid = FD.firenocuuid) JOIN eg_fn_address FBA ON (FD.uuid = FBA.firenocdetailsuuid) JOIN eg_fn_owner FO ON (FD.uuid = FO.firenocdetailsuuid) JOIN eg_fn_buidlings FB ON (FD.uuid = FB.firenocdetailsuuid) JOIN eg_fn_buildinguoms FUOM ON (FB.uuid = FUOM.buildinguuid) LEFT OUTER JOIN eg_fn_buildingdocuments FBD on(FB.uuid = FBD.buildinguuid) where FN.tenantid = '${
      queryObj.tenantId
    }' AND`;

    const queryKeys = Object.keys(queryObj);
    let sqlQuery = text;

    if (queryKeys) {
      queryKeys.forEach(item => {
        if (queryObj[item]) {
          if (
            item != "fromDate" &&
            item != "toDate" &&
            item != "tenantId" &&
            item != "status"
          ) {
            sqlQuery = `${sqlQuery} ${item}='${queryObj[item]}' AND`;
          }
        }
      });
    }
    if (
      queryObj.hasOwnProperty("fromDate") &&
      queryObj.hasOwnProperty("toDate")
    ) {
      sqlQuery = `${sqlQuery} FN.createdtime >= ${
        queryObj.fromDate
      } AND FN.createdtime <= ${queryObj.toDate} ORDER BY FN.uuid`;
    } else if (
      queryObj.hasOwnProperty("fromDate") &&
      !queryObj.hasOwnProperty("toDate")
    ) {
      sqlQuery = `${sqlQuery} FN.createdtime >= ${
        queryObj.fromDate
      } ORDER BY FN.uuid`;
    } else {
      sqlQuery = `${sqlQuery.substring(
        0,
        sqlQuery.length - 3
      )} ORDER BY FN.uuid`;
    }

    console.log(text);

    db.query(sqlQuery, async (err, res) => {
      if (err) {
        console.log(err.stack);
      } else {
        // console.log(JSON.stringify(res.rows));
        response.FireNOCs =
          res.rows && !isEmpty(res.rows)
            ? await mergeSearchResults(
                res.rows,
                request.query,
                request.body.RequestInfo
              )
            : [];
        apiRes.json(response);
      }
    });
  });
  return api;
};
