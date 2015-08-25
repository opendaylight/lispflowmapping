define(['app/lispui/lispui.module', 'app/lispui/lispui.services'], function(lispui) {

  lispui.register.controller('MappingsLispuiCtrl', ['$scope', 'LispuiDashboardSvc', 'LispuiUtils',
    function($scope, LispuiDashboardSvc, LispuiUtils) {
      $scope.data = [];
      var database = {};

      var loadTable = function () {
        $scope.data = [];
        LispuiDashboardSvc.getMappings().then(function (mappings) {
          $scope.data = mappings;
        });
      };

      var deleteMapping = function (eid) {
        LispuiDashboardSvc.deleteMapping(eid).then(function (mapping) {
          LispuiDashboardSvc.postDeleteMapping().post('', mapping).then(function (success) {
            $scope.loadTable();
          })
        })
      }

      $scope.loadTable = loadTable;
      $scope.expandSingleRow = LispuiDashboardSvc.expandSingleRow;
      $scope.deleteMapping = deleteMapping
      loadTable();
  }]);

  lispui.register.controller('MappingsCreateLispuiCtrl',['$scope', 'LispuiNodeFormSvc',
    function($scope, LispuiNodeFormSvc) {
      LispuiNodeFormSvc.initValues($scope);
      LispuiNodeFormSvc.loadNode("add-mapping", $scope);

      $scope.getNodeName = LispuiNodeFormSvc.getNodeName;
      $scope.buildRoot = LispuiNodeFormSvc.buildRoot($scope);
      $scope.executeOperation = LispuiNodeFormSvc.executeOperation($scope);

  }]);

  lispui.register.controller('MappingsGetLispuiCtrl',['$scope', 'LispuiNodeFormSvc',
    function($scope, LispuiNodeFormSvc) {
      LispuiNodeFormSvc.initValues($scope);
      LispuiNodeFormSvc.loadNode("get-mapping", $scope);

      $scope.getNodeName = LispuiNodeFormSvc.getNodeName;
      $scope.buildRoot = LispuiNodeFormSvc.buildRoot($scope);
      $scope.executeOperation = LispuiNodeFormSvc.executeOperation($scope);

  }]);
  
});
