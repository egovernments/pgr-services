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

export const upadteForAuditDetails = (
  auditDetails,
  requestInfo,
  isupdate = false
) => {
  if (!isupdate) {
    auditDetails.createdBy = requestInfo.userInfo.uuid;
    auditDetails.createdDate = new Date().getTime();
  } else {
    auditDetails.lastModifiedBy = requestInfo.userInfo.uuid;
    auditDetails.lastModifiedDate = new Date().getTime();
  }
};