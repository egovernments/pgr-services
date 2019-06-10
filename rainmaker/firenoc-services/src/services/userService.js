import envVariables from "../envVariables";
import { httpRequest } from "../utils/api";

export const searchUser = async (requestInfo, userSearchReqCriteria) => {
  let requestBody = { RequestInfo: requestInfo, ...userSearchReqCriteria };

  try {
    var userSearchResponse = await httpRequest({
      hostURL: envVariables.EGOV_USER_HOST,
      endPoint: `${envVariables.EGOV_USER_CONTEXT_PATH}${
        envVariables.EGOV_USER_SEARCH_ENDPOINT
      }`,
      requestBody
    });
  } catch (error) {
    throw `Search User error ${error}`;
  }
  var dobFormat = "yyyy-MM-dd";
  userSearchResponse = parseResponse(userSearchResponse, dobFormat);
  return userSearchResponse;
};

export const createUser = async (requestInfo, user) => {
  try {
    let requestBody = { RequestInfo: requestInfo, user: user };
    var userCreateResponse = await httpRequest({
      hostURL: envVariables.EGOV_USER_HOST,
      endPoint: `${envVariables.EGOV_USER_CONTEXT_PATH}${
        envVariables.EGOV_USER_CREATE_ENDPOINT
      }`,
      requestBody
    });
  } catch (error) {
    throw `Create User error ${error}`;
  }
  var dobFormat = "dd/MM/yyyy";
  userCreateResponse = parseResponse(userCreateResponse, dobFormat);

  return userCreateResponse;
};

export const updateUser = async (requestInfo, user) => {
  try {
    let requestBody = { RequestInfo: requestInfo, user: user };
    var userUpdateResponse = await httpRequest({
      hostURL: envVariables.EGOV_USER_HOST,
      endPoint: `${envVariables.EGOV_USER_CONTEXT_PATH}${
        envVariables.EGOV_USER_UPDATE_ENDPOINT
      }`,
      requestBody
    });
  } catch (error) {
    throw `Update User error ${error.toString()}`;
  }
  var dobFormat = "yyyy-MM-dd";
  userUpdateResponse = parseResponse(userUpdateResponse, dobFormat);

  return userUpdateResponse;
};

const parseResponse = (userResponse, dobFormat) => {
  var format1 = "dd-MM-yyyy HH:mm:ss";
  userResponse.user &&
    userResponse.user.map(user => {
      user.createdDate =
        user.createdDate && dateToLong(user.createdDate, format1);
      user.lastModifiedDate =
        user.lastModifiedDate && dateToLong(user.lastModifiedDate, format1);
      user.dob = user.dob && dateToLong(user.dob, dobFormat);
      user.pwdExpiryDate =
        user.pwdExpiryDate && dateToLong(user.pwdExpiryDate, format1);
    });
  return userResponse;
};

const dateToLong = (date, format) => {
  var epoch = null;
  var formattedDays = null;

  switch (format) {
    case "dd-MM-yyyy HH:mm:ss":
      formattedDays = date.split(/\D+/);
      epoch = new Date(
        formattedDays[2],
        formattedDays[1] - 1,
        formattedDays[0],
        formattedDays[3],
        formattedDays[4],
        formattedDays[5]
      );
      break;
    case "yyyy-MM-dd":
      formattedDays = date.split("-");
      epoch = new Date(
        formattedDays[0],
        formattedDays[1] - 1,
        formattedDays[2]
      );

      break;
    case "dd/MM/yyyy":
      formattedDays = date.split("/");
      epoch = new Date(
        formattedDays[2],
        formattedDays[1] - 1,
        formattedDays[0]
      );

      break;
  }
  return epoch.getTime();
};

export default { searchUser, createUser, updateUser };
