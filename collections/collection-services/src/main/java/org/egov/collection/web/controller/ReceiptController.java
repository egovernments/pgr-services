/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) 2016  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.collection.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.collection.model.ReceiptSearchCriteria;
import org.egov.collection.model.enums.ReceiptStatus;
import org.egov.collection.service.CollectionService;
import org.egov.collection.service.WorkflowService;
import org.egov.collection.web.contract.Receipt;
import org.egov.collection.web.contract.ReceiptReq;
import org.egov.collection.web.contract.ReceiptRes;
import org.egov.collection.web.contract.ReceiptWorkflowRequest;
import org.egov.collection.web.contract.factory.RequestInfoWrapper;
import org.egov.collection.web.contract.factory.ResponseInfoFactory;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/receipts")
@Slf4j
public class ReceiptController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private WorkflowService workflowService;

    @Value("#{'${search.ignore.status}'.split(',')}")
    private List<String> searchIgnoreStatus;

    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ReceiptRes> search(@ModelAttribute ReceiptSearchCriteria receiptSearchCriteria,
            @RequestBody @Valid final RequestInfoWrapper requestInfoWrapper) {

        final RequestInfo requestInfo = requestInfoWrapper.getRequestInfo();

        // Only do this if there is no receipt number search
        // Only do this when search ignore status has been defined in application.properties
        // Only do this when status has not been already provided for the search
        if ((receiptSearchCriteria.getReceiptNumbers() == null
              || receiptSearchCriteria.getReceiptNumbers().isEmpty()) &&
                !searchIgnoreStatus.isEmpty() &&
                (receiptSearchCriteria.getStatus() == null ||
                        receiptSearchCriteria.getStatus().isEmpty()) ) {
            // Do not return ignored status for receipts by default
            Set<String> defaultStatus = new HashSet<>();
            for (ReceiptStatus receiptStatus : ReceiptStatus.values()) {
                if (!searchIgnoreStatus.contains(receiptStatus.toString())) {
                    defaultStatus.add(receiptStatus.toString());
                }
            }

            receiptSearchCriteria.setStatus(defaultStatus);
        }

        List<Receipt> receipts = collectionService.getReceipts(requestInfo, receiptSearchCriteria);

        return getSuccessResponse(receipts, requestInfo);
    }

    @RequestMapping(value = "/_create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ReceiptRes> create(@RequestBody @Valid ReceiptReq receiptRequest) {

        Receipt receiptInfo = collectionService.createReceipt(receiptRequest);
        return getSuccessResponse(Collections.singletonList(receiptInfo), receiptRequest.getRequestInfo());

    }

    @RequestMapping(value = "/_workflow", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> workflow(@RequestBody @Valid ReceiptWorkflowRequest receiptWorkflowRequest) {

        List<Receipt> receipts = workflowService.performWorkflow(receiptWorkflowRequest);
        return getSuccessResponse(receipts, receiptWorkflowRequest.getRequestInfo());
    }

    @RequestMapping(value = "/_update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> update(@RequestBody @Valid ReceiptReq receiptRequest) {

        List<Receipt> receiptInfo = collectionService.updateReceipt(receiptRequest);

        return getSuccessResponse(receiptInfo, receiptRequest.getRequestInfo());
    }

    @RequestMapping(value = "/_validate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> validate(@RequestBody @Valid ReceiptReq receiptReq) {

        List<Receipt> receipt = collectionService.validateReceipt(receiptReq);
        return getSuccessResponse(receipt, receiptReq.getRequestInfo());

    }

    private ResponseEntity<ReceiptRes> getSuccessResponse(List<Receipt> receipts,
            RequestInfo requestInfo) {
        final ResponseInfo responseInfo = ResponseInfoFactory
                .createResponseInfoFromRequestInfo(requestInfo, true);
        responseInfo.setStatus(HttpStatus.OK.toString());

        ReceiptRes receiptResponse = new ReceiptRes(responseInfo, receipts);
        return new ResponseEntity<>(receiptResponse, HttpStatus.OK);
    }
}