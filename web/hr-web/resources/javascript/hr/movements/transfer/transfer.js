class EmployeeTransfer extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        movement: {
          employeeId: "",
          typeOfMovement: "TRANSFER",
          currentAssignment: "",
          transferType: "",
          promotionBasis: {
            id: ""
          },
          remarks: "",
          reason: {
            id: ""
          },
          effectiveFrom: "",
          enquiryPassedDate: "",
          transferedLocation: "",
          departmentAssigned: "",
          designationAssigned: "",
          positionAssigned: "",
          fundAssigned: "",
          functionAssigned: "",
          employeeAcceptance: "",
          workflowDetails: {
            assignee: "",
            department: "",
            designation: ""
          },
          tenantId: tenantId
        },
        employee: {
          id: "",
          name: "",
          code: "",
          departmentId: "",
          designationId: "",
          positionId: ""
        },
        transferWithPromotion: false,
        positionList: [],
        departmentList: [],
        designationList: [],
        ulbDepartmentList:[],
        ulbDesignationList:[],
        employees: [],
        promotionList: [],
        wfDesignationList: [],
        fundList: [],
        functionaryList: [],
        districtList: [],
        transferList: [],
        reasonList: [],
        pNameList: [],
        userList: []
      }
      this.handleChange = this.handleChange.bind(this);
      this.addOrUpdate = this.addOrUpdate.bind(this);
      this.setInitialState = this.setInitialState.bind(this);
      this.vacantPositionFun = this.vacantPositionFun.bind(this);
      this.getUlbDetails = this.getUlbDetails.bind(this);
      this.getUsersFun = this.getUsersFun.bind(this);
      this.makeAjaxUpload = this.makeAjaxUpload.bind(this);

    }

    addOrUpdate(e) {

      e.preventDefault();

      var _this = this;
      var movement = Object.assign({}, _this.state.movement);
      var userInfo;

      if (_this.state.transferWithPromotion) {
        movement.typeOfMovement = "TRANSFER_CUM_PROMOTION"
      } else {
        movement.typeOfMovement = "TRANSFER";
        movement.promotionBasis = {
          id: ""
        };
        movement.enquiryPassedDate = "";
      }

      if (movement.documents && movement.documents.constructor == FileList) {
        let counter = movement.documents.length,
          breakout = 0,
          docs = [];
        for (let i = 0, len = movement.documents.length; i < len; i++) {
          this.makeAjaxUpload(movement.documents[i], function(err, res) {
            if (breakout == 1) {
              console.log("breakout", breakout);
              return;
            } else if (err) {
              showError("Error uploding the files. Please contact Administrator");
              breakout = 1;
            } else {
              counter--;
              docs.push(res.files[0].fileStoreId);
              console.log("docs", docs);
              if (counter == 0 && breakout == 0) {
                movement.documents = docs;

                var body = {
                  "RequestInfo": requestInfo,
                  "Movement": [movement]
                };

                $.ajax({
                  url: baseUrl + "/hr-employee-movement/movements/_create?tenantId=" + tenantId,
                  type: 'POST',
                  dataType: 'json',
                  data: JSON.stringify(body),
                  contentType: 'application/json',
                  headers: {
                    'auth-token': authToken
                  },
                  success: function(res) {
                    var employee, designation;

                    var asOnDate = new Date();
                    var dd = asOnDate.getDate();
                    var mm = asOnDate.getMonth() + 1; 
                    var yyyy = asOnDate.getFullYear();
              
                    if (dd < 10) {
                      dd = '0' + dd
                    }
              
                    if (mm < 10) {
                      mm = '0' + mm
                    }
              
                    asOnDate = dd + '/' + mm + '/' + yyyy;

                    commonApiPost("hr-employee", "employees", "_search", {
                      "positionId": movement.workflowDetails.assignee,
                      tenantId,
                      asOnDate
                    }, function(err, res) {
                      if (res && res.Employee && res.Employee[0])
                        employee = res.Employee[0];

                      employee.assignments.forEach(function(item) {
                        if (item.isPrimary)
                          designation = item.designation;
                      });
                      var ownerDetails = employee.name + " - " + employee.code + " - " + getNameById(_this.state.designationList, designation);

                      window.location.href = `app/hr/movements/ack-page.html?type=TransferApply&owner=${ownerDetails}`;
                    });
                  },
                  error: function(err) {
                    if (err["responseJSON"].message)
                      showError(err["responseJSON"].message);
                    else if (err["responseJSON"].Movement[0]) {
                      showError(err["responseJSON"].Movement[0].errorMsg)
                    } else {
                      showError("Something went wrong. Please contact Administrator");
                    }
                  }

                })

              }
            }
          })
        }
        // if (breakout == 1)
        //     return;
      } else {



        var body = {
          "RequestInfo": requestInfo,
          "Movement": [movement]
        };

        $.ajax({
          url: baseUrl + "/hr-employee-movement/movements/_create?tenantId=" + tenantId,
          type: 'POST',
          dataType: 'json',
          data: JSON.stringify(body),
          contentType: 'application/json',
          headers: {
            'auth-token': authToken
          },
          success: function(res) {
            var employee, designation;

            var asOnDate = new Date();
            var dd = asOnDate.getDate();
            var mm = asOnDate.getMonth() + 1; 
            var yyyy = asOnDate.getFullYear();
      
            if (dd < 10) {
              dd = '0' + dd
            }
      
            if (mm < 10) {
              mm = '0' + mm
            }
      
            asOnDate = dd + '/' + mm + '/' + yyyy;

            commonApiPost("hr-employee", "employees", "_search", {
              "positionId": movement.workflowDetails.assignee,
              tenantId,
              asOnDate
            }, function(err, res) {
              if (res && res.Employee && res.Employee[0])
                employee = res.Employee[0];

              employee.assignments.forEach(function(item) {
                if (item.isPrimary)
                  designation = item.designation;
              });
              var ownerDetails = employee.name + " - " + employee.code + " - " + getNameById(_this.state.designationList, designation);

              window.location.href = `app/hr/movements/ack-page.html?type=TransferApply&owner=${ownerDetails}`;
            });
          },
          error: function(err) {
            if (err["responseJSON"].message)
              showError(err["responseJSON"].message);
            else if (err["responseJSON"].Movement[0]) {
              showError(err["responseJSON"].Movement[0].errorMsg)
            } else {
              showError("Something went wrong. Please contact Administrator");
            }
          }

        })

      }

    }

    setInitialState(initState) {
      this.setState(initState);
    }



    componentDidUpdate() {
      var _this = this;
      $('#enquiryPassedDate').datepicker({
        format: 'dd/mm/yyyy',
        autoclose: true,
        defaultDate: ""
      });

      $('#enquiryPassedDate').on('changeDate', function(e) {
        _this.setState({
          movement: {
            ..._this.state.movement,
            "enquiryPassedDate": $("#enquiryPassedDate").val(),
          }
        });
      });


    }

    componentDidMount() {
      var type = getUrlVars()["type"],
        _this = this,
        id = getUrlVars()["id"];

      if (window.opener && window.opener.document) {
        var logo_ele = window.opener.document.getElementsByClassName("homepage_logo");
        if (logo_ele && logo_ele[0]) {
          document.getElementsByClassName("homepage_logo")[0].src = window.location.origin + logo_ele[0].getAttribute("src");
        }
      }
      $('#hp-citizen-title').text(titleCase(getUrlVars()["type"]) + " Employee Transfer");

      var _state = {},
        _this = this,
        count = 9;
      const checkCountAndCall = function(key, res) {
        _state[key] = res;
        count--;
        if (count == 0){
          _state.ulbDepartmentList = _state.departmentList;
          _state.ulbDesignationList = _state.designationList;
          _this.setInitialState(_state);
        }
      }

      getDropdown("districtList", function(res) {
        checkCountAndCall("districtList", res);
      });
      getDropdown("assignments_designation", function(res) {
        checkCountAndCall("designationList", res);
      });
      getDropdown("assignments_department", function(res) {
        checkCountAndCall("departmentList", res);
      });
      getDropdown("assignments_position", function(res) {
        checkCountAndCall("positionList", res);
      });
      getDropdown("assignments_fund", function(res) {
        checkCountAndCall("fundList", res);
      });
      getDropdown("assignments_functionary", function(res) {
        checkCountAndCall("functionaryList", res);
      });
      getDropdown("transferReason", function(res) {
        checkCountAndCall("reasonList", res);
      });
      getDropdown("promotionBasis", function(res) {
        checkCountAndCall("promotionList", res);
      });
      getDropdown("transferType", function(res) {
        checkCountAndCall("transferList", res);
      });



      // $('#code,#name,#departmentId,#designationId').prop("disabled", true);
      $('#effectiveFrom').datepicker({
        format: 'dd/mm/yyyy',
        startDate: new Date(),
        autoclose: true,
        defaultDate: ""
      });

      $('#effectiveFrom').val("");
      $('#effectiveFrom').on('changeDate', function(e) {
        _this.setState({
          movement: {
            ..._this.state.movement,
            "effectiveFrom": $("#effectiveFrom").val()
          }
        });
        if (_this.state.movement.designationAssigned && _this.state.movement.departmentAssigned) {
          var _designation = _this.state.movement.designationAssigned;
          var _department = _this.state.movement.departmentAssigned;
          var _effectiveFrom = _this.state.movement.effectiveFrom;
          var _ulb = _this.state.movement.transferedLocation;
          _this.vacantPositionFun(_department, _designation, _effectiveFrom, _ulb);
        }
      });

      getCommonMasterById("hr-employee", "employees", id, function(err, res) {
        console.log("Employee called");
        if (res && res.Employee) {
          var obj = res.Employee[0];
          var ind = 0;
          if (obj.length > 0) {
            obj.map((item, index) => {
              for (var i = 0; i < item.assignments.length; i++) {
                if ([true, "true"].indexOf(item.assignments[i].isPrimary) > -1) {
                  ind = i;
                  break;
                }
              }
            });
          }
          _this.setState({
            ..._this.state,
            employee: {
              name: obj.name,
              code: obj.code,
              departmentId: obj.assignments[ind].department,
              designationId: obj.assignments[ind].designation,
              positionId: obj.assignments[ind].position
            },
            movement: {
              ..._this.state.movement,
              employeeId: obj.id,
              currentAssignment: obj.assignments[ind].position
            }
          })
        }
      });
    }

    getUsersFun(departmentId, designationId) {
      var _this = this;

      var asOnDate = new Date();
      var dd = asOnDate.getDate();
      var mm = asOnDate.getMonth() + 1; //January is 0!
      var yyyy = asOnDate.getFullYear();

      if (dd < 10) {
        dd = '0' + dd
      }

      if (mm < 10) {
        mm = '0' + mm
      }

      asOnDate = dd + '/' + mm + '/' + yyyy;

      commonApiPost("hr-employee", "employees", "_search", {
        tenantId,
        departmentId,
        designationId,
        asOnDate,
        active:true
      }, function(err, res) {
        if (res) {
          _this.setState({
            ..._this.state,
            userList: res.Employee
          })
        }
      })
    }

    vacantPositionFun(departmentId, designationId, effectiveFrom, ulb) {
      var _this = this;
      commonApiPost("hr-masters", "vacantpositions", "_search", {
        tenantId: ulb ? ulb : tenantId,
        departmentId: departmentId,
        designationId: designationId,
        asOnDate: effectiveFrom
      }, function(err, res) {
        if (res) {
          _this.setState({
            movement: {
              ..._this.state.movement,
            },
            pNameList: res.Position
          })
        }
      });

    }

  getUlbDetails(ulb) {

    var _this = this;
    var _baseUrl = baseUrl

    if (ulb) {
      console.log(baseUrl);
      _baseUrl = baseUrl.replace(tenantId.split(".")[1], ulb);
      console.log(baseUrl);
    }

    var _tenantId = ulb ? ulb : tenantId;

    $.ajax({
      url: _baseUrl + "/egov-common-masters/departments/_search?tenantId=" + _tenantId + "pageSize=500",
      type: 'POST',
      dataType: 'json',
      data: JSON.stringify({}),
      contentType: 'application/json',
      headers: {
        'auth-token': authToken
      },
      success: function (res) {

        _this.setState({
          ..._this.state,
          ulbDepartmentList: res.Department
        })
      }
    });

    $.ajax({
      url: _baseUrl + "/hr-masters/designations/_search?tenantId=" + _tenantId + "pageSize=500",
      type: 'POST',
      dataType: 'json',
      data: JSON.stringify({}),
      contentType: 'application/json',
      headers: {
        'auth-token': authToken
      },
      success: function (res) {

        _this.setState({
          ..._this.state,
          ulbDesignationList: res.Designation
        })
      }
    });

  }
  
    makeAjaxUpload(file, cb) {
      if (file.constructor == File) {
        let formData = new FormData();
        formData.append("jurisdictionId", "ap.public");
        formData.append("module", "PGR");
        formData.append("file", file);
        $.ajax({
          url: baseUrl + "/filestore/v1/files?tenantId=" + tenantId,
          data: formData,
          cache: false,
          contentType: false,
          processData: false,
          type: 'POST',
          success: function(res) {
            cb(null, res);
          },
          error: function(jqXHR, exception) {
            cb(jqXHR.responseText || jqXHR.statusText);
          }
        });
      } else {
        cb(null, {
          files: [{
            fileStoreId: file
          }]
        });
      }
    }


    handleChange(e, name) {
      var _this = this;

      if(!this.state.movement.transferType && name !== "transferType"){
        return showError("Please select Transfer type");
      }

      switch (name) {
        case "designationAssigned":
          if (this.state.movement.departmentAssigned && this.state.movement.effectiveFrom) {
            var _department = this.state.movement.departmentAssigned;
            var _date = this.state.movement.effectiveFrom;
            var _ulb = _this.state.movement.transferedLocation;
            _this.vacantPositionFun(_department, e.target.value, _date, _ulb);
          }
          break;
        case "departmentAssigned":
          if (this.state.movement.designationAssigned && this.state.movement.effectiveFrom) {
            var _designation = this.state.movement.designationAssigned;
            var _date = this.state.movement.effectiveFrom;
            var _ulb = _this.state.movement.transferedLocation;
            _this.vacantPositionFun(e.target.value, _designation, _date, _ulb);
          }
          break;
        case "department":
          _this.state.movement.workflowDetails.assignee = "";
          if (this.state.movement.workflowDetails.designation) {
            var _designation = this.state.movement.workflowDetails.designation;
            _this.getUsersFun(e.target.value, _designation);
          }
          break;
        case "designation":
          _this.state.movement.workflowDetails.assignee = "";
          if (this.state.movement.workflowDetails.department) {
            var _department = this.state.movement.workflowDetails.department;
            _this.getUsersFun(_department, e.target.value);
          }
          break;
        case "transferedLocation":
          _this.getUlbDetails(e.target.value);
          break;

      }


      if (name === "transferWithPromotion") {
        this.setState({
          ...this.state,
          transferWithPromotion: !this.state.transferWithPromotion
        })
      } else if (name === "promotionBasis") {
        this.setState({
          movement: {
            ...this.state.movement,
            promotionBasis: {
              id: e.target.value
            }
          }
        })
      } else if (name === "reason") {
        this.setState({
          movement: {
            ...this.state.movement,
            reason: {
              id: e.target.value
            }
          }
        })
      } else if (name === "department") {

        this.setState({
          movement: {
            ...this.state.movement,
            workflowDetails: {
              ...this.state.movement.workflowDetails,
              department: e.target.value
            }
          }
        })

        // getCommonMasterById("hr-employee","employees", e.target.value, function(err, res) {
        //           if(res && res.Employee && res.Employee[0] && res.Employee[0].userName) {
        //             _this.setState({
        //               revaluationSet: {
        //                 ..._this.state.revaluationSet,
        //                 reevaluatedBy: res.Employee[0].userName
        //               }
        //             })
        //           }
        //         })

      } else if (name === "designation") {
        this.setState({
          movement: {
            ...this.state.movement,
            workflowDetails: {
              ...this.state.movement.workflowDetails,
              designation: e.target.value
            }
          }
        })

      } else if (name === "assignee") {
        this.setState({
          movement: {
            ...this.state.movement,
            workflowDetails: {
              ...this.state.movement.workflowDetails,
              assignee: e.target.value
            }
          }
        })

      } else if (name === "documents") {
        var fileTypes = ["application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/pdf", "image/png", "image/jpeg"];

        if (e.currentTarget.files.length != 0) {
          for (var i = 0; i < e.currentTarget.files.length; i++) {
            //2097152 = 2mb
            if (e.currentTarget.files[i].size > 2097152 && fileTypes.indexOf(e.currentTarget.files[i].type) == -1) {
              $("#documents").val('');
              return showError("Maximum file size allowed is 2 MB.\n Please upload only DOC, PDF, xls, xlsx, png, jpeg file.");
            } else if (e.currentTarget.files[i].size > 2097152) {
              $("#documents").val('');
              return showError("Maximum file size allowed is 2 MB.");
            } else if (fileTypes.indexOf(e.currentTarget.files[i].type) == -1) {
              $("#documents").val('');
              return showError("Please upload only DOC, PDF, xls, xlsx, png, jpeg file.");
            }
          }

          this.setState({
            movement: {
              ...this.state.movement,
              documents: e.currentTarget.files
            }
          })
        } else {
          this.setState({
            movement: {
              ...this.state.movement,
              documents: []
            }
          })
        }

      } else {
        this.setState({
          movement: {
            ...this.state.movement,
            [name]: e.target.value
          }
        })
      }
    }


    close() {
      open(location, '_self').close();
    }

  render() {
    let {handleChange,addOrUpdate,handleChangeTwoLevel}=this;
    let _this = this;

    let {employeeId, typeOfMovement, currentAssignment, transferType, promotionBasis, remarks, reason, effectiveFrom, enquiryPassedDate, transferedLocation,
          departmentAssigned, designationAssigned, positionAssigned, fundAssigned, functionAssigned, employeeAcceptance, workflowDetails, tenantId} = this.state.movement
    let {isSearchClicked,employee,transferWithPromotion}=this.state;

    const renderOption=function(list) {
        if(list)
        {
            return list.map((item, ind)=>
            {
                return (<option key={ind} value={typeof item == "object" ? item.id : item}>
                        {typeof item == "object" ? item.name : item}
                  </option>)
            })
        }
    }

    const renderOptionForDesc=function(list) {
        if(list)
        {
            return list.map((item, ind)=>
            {
                return (<option key={ind} value={typeof item == "object" ? item.id : item}>
                        {typeof item == "object" ? item.description : item}
                  </option>)
            })
        }
    }

    const renderOptionForDistrict=function(list) {
        if(list)
        {
            return list.map((item, ind)=>
            {
                return (<option key={ind} value={typeof item == "object" ? item.code : item}>
                        {typeof item == "object" ? item.city.districtName +" - "+item.city.name : item}
                  </option>)
            })
        }
    }

    const renderOptionForUser=function(list) {
        if(list)
        {
            return list.map((item, ind)=>
            {
              var positionId;
              item.assignments.forEach(function(item) {
                                  if(item.isPrimary)
                                  {
                                    positionId = item.position;
                                  }
                              });

                return (<option key={ind} value={positionId}>
                        {item.name}
                  </option>)
            })
        }
    }

    const promotionFunc=function() {
      if(transferWithPromotion=="true"||transferWithPromotion==true){
        return(<div className="row">
          <div className="col-sm-6">
              <div className="row">
                  <div className="col-sm-6 label-text">
                    <label htmlFor="">Enquiry passed Date</label>
                  </div>
                  <div className="col-sm-6">
                    <div className="text-no-ui">
                      <span><i className="glyphicon glyphicon-calendar"></i></span>
                      <input type="text" id="enquiryPassedDate" name="enquiryPassedDate" value="enquiryPassedDate" value={enquiryPassedDate}
                              onChange={(e)=>{handleChange(e,"enquiryPassedDate")}} />
                    </div>
                  </div>
              </div>
        </div>
        <div className="col-sm-6">
            <div className="row">
                <div className="col-sm-6 label-text">
                  <label htmlFor="">Promotion Basis<span>*</span></label>
                </div>
                <div className="col-sm-6">
                  <div className="styled-select">
                    <select id="promotionBasis" name="promotionBasis" value={promotionBasis.id}
                    onChange={(e)=>{handleChange(e,"promotionBasis")}} required>
                      <option value="">Select Promotion Basis</option>
                      {renderOptionForDesc(_this.state.promotionList)}
                   </select>
                   </div>
                </div>
            </div>
          </div>
      </div>);
      }
    };


    const districtFunc=function() {
      if(transferType!="TRANSFER_WITHIN_DEPARTMENT_OR_CORPORATION_OR_ULB"){
        return(
          <div className="col-sm-6">
          <div className="row">
              <div className="col-sm-6 label-text">
                <label htmlFor="">District-ULB <span>*</span></label>
              </div>
              <div className="col-sm-6">
                <div className="styled-select">
                  <select id="transferedLocation" name="transferedLocation" value={transferedLocation}
                    onChange={(e)=>{  handleChange(e,"transferedLocation") }}required>
                    <option value="">Select District-ULB</option>
                    {renderOptionForDistrict(_this.state.districtList)}
                 </select>
                 </div>
              </div>
          </div>
        </div>
        );
      }
    };


    return (
      <div>
        <form onSubmit={(e)=> {addOrUpdate(e)}}>
        <div className="form-section">
            <div className="row">
              <div className="col-md-8 col-sm-8">
                <h3 className="categoryType">Employee Details </h3>
              </div>
            </div>
            <div className="row">
              <div className="col-sm-6">
                  <div className="row">
                      <div className="col-sm-6 label-text">
                        <label htmlFor="code">Employee Code  </label>
                      </div>
                      <div className="col-sm-6">
                          <input type="text" name="code" id="code" value={employee.code}
                          onChange={(e)=>{handleChange(e,"employee","code")}} disabled />
                      </div>
                  </div>
                </div>
                <div className="col-sm-6">
                    <div className="row">
                        <div className="col-sm-6 label-text">
                          <label htmlFor=""> Employee Name  </label>
                        </div>
                        <div className="col-sm-6">
                            <input type="text" name="name" id="name" value= {employee.name}
                                onChange={(e)=>{handleChangeTwoLevel(e,"employee","name")}} disabled/>
                        </div>
                    </div>
                  </div>
            </div>
          <div className="row">
            <div className="col-sm-6">
                <div className="row">
                    <div className="col-sm-6 label-text">
                      <label htmlFor="">Department  </label>
                    </div>
                    <div className="col-sm-6">
                      <div className="styled-select">
                        <select name="departmentId" value={employee.departmentId}
                          onChange={(e)=>{  handleChangeTwoLevel(e,"employee","departmentId")}} disabled>
                          <option value="">Select department</option>
                          {renderOption(this.state.departmentList)}
                       </select>
                       </div>
                    </div>
                </div>
              </div>
              <div className="col-sm-6">
                  <div className="row">
                      <div className="col-sm-6 label-text">
                        <label htmlFor="">Designation  </label>
                      </div>
                      <div className="col-sm-6">
                        <div className="styled-select">
                            <select name="designationId" value={employee.designationId}
                              onChange={(e)=>{ handleChange(e,"employee","designationId")}} disabled>
                            <option value="">Select Designation</option>
                            {renderOption(this.state.designationList)}
                           </select>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div className="row">
              <div className="col-sm-6">
                  <div className="row">
                      <div className="col-sm-6 label-text">
                        <label htmlFor="">Position  </label>
                      </div>
                      <div className="col-sm-6">
                        <div className="styled-select">
                            <select name="positionId" value={employee.positionId}
                              onChange={(e)=>{ handleChange(e,"employee","positionId")}} disabled >
                            <option value="">Select Position</option>
                            {renderOption(this.state.positionList)}
                           </select>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <br/>
            <div className="form-section">
                <div className="row">
                  <div className="col-md-8 col-sm-8">
                    <h3 className="categoryType">Transfer Details </h3>
                  </div>
                </div>
            <div className="row">
              <div className="col-sm-6">
                  <div className="row">
                      <div className="col-sm-6 label-text">
                        <label htmlFor="">Transfer Type <span>*</span></label>
                      </div>
                      <div className="col-sm-6">
                        <div className="styled-select">
                          <select id="transferType" name="transferType" value={transferType}
                          onChange={(e)=>{handleChange(e,"transferType")}}required>
                            <option value="">Select Transfer Type</option>
                            {renderOption(this.state.transferList)}
                         </select>
                         </div>
                      </div>
                  </div>
                </div>
                <div className="col-sm-6">
                    <div className="row">
                        <div className="col-sm-6 label-text">
                          <label htmlFor="">Reason for Transfer <span>*</span></label>
                        </div>
                        <div className="col-sm-6">
                          <div className="styled-select">
                              <select id="reason" name="reason" value={reason.id}
                                onChange={(e)=>{  handleChange(e,"reason")}}required>
                              <option value="">Select Reason</option>
                              {renderOptionForDesc(this.state.reasonList)}
                             </select>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="row">
                {districtFunc()}
                <div className="col-sm-6">
                    <div className="row">
                      <div className="col-sm-6 label-text">
                          <label htmlFor="transferWithPromotion">Transfer with Promotion</label>
                      </div>
                          <div className="col-sm-6">
                                <label className="radioUi">
                                  <input type="checkbox" name="transferWithPromotion" value="transferWithPromotion" checked={transferWithPromotion == "true" || transferWithPromotion  ==  true}
                                   onChange={(e)=>{ handleChange(e,"transferWithPromotion")}}/>
                                </label>
                          </div>
                      </div>
                    </div>
                </div>
                {promotionFunc()}
                <div className="row">
                  <div className="col-sm-6">
                    <div className="row">
                        <div className="col-sm-6 label-text">
                          <label htmlFor="">Department <span>*</span></label>
                        </div>
                        <div className="col-sm-6">
                          <div className="styled-select">
                            <select id="departmentAssigned" name="departmentAssigned" value={departmentAssigned}
                              onChange={(e)=>{  handleChange(e,"departmentAssigned")}}required>
                              <option value="">Select department</option>
                              {renderOption(this.state.ulbDepartmentList)}
                           </select>
                           </div>
                        </div>
                    </div>
                  </div>

                  <div className="col-sm-6">
                      <div className="row">
                          <div className="col-sm-6 label-text">
                            <label htmlFor="">Designation <span>*</span></label>
                          </div>
                          <div className="col-sm-6">
                            <div className="styled-select">
                                <select id="designationAssigned" name="designationAssigned" value={designationAssigned}
                                    onChange={(e)=>{handleChange(e,"designationAssigned")}}required>
                                <option value="">Select Designation</option>
                                {renderOption(this.state.ulbDesignationList)}
                               </select>
                          </div>
                        </div>
                      </div>
                    </div>
                    </div>
                  <div className="row">
                    <div className="col-sm-6">
                      <div className="row">
                          <div className="col-sm-6 label-text">
                            <label htmlFor="">Transfer/Promotion effective from <span>*</span></label>
                          </div>
                          <div className="col-sm-6">
                            <div className="text-no-ui">
                              <span><i className="glyphicon glyphicon-calendar"></i></span>
                              <input type="text" id="effectiveFrom" name="effectiveFrom" value="effectiveFrom" value={effectiveFrom}
                              onChange={(e)=>{handleChange(e,"effectiveFrom")}} required/>
                          </div>
                      </div>
                  </div>
                </div>
                    <div className="col-sm-6">
                        <div className="row">
                            <div className="col-sm-6 label-text">
                              <label htmlFor="">Position <span>*</span></label>
                            </div>
                            <div className="col-sm-6">
                              <div className="styled-select">
                                  <select id="positionAssigned" name="positionAssigned" value={positionAssigned}
                                    onChange={(e)=>{  handleChange(e,"positionAssigned")}}required>
                                  <option value="">Select Position</option>
                                  {renderOption(this.state.pNameList)}
                                 </select>
                            </div>
                          </div>
                        </div>
                      </div>
                  </div>
                  <div className="row">
                    <div className="col-sm-6">
                        <div className="row">
                            <div className="col-sm-6 label-text">
                              <label htmlFor="">Fund</label>
                            </div>
                            <div className="col-sm-6">
                              <div className="styled-select">
                                <select id="fundAssigned" name="fundAssigned" value={fundAssigned}
                                    onChange={(e)=>{handleChange(e,"fundAssigned")}}>
                                  <option value="">Select Fund</option>
                                  {renderOption(this.state.fundList)}
                               </select>
                               </div>
                            </div>
                        </div>
                      </div>
                      <div className="col-sm-6">
                          <div className="row">
                              <div className="col-sm-6 label-text">
                                <label htmlFor="">Function</label>
                              </div>
                              <div className="col-sm-6">
                                <div className="styled-select">
                                    <select id="functionAssigned" name="functionAssigned" value={functionAssigned}
                                      onChange={(e)=>{  handleChange(e,"functionAssigned") }}>
                                    <option value="">Select functionary</option>
                                    {renderOption(this.state.functionaryList)}
                                   </select>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    <div className="row">
                      <div className="col-sm-6">
                          <div className="row">
                              <div className="col-sm-6 label-text">
                                  <label htmlFor="remark">Remark <span>*</span></label>
                              </div>
                              <div className="col-sm-6">
                              <textarea rows="4" cols="50" id="remarks" name="remarks" value={remarks}
                              onChange={(e)=>{handleChange(e,"remarks")}} ></textarea>
                              </div>
                          </div>
                      </div>
                  <div className="col-sm-6">
                      <div className="row">
                          <div className="col-sm-6 label-text">
                              <label htmlFor="documents">Attachments docs</label>
                          </div>
                          <div className="col-sm-6">
                              <div className="styled-file">
                              <input id="documents" name="documents" type="file"
                                 onChange={(e)=>{handleChange(e,"documents")}} multiple/>
                             </div>
                          </div>
                      </div>
                  </div>
              </div>
              <div className="row">
                <div className="col-sm-6">
                    <div className="row">
                        <div className="col-sm-6 label-text">
                          <label htmlFor="">Transfer Accepted by Employee Y/N  <span>*</span></label>
                        </div>
                        <div className="col-sm-6">
                          <div className="styled-select">
                              <select id="employeeAcceptance" name="employeeAcceptance" value={employeeAcceptance}
                                onChange={(e)=>{  handleChange(e,"employeeAcceptance") }} required >
                              <option value="">Select Employee Acceptance</option>
                              <option value="true">Yes</option>
                              <option value="false">No</option>
                             </select>
                        </div>
                      </div>
                    </div>
                  </div>
              </div>
            </div>




            <br/>
            <div className="form-section">
                <div className="row">
                  <div className="col-md-8 col-sm-8">
                    <h3 className="categoryType">Workflow Details </h3>
                  </div>
                </div>
            <div className="row">
              <div className="col-sm-6">
                  <div className="row">
                      <div className="col-sm-6 label-text">
                        <label htmlFor="">Department <span>*</span></label>
                      </div>
                      <div className="col-sm-6">
                        <div className="styled-select">
                            <select id="department" name="department" value={workflowDetails.department}
                                 onChange={(e)=>{  handleChange(e,"department") }} required >
                                 <option value="">Select Department</option>
                                 {renderOption(this.state.departmentList)}
                            </select>
                         </div>
                      </div>
                  </div>
                </div>
                <div className="col-sm-6">
                    <div className="row">
                        <div className="col-sm-6 label-text">
                          <label htmlFor="">Designation <span>*</span></label>
                        </div>
                        <div className="col-sm-6">
                          <div className="styled-select">
                              <select id="designation" name="designation" value={workflowDetails.designation}
                                  onChange={(e)=>{  handleChange(e,"designation") }} required >
                                  <option value="">Select Designation</option>
                                  {renderOption(this.state.designationList)}//TODO: get designation based on departments
                             </select>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="row">
                  <div className="col-sm-6">
                      <div className="row">
                          <div className="col-sm-6 label-text">
                            <label htmlFor="">User Name <span>*</span></label>
                          </div>
                          <div className="col-sm-6">
                            <div className="styled-select">
                              <select id="assignee" name="assignee" value={workflowDetails.assignee}
                                onChange={(e)=>{  handleChange(e,"assignee") }}required>
                                <option value="">Select User</option>
                                {renderOptionForUser(this.state.userList)}
                             </select>
                             </div>
                          </div>
                      </div>
                    </div>
                </div>
              </div>



          <br/>
            <div className="text-center">
              <button id="sub" type="submit"  className="btn btn-submit">Create</button>&nbsp;&nbsp;
              <button type="button" className="btn btn-close" onClick={(e)=>{this.close()}}>Close</button>
            </div>
          </form>
      </div>
    );
  }
}



ReactDOM.render(
  <EmployeeTransfer />,
  document.getElementById('root')
);
