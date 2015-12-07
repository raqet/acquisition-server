angular.module('raqetApp.controllers', []).controller('CaseListController', function($scope, $state, popupService, $window, Case, Computer) {
  $scope.cases = Case.query(); //fetch all cases. Issues a GET to /api/cases

  $scope.deleteCase = function(caseobject) { // Delete a case. Issues a DELETE to /api/cases/:id
    if (popupService.showPopup('Really delete this?')) {
      caseobject.$delete(function() {
        $window.location.href = ''; //redirect to home
      });
    }
  };
}).controller('CaseViewController', function($scope, $stateParams, Case, Computer) {
  $scope.caseobject = Case.get({ casename: $stateParams.casename }); //Get a single case.Issues a GET to /api/cases/:id
  $scope.computers = Computer.query({ casename: $stateParams.casename }); //fetch all computers. Issues a GET to /api/cases/:casename/
}).controller('CaseCreateController', function($scope, $state, $stateParams, Case, Computer) {
  $scope.caseobject = new Case();  //create new case instance. Properties will be set via ng-model on UI

  $scope.addCase = function() { //create a new case. Issues a POST to /api/cases
    $scope.caseobject.$save(function() {
      $state.go('cases'); // on success go back to home i.e. cases state.
    });
  };
}).controller('CaseEditController', function($scope, $state, $stateParams, Case, Computer) {
  $scope.updateCase = function() { //Update the edited case. Issues a PUT to /api/cases/:id
    $scope.caseobject.$update(function() {
      $state.go('cases'); // on success go back to home i.e. cases state.
    });
  };

  $scope.loadCase = function() { //Issues a GET request to /api/cases/:id to get a case to update
    $scope.caseobject = Case.get({ casename: $stateParams.casename });
  };

  $scope.loadCase(); // Load a case which can be edited on UI
}).controller('ComputerViewController', function($scope, $stateParams, Case, Computer) {
  $scope.caseobject = Case.get({ casename: $stateParams.casename }); //Get a single case.Issues a GET to /api/cases/:id
  $scope.computer = Computer.get({ casename: $stateParams.casename, computername: $stateParams.computername}); //fetch all computers. Issues a GET to /api/cases/:casename/computers/:computername
}).controller('ComputerCreateController', function($scope, $state, $stateParams, Case, Computer) {
  $scope.computer = new Computer();  //create new computer instance. Properties will be set via ng-model on UI

  $scope.addComputer = function() { //create a new case. Issues a POST to /api/cases
    $scope.computer.$save({ casename: $stateParams.casename}, function() {
      $state.go('viewCase',{ casename: $stateParams.casename}); // on success go back to home i.e. cases state.
    });
  };
});
