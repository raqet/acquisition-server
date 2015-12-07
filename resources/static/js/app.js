angular.module('raqetApp', ['ui.router', 'ngResource', 'raqetApp.controllers', 'raqetApp.services']);

angular.module('raqetApp').config(function($stateProvider) {
  $stateProvider.state('cases', { // state for showing all cases
    url: '/cases',
    templateUrl: 'partials/cases.html',
    controller: 'CaseListController'
  }).state('viewCase', { //state for showing single case
    url: '/cases/:casename/view',
    templateUrl: 'partials/case-view.html',
    controller: 'CaseViewController'
  }).state('newCase', { //state for adding a new case
    url: '/cases/new',
    templateUrl: 'partials/case-add.html',
    controller: 'CaseCreateController'
  }).state('editCase', { //state for updating a case
    url: '/cases/:casename/edit',
    templateUrl: 'partials/case-edit.html',
    controller: 'CaseEditController'
  }).state('viewComputer', { //state for showing single case
    url: '/cases/:casename/computers/:computername/view',
    templateUrl: 'partials/computer-view.html',
    controller: 'ComputerViewController'
  }).state('newComputer', { //state for adding a new case
    url: '/cases/:casename/computers/new',
    templateUrl: 'partials/computer-add.html',
    controller: 'ComputerCreateController'
  });
}).run(function($state) {
  $state.go('cases'); //make a transition to cases state when app starts
});
