app.controller("contentController",function ($scope,contentService) {

    //广告类型列表
    $scope.contentList=[];
    $scope.findByContentCategoryId=function (categoryId) {
        contentService.findByContentCategoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId]=response;
            }
        );
    }

    $scope.search=function(){
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
});