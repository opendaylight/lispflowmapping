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

      var deleteKey = function (eid) {
        LispuiDashboardSvc.deleteKey(eid).then(function (key) {
          LispuiDashboardSvc.postDeleteKey().post('', key).then(function (success) {
            $scope.loadTable();
          })
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

  lispui.register.controller('KeysEditLispuiCtrl',['$scope', '$stateParams', 'LispuiDashboardSvc', 'LispuiNodeFormSvc', 'LispuiUtils',
    function($scope, $stateParams, LispuiDashboardSvc, LispuiNodeFormSvc, LispuiUtils) {
      var eid = $stateParams.eid.replace('%2f', '/');
      $scope.key = null;
      console.info('eid:', eid);
      LispuiDashboardSvc.getSingleKey(eid).then(function (key) {
        console.log('key: ', key);
        $scope.key = key;
      })

      LispuiNodeFormSvc.initValues($scope);
      LispuiNodeFormSvc.loadNode("update-key", $scope, function () {
        console.log('Fill');
        $scope.node.children[0].children[0].children[1].fill('mask-length', $scope.key['mask-length'])
        var typeAddress = Object.keys($scope.key.LispAddressContainer);
        console.log(LispuiUtils.transformLispAddress(typeAddress));
        // $scope.node.children[0].children[0].children[2].children[1].fill(LispuiUtils.transformLispAddress(typeAddress), $scope.key.LispAddressContainer[typeAddress]);
        $scope.node.children[0].children[1].children[1].fill('key-type', $scope['key-type']);
        $scope.node.children[0].children[1].children[2].fill('authkey', $scope['authkey']);

      });

      $scope.getNodeName = LispuiNodeFormSvc.getNodeName;
      $scope.buildRoot = LispuiNodeFormSvc.buildRoot($scope);
      $scope.executeOperation = LispuiNodeFormSvc.executeOperation($scope);

  }]);
});
