//控制层
app.controller('goodsController', function ($scope, $controller, $location, goodsService, uploadService, itemCatService, typeTemplateService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    };

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //查询实体
    $scope.findOne = function () {
        var id = $location.search()['id'];
        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                $scope.flagId = id;
                editor.html($scope.entity.goodsDesc.introduction);
                if ($scope.entity.goodsDesc.itemImages != null) {
                    $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                } else {
                    $scope.entity.goodsDesc.itemImages = [];
                }
                if ($scope.entity.goodsDesc.customAttributeItems != null) {
                    $scope.entity.goodsDesc.customAttributeItems =
                        JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                }

                if ($scope.entity.goodsDesc.specificationItems != null) {
                    $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                } else {
                    $scope.entity.goodsDesc.specificationItems = [];
                }

                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec =
                        JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    };

    //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
    $scope.checkAttributeExit = function (checkKey, checkValue) {
        if ($scope.entity.goodsDesc.specificationItems == null) {
            return;
        }
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', checkKey);
        if (object != null && object.attributeValue.indexOf(checkValue) >= 0) {
            return true;
        }
        return false;
    }

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.Introduction = editor.html();

        var serverObject;
        if ($scope.entity.goods.id == null) {
            serverObject = goodsService.add($scope.entity);
        } else {
            serverObject = goodsService.update($scope.entity);
        }

        serverObject.success(
            function (response) {
                if (response.success) {
                    alert("保存成功！");
                    location.href = 'goods.html';
                } else {
                    alert(response.message);
                }
            }
        );
    };

    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    };

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    };

    //文件上传
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                if (response.success) {
                    $scope.image_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }
        );
    };


    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};
    //添加图片到列表
    $scope.addItemImages = function () {
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };

    //列表中移除图片
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    };

    //实现3级联动
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.ItemCat1list = response;
            }
        );
    };
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        if (newValue == oldValue) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.ItemCat2list = response;
            }
        );
    });
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        if (newValue == oldValue) {
            return;
        }
        itemCatService.findByParentId(newValue).success(
            function (response) {
                $scope.ItemCat3list = response;
            }
        );
    });
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        if (newValue == oldValue) {
            return;
        }
        itemCatService.findOne(newValue).success(
            function (response) {
                $scope.entity.goods.typeTemplateId = response.typeId;
            }
        );
    });

    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        if (newValue == oldValue) {
            return;
        }

        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.template = response;
                $scope.template.brandIds = JSON.parse(response.brandIds);
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            }
        );

        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;
            }
        );
    });

    //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", name);

        if (object == null) {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        } else {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        }
    }

    //[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["6寸","5寸"]}]
    //创建SKU列表信息，对应tb_item

    $scope.createItemList = function () {
        //初始化变量
        $scope.entity.itemList = [{spec: {}, price: 0, num: 0, status: '0', isDefault: '0'}];
        var specItemsList = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < specItemsList.length; i++) {
            $scope.entity.itemList = $scope.addColumn($scope.entity.itemList, specItemsList[i].attributeName, specItemsList[i].attributeValue);
            // alert(JSON.stringify($scope.entity.itemList));
        }
    }

    $scope.addColumn = function (sourceList, attributeName, attributeValueList) {
        var newList = [];
        for (var i = 0; i < sourceList.length; i++) {
            //{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}
            var oldCol = sourceList[i];
            for (var j = 0; j < attributeValueList.length; j++) {
                var newCol = JSON.parse(JSON.stringify(oldCol));
                newCol.spec[attributeName] = attributeValueList[j];
                // alert(JSON.stringify(newCol));
                newList.push(newCol);
            }
        }
        return newList;
    }

    //定义entity.auditStatus状态对应的中文描述
    $scope.auditStatusList = ["审核中", "已通过", "审核未通过", "已关闭"];

    $scope.itemCatAllMap = {};
    $scope.findItemCatAllMap = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatAllMap[response[i].id] = response[i].name;
                }
            }
        );
    }
});