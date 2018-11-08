app.controller('baseController',function ($scope) {

    //分页空间配置
    $scope.paginationConf={
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    //重新加载列表 数据
    $scope.reloadList=function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage);
    }

    $scope.selectIds=[];//用户勾选的id集合
    $scope.updateSelection=function ($event, id) {
        if ($event.target.checked) {
            //向集合添加元素
            $scope.selectIds.push(id);
        } else {
            //查找数据所在位置
            var index=$scope.selectIds.indexOf(id);
            //index移除的位置，1：移除的个数
            $scope.selectIds.splice(index, 1);
        }
    }

    $scope.jsonToString=function(jsonString,key){
        var json=JSON.parse(jsonString);//将 json 字符串转换为 json 对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=", "
            }
            value+=json[i][key];
        }
        return value;
    }
});