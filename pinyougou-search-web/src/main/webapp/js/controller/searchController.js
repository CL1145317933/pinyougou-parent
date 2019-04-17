app.controller('searchController',function($scope,searchService,$location){

    //搜索
    $scope.search=function(){
        $scope.searchMap.pageNum=parseInt($scope.searchMap.pageNum);
        searchService.search($scope.searchMap).success(
            function(response){
                $scope.resultMap=response;
                buildPageLabel();
            }
        );

    }

    //搜索对象
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNum':1,'pageSize':40,'sortField':'','sortValue':''};

    //添加搜索项
    $scope.addSearchItem=function (key,value) {
        if (key=='category' || key=='brand' || key=='price') {//搜索分类或者品牌
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;//搜索规格
        }
        //添加复合搜索
        $scope.search();
    }

    //移除对象
    $scope.removeItem=function (key) {

        if(key=='category' || key=='brand' || key=='price') {//移除该分类或者该品牌
            $scope.searchMap[key]='';
        }else{
            delete $scope.searchMap.spec[key];//移除此规格属性
        }
        //移除符合搜索
        $scope.search();
    }

    //构建分页标签(totalPages 为总页数)
    buildPageLabel=function () {
        $scope.pageLabel=[];//新增分页栏属性

        var maxPageNum = $scope.resultMap.totalPages;//获取最后的页码
        var firstPage = 1;//开始页码
        var lastPage = maxPageNum;//终止页码

        //默认前后都没点
        $scope.firstDot=false;
        $scope.lastDot=false;

        if ($scope.resultMap.totalPages>5) {//如果总页数大于5页,显示部分页码
            if($scope.searchMap.pageNum<=3) {//如果当前页小于等于3
                lastPage = 5;
                $scope.lastDot=true;//后面有点
            } else if($scope.searchMap.pageNum>=$scope.resultMap.totalPages-2) {//如果当前页大于等于总页数-2
                firstPage=maxPageNum-4;
                $scope.firstDot=true;//前面有点
            } else{
                firstPage=$scope.searchMap.pageNum-2;
                lastPage=$scope.searchMap.pageNum+2;
                //前后都有点
                $scope.firstDot=true;
                $scope.lastDot=true;
            }
        }
        //循环产生页码
        for(var i=firstPage;i<=lastPage;i++) {
            $scope.pageLabel.push(i);
        }
    }

    //根据页码查询
    $scope.queryByPage=function (pageNum) {

        if(pageNum<1||pageNum>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNum=pageNum;

        $scope.search();
    }

    //设置排序规则
    $scope.searchSort=function (sortField,sortValue) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sortValue=sortValue;

        $scope.search();
    }

    //判断输入的关键字是否是品牌
    $scope.keywordsIsBrand=function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }

    //加载主页输入的关键字
    $scope.indexKeywords=function () {
        $scope.searchMap.keywords=$location.search()['keywords'];
        $scope.search();
    }

});