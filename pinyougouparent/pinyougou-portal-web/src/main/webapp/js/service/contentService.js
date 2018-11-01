app.service("contentService",function ($http) {
    this.findByContentCategoryId=function (categoryId) {
        return $http.get("content/findByContentCategoryId.do?categoryId="+categoryId);
    }
});