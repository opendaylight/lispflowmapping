define(['app/lispui/lispui.module'], function(lispui) {

  lispui.register.factory('LispuiNodeFormSvc', ['$filter', '$location', 'apiBuilder', 'constants', 'eventDispatcher', 'LispuiUtils', 'nodeWrapper', 'reqBuilder', 'syncFact', 'YangUIApis', 'yangUtils', 'YangUtilsRestangular',
  function($filter, $location, apiBuilder, constants, eventDispatcher, LispuiUtils, nodeWrapper, reqBuilder, syncFact, YangUIApis, yangUtils, YangUtilsRestangular) {
    var api = {};

    api.initValues = function(scope) {
      scope.currentPath = 'src/app/yangui/views/';
      scope.apiType = '';
      scope.node = null;
      scope.selApi = null;
      scope.selSubApi = null;
      scope.filterRootNode = null;
      scope.selectedOperation = null;
      return scope;
    };

    api.generateNodesToApis = function (callback, errorCbk) {
        var allRootNodes = [],
            topLevelSync = syncFact.generateObj(),
            reqAll = topLevelSync.spawnRequest('all');

        YangUIApis.getAllModules().get().then(
            function (data) {
                var modules = {};
                modules.module = [];
                for (m of data.modules.module) {
                  if (m.name == 'lfm-mapping-database')
                    modules.module.push(m);
                }
                yangUtils.processModules(modules, function (result) {
                    allRootNodes = result.map(function (node) {
                        var copy = node.deepCopy();

                        nodeWrapper.wrapAll(copy);
                        return copy;
                    });
                    topLevelSync.removeRequest(reqAll);
                });
            }, function (result) {
                console.error('Error getting API data:', result);
                topLevelSync.removeRequest(reqAll);
            }
        );

        topLevelSync.waitFor(function () {
            try {
                eventDispatcher.dispatch(constants.EV_SRC_MAIN, 'Building apis');
                var abApis = apiBuilder.processAllRootNodes(allRootNodes);
                callback(abApis, allRootNodes);
            } catch (e) {
                errorCbk(e);
                throw(e); //do not lose debugging info
            }
        });

    };

    api.loadNode = function(type, scope, success) {

        loadingCallback(scope, '');
        yangUtils.generateNodesToApis(function(apis, allNodes) {
            scope.apis = apis;
            scope.allNodes = allNodes;
            console.info('got data',scope.apis, allNodes);
            scope.treeApis = yangUtils.generateApiTreeData(apis);
            console.info('tree api', scope.treeApis);

            var numApis = scope.apis.length;
            for(i=0; i<numApis; i++)
              if(scope.apis[i].module == "lfm-mapping-database")
                scope.selApi = scope.apis[i];
            console.info('selApi:', scope.selApi);

            if(!scope.selApi) {
                errorLoadingCallback(scope, 'Error loading node');
                return null;
            }

            var numSubApis = scope.selApi.subApis.length;
            for(j=0; j<numSubApis; j++)
              if(scope.selApi.subApis[j].node.label == type)
                scope.selSubApi = scope.selApi.subApis[j];
            console.info('selSubApi:', scope.selSubApi);

            scope.apiType = scope.selSubApi.pathArray[0].name === 'operational' ? 'operational/':'';
            scope.node = scope.selSubApi.node;
            console.info('node:', scope.node);
            scope.filterRootNode = scope.selSubApi.node;
            scope.node.clear();
            if(scope.selSubApi && scope.selSubApi.operations) {
                scope.selectedOperation = scope.selSubApi.operations[0];
            }
            successloadingCallback(scope, '');
            success();

        }, function(e) {
            errorLoadingCallback(scope, 'Error loading node');
            console.error(e);
        });

    };

    api.executeOperation = function(scope) {
        return function(operation, callback, reqPath) {
            var reqString = scope.selSubApi.buildApiRequestString(),
                requestData = {},
                preparedRequestData = {},
                headers = { "Content-Type": "application/yang.data+json"};

            reqString = reqPath ? reqPath.slice(scope.selApi.basePath.length, reqPath.length) : reqString;
            var requestPath = scope.selApi.basePath + reqString;
            scope.node.buildRequest(reqBuilder, requestData);
            angular.copy(requestData, preparedRequestData);
            preparedRequestData = yangUtils.prepareRequestData(preparedRequestData, operation, reqString, scope.selSubApi);
            //requestWorkingCallback();

            operation = operation === 'DELETE' ? 'REMOVE' : operation;

            YangUtilsRestangular.one('restconf').customOperation(operation.toLowerCase(), reqString, null, headers, preparedRequestData).then(
                function(data) {
                    if(operation === 'REMOVE'){
                        scope.node.clear();
                    }

                    if(data) {
                        scope.node.clear();
                        var props = Object.getOwnPropertyNames(data);

                        props.forEach(function(p) { //fill each property - needed for root mountpoint node, in other cases there should be only one property anyway
                            scope.node.fill(p, data[p]);
                        });
                        scope.node.expanded = true;
                    }

                    //requestSuccessCallback();
                    successCallback(scope, scope.selSubApi.node.label, '');

                    //TODO after first GET we have set scope.node with data so build from the top of this function return requestData
                    if(operation === 'GET'){
                        requestData = {};
                    }
                    console.info('Success');

                    if ( angular.isFunction(callback) ) {
                      callback(data);
                    }

                }, function(resp) {
                    var errorMsg = '';

                    if(resp.data && resp.data.errors && resp.data.errors.error && resp.data.errors.error.length) {
                        errorMsg = ': ' + resp.data.errors.error.map(function(e) {
                            return e['error-message'];
                        }).join(', ');
                    }

                    errorCallback(scope, scope.selSubApi.node.label, errorMsg);
                    // requestErrorCallback(errorMsg, resp);

                    //TODO after first GET we have set scope.node with data so build from the top of this function return requestData
                    if(operation === 'GET'){
                        requestData = {};
                    }

                    console.info('error sending request to',scope.selSubApi.buildApiRequestString(),'reqString',reqString,'got',resp.status,'data',resp.data);
                }
            );
        };
    }

    api.getNodeName = function(localeLabel, label) {
        return label;
    };

    api.buildRoot = function(scope) {
        return function() {
          scope.node.buildRequest(reqBuilder, {});
        };
    };

    var loadingCallback = function(scope, e) {
        scope.status = {
            show: true,
            isWorking: true,
            type: 'success',
            msg: 'LOADING_NODE',
            rawMsg: e || ''
        };
    };

    var errorLoadingCallback = function(scope, e) {
        scope.status = {
            show: true,
            isWorking: false,
            type: 'danger',
            msg: 'LOADING_ERROR',
            rawMsg: e || ''
        };
    };

    var successloadingCallback = function(scope, e) {
        scope.status = {
            show: true,
            isWorking: false,
            type: 'success',
            msg: 'LOADING_SUCCESS',
            rawMsg: e || ''
        };

        // TODO: show = true and after 1 second show=false
        setTimeout(function() {
            scope.status.show = false;
        }, 2000);
    };

    var errorCallback = function(scope, type, e) {
        var errortype = LispuiUtils.getLocale(type).concat('_ERROR');
        scope.status = {
            show: true,
            isWorking: false,
            type: 'danger',
            msg: errortype,
            rawMsg: e || ''
        };
    };

    var successCallback = function(scope, type, e) {
        var errortype = LispuiUtils.getLocale(type).concat('_SUCCESS');
        scope.status = {
            show: true,
            isWorking: false,
            type: 'success',
            msg: errortype,
            rawMsg: e || ''
        };
    };

    return api;

  }]);

  lispui.register.factory('LispuiRestangular', function(Restangular, ENV) {
    return Restangular.withConfig(function(RestangularConfig) {
      RestangularConfig.setBaseUrl(ENV.getBaseURL("MD_SAL"));
    });
  });

  lispui.register.factory('LispuiDashboardSvc', ['LispuiNodeFormSvc' , 'LispuiRestangular', 'LispuiUtils',
  function(LispuiNodeFormSvc, LispuiRestangular, LispuiUtils) {
      var api = {};

      api.getAll = function() {
          return LispuiRestangular.one('restconf').one('config').one('lfm-mapping-database:mapping-database');
      };

      api.postDeleteKey = function () {
        return LispuiRestangular.one('restconf').one('operations').one('lfm-mapping-database:remove-key');
      }

      api.postDeleteMapping = function () {
        return LispuiRestangular.one('restconf').one('operations').one('lfm-mapping-database:remove-mapping');
      }

      api.expandSingleRow = function (element, data, op) {
        temp = element[op];
        for(k of data) {
          k.detailHide = true;
          k.deleteHide = true;
        }
        element[op] = !temp;
      };

      api.getOriginalKeys = function () {
        return api.getAll().get().then(function(data) {
          var database = [];
          console.info('data:', data);
          for(iid of data['mapping-database']['instance-id']) {
            if(iid['authentication-key'] != null) {
              console.info('database:', database);
              for(key of iid['authentication-key'])
                database.push(key);
            }
          }
          console.info('database,', database);
          return database;
        });
      };

      api.getOriginalMappings = function () {
        return api.getAll().get().then(function(data) {
          var database = [];
          console.info('data:', data);
          for(iid of data['mapping-database']['instance-id']) {
            if(iid.mapping != null) {
              for(mapping of iid.mapping)
                database.push(mapping);
            }
          }
          return database;
        });
      };

      api.getKeys = function () {
        return api.getOriginalKeys().then(function(database) {
          var data = []
          console.info('database:', database);
          for (key of database) {
            key.data = JSON.stringify(key);
            // key.data = LispuiUtils.getPrettyString(JSON.stringify(key));
            key.detailHide = true;
            key.deleteHide = true;
            key.iid = iid.iid;
            key.url = key.eid.replace('/', '%2f');
            data.push(key);
          }
          console.info('keys:', data)
          return data;
        });
      };

      api.getMappings = function () {
        return api.getOriginalMappings().then(function (database) {
          var data = [];
          for (mapping of database) {
            mapping.data = JSON.stringify(mapping);
            // mapping.data = LispuiUtils.getPrettyString(JSON.stringify(mapping));
            mapping.detailHide = true;
            mapping.deleteHide = true;
            mapping.iid = iid.iid;
            mapping.url = mapping.eid.replace('/', ' &');
            var numLocators = 0;
            var locatorString = '';
            var mainLocatorRecord = null;
            if (mapping.LocatorRecord) {
              numLocators = mapping.LocatorRecord.length;
              mainLocatorRecord = mapping.LocatorRecord[0];
              for(i=1; i<numLocators; i++){
                if(mapping.LocatorRecord[i].priority < mainLocatorRecord.priority ||
                (mapping.LocatorRecord[i].priority == mainLocatorRecord.priority &&
                mapping.LocatorRecord[i].weight > mainLocatorRecord.weight))
                  mainLocatorRecord = mapping.LocatorRecord[i];
              }
              locatorString += LispuiUtils.renderLispAddress(mainLocatorRecord.LispAddressContainer);
              if (numLocators > 1) {
                numLocators--;
                locatorString += ' (+' + numLocators + ')';
              }

              // FLAGS
              var flags = '';
              var previous = false;
              if (mainLocatorRecord.localLocator) {
                flags += 'Local';
                previous = true;
              } if (mainLocatorRecord.rlocProbed) {
                flags += previous ? ' | Probed' : 'Probed';
                previous = true;
              } if (mainLocatorRecord.routed) {
                flags += previous ? ' | Up' : 'Up';
                previous = true;
              }

              // TTL
              var ttl = '';
              ttl += mainLocatorRecord.priority.toString() + '/' + mainLocatorRecord.weight.toString();
              ttl += '/' + mainLocatorRecord.multicastPriority.toString() + '/' + mainLocatorRecord.multicastWeight.toString();
              mapping.ttl = ttl;
            }
            else {
              locatorString += mapping.action;
            }
            mapping.locatorString = locatorString;
            mapping.flags = flags;

            data.push(mapping);
          }
          return data;
        });
      }

      api.getSingleKey = function (eid) {
        return api.getOriginalKeys().then(function (database) {
          var wantedKey = null;
          for (key of database) {
            if (key.eid == eid) {
              wantedKey = key;
            }
          }
          return wantedKey;
        })
      }

      api.getSingleMapping = function (eid) {
        return api.getOriginalMappings().then(function (database) {
          var wantedMapping = null;
          for (mapping of database) {
            if (mapping.eid == eid) {
              wantedMapping = mapping;
            }
          }
          return wantedMapping;
        })
      }

      api.deleteKey = function (eid) {
        return api.getSingleKey(eid).then(function (key) {
          var postKey = {"input":{}};
          postKey.input.LispAddressContainer = key.LispAddressContainer;
          postKey.input['mask-length'] = key['mask-length'];
          console.log('postKey', postKey);
          return postKey;
        })
      }

      api.deleteMapping = function (eid) {
        return api.getSingleMapping(eid).then(function (mapping) {
          var postMapping = {"input":{}};
          postMapping.input.LispAddressContainer = mapping.LispAddressContainer;
          postMapping.input['mask-length'] = mapping['maskLength'];
          console.log('postMapping', postMapping);
          return postMapping;
        })
      }

      return api;
  }]);

  lispui.register.factory('LispuiUtils', ['$filter',
  function($filter) {
      var api = {};

      api.getLocale = function(label) {
          locale = '';

          locale = label=='add-key' ? 'ADD_KEY' : locale;
          locale = label=='get-key' ? 'GET_KEY' : locale;
          locale = label=='update-key' ? 'EDIT_KEY' : locale;
          locale = label=='add-mapping' ? 'ADD_MAPPING' : locale;
          locale = label=='get-mapping' ? 'GET_MAPPING' : locale;
          locale = label=='update-mapping' ? 'EDIT_MAPPING' : locale;

          return locale;

      };

      api.getPrettyString = function (input) {
        output = '<p>';
        length = input.length;
        indx = 0;
        console.info('input:', input);
        console.info('length:', length);

        for(i=0; i<length; i++) {
          if(input[i] == '{' || input[i] == '[') {
            output = output.concat(input[i]).concat('<br>');
            indx++;
            for(j=0; j<indx; j++)
              output = output.concat('&nbsp;&nbsp;&nbsp;');
          }
          else if (input[i] == '}' || input[i] == ']') {
            output = output.concat('<br>');
            indx--;
            for(j=0; j<indx; j++)
              output = output.concat('&nbsp;&nbsp;&nbsp;');
            output = output.concat(input[i]);
          }
          else if (input[i] == ',') {
            output = output.concat(',<br>');
            for(j=0; j<indx; j++)
              output = output.concat('&nbsp;&nbsp;&nbsp;');
          }
          else
            output = output.concat(input[i]);

        }
        output = output.concat('</p>');
        console.info('output:', output);
        return output;
      }

      api.transformLispAddress = function (keyAddress) {
        var editAddress = {
            "AS": "AS",
            "distinguishedName": "DistinguishedName",
            "Ipv4Address": "Ipv4",
            "Ipv6Address": "Ipv6",

            "Mac": "MacAddress"
        }
        return editAddress[keyAddress];
      }

      api.getAddress = function (address) {
        var afi = address.afi;
        var string = '';
        if (afi == 1) {
          string += address.Ipv4Address;
        } else if (afi == 2) {
          string += address.Ipv6Address;
        } else if (afi == 16389) {
          string += address.MacAddress;
        } else if (afi == 17) {
          string += address.distinguishedName;
        } else if (afi == 18) {
          string += address.AS;
        }
        return string;
      }

      api.renderLispAddress = function (lispAddress) {
        var string  = '';

        if (!lispAddress)
          return string;

        var addressType = Object.keys(lispAddress)[0];
        var afi = lispAddress[addressType].afi;

        if (afi == 0) {
          string += 'no:No Address Present';
        } else if (afi == 1) {
          string += 'ipv4:';
          string += api.getAddress(lispAddress[addressType]);
        } else if (afi == 2) {
          string += 'ipv6:';
          string += api.getAddress(lispAddress[addressType]);
        } else if (afi == 16389) {
          string += 'mac:';
          string += api.getAddress(lispAddress[addressType]);
        } else if (afi == 17) {
          string += 'dn:';
          string += api.getAddress(lispAddress[addressType]);
        } else if (afi == 18) {
          string += 'as:AS';
          string += api.getAddress(lispAddress[addressType]);
        } else if (afi == 16387) {
          var lcaf = lispAddress[addressType].lcafType;
          if (lcaf == 1) {
            string += 'list:';
            //string += '{' + api.getAddress(lispAddress[addressType].);
            // TODO
          } else if (lcaf == 2) {
            string += '[' + lispAddress[addressType].instanceId + '] ';
            string += api.getAddress(lispAddress[addressType].Address);
            string += '/' + lispAddress[addressType].iidMaskLength;
          } else if (lcaf == 4) {
            string += 'appdata:'
            // TODO
          } else if (lcaf == 10) {
            string += 'elp:';
            string += '{';
            var hops = lispAddress[addressType].Hops;
            first = true;
            for (hop of hops) {
              string += (first) ? '' : '→';
              first = false;
              string += api.getAddress(hop.hop);
            }
            string += '}';
          } else if (lcaf == 12) {
            string += 'srcdst:';
            string += api.getAddress(lispAddress[addressType].srcAddress);
            string += '/' + lispAddress[addressType].srcMaskLength + '|';
            string += api.getAddress(lispAddress[addressType].dstAddress);
            string += '/' + lispAddress[addressType].dstMaskLength;
          } else if (lcaf == 15) {
            string += api.getAddress(lispAddress[addressType].key);
            string += '=>';
            string += api.getAddress(lispAddress[addressType].value);
          }
        }

        return string;
      }


      return api;
  }]);

});
