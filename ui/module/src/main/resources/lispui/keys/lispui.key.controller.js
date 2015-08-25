define(['app/lispui/lispui.module', 'app/lispui/lispui.services'], function(lispui) {

  lispui.register.controller('KeysLispuiCtrl', ['$scope', 'LispuiDashboardSvc', 'LispuiNodeFormSvc', 'LispuiUtils',
    function($scope, LispuiDashboardSvc, LispuiNodeFormSvc, LispuiUtils) {
      $scope.data=[];

      var loadTable = function () {
        $scope.data=[];
        LispuiDashboardSvc.getKeys().then(function (keys) {
          $scope.data = keys;
        })
      };

      var deleteKey = function (key) {
        var postKey = LispuiDashboardSvc.getDeleteKey(key);
        LispuiDashboardSvc.postDeleteKey().post('', postKey).then(function (success) {
          $scope.loadTable();
        })
      }

      $scope.loadTable = loadTable;
      $scope.expandSingleRow = LispuiDashboardSvc.expandSingleRow;
      $scope.deleteKey = deleteKey;
      loadTable();
  }]);

  lispui.register.controller('KeysCreateLispuiCtrl',['$scope', 'LispuiNodeFormSvc',
    function($scope, LispuiNodeFormSvc) {
      LispuiNodeFormSvc.initValues($scope);
      LispuiNodeFormSvc.loadNode("add-key", $scope);

      $scope.getNodeName = LispuiNodeFormSvc.getNodeName;
      $scope.buildRoot = LispuiNodeFormSvc.buildRoot($scope);
      $scope.executeOperation = LispuiNodeFormSvc.executeOperation($scope);

  }]);

  lispui.register.controller('KeysGetLispuiCtrl',['$scope', 'LispuiNodeFormSvc',
    function($scope, LispuiNodeFormSvc) {
      LispuiNodeFormSvc.initValues($scope);
      LispuiNodeFormSvc.loadNode("get-key", $scope);

      $scope.getNodeName = LispuiNodeFormSvc.getNodeName;
      $scope.buildRoot = LispuiNodeFormSvc.buildRoot($scope);
      $scope.executeOperation = LispuiNodeFormSvc.executeOperation($scope);

  }]);

});
