import { addIDGenId, uuidv1 } from "../utils";
import envVariables from "../envVariables";
import get from "lodash/get";
import userService from "../services/userService";
import isEmpty from "lodash/isEmpty";

export const addUUIDAndAuditDetails = async request => {
  let { FireNOCs, RequestInfo } = request;
  //for loop should be replaced new alternative
  for (var i = 0; i < FireNOCs.length; i++) {
    FireNOCs[i].id = uuidv1();
    FireNOCs[i].fireNOCDetails.id = uuidv1();
    FireNOCs[i].fireNOCDetails.applicationNumber = await addIDGenId(
      RequestInfo,
      [
        {
          tenantId: FireNOCs[i].tenantId,
          format: envVariables.EGOV_APPLICATION_FORMATE
        }
      ]
    );
    FireNOCs[i].fireNOCDetails.buildings = FireNOCs[
      i
    ].fireNOCDetails.buildings.map(building => {
      building.id = uuidv1();
      building.applicationDocuments = building.applicationDocuments.map(
        applicationDocument => {
          applicationDocument.id = uuidv1();
          return applicationDocument;
        }
      );
      building.uoms = building.uoms.map(uom => {
        uom.id = uuidv1();
        return uom;
      });
      return building;
    });
    FireNOCs[i].fireNOCDetails.propertyDetails.address.id = uuidv1();
    FireNOCs[i].fireNOCDetails.applicantDetails.additionalDetail.id = uuidv1();
    // FireNOCs[i].fireNOCDetails.applicantDetails.owners = FireNOCs[
    //   i
    // ].fireNOCDetails.applicantDetails.owners.map(owner => {
    //   owner.id = uuidv1();
    //   return owner;
    // });
    FireNOCs[i].auditDetails = {
      createdBy: get(RequestInfo, "userInfo.uuid", ""),
      lastModifiedBy: "",
      createdTime: new Date().getTime(),
      lastModifiedTime: 0
    };
    if (
      FireNOCs[i].fireNOCDetails.applicantDetails.owners &&
      !isEmpty(FireNOCs[i].fireNOCDetails.applicantDetails.owners)
    ) {
      let owners = FireNOCs[i].fireNOCDetails.applicantDetails.owners;
      for (var owneriter = 0; owneriter < owners.length; owneriter++) {
        let userCreateResponse = await createUser(
          RequestInfo,
          owners[owneriter],
          FireNOCs[i].tenantId
        );
        owners[owneriter] = {
          ...owners[owneriter],
          ...userCreateResponse.user[0]
        };
      }
    }
  }
  request.FireNOCs = FireNOCs;
  console.log(JSON.stringify(request));
  return request;
};

const createUser = async (requestInfo, owner, tenantId) => {
  let userSearchReqCriteria = {};
  let userSearchResponse = {};
  let userCreateResponse = {};
  if (!owner.uuid) {
    //uuid of user not present
    userSearchReqCriteria.userType = "CITIZEN";
    (userSearchReqCriteria.tenantId = tenantId),
      (userSearchReqCriteria.mobileNumber = owner.momobileNumber);
    userSearchResponse = await userService.searchUser(
      requestInfo,
      userSearchReqCriteria
    );
    if (userSearchResponse.user && !isEmpty(userSearchResponse.user)) {
      userCreateResponse = await userService.updateUser(requestInfo, {
        ...userSearchResponse.user[0],
        ...owner
      });
    } else {
      owner = addDefaultUserDetails(tenantId, owner);
      userCreateResponse = await userService.createUser(requestInfo, {
        ...userSearchResponse.user[0],
        ...owner
      });
    }
  } else {
    //uuid present
    userSearchReqCriteria.uuid = [owner.uuid];
    userSearchResponse = await userService.searchUser(
      requestInfo,
      userSearchReqCriteria
    );
    if (userSearchResponse.user && !isEmpty(userSearchResponse.user)) {
      userCreateResponse = await userService.updateUser(requestInfo, {
        ...userSearchResponse.user[0],
        ...owner
      });
    } else {
      throw "User not found";
    }
    return userCreateResponse;
  }
};

const addDefaultUserDetails = (tenantId, owner) => {
  if (!owner.userName || isEmpty(owner.userName))
    owner.userName = owner.momobileNumber;
  owner.tenantId = tenantId.split(".")[0];
  owner.type = "CITIZEN";
  owner.role = [
    { code: "CITIZEN", name: "Citizen", tenantId: tenantId.split(".")[0] }
  ];
  return owner;
};
