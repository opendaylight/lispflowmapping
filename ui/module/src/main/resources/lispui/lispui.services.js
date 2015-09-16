define(['app/lispui/lispui.module'], function(lispui) {

    lispui.register.factory('LispuiNodeFormSvc', ['$filter',
        '$location', 'apiBuilder', 'constants',
        'eventDispatcher', 'LispuiUtils', 'nodeWrapper',
        'reqBuilder', 'syncFact', 'YangUIApis', 'yangUtils',
        'YangUtilsRestangular',
        function($filter, $location, apiBuilder, constants,
            eventDispatcher, LispuiUtils, nodeWrapper,
            reqBuilder, syncFact, YangUIApis, yangUtils,
            YangUtilsRestangular) {
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

            ////////// Snippet originally from yangui.controller.js //////////
            // It has been changed to get only the node desired passed as an input of the function [type]
            api.loadNode = function(type, scope) {

                loadingCallback(scope, '');
                yangUtils.generateNodesToApis(function(apis,
                    allNodes) {
                    scope.apis = apis;
                    scope.allNodes = allNodes;
                    console.info('got data', scope.apis,
                        allNodes);
                    scope.treeApis = yangUtils.generateApiTreeData(
                        apis);
                    console.info('tree api', scope.treeApis);

                    // Select the api 'mappingservice'
                    var numApis = scope.apis.length;
                    for (i = 0; i < numApis; i++)
                        if (scope.apis[i].module ==
                            "mappingservice")
                            scope.selApi = scope.apis[
                                i];
                    console.info('selApi:', scope.selApi);

                    if (!scope.selApi) {
                        errorLoadingCallback(scope,
                            'Error loading node'
                        );
                        return null;
                    }

                    // Select the subapi 'type' pass as an argument
                    var numSubApis = scope.selApi.subApis
                        .length;
                    for (j = 0; j < numSubApis; j++)
                        if (scope.selApi.subApis[j]
                            .node.label == type)
                            scope.selSubApi = scope
                            .selApi.subApis[j];
                    console.info('selSubApi:',
                        scope.selSubApi);

                    scope.apiType = scope.selSubApi
                        .pathArray[0].name ===
                        'operational' ?
                        'operational/' : '';
                    scope.node = scope.selSubApi.node;
                    console.info('node:', scope.node);
                    scope.filterRootNode = scope.selSubApi
                        .node;
                    scope.node.clear();
                    if (scope.selSubApi && scope.selSubApi
                        .operations) {
                        scope.selectedOperation =
                            scope.selSubApi.operations[
                                0];
                    }
                    successloadingCallback(scope, '');

                }, function(e) {
                    errorLoadingCallback(scope,
                        'Error loading node');
                    console.error(e);
                });

            };
            ////////// End snippet originally from yangui.controller.js //////////


            ////////// Snippet originally from yangui.controller.js //////////
            api.executeOperation = function(scope) {
                    return function(operation, callback,
                        reqPath) {
                        var reqString = scope.selSubApi.buildApiRequestString(),
                            requestData = {},
                            preparedRequestData = {},
                            headers = {
                                "Content-Type": "application/yang.data+json"
                            };

                        reqString = reqPath ? reqPath.slice(
                            scope.selApi.basePath.length,
                            reqPath.length) : reqString;
                        var requestPath = scope.selApi.basePath +
                            reqString;
                        scope.node.buildRequest(reqBuilder,
                            requestData);
                        angular.copy(requestData,
                            preparedRequestData);
                        preparedRequestData = yangUtils.prepareRequestData(
                            preparedRequestData,
                            operation, reqString, scope
                            .selSubApi);
                        //requestWorkingCallback();

                        operation = operation === 'DELETE' ?
                            'REMOVE' : operation;

                        YangUtilsRestangular.one('restconf')
                            .customOperation(operation.toLowerCase(),
                                reqString, null, headers,
                                preparedRequestData).then(
                                function(data) {
                                    if (operation ===
                                        'REMOVE') {
                                        scope.node.clear();
                                    }

                                    if (data) {
                                        scope.node.clear();
                                        var props = Object.getOwnPropertyNames(
                                            data);

                                        props.forEach(
                                            function(p) {
                                                scope.node
                                                    .fill(
                                                        p,
                                                        data[
                                                            p
                                                        ]
                                                    );
                                            });
                                        scope.node.expanded =
                                            true;
                                    }

                                    successCallback(scope,
                                        scope.selSubApi
                                        .node.label, ''
                                    );

                                    if (operation === 'GET') {
                                        requestData = {};
                                    }
                                    console.info('Success');

                                    if (angular.isFunction(
                                            callback)) {
                                        callback(data);
                                    }

                                },
                                function(resp) {
                                    var errorMsg = '';
                                    if (resp.data && resp.data
                                        .errors && resp.data
                                        .errors.error &&
                                        resp.data.errors.error
                                        .length) {
                                        errorMsg = ': ' +
                                            resp.data.errors
                                            .error.map(
                                                function(e) {
                                                    return e[
                                                        'error-message'
                                                    ];
                                                }).join(
                                                ', ');
                                    }

                                    errorCallback(scope, scope.selSubApi.node.label, errorMsg);

                                    if (operation === 'GET') {
                                        requestData = {};
                                    }

                                    console.info(
                                        'error sending request to',
                                        scope.selSubApi
                                        .buildApiRequestString(),
                                        'reqString',
                                        reqString,
                                        'got', resp.status,
                                        'data', resp.data
                                    );
                                }
                            );
                    };
                }
                ////////// End snippet originally from yangui.controller.js //////////

            // Function required from the html view files imported from the yangui
            api.getNodeName = function(localeLabel, label) {
                return label;
            };

            ////////// Snippet originally from yangui.controller.js //////////
            api.buildRoot = function(scope) {
                return function() {
                    scope.node.buildRequest(reqBuilder, {});
                };
            };
            ////////// End snippet originally from yangui.controller.js //////////


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

                setTimeout(function() {
                    scope.status.show = false;
                }, 2000);
            };

            var errorCallback = function(scope, type, e) {
                var errortype = LispuiUtils.getLocale(type)
                    .concat('_ERROR');
                scope.status = {
                    show: true,
                    isWorking: false,
                    type: 'danger',
                    msg: errortype,
                    rawMsg: e || ''
                };
            };

            var successCallback = function(scope, type, e) {
                var errortype = LispuiUtils.getLocale(type)
                    .concat('_SUCCESS');
                scope.status = {
                    show: true,
                    isWorking: false,
                    type: 'success',
                    msg: errortype,
                    rawMsg: e || ''
                };
            };

            return api;

        }
    ]);

    lispui.register.factory('LispuiRestangular', function(Restangular,
        ENV) {
        return Restangular.withConfig(function(
            RestangularConfig) {
            RestangularConfig.setBaseUrl(ENV.getBaseURL(
                "MD_SAL"));
        });
    });

    lispui.register.factory('LispuiDashboardSvc', ['LispuiNodeFormSvc', 'LispuiRestangular', 'LispuiUtils',
        function(LispuiNodeFormSvc, LispuiRestangular, LispuiUtils) {
            var api = {};

            api.getAll = function() {
                return LispuiRestangular.one('restconf').one(
                    'config').one(
                    'mappingservice:mapping-database');
            };

            api.postDeleteKey = function() {
                return LispuiRestangular.one('restconf').one(
                    'operations').one(
                    'mappingservice:remove-key');
            }

            api.postDeleteMapping = function() {
                return LispuiRestangular.one('restconf').one(
                    'operations').one(
                    'mappingservice:remove-mapping');
            }

            api.expandSingleRow = function(element, data, op) {
                temp = element[op];
                for (k of data) {
                    k.detailHide = true;
                    k.deleteHide = true;
                }
                element[op] = !temp;
            };

            api.getOriginalKeys = function() {
                return api.getAll().get().then(function(
                    data) {
                    var database = [];
                    for (iid of data['mapping-database']['instance-id']) {
                        if (iid['authentication-key'] != null) {
                            for (key of iid['authentication-key']) {
                                console.log('iid', iid.iid)
                                key.iid = iid.iid;
                                database.push(key);
                            }
                        }
                    }
                    console.info('database,', database);
                    return database;
                });
            };

            api.getOriginalMappings = function() {
                return api.getAll().get().then(function(
                    data) {
                    var database = [];
                    console.info('data:', data);
                    for (iid of data[
                            'mapping-database'][
                            'instance-id'
                        ]) {
                        if (iid.mapping != null) {
                            for (mapping of iid.mapping) {
                                mapping.iid = iid.iid;
                                database.push(mapping);
                            }
                        }
                    }
                    return database;
                });
            };

            api.getKeys = function() {
                return api.getOriginalKeys().then(function(
                    database) {
                    var data = []
                    console.info('database:',
                        database);
                    for (key of database) {
                        key.data = LispuiUtils.getPrettyString(JSON.stringify(key));
                        key.detailHide = true;
                        key.deleteHide = true;
                        key.url = key.eid.replace(
                            '/', '%2f');
                        data.push(key);
                    }
                    console.info('keys:', data)
                    return data;
                });
            };

            api.getMappings = function() {
                return api.getOriginalMappings().then(
                    function(database) {
                        var data = [];
                        for (mapping of database) {
                            mapping.data = LispuiUtils.getPrettyString(JSON.stringify(mapping));
                            console.log(mapping.data);
                            mapping.detailHide = true;
                            mapping.deleteHide = true;
                            mapping.url = mapping.eid.replace('/', '%2f');
                            var numLocators = 0;
                            var locatorString = '';
                            var mainLocatorRecord =
                                null;
                            if (mapping.LocatorRecord) {
                                numLocators = mapping.LocatorRecord
                                    .length;
                                // Take the most important Locator
                                mainLocatorRecord =
                                    mapping.LocatorRecord[
                                        0];
                                for (i = 1; i <
                                    numLocators; i++) {
                                    if (mapping.LocatorRecord[
                                            i].priority <
                                        mainLocatorRecord
                                        .priority ||
                                        (mapping.LocatorRecord[
                                                i].priority ==
                                            mainLocatorRecord
                                            .priority &&
                                            mapping.LocatorRecord[
                                                i].weight >
                                            mainLocatorRecord
                                            .weight))
                                        mainLocatorRecord =
                                        mapping.LocatorRecord[
                                            i];
                                }
                                locatorString +=
                                    LispuiUtils.renderLispAddress(
                                        mainLocatorRecord
                                        .LispAddressContainer
                                    );
                                if (numLocators > 1) {
                                    numLocators--;
                                    locatorString +=
                                        ' (+' +
                                        numLocators +
                                        ')';
                                }

                                // FLAGS
                                var flags = '';
                                var previous = false;
                                if (mainLocatorRecord.localLocator) {
                                    flags += 'Local';
                                    previous = true;
                                }
                                if (mainLocatorRecord.rlocProbed) {
                                    flags += previous ?
                                        ' | Probed' :
                                        'Probed';
                                    previous = true;
                                }
                                if (mainLocatorRecord.routed) {
                                    flags += previous ?
                                        ' | Up' : 'Up';
                                    previous = true;
                                }

                                // TTL
                                var ttl = '';
                                ttl +=
                                    mainLocatorRecord.priority
                                    .toString() + '/' +
                                    mainLocatorRecord.weight
                                    .toString();
                                ttl += '/' +
                                    mainLocatorRecord.multicastPriority
                                    .toString() + '/' +
                                    mainLocatorRecord.multicastWeight
                                    .toString();
                                mapping.ttl = ttl;
                            } else {
                                locatorString +=
                                    mapping.action;
                            }
                            mapping.locatorString = locatorString;
                            mapping.flags = flags;

                            data.push(mapping);
                        }
                        return data;
                    });
            }

            api.getDeleteKey = function(key) {
                var postKey = {
                    "input": {}
                };
                if (key.iid == '0' || Object.keys(key.LispAddressContainer)[0] == 'LcafSegmentAddr') {
                    postKey.input.LispAddressContainer = key.LispAddressContainer;
                } else {
                    // In case the IID is not 0 and the LispAddressContainer is not coded as LCAF, code it as LcafSegmentAddr
                    postKey.input.LispAddressContainer = {
                        "LcafSegmentAddr": {
                            "afi": 16387,
                            "lcafType": 2,
                            "instanceId": parseInt(key.iid),
                            "iidMaskLength": 32,
                            "Address": key.LispAddressContainer
                        }
                    }
                }
                postKey.input['mask-length'] = key['mask-length'];
                console.log('postKey', postKey);
                return postKey;
            }

            api.getDeleteMapping = function(mapping) {
                var postMapping = {
                    "input": {}
                };
                if (mapping.iid == '0' || Object.keys(mapping.LispAddressContainer)[0] == 'LcafSegmentAddr') {
                    postMapping.input.LispAddressContainer = mapping.LispAddressContainer;
                } else {
                    // In case the IID is not 0 and the LispAddressContainer is not coded as LCAF, code it as LcafSegmentAddr
                    postMapping.input.LispAddressContainer = {
                        "LcafSegmentAddr": {
                            "afi": 16387,
                            "lcafType": 2,
                            "instanceId": parseInt(mapping.iid),
                            "iidMaskLength": 32,
                            "Address": mapping.LispAddressContainer
                        }
                    }
                }
                postMapping.input['mask-length'] = mapping['maskLength'];
                console.log('postMapping', postMapping);
                return postMapping;
            }

            return api;
        }
    ]);

    lispui.register.factory('LispuiUtils', ['$sce',
        function($sce) {
            var api = {};

            api.getLocale = function(label) {
                locale = '';

                locale = label == 'add-key' ? 'ADD_KEY' :
                    locale;
                locale = label == 'get-key' ? 'GET_KEY' :
                    locale;
                locale = label == 'update-key' ? 'EDIT_KEY' :
                    locale;
                locale = label == 'add-mapping' ?
                    'ADD_MAPPING' : locale;
                locale = label == 'get-mapping' ?
                    'GET_MAPPING' : locale;
                locale = label == 'update-mapping' ?
                    'EDIT_MAPPING' : locale;

                return locale;

            };

            api.getPrettyString = function(input) {
                output = '<p>';
                length = input.length;
                indx = 0;

                for (i = 0; i < length; i++) {
                    if (input[i] == '{' || input[i] == '[') {
                        output = output.concat(input[i]).concat('<br>');
                        indx++;
                        for (j = 0; j < indx; j++)
                            output = output.concat('&nbsp;&nbsp;&nbsp;&nbsp;');
                    } else if (input[i] == '}' || input[i] == ']') {
                        output = output.concat('<br>');
                        indx--;
                        for (j = 0; j < indx; j++)
                            output = output.concat('&nbsp;&nbsp;&nbsp;&nbsp;');
                        output = output.concat(input[i]);
                    } else if (input[i] == ',') {
                        output = output.concat(',<br>');
                        for (j = 0; j < indx; j++)
                            output = output.concat('&nbsp;&nbsp;&nbsp;&nbsp;');
                    } else {
                        output = output.concat(input[i]);
                        if (input[i] == ':')
                            output = output.concat(' ');
                    }

                }
                output = output.concat('</p>');
                // Return a trusted HTML for ng-bind-html
                return $sce.trustAsHtml(output);
            }

            api.getAddress = function(address) {
                console.log(address);
                if (address.afi == null) {
                    address = address[Object.keys(address)[0]];
                }
                var afi = address.afi;

                console.log(afi);
                var string = '';
                if (afi == 1) {
                    string = address.Ipv4Address;
                } else if (afi == 2) {
                    string = address.Ipv6Address;
                } else if (afi == 16389) {
                    string = address.MacAddress;
                } else if (afi == 17) {
                    string = address.distinguishedName;
                } else if (afi == 18) {
                    string = address.AS;
                }
                return string;
            }

            api.renderLispAddress = function(lispAddress) {
                var string = '';

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
                        string += 'list:{';
                        var addresses = lispAddress[addressType].Addresses;
                        var first = true;
                        for (add of addresses) {
                            string += (first) ? '' : ',';
                            first = false;
                            delete add.name;
                            string += api.getAddress(add);
                        }
                        string += '}';
                    } else if (lcaf == 2) {
                        string += '[' + lispAddress[addressType].instanceId + '] ';
                        string += api.getAddress(lispAddress[addressType].Address);
                        string += '/' + lispAddress[addressType].iidMaskLength;
                    } else if (lcaf == 4) {
                        string += 'appdata:'
                        console.log(lispAddress[addressType].Address);
                        string += api.getAddress(lispAddress[addressType].Address);
                        string += '!' + lispAddress[addressType].ipTos;
                        string += '!' + lispAddress[addressType].protocol;
                        string += '!' + lispAddress[addressType].localPortLow;
                        string += '-' + lispAddress[addressType].localPortHigh;
                        string += '!' + lispAddress[addressType].remotePortLow;
                        string += '-' + lispAddress[addressType].remotePortHigh;
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
                        string += api.getAddress(
                            lispAddress[addressType].srcAddress
                        );
                        string += '/' + lispAddress[
                                addressType].srcMaskLength +
                            '|';
                        string += api.getAddress(
                            lispAddress[addressType].dstAddress
                        );
                        string += '/' + lispAddress[
                            addressType].dstMaskLength;
                    } else if (lcaf == 15) {
                        string += api.getAddress(
                            lispAddress[addressType].key
                        );
                        string += '=>';
                        string += api.getAddress(
                            lispAddress[addressType].value
                        );
                    }
                }

                return string;
            }

            return api;
        }
    ]);

});
