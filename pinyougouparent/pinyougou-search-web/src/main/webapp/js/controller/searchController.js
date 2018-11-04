app.controller('searchController', function ($scope,$location, searchService) {

    //搜索
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                buildPageLabel();
            }
        );
    }

    $scope.loadkeywords=function(){
        if($location.search()['keywords'] != null){
            $scope.searchMap.keywords = $location.search()['keywords'];
            $scope.search();
        }
    }

    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'price': '',
        'spec': {},
        'pageNo': 1,
        'pageSize': 20,
        'sort': '',
        'sortField': ''
    };//搜索对象

    $scope.addSearchItem = function (key, value) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();//执行搜索
    }

    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == "price") {
            $scope.searchMap[key] = '';
        } else {
            delete $scope.searchMap.spec[key];
        }
        $scope.search();//执行搜索
    }

    $scope.queryByPage = function (pageNoQuery) {
        if (pageNoQuery < 1 || pageNoQuery > $scope.searchMap.totalPages) {
            return;
        }
        $scope.searchMap.pageNo = pageNoQuery;
        $scope.search();
    }

    $scope.sortSearch = function (sort, sortField) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;

        $scope.search();
    }

    buildPageLabel = function () {
        $scope.pageLabel = [];
        var pageStart = 1;
        var pageEnd = $scope.resultMap.totalPages;
        $scope.firstDot = true;//前面有点
        $scope.lastDot = true;//后边有点

        if (pageEnd > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                pageEnd = 5;
                $scope.firstDot = false;
            } else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages - 2) {
                pageStart = $scope.resultMap.totalPages - 4;
                $scope.lastDot = false;//后边有点
            } else {
                pageStart = $scope.searchMap.pageNo - 2;
                pageEnd = $scope.searchMap.pageNo + 2;
            }
        } else {
            $scope.firstDot = false;//前面有点
            $scope.lastDot = false;//后边有点
        }

        for (var i = pageStart; i <= pageEnd; i++) {
            $scope.pageLabel.push(i);
        }

    }

    //判断当前页为第一页
    $scope.isTopPage = function () {
        if ($scope.searchMap.pageNo == 1) {
            return true;
        }
        return false;
    }
//判断当前页是否未最后一页
    $scope.isEndPage = function () {
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {
            return true;
        }
        return false;
    }

    $scope.isKeywordsABrand = function () {
        if (null != $scope.resultMap.brandList) {
            for(var i=0;i<$scope.resultMap.brandList.length;i++){
                if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                    return true;
                }
            }
        }
        return false;
    }
});