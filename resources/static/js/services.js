angular.module('raqetApp.services', []).factory('Case', function($resource) {
  return $resource('/remotes/json/cases/:casename', { casename: '@casename' }, {
    update: {
      method: 'PUT'
    }
  });
}).factory('Computer', function($resource) {
  return $resource('/remotes/json/cases/:casename/computers/:computername', { casename: '@casename' , computername: '@computername' }, {
    update: {
      method: 'PUT'
    }
  });
}).service('popupService',function($window){
        this.showPopup=function(message){
                    return $window.confirm(message);
                        }
});
